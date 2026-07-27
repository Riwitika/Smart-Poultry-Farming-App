import time
import os
import numpy as np
from PIL import Image
import onnxruntime as ort
from .config import settings

class YoloPredictor:
    def __init__(self):
        self.session = None
        self.input_name = None

    @property
    def model(self):
        return self.session

    def load_model(self):
        if self.session is None:
            # Load ONNX model instead of PyTorch model
            onnx_path = settings.MODEL_PATH.replace(".pt", ".onnx")
            if not os.path.exists(onnx_path):
                raise FileNotFoundError(f"ONNX model file not found at {onnx_path}")
            
            print(f"Loading YOLOv5 ONNX model from {onnx_path}...")
            # Load ONNX session (CPU only, using lightweight CPU execution provider)
            self.session = ort.InferenceSession(onnx_path, providers=['CPUExecutionProvider'])
            self.input_name = self.session.get_inputs()[0].name
            print("ONNX model loaded successfully!")

    def predict(self, image_path: str):
        if self.session is None:
            self.load_model()
        
        start_time = time.time()
        
        # Load and resize image to 640x640 as expected by YOLOv5
        img = Image.open(image_path).convert("RGB")
        img_resized = img.resize((640, 640), Image.Resampling.BILINEAR)
        
        # Convert PIL image to numpy array, normalize to [0, 1] range, and transpose to [1, 3, 640, 640]
        img_np = np.array(img_resized, dtype=np.float32) / 255.0
        img_np = np.transpose(img_np, (2, 0, 1)) # shape: [3, 640, 640]
        tensor = np.expand_dims(img_np, axis=0) # shape: [1, 3, 640, 640]
        
        # Run inference using ONNX Runtime
        outputs = self.session.run(None, {self.input_name: tensor})
        # outputs[0] has shape [1, 25200, 41] (custom segment model has 41 output elements)
        output = outputs[0][0] # shape [25200, 41]
        
        # Filter detections (confidence threshold 0.25)
        conf_thres = 0.25
        obj_conf = output[:, 4]
        class_scores = output[:, 5:]
        class_ids = np.argmax(class_scores, axis=1)
        class_confs = class_scores[np.arange(len(class_scores)), class_ids]
        
        scores = obj_conf * class_confs
        mask = scores > conf_thres
        
        filtered_output = output[mask]
        filtered_scores = scores[mask]
        filtered_class_ids = class_ids[mask]
        filtered_boxes = filtered_output[:, :4] # [x_center, y_center, width, height]
        
        # Run Non-Maximum Suppression (NMS) in NumPy
        keep = self.numpy_nms(filtered_boxes, filtered_scores, iou_threshold=0.45)
        
        # Extract class mapping
        class_mapping = {
            "healthy": "Healthy",
            "cocci": "Coccidiosis",
            "salmo": "Salmonella",
            "ncd": "Newcastle Disease"
        }
        
        names_list = ["healthy", "cocci", "salmo", "ncd"]
        
        if len(keep) > 0:
            top_idx = keep[0]
            class_idx = filtered_class_ids[top_idx]
            confidence = round(float(filtered_scores[top_idx]) * 100.0, 2)
            
            raw_class = names_list[class_idx] if class_idx < len(names_list) else "healthy"
        else:
            raw_class = "healthy"
            confidence = 100.0
            
        prediction = class_mapping.get(raw_class.lower(), raw_class.capitalize())
        processing_time_ms = int((time.time() - start_time) * 1000)
        
        return prediction, confidence, processing_time_ms

    def numpy_nms(self, boxes, scores, iou_threshold=0.45):
        if len(boxes) == 0:
            return []
        
        x1 = boxes[:, 0] - boxes[:, 2] / 2
        y1 = boxes[:, 1] - boxes[:, 3] / 2
        x2 = boxes[:, 0] + boxes[:, 2] / 2
        y2 = boxes[:, 1] + boxes[:, 3] / 2
        
        areas = (x2 - x1) * (y2 - y1)
        order = scores.argsort()[::-1]
        
        keep = []
        while order.size > 0:
            i = order[0]
            keep.append(i)
            
            xx1 = np.maximum(x1[i], x1[order[1:]])
            yy1 = np.maximum(y1[i], y1[order[1:]])
            xx2 = np.minimum(x2[i], x2[order[1:]])
            yy2 = np.minimum(y2[i], y2[order[1:]])
            
            w = np.maximum(0.0, xx2 - xx1)
            h = np.maximum(0.0, yy2 - yy1)
            inter = w * h
            
            ovr = inter / (areas[i] + areas[order[1:]] - inter)
            inds = np.where(ovr <= iou_threshold)[0]
            order = order[inds + 1]
            
        return keep

predictor = YoloPredictor()

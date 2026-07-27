import torch
import torch.hub
import time
import os
import sys
from PIL import Image
from .config import settings

class YoloPredictor:
    def __init__(self):
        self.model = None

    def load_model(self):
        if self.model is None:
            if not os.path.exists(settings.MODEL_PATH):
                raise FileNotFoundError(f"Model file not found at {settings.MODEL_PATH}")
            
            print(f"Loading YOLOv5 model from {settings.MODEL_PATH}...")
            # Load the custom model using PyTorch Hub (this will download/verify the repo first)
            self.model = torch.hub.load(
                'ultralytics/yolov5',
                'custom',
                path=settings.MODEL_PATH,
                force_reload=False,
                trust_repo=True
            )
            print("Model loaded successfully!")
            
            # Dynamically locate the downloaded YOLOv5 repository in the torch hub cache directory
            hub_dir = torch.hub.get_dir()
            if os.path.exists(hub_dir):
                for d in os.listdir(hub_dir):
                    if d.startswith("ultralytics_yolov5"):
                        yolov5_dir = os.path.join(hub_dir, d)
                        if yolov5_dir not in sys.path:
                            sys.path.append(yolov5_dir)
                            print(f"Dynamically registered YOLOv5 repository in sys.path: {yolov5_dir}")
                        break

    def predict(self, image_path: str):
        if self.model is None:
            self.load_model()
        
        start_time = time.time()
        
        # Load and resize image to 640x640 as expected by YOLOv5
        img = Image.open(image_path).convert("RGB")
        
        # Convert PIL image to Torch tensor normalized to [0, 1] range
        import torchvision.transforms as T
        transform = T.Compose([
            T.Resize((640, 640)),
            T.ToTensor()
        ])
        tensor = transform(img).unsqueeze(0) # Shape: [1, 3, 640, 640]
        
        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        tensor = tensor.to(device)
        self.model.to(device)
        self.model.eval()
        
        # Run raw forward pass
        with torch.no_grad():
            outputs = self.model(tensor)
            
        # Get raw predictions tensor (first element of output tuple/list)
        if isinstance(outputs, (list, tuple)):
            raw_pred = outputs[0]
        else:
            raw_pred = outputs
            
        # Run Non-Maximum Suppression (NMS) to filter boxes
        from utils.general import non_max_suppression
        detections = non_max_suppression(raw_pred, conf_thres=0.25, iou_thres=0.45)[0]
        
        # Extract class mapping
        class_mapping = {
            "healthy": "Healthy",
            "cocci": "Coccidiosis",
            "salmo": "Salmonella",
            "ncd": "Newcastle Disease"
        }
        
        if detections is not None and len(detections) > 0:
            # Sort detections by confidence (index 4) descending
            det_list = detections.tolist()
            det_list.sort(key=lambda x: x[4], reverse=True)
            top_det = det_list[0]
            
            class_idx = int(top_det[5])
            confidence = round(float(top_det[4]) * 100.0, 2)
            
            # Get class name from model names dictionary
            raw_class = self.model.names.get(class_idx, "healthy")
        else:
            raw_class = "healthy"
            confidence = 100.0
            
        prediction = class_mapping.get(raw_class.lower(), raw_class.capitalize())
        processing_time_ms = int((time.time() - start_time) * 1000)
        
        return prediction, confidence, processing_time_ms

predictor = YoloPredictor()

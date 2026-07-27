import os
import shutil
import uuid
from fastapi import FastAPI, UploadFile, File, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from backend.app.config import settings
from backend.app.schemas import StatusResponse, HealthResponse, PredictionResponse
from backend.app.predictor import predictor
from backend.app.utils import is_allowed_file

app = FastAPI(
    title=settings.PROJECT_NAME,
    description="FastAPI Backend for Smart Poultry Farming disease detection using YOLOv5",
    version="1.0.0"
)

# Enable CORS for localhost and mobile emulators
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Create folders if they do not exist
os.makedirs(settings.UPLOAD_DIR, exist_ok=True)
os.makedirs(settings.OUTPUT_DIR, exist_ok=True)

@app.on_event("startup")
async def startup_event():
    # Load YOLO model once during startup
    try:
        predictor.load_model()
    except Exception as e:
        print(f"CRITICAL: Failed to load YOLOv5 model on startup: {e}")

@app.get("/", response_model=StatusResponse)
async def get_status():
    return {
        "status": "running",
        "model": "loaded" if predictor.model is not None else "failed_to_load"
    }

@app.get("/health", response_model=HealthResponse)
async def get_health():
    return {
        "status": "healthy"
    }

@app.post("/predict", response_model=PredictionResponse)
async def predict_image(file: UploadFile = File(...)):
    # Validate missing image
    if not file:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No file uploaded"
        )
    
    # Validate unsupported extension
    if not is_allowed_file(file.filename):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Unsupported file type. Allowed formats: {', '.join(settings.ALLOWED_EXTENSIONS)}"
        )
    
    # Generate unique path for the uploaded file
    file_ext = file.filename.rsplit(".", 1)[1].lower()
    unique_filename = f"{uuid.uuid4()}.{file_ext}"
    temp_file_path = os.path.join(settings.UPLOAD_DIR, unique_filename)
    
    # Save the file temporarily
    try:
        with open(temp_file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to save temporary file: {str(e)}"
        )
        
    # Run YOLO inference
    try:
        prediction, confidence, processing_time_ms = predictor.predict(temp_file_path)
    except Exception as e:
        # Clean up temp file on failure
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Prediction failed: {str(e)}"
        )
        
    # Clean up temp file on success
    try:
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)
    except Exception as e:
        print(f"Warning: Failed to delete temp file {temp_file_path}: {e}")
        
    return {
        "success": True,
        "prediction": prediction,
        "confidence": confidence,
        "processing_time_ms": processing_time_ms,
        "message": "Prediction completed successfully"
    }

# Smart Poultry Farming Backend

FastAPI-based backend API for diagnosing poultry diseases (Coccidiosis, Salmonella, Newcastle Disease) through YOLOv5 Instance Segmentation models.

---

## 🛠️ Requirements & Installation

1. **Create virtual environment**:
   ```bash
   python3 -m venv venv
   source venv/bin/activate
   ```

2. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

---

## 🚀 Running the Server

Start the Uvicorn ASGI server from the `backend` parent directory:

```bash
uvicorn backend.app.main:app --host 0.0.0.0 --port 8000 --reload
```

---

## 📖 API Documentation (Swagger)

FastAPI provides automatic interactive API documentation:
- **Swagger UI**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **ReDoc**: [http://localhost:8000/redoc](http://localhost:8000/redoc)

---

## 📡 API Endpoints

### 1. GET `/`
Check status of the server and confirm if the YOLOv5 model weights loaded successfully.
- **Response**:
  ```json
  {
    "status": "running",
    "model": "loaded"
  }
  ```

### 2. GET `/health`
Health check endpoint.
- **Response**:
  ```json
  {
    "status": "healthy"
  }
  ```

### 3. POST `/predict`
Upload an image of poultry feces to diagnose its health status.
- **Request Type**: `multipart/form-data`
- **Parameter**: `file` (Upload file: `.jpg`, `.jpeg`, or `.png`)
- **Response**:
  ```json
  {
    "success": true,
    "prediction": "Coccidiosis",
    "confidence": 94.31,
    "processing_time_ms": 112,
    "message": "Prediction completed successfully"
  }
  ```

from pydantic import BaseModel

class StatusResponse(BaseModel):
    status: str
    model: str

class HealthResponse(BaseModel):
    status: str

class PredictionResponse(BaseModel):
    success: bool
    prediction: str
    confidence: float
    processing_time_ms: int
    message: str

import os

class Settings:
    PROJECT_NAME: str = "Smart Poultry Farming Backend"
    MODEL_PATH: str = os.path.abspath(os.path.join(os.path.dirname(__file__), "../models/best.pt"))
    UPLOAD_DIR: str = os.path.abspath(os.path.join(os.path.dirname(__file__), "../uploads"))
    OUTPUT_DIR: str = os.path.abspath(os.path.join(os.path.dirname(__file__), "../outputs"))
    ALLOWED_EXTENSIONS: set = {"jpg", "jpeg", "png"}

settings = Settings()

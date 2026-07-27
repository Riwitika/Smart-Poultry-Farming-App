from .config import settings

def is_allowed_file(filename: str) -> bool:
    if "." not in filename:
        return False
    ext = filename.rsplit(".", 1)[1].lower()
    return ext in settings.ALLOWED_EXTENSIONS

# Smart Poultry Farming

## Overview
Smart Poultry Farming is an enterprise-grade AI-powered system designed to monitor poultry health and diagnose diseases (Coccidiosis, Salmonella, Newcastle Disease) through instance segmentation of poultry fecal samples. The project consists of a Kotlin-based Android application utilizing Jetpack Compose and Material 3, a FastAPI backend serving a YOLOv5 Segmentation model, and Firebase integration for authentication and storage.

---

## 📂 Folder Structure
The workspace is organized as follows:

```
Smart Poultry Farming/
├── android-app/          # Kotlin Android Application (Jetpack Compose, MVVM)
├── backend/              # FastAPI Application & ML Model Services
│   ├── app/              # Backend Application Logic
│   ├── models/           # Trained Machine Learning Model Weights (best.pt)
│   ├── uploads/          # Directory for Uploaded Test Images
│   └── outputs/          # Directory for Processed Segmentation Outputs
├── dataset/              # Dataset Archives for Training and Testing (.zip)
├── training/             # YOLOv5 Training Metrics, Logs, and Evaluation Artifacts
├── docs/                 # Project Documentation and Reports
│   ├── report/           # Academic / Project Reports
│   ├── screenshots/      # UI Screenshots
│   └── presentation/     # Presentation Slides (PPTX)
├── README.md             # Project Documentation Overview
└── .gitignore            # Git Ignore Configuration
```

---

## 🛠️ Technologies
- **Mobile Frontend**: Kotlin, Jetpack Compose, Jetpack Lifecycle (ViewModel), Navigation Compose, Material 3, StateFlow, Coil, Retrofit, OkHttp, Coroutines
- **Backend API**: Python, FastAPI, Uvicorn, Render (Deployment Host)
- **Computer Vision / AI**: YOLOv5 Instance Segmentation (PyTorch), OpenCV
- **Cloud Infrastructure**: Firebase Auth, Firebase Cloud Firestore, Firebase Storage

---

## 📊 Dataset Source
*Placeholder: Information about the dataset source, origins of the fecal sample images, and annotation methodologies will be documented here.*

---

## 🚀 Setup Instructions
*Placeholder: Setup instructions for setting up the Android application, installing Python dependencies, running the FastAPI server locally, and deploying the model backend will be documented here.*

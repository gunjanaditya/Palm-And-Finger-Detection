📌 Project Overview
A production-level Android application that performs real-time palm and finger detection using the device camera. The app captures palm and individual finger images, validates them using simulated biometric matching, and stores them with full capture metadata.

🚀 Build & Run Instructions
Prerequisites

Android Studio Hedgehog or above
Physical Android device (API 26+) — emulator not supported (CameraX + MediaPipe)
JDK 17

Steps
# 1. Clone the repository

# 2. Open in Android Studio

# 3. Download MediaPipe model and place in assets/
# URL: https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
# Path: app/src/main/assets/hand_landmarker.task

# 4. Connect physical device and run
Required Permissions

CAMERA
WRITE_EXTERNAL_STORAGE (API ≤ 28)
READ_MEDIA_IMAGES (API ≥ 33)

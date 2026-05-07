Assignment 1: Palm &amp; Finger detection
Objective: Develop a native mobile application that captures your palm using a custom
camera to detect the number of fingers and the minutiae points of each finger. After detecting
the hand, validate each finger against the minutiae records captured during palm detection to
determine whether it is same finger of that person.
a) Create a custom base activity which will handle fragments as well as runtime permission as per
the usage.
b) The UI/UX should be simple, responsive and clean.
c) Implement runtime permissions for camera usage and read/write storage.
d) Use any lightweight AI/ML Kit for palm and finger detection.
e) Create a custom camera using Camerax or any other camera API.
f) Develop a luminosity analyzer or camera sensor-based analyzer to detect low light, bright light
and normal light. Based on this, the app should adjust the camera brightness and ensure the
picture is captured under consistent lighting conditions.
g) Add an overlay on the custom camera for palm detection.
h) After palm detection, create another oval overlay (Centered, with a blurred background) on a
custom camera for finger detection.
i) During finger detection, display the number of fingers scanned (like ⅕) as text.
j) Capture each finger and save it to a separate folder within external storage named “Finger Data”
k) Ensure the camera has auto-focus functionality when capturing palm and finger images, so that
clear images are obtained.
l) Implement a blur detection mechanism before saving any finger image to external storage. If an
image is blurred, prompt the user to recapture the finger.
m) Before capturing the palm, detect which hand (left/right) it is.
n) Before capturing a finger, validate it against the minutiae records of each finger captured during
palm detection, and display a toast message indicating which finger it is from the respective palm.
o)  If you detect your palm and then try to scan the finger of another person, the app should display
an error message: “Finger does not match”.
p)  If you detect your left palm and then try to scan your right-hand fingers or vice-versa, the app
should also display an error message: “Incorrect Finger” or “Finger does not match.”
q) During palm detection, if the user shows the dorsal side of the palm, the app should detect this
and display an error message at the bottom of the screen in red or white: “Palm dorsal side
detected, minutiae points won’t be extracted.”
r) During finger detection, if the user shows the dorsal side of the finger, the app should detect this
and display an error message at the bottom of the screen in red or white: “Finger dorsal side
detected, please show palm side finger which contains finger record or minutiae points”.

Technical Assignment

s) Based on Device ID save the data of luminosity analyzer data points like brightness score, type of
camera (rear/front), camera details like focal length, aperture score, focus distance, blur score.
t) Post capture of the image , fetch the blur score , brightness score and focus distance on the final
screen , as a result.
u) After capturing the palm and finger, save the image into the “Finger Data” folder using following
format.
Palm Format
“Left_Hand_timestampwithtime.png”,
“Right_Hand_timestampwithtime.jpg”

Finger Format
“Left_Hand_Thumb_Finger_timestampwithtime.jpg”,
“Left_Hand_Index_Finger_timestampwithtime.jpg”,
“Left_Hand_Middle_Finger_timestampwithtime.jpg”,
“Left_Hand_Ring_Finger_timestampwithtime.jpg”,
“Left_Hand_Little_Finger_timestampwithtime.jpg”,
“Right_Hand_Thumb_Finger_timestampwithtime.jpg”,
“Right_Hand_Index_Finger_timestampwithtime.jpg”
,“Right_Hand_Middle_Finger_timestampwithtime.jpg”
,“Right_Hand_Ring_Finger_timestampwithtime.jpg” ,
“Right_Hand_Little_Finger_timestampwithtime.jpg”.

Evaluation Criteria:
● Code Quality: Clarity, organization, adherence to platform-specific best practices (eg: using
ViewModel on Android (Kotlin), custom camera proper memory management on Android &amp; iOS).
● Architecture: Use MVVM or MVP or MVI architecture for the development process. Please do
not use MVC architecture for development.
● Computer Vision Awareness: Understanding how the image quality and classification would tie
in, even if simulated
● Error Handling: Graceful handling of scenarios like camera based services being disabled or
permission not granted.
● UI implementation: Basic but functional UI.
● Problem Solving: How the edge cases can be handled (example crash recovery or partial
submission)

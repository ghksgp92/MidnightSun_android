LOCAL_PATH := $(call my-dir)

OPENCV_INSTALL_MODULES := on
OPENCV_CAMERA_MODULES := on
OPENCV_LIB_TYPE := SHARED

include $(CLEAR_VARS)

LOCAL_LDLIBS += -latomic

# $(LOCAL_PATH) gives directory names with spaces (eg. "Folder Name")새
# GNU make doesn't handle spaces, should be DOS style directory names
# LOCAL_SRC_FILES := ../$(LOCAL_PATH)/dlib/all/source.cpp
# Your dlib source.cpp path of your local copy of project in DOS style
LOCAL_SRC_FILES := C:/Users/hjjin/workspace/workspace-movie/android/MOVIE/app/src/main/jni/dlib/all/source.cpp

LOCAL_MODULE := dlib

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

# You can define environment variable as "OPENCV_ANDROID_SDK" that points OpenCV local repo path
ifdef OPENCV_ANDROID_SDK
  include ${OPENCV_ANDROID_SDK}/sdk/native/jni/OpenCV.mk
else
  include C:/0setting/OpenCV-android-sdk/OpenCV-2411/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk
endif

# LOCAL_SRC_FILES := ../$(LOCAL_PATH)/DetectionBasedTracker.cpp
LOCAL_SRC_FILES := C:/Users/hjjin/workspace/workspace-movie/android/MOVIE/app/src/main/jni/DetectionBasedTracker.cpp

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS += -llog -ldl -latomic

LOCAL_MODULE := detectionbasedtracker

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

ifdef OPENCV_ANDROID_SDK
  include ${OPENCV_ANDROID_SDK}/sdk/native/jni/OpenCV.mk
else
  include C:/0setting/OpenCV-android-sdk/OpenCV-2411/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk
endif

# LOCAL_SRC_FILES := ../$(LOCAL_PATH)/FaceSwapper.cpp
LOCAL_SRC_FILES := C:/Users/hjjin/workspace/workspace-movie/android/MOVIE/app/src/main/jni/FaceSwapper.cpp

LOCAL_LDLIBS += -llog -ldl -latomic

LOCAL_MODULE := faceswapper

include $(BUILD_SHARED_LIBRARY)
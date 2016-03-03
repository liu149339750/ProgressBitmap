LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ProgressBitmap
LOCAL_SRC_FILES := ProgressBitmap.cpp
LOCAL_LDLIBS += -ljnigraphics -llog

include $(BUILD_SHARED_LIBRARY)


LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)

LOCAL_MODULE := libbackend_native
LOCAL_SRC_FILES := backendlib.cpp

LOCAL_CFLAGS += -O3 -ffast-math
LOCAL_DISABLE_FATAL_LINKER_WARNINGS := true

include $(BUILD_SHARED_LIBRARY)
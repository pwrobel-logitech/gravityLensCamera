
LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)

LOCAL_MODULE := libbackend_native
LOCAL_SRC_FILES := backendlib.cpp

LOCAL_CFLAGS += -O3 -ffast-math
LOCAL_DISABLE_FATAL_LINKER_WARNINGS := true
LOCAL_LDFLAGS := -llog
LOCAL_SHARED_LIBRARIES:=liblog

include $(BUILD_SHARED_LIBRARY)
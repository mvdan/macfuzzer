LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
TESTS := tests/jni
TINYTEST := external/tinytest
NATIVEJNI := ../../jni

LOCAL_MODULE    := nativetests
LOCAL_SRC_FILES := $(TINYTEST)/tinytest.c test_chmacaddr.c $(NATIVEJNI)/chmacaddr.c $(NATIVEJNI)/native_ioctller.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(TINYTEST) $(LOCAL_PATH)/../../jni
LOCAL_LDLIBS := -llog
include $(BUILD_EXECUTABLE)



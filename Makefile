all: native
	ant debug

native:
	~/android-ndk-r10c/ndk-build -B
	install -v libs/armeabi/change_mac res/raw/
	install -v libs/armeabi/libnative_ioctller.so res/raw/

install: all
	adb install -r bin/ChMacAddroid-debug.apk

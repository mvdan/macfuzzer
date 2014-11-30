all: native
	ant debug

native:
	~/android-ndk-r10c/ndk-build -B
	install -v libs/armeabi/chmacaddr res/raw/
	install -v libs/armeabi/libnative_ioctller.so res/raw/

install: all
	adb install -r bin/ChMacAddroid-debug.apk

clean:
	ant clean && cd tests && ant clean

test: clean install
	  cd tests && ant debug && \
	  adb install -r bin/ChMacAddroidTest-debug.apk && \
	  adb logcat -c && \
	  ant test && \
	  adb logcat -C


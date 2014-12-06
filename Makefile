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
	rm -rf libs/

test: clean native
	  cd tests && ant uninstall && \
          ant debug && \
	  install -v ../libs/armeabi/nativetests ../res/raw/
	cd tests && ant debug install && \
	  adb logcat -c && \
	  ant test && \
	  adb logcat -C -t 1000


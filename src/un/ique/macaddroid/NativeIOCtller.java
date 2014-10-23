package un.ique.macaddroid;

import un.ique.macaddroid.Layer2Address;

public class NativeIOCtller {
    private String mInterface;
    private byte[] mAddr;

    static {
        System.loadLibrary("native_ioctller");
    }

    public NativeIOCtller(Layer2Address macAddr) {
        mAddr = macAddr.getAddress();
        mInterface = macAddr.getInterfaceName();
    }

    public native byte[] getCurrentMacAddr();
    public native String getCurrentMacAddrError();

    public native int setMacAddr(byte[] mac);
}

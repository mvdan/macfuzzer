package un.ique.macaddroid;

import java.security.SecureRandom;

public class Layer2Address {
    private String mInterface;
    private byte[] mAddr;

    public Layer2Address(byte[] addr, String iface) {
        mAddr = new byte[6];
        mInterface = iface;
        mAddr = addr;
    }

    public Layer2Address() {
        mAddr = new byte[6];
    }

    public void setInterfaceName(String iface) {
        mInterface = iface;
    }

    public void setAddress(byte[] addr) {
        mAddr = addr;
    }

    public String getInterfaceName() {
        return mInterface;
    }

    public byte[] getAddress() {
        return mAddr;
    }

    public byte[] generateNewAddress() {
        // We need to respect U/L and Uni/Multicast flag bits
        // See MACADDRESSING and [0] for details.
        // [0] http://standards.ieee.org/develop/regauth/tut/macgrp.pdf

        byte[] newAddr = new byte[6];

        {
            // Let's generate some random bytes
            SecureRandom sr = new SecureRandom();
            byte[] output = new byte[6];
            sr.nextBytes(output);

            for (int i = 0; i < 6; i++) {
                if (output[i] < 0) {
                    output[i] = (byte) (0x40 - (int)output[i]);
                }
                newAddr[i] = output[i];
            } 
        }

        // Always pretend to be the burned-in address
        newAddr[0] &= ~2;

        // We only want to be a unicast interface
        newAddr[0] &= ~1;

        return newAddr;
    }

    public String formatAddress() {
        String strAddr = "";
        for (int i = 0; i < 6; i++) {
            strAddr += String.format("%02x%s", mAddr[i], (i!=5?":":""));
        }
        return strAddr;
    }
}

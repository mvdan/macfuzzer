/*
 * ChMacAddroid - Android app that changes a network devices MAC address
 * Copyright (C) 2014 Matthew Finkel <Matthew.Finkel@gmail.com>
 *
 * This file is part of ChMacAddroid
 *
 * ChMacAddroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ChMacAddroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ChMacAddroid, in the COPYING file.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package un.ique.chmacaddroid;

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
            sr.nextBytes(newAddr);
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

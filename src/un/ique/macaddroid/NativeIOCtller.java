/*
 * MacAddroid - Android app that changes a network devices MAC address
 * Copyright (C) 2014 Matthew Finkel <Matthew.Finkel@gmail.com>
 *
 * This file is part of MacAddroid
 *
 * MacAddroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MacAddroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MacAddroid, in the COPYING file.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

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
    public native String getErrorString(int errcode);

    public native int getCurrentUID();
}

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

package un.ique.chmacaddroid.test;

import android.util.Log;
import java.security.SecureRandom;
import un.ique.chmacaddroid.Layer2Address;

import junit.framework.TestCase;

public class Layer2AddressTest extends TestCase {
    private Layer2Address mL2a;
    private String mCurrAddr = "";
    private String mIface;

    public Layer2AddressTest() {
        super("un.ique.chmacaddroid.Layer2Address");
    }

    @Override
    public void setUp() throws Exception {
        mL2a = new Layer2Address();
        mIface = "testdev";
    }

    public void testConstructors() {
        byte[] addr = {0x12, 0x34, 0x56, 0x78, 0x76, 0x54};
        Layer2Address l2a = new Layer2Address(addr, mIface);
        assertEquals(l2a.getAddress(), addr);
        assertEquals(l2a.getInterfaceName(), mIface);
    }

    public void testInterfaceName() {
        mL2a.setInterfaceName(mIface);
        assertEquals(mIface, mL2a.getInterfaceName());
    }

    public void testAddress() {
        byte[] addr = mL2a.generateNewAddress();
        mL2a.setAddress(addr);
        assertEquals(addr, mL2a.getAddress());

        byte[] addr2 = {0x77, 0x66, 0x55, 0x01, 0x23, 0x45};
        String expectedAddr = "77:66:55:01:23:45";
        mL2a.setAddress(addr2);
        String actualAddr = mL2a.formatAddress();
        assertEquals(expectedAddr, actualAddr);
    }
}

/*
 * Copyright (C) 2014 Matthew Finkel <Matthew.Finkel@gmail.com>
 * Copyright 2015 Daniel Martí <mvdan@mvdan.cc>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cc.mvdan.macfuzzer;

import java.security.SecureRandom;
import java.util.Locale;

public class Layer2Address {
    private byte[] bytes;

    public Layer2Address() {
        this.bytes = new byte[6];
    }

    public Layer2Address(byte[] bytes) {
        setBytes(bytes);
    }

    public void setBytes(byte[] bytes) {
        if (bytes.length != 6) {
            return;
        }
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void randomize() {
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(bytes);

        // We need to respect U/L and Uni/Multicast flag bits
        // See MACADDRESSING and [0] for details.
        // [0] http://standards.ieee.org/develop/regauth/tut/macgrp.pdf

        // Always pretend to be the burned-in address
        bytes[0] &= ~2;

        // We only want to be a unicast interface
        bytes[0] &= ~1;
    }

    public String toString() {
        return String.format(Locale.ENGLISH,
                "%02X:%02X:%02X:%02X:%02X:%02X",
                bytes[0] & 0xFF,
                bytes[1] & 0xFF,
                bytes[2] & 0xFF,
                bytes[3] & 0xFF,
                bytes[4] & 0xFF,
                bytes[5] & 0xFF)
            .toUpperCase(Locale.ENGLISH);
    }
}

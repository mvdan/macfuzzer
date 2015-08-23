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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String dev = "wlan0";
    private NativeIOCtller ctller;
    private Layer2Address addr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ctller = new NativeIOCtller(dev);
        addr = new Layer2Address();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        addr.setBytes(ctller.getCurrentMacAddr());
        TextView macField = (TextView) findViewById(R.id.textview_current_mac);
        macField.setText(addr.toString());
    }

    private String getInputStr(int id) {
        return ((TextView) findViewById(id)).getText().toString();
    }

    private void setInputStr(int id, String str) {
        ((TextView) findViewById(id)).setText(str);
    }

    private int getInputByte(int id) {
        String str = getInputStr(id);

        int result;
        try {
            result = Integer.parseInt(str, 16);
        } catch (NumberFormatException e) {
            return -1;
        }
        if (result < 0 || result > 255) {
            return -1;
        }
        return result;
    }

    private void setInputByte(int id, int b) {
        String str = String.format(Locale.ENGLISH, "%02X", b).toUpperCase();

        setInputStr(id, str);
    }

    private String getInputMac() {
        int b1 = getInputByte(R.id.edittext_mac_byte1);
        int b2 = getInputByte(R.id.edittext_mac_byte2);
        int b3 = getInputByte(R.id.edittext_mac_byte3);
        int b4 = getInputByte(R.id.edittext_mac_byte4);
        int b5 = getInputByte(R.id.edittext_mac_byte5);
        int b6 = getInputByte(R.id.edittext_mac_byte6);

        if (b1 < 0 || b2 < 0 || b3 < 0 || b4 < 0 || b5 < 0 || b6 < 0) {
            return null;
        }

        return String.format(Locale.ENGLISH,
                "%02X:%02X:%02X:%02X:%02X:%02X", b1, b2, b3, b4, b5, b6)
            .toUpperCase(Locale.ENGLISH);
    }

    private void setInputMac(byte[] bytes) {
        if (bytes.length != 6) {
            return;
        }
        setInputByte(R.id.edittext_mac_byte1, bytes[0]);
        setInputByte(R.id.edittext_mac_byte2, bytes[1]);
        setInputByte(R.id.edittext_mac_byte3, bytes[2]);
        setInputByte(R.id.edittext_mac_byte4, bytes[3]);
        setInputByte(R.id.edittext_mac_byte5, bytes[4]);
        setInputByte(R.id.edittext_mac_byte6, bytes[5]);
    }

    public void macApply(View view) {
        String addr = getInputMac();
        if (addr == null) {
            return;
        }
        String uid = Integer.toString(ctller.getCurrentUID());
        FileStuff fs = new FileStuff(this);
        File exe = fs.copyBinaryFile();

        ProcessResult pr = fs.runBlob(dev, addr, uid);
        refresh();
    }

    public void macReset(View view) {
        setInputMac(ctller.getCurrentMacAddr());
    }

    public void macRand(View view) {
        Layer2Address randAddr = new Layer2Address();
        randAddr.randomize();
        setInputMac(randAddr.getBytes());
    }

    public void macClear(View view) {
        setInputStr(R.id.edittext_mac_byte1, null);
        setInputStr(R.id.edittext_mac_byte2, null);
        setInputStr(R.id.edittext_mac_byte3, null);
        setInputStr(R.id.edittext_mac_byte4, null);
        setInputStr(R.id.edittext_mac_byte5, null);
        setInputStr(R.id.edittext_mac_byte6, null);
    }

}

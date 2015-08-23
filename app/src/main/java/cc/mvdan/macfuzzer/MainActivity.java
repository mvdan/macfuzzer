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
        PRNGFixes.apply();
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
        TextView macField = (TextView) findViewById(R.id.textview_current_mac_addr);
        macField.setText(addr.toString());
    }

    private String getInputStr(int id) {
        return ((TextView) findViewById(id)).getText().toString();
    }

    private void setInputStr(int id, String str) {
        ((TextView) findViewById(id)).setText(str);
    }

    private byte getInputByte(int id) {
        String str = getInputStr(id);

        int i;
        try {
            i = Integer.parseInt(str, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
        if (i < 0 || i > 255) {
            return 0;
        }
        return (byte)i;

    }

    private void setInputByte(int id, int b) {
        String str = String.format(Locale.ENGLISH,
                "%02X", b).toUpperCase(Locale.ENGLISH);

        setInputStr(id, str);
    }

    private byte[] getInputBytes() {
        byte b1 = getInputByte(R.id.edittext_mac_byte1);
        byte b2 = getInputByte(R.id.edittext_mac_byte2);
        byte b3 = getInputByte(R.id.edittext_mac_byte3);
        byte b4 = getInputByte(R.id.edittext_mac_byte4);
        byte b5 = getInputByte(R.id.edittext_mac_byte5);
        byte b6 = getInputByte(R.id.edittext_mac_byte6);

        return new byte[]{b1, b2, b3, b4, b5, b6};
    }

    private void setInputBytes(byte[] bytes) {
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
        byte[] bytes = getInputBytes();
        if (bytes == null) {
            return;
        }
        String uid = Integer.toString(ctller.getCurrentUID());
        FileStuff fs = new FileStuff(this);
        File exe = fs.copyBinaryFile();

        Layer2Address addr = new Layer2Address(bytes);
        ProcessResult pr = fs.runBlob(dev, addr.toString(), uid);
        refresh();
    }

    public void macReset(View view) {
        setInputBytes(ctller.getCurrentMacAddr());
    }

    public void macRand(View view) {
        Layer2Address randAddr = new Layer2Address();
        randAddr.randomize();
        setInputBytes(randAddr.getBytes());
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

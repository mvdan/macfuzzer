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

import android.app.Activity;
import android.os.Bundle;
import un.ique.chmacaddroid.Layer2Address;
import un.ique.chmacaddroid.NativeIOCtller;
import un.ique.chmacaddroid.FileStuff;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.lang.NumberFormatException;

public class ManualMac extends Activity {
    // Let's hardcode wlan0, for now
    private String dev = "wlan0";
    private Layer2Address mNewNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manualmac);

        Layer2Address newNet = new Layer2Address();
        newNet.setInterfaceName(dev);
        mNewNet = newNet;
        NativeIOCtller ctller = new NativeIOCtller(newNet);
        newNet.setAddress(ctller.getCurrentMacAddr());
        String addr = newNet.formatAddress();
        TextView macField = (TextView)
            findViewById(R.id.manualmac_macaddress);
        if (macField != null) {
            macField.setText(addr);
        }
    }

    public String getManualMac() {
        String byte1, byte2, byte3, byte4, byte5, byte6;

        TextView macField = (TextView)
            findViewById(R.id.manualmac_byte1);
        if (macField != null) {
            byte1 = macField.getText().toString();
        } else {
            /* TODO Create user notification */
            return "";
        }
        try {
            Integer.parseInt(byte1, 16);
        } catch (NumberFormatException e) {
            // TODO Useful user message
        }
        if (byte1 == "" || byte1.length() != 2) {
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte2);
        if (macField != null) {
            byte2 = macField.getText().toString();
        } else {
            return "";
        }
        try {
            Integer.parseInt(byte1, 16);
        } catch (NumberFormatException e) {
            // TODO Useful user message
        }
        if (byte2 == "" || byte2.length() != 2) {
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte3);
        if (macField != null) {
            byte3 = macField.getText().toString();
        } else {
            return "";
        }
        try {
            Integer.parseInt(byte2, 16);
        } catch (NumberFormatException e) {
            // TODO Useful user message
           return "";
        }
        if (byte3 == "" || byte3.length() != 2) {
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte4);
        if (macField != null) {
            byte4 = macField.getText().toString();
        } else {
            return "";
        }
        try {
            Integer.parseInt(byte3, 16);
        } catch (NumberFormatException e) {
            // TODO Useful user message
           return "";
        }
        if (byte4 == "" || byte4.length() != 2) {
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte5);
        if (macField != null) {
            byte5 = macField.getText().toString();
        } else {
            return "";
        }
        try {
            Integer.parseInt(byte4, 16);
        } catch (NumberFormatException e) {
            // TODO Useful user message
           return "";
        }
        if (byte5 == "" || byte5.length() != 2) {
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte6);
        if (macField != null) {
            byte6 = macField.getText().toString();
        } else {
            return "";
        }
        try {
            Integer.parseInt(byte6, 16);
        } catch (NumberFormatException e) {
            // TODO Useful user message
           return "";
        }
        if (byte6 == "" || byte6.length() != 2) {
            return "";
        }

        String mac = byte1.toLowerCase() + ":" + byte2.toLowerCase();
        mac += ":" + byte3.toLowerCase() + ":" + byte4.toLowerCase();
        mac += ":" + byte5.toLowerCase() + ":" + byte6.toLowerCase();
        return mac;
    }


    public void applyNewAddress(View view) {
        String addr = getManualMac();
        if (addr == "") {
            /* TODO alert */
            return;
        }
        NativeIOCtller ctller = new NativeIOCtller(mNewNet);
        String uid = Integer.toString(ctller.getCurrentUID());
        FileStuff fs = new FileStuff(this);
        File exe = fs.copyBinaryFile();
        /* TOCTOU but this let's us handle the failure easier */
        if (exe == null) {
            // TODO Show a useful message like "Please restart this app.
            // We're broken?"
        }

        fs.runBlob(dev, addr, uid);

        mNewNet.setAddress(ctller.getCurrentMacAddr());
        TextView macField = (TextView)
            findViewById(R.id.manualmac_macaddress);
        if (macField != null) {
            macField.setText(addr);
        }
    }
}

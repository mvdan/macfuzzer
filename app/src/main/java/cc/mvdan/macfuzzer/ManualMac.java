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
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

public class ManualMac extends Activity {
    // Let's hardcode wlan0, for now
    private String dev = "wlan0";
    private Layer2Address mNewNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manualmac);
    }

    @Override
    public void onResume() {
        super.onResume();

        Layer2Address newNet = new Layer2Address();
        newNet.setInterfaceName(dev);
        mNewNet = newNet;
        NativeIOCtller ctller = new NativeIOCtller(newNet);
        newNet.setAddress(ctller.getCurrentMacAddr());
        String addr = newNet.formatAddress();
        TextView macField = (TextView) findViewById(R.id.manualmac_macaddress);
        macField.setText(addr);
    }

    private int getFieldByte(int id) {
        int result;
        TextView tv = (TextView) findViewById(id);

        String str = tv.getText().toString();

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

    private String getManualMac() {

        int byte1 = getFieldByte(R.id.manualmac_byte1);
        if (byte1 == -1) {
            return null;
        }
        int byte2 = getFieldByte(R.id.manualmac_byte2);
        if (byte2 == -1) {
            return null;
        }
        int byte3 = getFieldByte(R.id.manualmac_byte3);
        if (byte3 == -1) {
            return null;
        }
        int byte4 = getFieldByte(R.id.manualmac_byte4);
        if (byte4 == -1) {
            return null;
        }
        int byte5 = getFieldByte(R.id.manualmac_byte5);
        if (byte5 == -1) {
            return null;
        }
        int byte6 = getFieldByte(R.id.manualmac_byte6);
        if (byte6 == -1) {
            return null;
        }

        return String.format(Locale.ENGLISH, "%02X:%02X:%02X:%02X:%02X:%02X",
                byte1, byte2, byte3, byte4, byte5, byte6);
    }


    public void cancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void applyNewAddress(View view) {
        String addr = getManualMac();
        if (addr == null) {
            return;
        }
        NativeIOCtller ctller = new NativeIOCtller(mNewNet);
        String uid = Integer.toString(ctller.getCurrentUID());
        FileStuff fs = new FileStuff(this);
        File exe = fs.copyBinaryFile();
        /* TOCTOU but this let's us handle the failure easier */

        ProcessResult pr = fs.runBlob(dev, addr, uid);

        mNewNet.setAddress(ctller.getCurrentMacAddr());
        if (addr.compareTo(mNewNet.formatAddress()) == 0) {
        } else {
            String msg = getString(R.string.current) + ": ";
            msg += mNewNet.formatAddress() + "\n";
            msg += getString(R.string.expected) + ": " + addr;
            msg += "\n\n";
            if (pr.getStdOut().compareTo("") != 0) {
                msg += pr.getStdOut() + "\n";
            }
            if (pr.getStdErr().compareTo("") != 0) {
                msg += pr.getStdErr();
            }

        }
        TextView macField = (TextView) findViewById(R.id.manualmac_macaddress);
        macField.setText(mNewNet.formatAddress());
    }
}

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

public class RandomMac extends Activity {
    // Let's hardcode wlan0, for now
    private String dev = "wlan0";
    private Layer2Address mNewNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.randommac);
    }

    @Override
    public void onResume() {
        super.onResume();

        Layer2Address newNet = new Layer2Address();
        newNet.setInterfaceName(dev);
        NativeIOCtller ctller = new NativeIOCtller(newNet);
        newNet.setAddress(ctller.getCurrentMacAddr());
        String addr = newNet.formatAddress();
        TextView macField = (TextView) findViewById(R.id.randommac_macaddress);
        macField.setText(addr);
        byte[] nextAddr = newNet.generateNewAddress();
        mNewNet = new Layer2Address(nextAddr, dev);
        TextView nextMacField = (TextView) findViewById(R.id.randommac_nextmacaddress);
        if (nextMacField != null) {
            nextMacField.setText(mNewNet.formatAddress());
        }
    }

    public void cancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void showNewAddress(View view) {
        byte[] nextAddr = mNewNet.generateNewAddress();
        mNewNet = new Layer2Address(nextAddr, dev);
        TextView nextMacField = (TextView) findViewById(R.id.randommac_nextmacaddress);
        if (nextMacField != null) {
            nextMacField.setText(mNewNet.formatAddress());
        }
    }

    public void applyNewAddress(View view) {
        NativeIOCtller ctller = new NativeIOCtller(mNewNet);
        String uid = Integer.toString(ctller.getCurrentUID());
        FileStuff fs = new FileStuff(this);
        File exe = fs.copyBinaryFile();
        String newAddr = mNewNet.formatAddress();
        Layer2Address newL2A = new Layer2Address();

        /* TOCTOU but this let's us handle the failure easier */
        ProcessResult pr = fs.runBlob(dev, newAddr, uid);

        newL2A.setAddress(ctller.getCurrentMacAddr());
        if (newAddr.compareTo(newL2A.formatAddress()) == 0) {
        } else {
            String msg = getString(R.string.current) + ": ";
            msg += newL2A.formatAddress() + "\n";
            msg += getString(R.string.expected) + ": " + newAddr;
            msg += "\n\n";
            if (pr.getStdOut().compareTo("") != 0) {
                msg += pr.getStdOut() + "\n";
            }
            if (pr.getStdErr().compareTo("") != 0) {
                msg += pr.getStdErr();
            }
        }
        TextView macField = (TextView) findViewById(R.id.randommac_macaddress);
        macField.setText(newL2A.formatAddress());
    }
}

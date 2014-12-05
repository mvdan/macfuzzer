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
import un.ique.chmacaddroid.UserNotice;
import un.ique.chmacaddroid.ProcessResult;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

public class RandomMac extends Activity {
    // Let's hardcode wlan0, for now
    private String dev = "wlan0";
    private Layer2Address mNewNet;
    private UserNotice mNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.randommac);
        mNotice = new UserNotice(this);

        Layer2Address newNet = new Layer2Address();
        newNet.setInterfaceName(dev);
        NativeIOCtller ctller = new NativeIOCtller(newNet);
        newNet.setAddress(ctller.getCurrentMacAddr());
        String addr = newNet.formatAddress();
        TextView macField = (TextView)
            findViewById(R.id.randommac_macaddress);
        if (macField != null) {
            macField.setText(addr);
        }
        byte[] nextAddr = newNet.generateNewAddress();
        mNewNet = new Layer2Address(nextAddr, dev);
        TextView nextMacField = (TextView)
            findViewById(R.id.randommac_nextmacaddress);
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
        TextView nextMacField = (TextView)
            findViewById(R.id.randommac_nextmacaddress);
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
        if (exe == null) {
            mNotice.showSuggestRestartAlert("noFileRestart");
        }
        ProcessResult pr = fs.runBlob(dev, newAddr, uid);

        newL2A.setAddress(ctller.getCurrentMacAddr());
        if (newAddr.compareTo(newL2A.formatAddress()) == 0) {
            mNotice.makeMeChangeStatusToast(R.string.change_success,
                                            Toast.LENGTH_SHORT);
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
            mNotice.launchAndCloseAlert(msg,
                            getString(R.string.change_failed),
                            R.string.button_okay, 0, "do_nothing",
                            "do_nothing", "failedChange");
        }
        TextView macField = (TextView)
            findViewById(R.id.randommac_macaddress);
        if (macField != null) {
            macField.setText(newL2A.formatAddress());
        }
    }
}

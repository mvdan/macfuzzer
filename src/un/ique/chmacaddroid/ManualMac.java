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
import un.ique.chmacaddroid.Main;
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
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.app.FragmentManager;

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
            showSuggestRestartAlert("byte1NoField");
            return "";
        }
        try {
            Integer.parseInt(byte1, 16);
        } catch (NumberFormatException e) {
            showInvalidEntryAlert(R.string.manualmac_notice_not_hex_1,
                                  "byte1NotHex");
            return "";
        }
        if (byte1.length() != 2) {
            showInvalidEntryAlert(R.string.manualmac_notice_2_chars_1,
                                  "byte1Not2");
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte2);
        if (macField != null) {
            byte2 = macField.getText().toString();
        } else {
            showSuggestRestartAlert("byte2NoField");
            return "";
        }
        try {
            Integer.parseInt(byte1, 16);
        } catch (NumberFormatException e) {
            showInvalidEntryAlert(R.string.manualmac_notice_not_hex_2,
                                  "byte2NotHex");
            return "";
        }
        if (byte2.length() != 2) {
            showInvalidEntryAlert(R.string.manualmac_notice_2_chars_2,
                                  "byte2Not2");
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte3);
        if (macField != null) {
            byte3 = macField.getText().toString();
        } else {
            showSuggestRestartAlert("byte3NoField");
            return "";
        }
        try {
            Integer.parseInt(byte2, 16);
        } catch (NumberFormatException e) {
            showInvalidEntryAlert(R.string.manualmac_notice_not_hex_3,
                                  "byte3NotHex");
            return "";
        }
        if (byte3.length() != 2) {
            showInvalidEntryAlert(R.string.manualmac_notice_2_chars_3,
                                  "byte3Not2");
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte4);
        if (macField != null) {
            byte4 = macField.getText().toString();
        } else {
            showSuggestRestartAlert("byte4NoField");
            return "";
        }
        try {
            Integer.parseInt(byte3, 16);
        } catch (NumberFormatException e) {
            showInvalidEntryAlert(R.string.manualmac_notice_not_hex_4,
                                  "byte4NotHex");
            return "";
        }
        if (byte4.length() != 2) {
            showInvalidEntryAlert(R.string.manualmac_notice_2_chars_4,
                                  "byte4Not2");
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte5);
        if (macField != null) {
            byte5 = macField.getText().toString();
        } else {
            showSuggestRestartAlert("byte5NoField");
            return "";
        }
        try {
            Integer.parseInt(byte4, 16);
        } catch (NumberFormatException e) {
            showInvalidEntryAlert(R.string.manualmac_notice_not_hex_5,
                                  "byte5NotHex");
            return "";
        }
        if (byte5.length() != 2) {
            showInvalidEntryAlert(R.string.manualmac_notice_2_chars_5,
                                  "byte5Not2");
            return "";
        }

        macField = (TextView)
            findViewById(R.id.manualmac_byte6);
        if (macField != null) {
            byte6 = macField.getText().toString();
        } else {
            showSuggestRestartAlert("byte6NoField");
            return "";
        }
        try {
            Integer.parseInt(byte6, 16);
        } catch (NumberFormatException e) {
            showInvalidEntryAlert(R.string.manualmac_notice_not_hex_6,
                                  "byte6NotHex");
            return "";
        }
        if (byte6.length() != 2) {
            showInvalidEntryAlert(R.string.manualmac_notice_2_chars_6,
                                  "byte6Not2");
            return "";
        }

        String mac = byte1.toLowerCase() + ":" + byte2.toLowerCase();
        mac += ":" + byte3.toLowerCase() + ":" + byte4.toLowerCase();
        mac += ":" + byte5.toLowerCase() + ":" + byte6.toLowerCase();
        return mac;
    }


    public void cancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void applyNewAddress(View view) {
        String addr = getManualMac();
        if (addr == "") {
            /* We already reported the error */
            return;
        }
        NativeIOCtller ctller = new NativeIOCtller(mNewNet);
        String uid = Integer.toString(ctller.getCurrentUID());
        FileStuff fs = new FileStuff(this);
        File exe = fs.copyBinaryFile();
        /* TOCTOU but this let's us handle the failure easier */
        if (exe == null) {
            showSuggestRestartAlert("noFileRestart");
        }

        fs.runBlob(dev, addr, uid);

        mNewNet.setAddress(ctller.getCurrentMacAddr());
        TextView macField = (TextView)
            findViewById(R.id.manualmac_macaddress);
        if (macField != null) {
            macField.setText(addr);
        }
    }

    public void showSuggestRestartAlert(String tag) {
        launchAndCloseRestartAlert(R.string.notice_restart,
                                   R.string.notice_restart_title,
                                   R.string.button_close_app,
                                   R.string.button_continue,
                                   "closeapp", "do_nothing",
                                   tag);
    }

    public void showInvalidEntryAlert(int messageId, String tag) {
        launchAndCloseRestartAlert(
                                messageId,
                                R.string.manualmac_notice_invalid_title,
                                R.string.button_okay,
                                0, "do_nothing", "do_nothing",
                                tag);
    }


    public void launchAndCloseRestartAlert(int messageId, int titleId,
                                           int pbutton, int nbutton,
                                           String cbPos, String cbNeg,
                                           String tag) {
        DialogFragment dialog =
                new AlertNoticeDialogFragment(messageId, titleId,
                                              pbutton, nbutton,
                                              cbPos, cbNeg);
        dialog.show(getFragmentManager(), tag);
        /*dialog.dismiss();*/
    }


    public void callbackActions(String cb) {
        if (cb == "closeapp") {
            setResult(Main.RESULT_EXIT);
            finish();
        } else if (cb == "do_nothing") {
            return;
        } else {
            return;
        }
    }

    private class AlertNoticeDialogFragment extends DialogFragment {
        int mId, tId, mPosButton, mNegButton;
        String callbackPos, callbackNeg;

        public AlertNoticeDialogFragment(int messageId, int titleId,
                                         int pbutton, int nbutton,
                                         String cbPos, String cbNeg) {
            super();
            mId = messageId;
            tId = titleId;
            mPosButton = pbutton;
            mNegButton = nbutton;
            callbackPos = cbPos;
            callbackNeg = cbNeg;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder =
                                 new AlertDialog.Builder(getActivity());
            builder.setMessage(mId).setTitle(tId);
            if (mNegButton != 0) {
                builder.setNegativeButton(mNegButton,
                                 new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        callbackActions(callbackNeg);
                    }
                });
            }
            if (mPosButton != 0) {
                builder.setPositiveButton(mPosButton,
                                 new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        callbackActions(callbackNeg);
                    }
                });
            }
            return builder.create();
        }
    }
}

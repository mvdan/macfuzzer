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
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String dev = "wlan0";
    private NativeIOCtller ctller;
    private Layer2Address addr;

    private EditText[] byteViews;

    private void watchJump(int curN, int nextN) {
        final EditText cur = byteViews[curN];
        final EditText next = byteViews[nextN];
        cur.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    focusEnd(next);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void afterTextChanged(Editable s) { }
        });
    }

    private static void focusEnd(EditText tv) {
        tv.requestFocus();
        tv.setSelection(tv.getText().length());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PRNGFixes.apply();
        setContentView(R.layout.main);
        byteViews = new EditText[]{
            (EditText) findViewById(R.id.edittext_mac_byte1),
            (EditText) findViewById(R.id.edittext_mac_byte2),
            (EditText) findViewById(R.id.edittext_mac_byte3),
            (EditText) findViewById(R.id.edittext_mac_byte4),
            (EditText) findViewById(R.id.edittext_mac_byte5),
            (EditText) findViewById(R.id.edittext_mac_byte6),
        };
        for (int i = 0; i < byteViews.length-1; i++) {
            byteViews[i].setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(2),
                new InputFilter.AllCaps(),
            });
        }
        for (int i = 0; i < byteViews.length-1; i++) {
            watchJump(i, i+1);
        }

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

    private String getInputStr(int n) {
        return byteViews[n].getText().toString();
    }

    private void setInputStr(int n, String str) {
        byteViews[n].setText(str);
    }

    private byte getInputByte(int n) {
        String str = getInputStr(n);

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

    private void setInputByte(int n, int b) {
        String str = String.format(Locale.ENGLISH,
                "%02X", b & 0xFF).toUpperCase(Locale.ENGLISH);

        setInputStr(n, str);
    }

    private byte[] getInputBytes() {
        byte[] bytes = new byte[6];
        for (int i = 0; i < byteViews.length; i++) {
            bytes[i] = getInputByte(i);
        }
        return bytes;
    }

    private void setInputBytes(byte[] bytes) {
        if (bytes == null) {
            for (int i = 0; i < byteViews.length; i++) {
                setInputStr(i, null);
            }
            focusEnd(byteViews[0]);
            return;
        }
        for (int i = 0; i < byteViews.length; i++) {
            setInputByte(i, bytes[i]);
        }
        focusEnd(byteViews[5]);
    }

    public void macApply(View view) {
        byte[] bytes = getInputBytes();
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
        setInputBytes(null);
    }

}

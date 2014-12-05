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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.os.Handler;
import android.os.Looper;
import java.lang.Runnable;
import un.ique.chmacaddroid.ManualMac;
import un.ique.chmacaddroid.Layer2Address;
import un.ique.chmacaddroid.NativeIOCtller;
import un.ique.chmacaddroid.R;

import android.test.ActivityInstrumentationTestCase2;

public class ManualMacTest extends ActivityInstrumentationTestCase2<ManualMac> {
    private ManualMac mMM;
    private TextView mHeader;
    private TextView mCurrMacLabel, mCurrMacVal;
    private TextView mColon1, mColon2, mColon3, mColon4, mColon5;
    private EditText mNextMacVal1, mNextMacVal2, mNextMacVal3;
    private EditText mNextMacVal4, mNextMacVal5, mNextMacVal6;
    private Button mApplyButton;
    private Button mCancelButton;

    public ManualMacTest() {
        super("un.ique.chmacaddroid", ManualMac.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mMM = getActivity();
        mHeader = (TextView) mMM.findViewById(R.id.manualmac_header);
        mCurrMacLabel =
            (TextView) mMM.findViewById(R.id.manualmac_current_mac);
        mCurrMacVal =
            (TextView) mMM.findViewById(R.id.manualmac_macaddress);
        mNextMacVal1 =
            (EditText) mMM.findViewById(R.id.manualmac_byte1);
        mNextMacVal2 =
            (EditText) mMM.findViewById(R.id.manualmac_byte2);
        mNextMacVal3 =
            (EditText) mMM.findViewById(R.id.manualmac_byte3);
        mNextMacVal4 =
            (EditText) mMM.findViewById(R.id.manualmac_byte4);
        mNextMacVal5 =
            (EditText) mMM.findViewById(R.id.manualmac_byte5);
        mNextMacVal6 =
            (EditText) mMM.findViewById(R.id.manualmac_byte6);
        mColon1 =
            (TextView) mMM.findViewById(R.id.manualmac_colon1);
        mColon2 =
            (TextView) mMM.findViewById(R.id.manualmac_colon2);
        mColon3 =
            (TextView) mMM.findViewById(R.id.manualmac_colon3);
        mColon4 =
            (TextView) mMM.findViewById(R.id.manualmac_colon4);
        mColon5 =
            (TextView) mMM.findViewById(R.id.manualmac_colon5);
        mApplyButton =
            (Button) mMM.findViewById(R.id.manualmac_apply_button);
        mCancelButton =
            (Button) mMM.findViewById(R.id.manualmac_cancel_button);
    }

    public void testPreconditions() {
        assertNotNull("mMM is null", mMM);
        assertNotNull("mHeader is null", mHeader);
        assertNotNull("mCurrMacLabel is null", mCurrMacLabel);
        assertNotNull("mCurrMacVal is null", mCurrMacVal);
        assertNotNull("mApplyButton is null", mApplyButton);
        assertNotNull("mCancelButton is null", mCancelButton);
    }

    public void testStaticStrings() {
        String expected =
              mMM.getString(R.string.manualmac_header);
        String actual = mHeader.getText().toString();
        assertEquals(expected, actual);

        expected =
              mMM.getString(R.string.current_mac);
        actual = mCurrMacLabel.getText().toString();
        assertEquals(expected, actual);

        expected =
              mMM.getString(R.string.button_apply);
        actual = mApplyButton.getText().toString();
        assertEquals(expected, actual);

        expected =
              mMM.getString(R.string.button_cancel);
        actual = mCancelButton.getText().toString();
        assertEquals(expected, actual);
    }

    public void testOnlyHexAccepted() throws InterruptedException {
        String hex = "ab";
        String notHex = "it";
        String emptyString = "";
        Handler handler = new Handler(Looper.getMainLooper());
        ManualMacRunnable mMR = new ManualMacRunnable(mNextMacVal1);
        int sleepTime = 250;

        mMR.setText(notHex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());

        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(hex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal2);
        mMR.setText(notHex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(hex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal3);
        mMR.setText(notHex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(hex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal4);
        mMR.setText(notHex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(hex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal5);
        mMR.setText(notHex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(hex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal6);
        mMR.setText(notHex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(hex);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        assertEquals("ab:ab:ab:ab:ab:ab", mMM.getManualMac());
    }

    public void testManualFieldsTwoChars() throws InterruptedException {
        Handler handler = new Handler(Looper.getMainLooper());
        ManualMacRunnable mMR = new ManualMacRunnable(mNextMacVal1);
        String newVal = "abc", validVal = "12";
        String emptyString = "";
        int sleepTime = 250;

        mMR.setText(newVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(validVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal2);
        mMR.setText(newVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(validVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal3);
        mMR.setText(newVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(validVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal4);
        mMR.setText(newVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(validVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal5);
        mMR.setText(newVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(validVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        mMR.setTextView(mNextMacVal6);
        mMR.setText(newVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        assertEquals("12:12:12:12:12:ab", mMM.getManualMac());
        mMR.setText(emptyString);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);
        mMR.setText(validVal);
        handler.postAtFrontOfQueue(mMR);
        Thread.sleep(sleepTime);

        assertEquals("12:12:12:12:12:12", mMM.getManualMac());
    }

    private class ManualMacRunnable implements Runnable {
        TextView tv;
        String newText;

        public ManualMacRunnable(TextView _tv) {
            tv = _tv;
        }

        public void setTextView(TextView _tv) {
            tv = _tv;
        }

        public void setText(String t) {
            newText = t;
        }

        public void run() {
            tv.setText(newText);
        }
    }
}

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
import android.widget.TextView;
import un.ique.chmacaddroid.Main;
import un.ique.chmacaddroid.Layer2Address;
import un.ique.chmacaddroid.NativeIOCtller;
import un.ique.chmacaddroid.R;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class un.ique.chmacaddroid.MainTest \
 * un.ique.chmacaddroid.tests/android.test.InstrumentationTestRunner
 */
public class MainTest extends ActivityInstrumentationTestCase2<Main> {
    private Main mMain;
    private TextView mAppName;
    private TextView mIntro;
    private TextView mButton1, mButton2;
    private TextView mCurrMacLabel, mCurrMacVal;
    private static final String TAG = "ChMacAddroidTest";

    public MainTest() {
        super("un.ique.chmacaddroid", Main.class);
        //super(Main.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mMain = getActivity();
        mAppName = (TextView) mMain.findViewById(R.id.main_header);
        mIntro = (TextView) mMain.findViewById(R.id.main_what_i_can_do);
        mButton1 = (TextView) mMain.findViewById(R.id.main_button_action_1);
        mButton2 = (TextView) mMain.findViewById(R.id.main_button_action_2);
        mCurrMacLabel =
                (TextView) mMain.findViewById(R.id.main_current_mac);
        mCurrMacVal =
                (TextView) mMain.findViewById(R.id.main_macaddress);
    }

    public void testPreconditions() {
        assertNotNull("mMain is null", mMain);
        assertNotNull("mAppName is null", mAppName);
        assertNotNull("mIntro is null", mIntro);
        assertNotNull("mButton1 is null", mButton1);
        assertNotNull("mButton2 is null", mButton2);
        assertNotNull("mCurrMacLabel is null", mCurrMacLabel);
        assertNotNull("mCurrMacVal is null", mCurrMacVal);
    }

    public void testStaticStringLabels() {
        String expectedHeader =
                mMain.getString(R.string.main_header);
        String actualHeader = mAppName.getText().toString();
        assertEquals(expectedHeader, actualHeader);

        String expectedIntro =
                mMain.getString(R.string.main_what_i_can_do);
        String actualIntro = mIntro.getText().toString();
        assertEquals(expectedIntro, actualIntro);

        String expectedButton1 =
                mMain.getString(R.string.main_action_1);
        String actualButton1 = mButton1.getText().toString();
        assertEquals(expectedButton1, actualButton1);

        String expectedButton2 =
                mMain.getString(R.string.main_action_2);
        String actualButton2 = mButton2.getText().toString();
        assertEquals(expectedButton2, actualButton2);

        String expectedMacLabel =
                mMain.getString(R.string.current_mac);
        String actualMacLabel = mCurrMacLabel.getText().toString();
        assertEquals(expectedMacLabel, actualMacLabel);
    }

    public void testMutableStringValues() {
        Layer2Address newNet = new Layer2Address();
        newNet.setInterfaceName("wlan0");
        NativeIOCtller ctller = new NativeIOCtller(newNet);
        newNet.setAddress(ctller.getCurrentMacAddr());
        String expectedMacAddr = newNet.formatAddress();
        String actualMacAddr = mCurrMacVal.getText().toString();
        assertEquals(expectedMacAddr, actualMacAddr);
    }
}

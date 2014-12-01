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
import android.widget.Button;
import un.ique.chmacaddroid.RandomMac;
import un.ique.chmacaddroid.Layer2Address;
import un.ique.chmacaddroid.NativeIOCtller;
import un.ique.chmacaddroid.R;

import android.test.ActivityInstrumentationTestCase2;

public class RandomMacTest extends ActivityInstrumentationTestCase2<RandomMac> {
    private RandomMac mRM;
    private TextView mHeader;
    private TextView mCurrMacLabel, mCurrMacVal;
    private TextView mNextMacLabel, mNextMacVal;
    private Button mRandomButton;
    private Button mApplyButton;
    private Button mCancelButton;

    public RandomMacTest() {
        super("un.ique.chmacaddroid", RandomMac.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mRM = getActivity();
        mHeader = (TextView) mRM.findViewById(R.id.randommac_header);
        mCurrMacLabel =
            (TextView) mRM.findViewById(R.id.randommac_current_mac);
        mCurrMacVal =
            (TextView) mRM.findViewById(R.id.randommac_macaddress);
        mNextMacLabel =
            (TextView) mRM.findViewById(R.id.randommac_next_mac);
        mNextMacVal =
            (TextView) mRM.findViewById(R.id.randommac_nextmacaddress);
        mRandomButton =
            (Button) mRM.findViewById(R.id.randommac_randomize_button);
        mApplyButton =
            (Button) mRM.findViewById(R.id.randommac_apply_button);
        mCancelButton =
            (Button) mRM.findViewById(R.id.randommac_cancel_button);
    }

    public void testPreconditions() {
        assertNotNull("mRM is null", mRM);
        assertNotNull("mHeader is null", mHeader);
        assertNotNull("mCurrMacLabel is null", mCurrMacLabel);
        assertNotNull("mCurrMacVal is null", mCurrMacVal);
        assertNotNull("mNextMacLabel is null", mNextMacLabel);
        assertNotNull("mNextMacVal is null", mNextMacVal);
        assertNotNull("mRandomButton is null", mRandomButton);
        assertNotNull("mApplyButton is null", mApplyButton);
        assertNotNull("mCancelButton is null", mCancelButton);
    }

    public void testStaticStrings() {
        String expected =
              mRM.getString(R.string.randommac_header);
        String actual = mHeader.getText().toString();
        assertEquals(expected, actual);

        expected =
              mRM.getString(R.string.current_mac);
        actual = mCurrMacLabel.getText().toString();
        assertEquals(expected, actual);

        expected =
              mRM.getString(R.string.next_mac);
        actual = mNextMacLabel.getText().toString();
        assertEquals(expected, actual);

        expected =
              mRM.getString(R.string.randommac_randomize);
        actual = mRandomButton.getText().toString();
        assertEquals(expected, actual);

        expected =
              mRM.getString(R.string.button_apply);
        actual = mApplyButton.getText().toString();
        assertEquals(expected, actual);

        expected =
              mRM.getString(R.string.button_cancel);
        actual = mCancelButton.getText().toString();
        assertEquals(expected, actual);
    }

    public void testMutableStringValues() {
        String dev = "wlan0";
        Layer2Address newNet = new Layer2Address();
        newNet.setInterfaceName(dev);
        NativeIOCtller ctller = new NativeIOCtller(newNet);

        String currMacAddr = mCurrMacVal.getText().toString();
        String nextMacAddr = mNextMacVal.getText().toString();
        assertTrue(currMacAddr.compareTo(nextMacAddr) != 0);
        assertTrue(nextMacAddr.compareTo("") != 0);

        newNet.setAddress(ctller.getCurrentMacAddr());
        String addr = newNet.formatAddress();
        assertTrue(currMacAddr.compareTo(addr) == 0);
        assertTrue(nextMacAddr.compareTo(addr) != 0);
    }
}

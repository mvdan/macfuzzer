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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class Main extends Activity
{
    private static final int RANDOMMAC_RESULT = 0x1d01;
    private static final int MANUALMAC_RESULT = 0x1d02;
    public static final int RESULT_EXIT = RESULT_FIRST_USER  + 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Layer2Address newNet = new Layer2Address();
        // Let's hardcode wlan0, for now
        newNet.setInterfaceName("wlan0");
        NativeIOCtller ctller = new NativeIOCtller(newNet);
        newNet.setAddress(ctller.getCurrentMacAddr());
        String addr = newNet.formatAddress();
        TextView macField = (TextView) findViewById(R.id.main_macaddress);
        if (macField != null) {
            macField.setText(addr);
        }
    }

    public void callManualMac(View view)
    {
        Intent intent = new Intent(this, ManualMac.class);
        startActivityForResult(intent, RANDOMMAC_RESULT);
    }

    public void callRandomMac(View view)
    {
        Intent intent = new Intent(this, RandomMac.class);
        startActivityForResult(intent, MANUALMAC_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == RESULT_EXIT) {
            finish();
        }
    }
}

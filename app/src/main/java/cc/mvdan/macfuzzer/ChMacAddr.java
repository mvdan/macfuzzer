/*
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

import android.content.Context;
import android.util.Log;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleExecutableCommand;

public class ChMacAddr {

    private static final String TAG = "ChMacAddr";
    private static final String EXECUTABLE = "chmacaddr";

    public static boolean run(Context context, String params) {
        Log.d(TAG, "Running chmacaddr...");

        Shell rootShell;
        try {
            rootShell = Shell.startRootShell();
        } catch (Exception e) {
            Log.e(TAG, "Problem while starting shell!", e);
            return false;
        }

        SimpleExecutableCommand cmd = new SimpleExecutableCommand(context,
                EXECUTABLE, params);

        try {
            rootShell.add(cmd).waitForFinish();
        } catch (Exception e) {
            Log.e(TAG, "Exception while running chmacaddr", e);
            return false;
        }

        return true;
    }
}

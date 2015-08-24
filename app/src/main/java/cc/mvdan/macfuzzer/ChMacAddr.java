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
import android.widget.Toast;

import java.io.IOException;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleExecutableCommand;

public class ChMacAddr {

    private static final String TAG = "ChMacAddr";
    private static final String EXECUTABLE = "chmacaddr";

    private static void warn(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        Log.e(TAG, text);
    }

    private static void warn(Context context, String text, Throwable e) {
        Toast.makeText(context, text + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, text, e);
    }

    public static void run(Context context, String params) {
        Log.i(TAG, "Running chmacaddr...");

        Shell rootShell;
        try {
            rootShell = Shell.startRootShell();
        } catch (Exception e) {
            Log.e(TAG, "Problem while starting shell!", e);
            return;
        }

        Log.i(TAG, "Command: " + EXECUTABLE + " " + params);
        SimpleExecutableCommand cmd = new SimpleExecutableCommand(context,
                EXECUTABLE, params);

        try {
            rootShell.add(cmd).waitForFinish();
        } catch (Exception e) {
            warn(context, "Exception while running chmacaddr", e);
        }
        int exitCode = cmd.getExitCode();
        if (exitCode != 0) {
            warn(context, "chmacaddr errored:\n" + cmd.getOutput().trim());
        }
        try {
            rootShell.close();
        } catch (IOException e) {
            warn(context, "Problem while closing shell", e);
        }
        Log.i(TAG, "Command exited.");
    }
}

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

import android.app.Activity;
import java.io.File;
import android.content.res.Resources;
import java.io.InputStream;
import java.io.FileOutputStream;
import android.content.Context;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.Process;
import java.lang.ProcessBuilder;
import junit.framework.TestCase;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import un.ique.chmacaddroid.FileStuff;
import un.ique.chmacaddroid.RandomMac;
import un.ique.chmacaddroid.ProcessResult;
import un.ique.chmacaddroid.R;


public class NativeTests extends ActivityInstrumentationTestCase2<RandomMac> {
    private String nativeTests = "nativetests";
    private FileStuff mFS;
    private File mBinaryFilePath;
    private static String TAG = "NativeTests";

    public NativeTests() {
        /* We need a valid Activity if we want to test this
           class. We'll abuse the RandomMac activity to satisfy
           this requirement. */
        super("un.ique.chmacaddroid", RandomMac.class);
    }

    @Override
    public void setUp() throws Exception {
        RandomMac rs = getActivity();
        mFS = new FileStuff(rs);
        mFS.copyBinaryFile(nativeTests, R.raw.nativetests);
        mBinaryFilePath = mFS.getPathToFile(nativeTests);
        mFS.copyBinaryFile(nativeTests, R.raw.nativetests);
    }

    public void testPreconditions() {
        assertNotNull("mFS is null", mFS);
        assertNotNull("mBinaryFilePath is null", mBinaryFilePath);
        assertNotNull("mBinaryFilePath is not a file",
                      mBinaryFilePath.isFile());
    }

    public void testCases() {
        String args[] = {"su", "0", mBinaryFilePath.getAbsolutePath()};
        ProcessResult res = mFS.runBlob(args, false);
        Log.i(TAG, "STDOUT:");
        Log.i(TAG, res.getStdOut());
        Log.i(TAG, "STDERR:");
        Log.i(TAG, res.getStdErr());
    }
}

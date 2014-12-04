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
import java.security.SecureRandom;
import java.io.File;
import un.ique.chmacaddroid.FileStuff;
import un.ique.chmacaddroid.RandomMac;
import un.ique.chmacaddroid.Layer2Address;
import un.ique.chmacaddroid.NativeIOCtller;

import junit.framework.TestCase;
import android.test.ActivityInstrumentationTestCase2;

public class FileStuffTest extends ActivityInstrumentationTestCase2<RandomMac> {
    private FileStuff mFS;
    private File mBinaryFilePath;

    public FileStuffTest() {
        /* We need a valid Activity if we want to test this
           class. We'll abuse the RandomMac activity to satisfy
           this requirement. */
        super("un.ique.chmacaddroid", RandomMac.class);
    }

    @Override
    public void setUp() throws Exception {
        RandomMac rs = getActivity();
        mFS = new FileStuff(rs);
        mBinaryFilePath = mFS.getPathToFile(null);
    }

    public void testPreconditions() {
        assertNotNull("mFS is null", mFS);
        assertNotNull("mBinaryFilePath is null", mBinaryFilePath);
    }

    public void testFileCopy() {
        File copiedFile = mFS.copyBinaryFile();
        assertNotNull("Copied file is null", copiedFile);
        assertEquals(copiedFile.getAbsolutePath(),
                     mBinaryFilePath.getAbsolutePath());
        assertTrue(copiedFile.canExecute());
    }

    public void testRunBlob() {
        String dev = "nondev";
        String addr = "aa:aa:aa:aa:aa:aa";
        String uid = "10101";
        Layer2Address newNet = new Layer2Address();

        assertTrue("Blob returned zero result",
                   mFS.runBlob(dev, addr, uid) != 0);

        dev = "wlan0";
        newNet.setInterfaceName(dev);
        NativeIOCtller ctller = new NativeIOCtller(newNet);
        uid = Integer.toString(ctller.getCurrentUID());
        assertTrue("Blob returned non-zero result",
                   mFS.runBlob(dev, addr, uid) == 0);
    }
}

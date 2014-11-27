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
import java.io.File;
import android.content.res.Resources;
import java.io.InputStream;
import java.io.FileOutputStream;
import android.content.Context;
import java.io.IOException;
import java.io.FileNotFoundException;

public class FileStuff
{
    private final String binaryName = "change_mac";
    private Activity mCurrAct = null;

    public FileStuff(Activity act) {
        mCurrAct = act;
    }

    public File copyBinaryFile() {
        try {
            InputStream is =
                    mCurrAct.getResources().openRawResource(R.raw.change_mac);
            FileOutputStream fos = mCurrAct.openFileOutput(binaryName,
                                                  Context.MODE_PRIVATE);
            int bytesRead = -1, round = 0;
            byte[] byteBuffer = new byte[100];
            while (bytesRead != 0) {
                bytesRead = is.read(byteBuffer, round,
                                    round + byteBuffer.length);
                if (bytesRead != byteBuffer.length) {
                    fos.write(byteBuffer, round, round + bytesRead);
                    break;
                }
                fos.write(byteBuffer, byteBuffer.length*round,
                          round + byteBuffer.length);
            }
            fos.close();
            is.close();
        } catch ( FileNotFoundException e) {
        } catch ( IOException e) {
        }
        File cm = getPathToFile();
        if (cm != null && cm.exists() && cm.isFile()) {
            cm.setExecutable(true);
        } else {
            return null;
        }
        return cm;
    }

    public File getPathToFile() {
        File dir = mCurrAct.getFilesDir();
        if (!dir.isDirectory())
            return null;
        return new File(dir, binaryName);
    }
}

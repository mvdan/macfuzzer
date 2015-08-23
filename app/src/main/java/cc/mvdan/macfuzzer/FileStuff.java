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
import java.io.File;
import android.content.res.Resources;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import android.content.Context;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.Process;
import java.lang.ProcessBuilder;
import cc.mvdan.macfuzzer.ProcessResult;

public class FileStuff
{
    private final String binaryName = "chmacaddr";
    private Activity mCurrAct = null;
    private String lastOut, lastErr;
    int lastCode;

    public FileStuff(Activity act) {
        mCurrAct = act;
        lastOut = "";
        lastErr = "";
    }

    public ProcessResult getLastProcessResult() {
        return new ProcessResult(lastOut, lastErr, lastCode);
    }

    public File copyBinaryFile() {
        return copyBinaryFile(binaryName, R.raw.chmacaddr);
    }

    public File copyBinaryFile(String filename, int resource) {
        if (filename == null) {
            filename = binaryName;
        }

        try {
            InputStream is =
                    mCurrAct.getResources().openRawResource(resource);
            FileOutputStream fos = mCurrAct.openFileOutput(filename,
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
        File cm = getPathToFile(filename);
        if (cm != null && cm.exists() && cm.isFile()) {
            cm.setExecutable(true);
        } else {
            return null;
        }
        return cm;
    }

    public File getPathToFile(String filename) {
        if (filename == null) {
            filename = binaryName;
        }

        File dir = mCurrAct.getFilesDir();
        if (!dir.isDirectory())
            return null;
        return new File(dir, filename);
    }

    private ProcessResult getPR(String out, String err, int code) {
        lastOut = out;
        lastErr = err;
        lastCode = code;
        return new ProcessResult(out, err, code);
    }

    public ProcessResult runBlob(String dev, String addr, String uid) {
        String[] args = {"su", "0",
                         getPathToFile(null).getAbsolutePath(),
                         dev, addr, uid};
        return runBlob(args, false);
    }

    public ProcessResult runBlob(String[] args, boolean printOut) {
        String out = "", err = "";
        int code = -1, bufsize = 8192;
        byte buf[] = new byte[bufsize];
        try {
            Process proc = new ProcessBuilder().
                               command(args).
                               redirectErrorStream(true).
                               start();
            BufferedInputStream outStream =
                    new BufferedInputStream(proc.getInputStream(),
                                            bufsize);
            BufferedInputStream errStream =
                    new BufferedInputStream(proc.getErrorStream(),
                                            bufsize);
            try {
                int bytesRead = 0;
                while (true) {
                    bytesRead = outStream.read(buf, 0, bufsize);
                    if (bytesRead != -1) {
                        out += new String(buf, 0, bytesRead);
                    }
                    bytesRead = errStream.read(buf, 0, bufsize);
                    if (bytesRead != -1) {
                        err += new String(buf, 0, bytesRead);
                    }
                    if (bytesRead == -1) {
                        try {
                            proc.exitValue();
                            break;
                        } catch (IllegalThreadStateException e) {
                            /* Process is still executing */
                        }
                    }
                }
                proc.waitFor();
                code = proc.exitValue();
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
            // TODO Show a useful notification in this case, too
            return getPR(out, err, code);
        }
        return getPR(out, err, code);
    }
}

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

public class ProcessResult
{
    String out, err;
    int exitcode;

    public ProcessResult(String _out, String _err, int _code) {
        out = _out;
        err = _err;
        exitcode = _code;
    }

    public String getStdOut() {
        return out;
    }

    public String getStdErr() {
        return err;
    }

    public int getExitCode() {
        return exitcode;
    }
}

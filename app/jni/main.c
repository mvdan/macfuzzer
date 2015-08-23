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

#include <stdio.h>
#include <errno.h>
#include <android/log.h>
#include "chmacaddr.h"

const static char *TAG = "chmacaddr_main";

/** Kick it off
 * Check if we can drop capabilities, then prevent regaining
 * any capabilities we drop in the future. Last, we drop all
 * capabilities we don't need.
 * Return -1 on failure.
 * Return 0 on success.
 */
int
main(int argc, char * argv[])
{
    int drop_caps = chmaddr_can_drop_caps();
    if (drop_caps != 1) {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "Neither SYS_ADMIN "
                            "nor SETPCAP are not available. grrrr! "
                            "%s\n", strerror(errno));
        goto bad_state;
    }

    if (chmaddr_lock_it_down()) {
        goto bad_state;
    }

    if (chmaddr_drop_unneeded_caps()) {
        goto bad_state;
    }

    return chmaddr_make_it_so(argc, (const char **)argv);

bad_state:
    fprintf(stderr, "We failed while entering a safe state. Check logs "
                    "for details. %s", errno ? strerror(errno) : "");
    return -1;
}


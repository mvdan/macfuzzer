/*
 * MacAddroid - Android app that changes a network devices MAC address
 * Copyright (C) 2014 Matthew Finkel <Matthew.Finkel@gmail.com>
 *
 * This file is part of MacAddroid
 *
 * MacAddroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MacAddroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MacAddroid, in the COPYING file.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <pwd.h>
#include <grp.h>
/* Copied from bionic source repo because ndk is lacking
 * some important constants. */
/* commit 1f5706c7eb8aacb76fdfa3ef03944be229510b66 */
#include "capability.h"
#include "securebits.h"
#include "prctl.h"

/* The android-20 ndk doesn't support these by default */
#ifndef PR_CAPBSET_READ
#define PR_CAPBSET_READ 23
#endif
#ifndef PR_CAPBSET_DROP
#define PR_CAPBSET_DROP 24
#endif
#ifndef PR_SET_NO_NEW_PRIVS
#define PR_SET_NO_NEW_PRIVS 38
#endif

int nativeioc_set_mac_addr(const char * iface, const char * mac);
static int confirm_caps_dropped(void);
static int can_drop_caps(void);
static int finish_what_we_started(int argc, const char * argv[]);

const uint8_t mac_hex_length = 17;
const uint8_t mac_byte_length = 6;

static int
verify_string_format(const char * str_mac)
{
    int i = 0;
    if (strlen(str_mac) != mac_hex_length) {
        return -1;
    }

    for (i = 2; i < mac_hex_length; i += 3) {
        if (str_mac[i] != ':') {
            return -2;
        }
    }

    return 0;
}

static int
convert_hex_to_byte(const char * strmac, uint8_t * mac)
{
    int i = 0, octet = 0;
    char hex[3];
    hex[2] = '\0';
    for (i = 0; i < mac_hex_length; i++) {
        hex[0] = strmac[i++];
        hex[1] = strmac[i++];
        long topfour = strtoul(hex, NULL, 16);
        //long bottomfour = strtoul(hex, NULL, 16);
        if (strmac[i] != ':' && i < mac_hex_length) {
            fprintf(stderr, "Found '%c' at %d when we expected a "
                            "colon.\n", strmac[i], i);
            return -1;
        }
        fprintf(stderr, "octet reached %d, i = %d. %ld\n", octet, i,
                        topfour);
        mac[octet++] = topfour & 0xFF;
        if (octet > 5) {
            fprintf(stderr, "octet reached 5, i = %d.\n", i);
            break;
        }

    }

    return 0;
}

static uid_t
get_uid(const char * id)
{
    return (uid_t) strtoul(id, NULL, 10);
}

static int
accept_from_stdin(const char * iface)
{
    return 0;
}

static int
can_drop_caps(void)
{
   return !prctl(PR_CAPBSET_READ, CAP_SYS_ADMIN, 0, 0, 0);
}

static int
drop_unneeded_caps(void)
{
    int i;
    struct __user_cap_header_struct hdr;
    struct __user_cap_data_struct data, have_data;

    for (i = 0; prctl(PR_CAPBSET_READ, i, 0, 0, 0) == 1; i++) {
        if (prctl(PR_CAPBSET_DROP, i, 0, 0, 0)) {
            fprintf(stderr, "Failed to drop cap: %d, %s\n", i,
                            strerror(errno));
            return -1;
        }
    }

    hdr.version = _LINUX_CAPABILITY_VERSION;
    hdr.pid = 0;

    data.effective = 0;
    data.effective = 1 << CAP_NET_ADMIN;
    data.effective += 1 << CAP_SETGID;
    data.effective += 1 << CAP_SETUID;
    data.permitted = data.effective;
    data.inheritable = 0;
    if (capset(&hdr, (const cap_user_data_t)&data)) {
        fprintf(stderr, "Failed to setcap. %s\n", strerror(errno));
        return -1;
    }

    if (capget(&hdr, &have_data)) {
        fprintf(stderr, "Sad. getcap failed. %s\n", strerror(errno));
        return -1;
    }

    if (data.effective != have_data.effective) {
        fprintf(stderr, "Our effective caps are not what we set! "
                        "Gotta go!\n");
        return -1;
    }

    return 0;
}

static int
lock_it_down(void)
{
    if (prctl(PR_SET_KEEPCAPS, 1L, 0, 0, 0)) {
        fprintf(stderr, "Failed to set KEEPCAPS. We don't keep "
                        "NET_ADMIN when we switch users. %s\n",
                        strerror(errno));
        return -1;
    }

    /* Introduced in Linux 3.5, wait for next update */
    #if 0
    if (prctl(PR_SET_NO_NEW_PRIVS, 1, 0, 0, 0)) {
        fprintf(stderr, "Failed to set PR_SET_NO_NEW_PRIVS cap: "
                        "%s\n", strerror(errno));
        return -1;
    }
    #endif

    if (prctl(PR_SET_SECUREBITS, SECBIT_KEEP_CAPS_LOCKED |
                                 SECBIT_NO_SETUID_FIXUP |
                                 SECBIT_NO_SETUID_FIXUP_LOCKED |
                                 SECBIT_NOROOT |
                                 SECBIT_NOROOT_LOCKED)) {
        fprintf(stderr, "Failed to set PR_SET_SECUREBITS cap: %s\n",
                        strerror(errno));
        return -1;
    }

    return 0;
}

static int
make_it_so(int argc, const char * argv[])
{
    int pid = fork();
    if (pid > 0) {
        int status;
        int ret = wait(&status);
        return ret || status;
    } else if (pid == 0) {
        return finish_what_we_started(argc, argv);
    } else {
        fprintf(stderr, "Failed to fork after dropping caps: %s\n",
                        strerror(errno));
        return pid;
    }
}

#if 0 /* Bionic doesn't implement getgrnam_r() nor getpwuid_r()! */
static int
get_group_id(const char *grpname, gid_t *gid)
{
    struct group grp;
    struct group *result;
    char *buf;
    size_t bufsize;
    int s;

    bufsize = sysconf(_SC_GETGR_R_SIZE_MAX);
    if (bufsize == -1)
        bufsize = 16384;

    buf = malloc(bufsize);
    if (buf == NULL) {
        fprintf(stderr, "Unable to allocate enough memory for  "
                        "getgrnam_r()\n");
        return -1;
    }

    s = getgrnam_r(grpname, &grp, buf, bufsize, &result);
    if (result == NULL) {
        if (s == 0) {
            fprintf(stderr, "No group found with group %s\n", grpname);
        } else {
            fprintf(stderr, "getgrnam_r lookup failed for group %s: "
                            "%s\n", groupname, strerror(s));
        }
        return -1;
    }
    *gid = grp.gr_gid;

    free(buf);
    free(result);
    return 0;
}

static const gid_t*
get_users_groups(uid_t uid)
{
    const char *inet_group = "inet";
    gid_t gid, *gids;
    struct passwd pwd;
    struct passwd *result;
    char *buf;
    size_t bufsize;
    int s;

    if (get_group_id(inet_group, &gid)) {
        fprintf(stderr, "Failed to retrieve username lookup.\n");
        return NULL;
    }

    bufsize = sysconf(_SC_GETPW_R_SIZE_MAX);
    if (bufsize == -1)
        bufsize = 512;

    buf = malloc(bufsize);
    if (buf == NULL) {
        fprintf(stderr, "Unable to allocate enough memory for  "
                        "getpwnam_r()\n");
        return NULL;
    }

    s = getpwuid_r(uid, &pwd, buf, bufsize, &result);
    if (result == NULL) {
        if (s == 0) {
            fprintf(stderr, "No user found with uid %u\n", uid);
        } else {
            fprintf(stderr, "getpwuid_r lookup failed for uid %u: "
                            "%s\n", uid, strerror(s));
        }
        return NULL;
    }
    gids = malloc(sizeof(gid_t)*2);
    gids[0] = pwd.pw_gid;
    gids[1] = gid;

    free(result);
    free(buf);
    return gids;
}
#else
static int
get_group_id(const char *grpname, gid_t *gid)
{
    struct group *grp;

    errno = 0;
    grp = getgrnam(grpname);
    if (grp == NULL) {
        if (errno == 0) {
            fprintf(stderr, "No group found with groupname %s\n",
                    grpname);
        } else {
            fprintf(stderr, "getpwuid_r lookup failed for groupname "
                    " %s: %s\n", grpname, strerror(errno));
        }
        return -1;
    }
    *gid = grp->gr_gid;
    return 0;
}

static const gid_t*
get_users_groups(uid_t uid)
{
    const char *inet_group = "inet";
    gid_t gid, *gids;
    struct passwd *pwd;

    if (get_group_id(inet_group, &gid)) {
        fprintf(stderr, "Failed to retrieve username lookup.\n");
        return NULL;
    }

    errno = 0;
    pwd = getpwuid(uid);
    if (pwd == NULL) {
        if (errno == 0) {
            fprintf(stderr, "No user found with uid %u\n", uid);
        } else {
            fprintf(stderr, "getpwuid_r lookup failed for uid %u: "
                            "%s\n", uid, strerror(errno));
        }
        return NULL;
    }
    gids = malloc(sizeof(gid_t)*2);
    gids[0] = pwd->pw_gid;
    gids[1] = gid;

    return gids;
}
#endif

static int
switch_user(uid_t uid)
{
    const gid_t *groups;
    struct passwd *pwd;
    struct __user_cap_header_struct hdr;
    struct __user_cap_data_struct data, have_data;

    if (!uid) {
        fprintf(stderr, "Do not try switching to uid 0.\n");
        return -1;
    }
    if (setresuid(uid, uid, uid)) {
        fprintf(stderr, "setuid to %u failed: %s.\n", uid,
                        strerror(errno));
        return -1;
    }
    if (getuid() != uid) {
        fprintf(stderr, "setuid failed. uid: %u.\n", getuid());
        return -1;
    }

    if (setresgid(uid, uid, uid)) {
        fprintf(stderr, "setgid to %u failed: %s.\n", uid,
                        strerror(errno));
        return -1;
    }
    if (getgid() != uid) {
        fprintf(stderr, "setgid failed. gid: %u.\n", getgid());
        return -1;
    }

    groups = get_users_groups(uid);
    if (groups == NULL) {
        fprintf(stderr, "Failed to retrieve user's groups.\n");
        return -1;
    }

    /* Most important, we need to re-add inet group */
    if (setgroups(2, groups)) {
        fprintf(stderr, "Failed to set supp groups groups. %s\n",
                        strerror(errno));
        return -1;
    }

    /* Drop SETUID/SETGID caps, we only need NET_ADMIN now */
    hdr.version = _LINUX_CAPABILITY_VERSION;
    hdr.pid = 0;

    data.effective = 0;
    data.effective = 1 << CAP_NET_ADMIN;
    data.permitted = data.effective;

    if (capset(&hdr, (const cap_user_data_t)&data)) {
        fprintf(stderr, "Failed to setcap - dropping SETGUID. %s\n",
                        strerror(errno));
        return -1;
    }

    if (capget(&hdr, &have_data)) {
        fprintf(stderr, "Sad. getcap failed after dropping SETGUID. "
                        "%s\n", strerror(errno));
        return -1;
    }

    if (data.effective != have_data.effective) {
        fprintf(stderr, "Our effective caps are not what we set after "
                        "dropping SETGUID! Gotta go!\n");
        return -1;
    }

    return 0;
}

static int
finish_what_we_started(int argc, const char * argv[])
{
    const char * iface;
    uint8_t mac[mac_byte_length];
    int i;

    if (argc > 4) {
        return -1;
    }
    if (argc == 2) {
        return accept_from_stdin(argv[1]);
    }
    iface = argv[1];

    fprintf(stderr, "Dev: %s. Beginning address format "
                    "verification.\n", iface);
    if (verify_string_format(argv[2])) {
        fprintf(stderr, "Address format failed: %s, %zd.\n", argv[2],
                        strlen(argv[2]));
        return -1;
    }
    fprintf(stderr, "Address format passed.\n");

    if (convert_hex_to_byte(argv[2], mac)) {
        fprintf(stderr, "Conversion from hex to byte failed.\n");
        return -1;
    }
    fprintf(stderr, "Conversion from hex to byte passed.\n");
    uid_t uid = get_uid(argv[3]);
    if (uid < 1) {
        return -1;
    }
    if (switch_user(uid)) {
        return -1;
    }

    if (confirm_caps_dropped()) {
        return -1;
    }

    int retval = nativeioc_set_mac_addr(iface, mac);
    if (retval) {
        fprintf(stderr, "set_mac_addr() returned with %d, %s.\n",
                        retval, strerror(errno));
        fprintf(stderr, "6 MAC octets: ");
        int i;
        for (i = 0; i < mac_byte_length; i++)
            fprintf(stderr, "%d ", mac[i]);
    }
    fprintf(stderr, "set_mac_addr() successful.\n");
    return retval;
}

#define CHROOT_DIR "/mnt"
static int
confirm_caps_dropped(void)
{
    /*if (prctl(PR_CAPBSET_DROP, CAP_SYS_CHROOT, 0, 0, 0) == 0) {
        fprintf(stderr, "We successfully dropped cap CAP_SYS_CHROOT "
                        "despite dropping privs and switching users\n");
        return -1;
    }*/
    if (chroot(CHROOT_DIR) == -1) {
        if (errno == EPERM) {
            return 0;
        }
        if (errno == ENOENT) {
            fprintf(stderr, "%s isn't a dir. Please report this.\n",
                    CHROOT_DIR);
            /* It's likely safe to assume this would've
             * successfully failed due to nocap if CHROOT_DIR existed,
             * but if we don't deny this then we'll never know this
             * is a bad check for some devices. */
        }
    }
    fprintf(stderr, "We think we can still chroot, cap drop failed!\n");
    return -1;
}
#undef CHROOT_DIR

int
main(int argc, char * argv[])
{
    if (can_drop_caps()) {
        fprintf(stderr, "PR_CAPBSET_DROP not available. grrrr! %s\n",
                        strerror(errno));
        return -1;
    }

    if (lock_it_down()) {
        return -1;
    }

    if (drop_unneeded_caps()) {
        return -1;
    }

    return make_it_so(argc, (const char **)argv);
}


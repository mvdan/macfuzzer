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

#include "tinytest.h"
#include "tinytest_macros.h"
#include "chmacaddr.h"
#include "native_ioctller.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <linux/capability.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <grp.h>
#include <pwd.h>
#include "prctl.h"
#include "securebits.h"


void
test_string_format(void *data)
{
    char *addr;
    int expected, actual;

    (void)data;

    addr = strdup("thisisnot valid");
    tt_int_op(chmaddr_verify_string_format(addr), ==, -1);
    free(addr);
    addr = strdup("thisis17character");
    tt_int_op(chmaddr_verify_string_format(addr), ==, -2);
    free(addr);
    addr = strdup("this:is:also:17ch");
    tt_int_op(chmaddr_verify_string_format(addr), ==, -2);
    free(addr);
    addr = strdup("th:is:is:17:ch:ar");
    tt_int_op(chmaddr_verify_string_format(addr), ==, -0);
    free(addr);
    addr = NULL;

end:
    if (addr)
        free(addr);
}

void
test_convert_hex_to_byte(void *data)
{
    char *hexmac;
    uint8_t bytemac[mac_byte_length];
    uint8_t expected1[] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    uint8_t expected2[] = {0x00, 0x11, 0x22, 0x33, 0x44, 0x55};
    memset(&bytemac, 0, mac_byte_length);

    (void)data;

    hexmac = strdup("invalidmacaddress");
    tt_int_op(chmaddr_convert_hex_decode(hexmac, bytemac), ==, -1);
    tt_mem_op(&bytemac, ==, &expected1, sizeof(bytemac));
    free(hexmac);
    hexmac = strdup("in:va:lid:macaddr");
    tt_int_op(chmaddr_convert_hex_decode(hexmac, bytemac), ==, -2);
    tt_mem_op(&bytemac, ==, &expected1, sizeof(bytemac));
    free(hexmac);
    hexmac = strdup("001:1:22:33:44:55:66");
    tt_int_op(chmaddr_convert_hex_decode(hexmac, bytemac), ==, -1);
    tt_mem_op(&bytemac, ==, &expected1, sizeof(bytemac));
    free(hexmac);
    hexmac = strdup("00:11:22:33:44:55:66");
    tt_int_op(chmaddr_convert_hex_decode(hexmac, bytemac), ==, 0);
    tt_mem_op(&bytemac, ==, &expected2, sizeof(bytemac));
    free(hexmac);
    hexmac = strdup("00:11:22:33:44:55");
    tt_int_op(chmaddr_convert_hex_decode(hexmac, bytemac), ==, 0);
    tt_mem_op(&bytemac, ==, &expected2, sizeof(bytemac));
    free(hexmac);
    hexmac = NULL;

end:
    if (hexmac)
        free(hexmac);
}    

void
test_get_uid(void *data)
{
    char *id;
    int err;
    (void)data;

    id = strdup("i'm not an user id");
    tt_int_op(chmaddr_get_uid(id, &err), ==, 0);
    tt_int_op(err, ==, 1);
    free(id);
    err = 0;
    id = strdup("1234xxx");
    tt_int_op(chmaddr_get_uid(id, &err), ==, 1234);
    tt_int_op(err, ==, 1);
    free(id);
    err = 0;
    id = strdup("-23456789012345678901");
    tt_int_op(chmaddr_get_uid(id, &err), ==, ULONG_MAX);
    tt_int_op(err, ==, 1);
    free(id);
    err = 0;
    id = strdup("1234");
    tt_int_op(chmaddr_get_uid(id, &err), ==, 1234);
    tt_int_op(err, ==, 0);
    free(id);
    id = NULL;

end:
    if (id)
        free(id);
}

int
fork_and_set_cap_with_res(uint32_t eff)
{
    int status;
    struct __user_cap_header_struct hdr;
    struct __user_cap_data_struct data, have_data;
    
    hdr.version = _LINUX_CAPABILITY_VERSION;
    hdr.pid = 0;

    pid_t pid = fork();
    if (!pid) {
        data.effective = 0;
        data.effective += eff;
        data.permitted = data.effective;
        data.inheritable = 0;

        if (capset(&hdr, (const cap_user_data_t)&data)) {
            fprintf(stderr, "Failed to setcap after fork(). %s\n", strerror(errno));
            exit(-2);
        }

        if (capget(&hdr, &have_data)) {
            fprintf(stderr, "Sad. getcap failed after fork(). %s\n", strerror(errno));
            exit(-2);
        }
        exit(chmaddr_drop_unneeded_caps());
    }

    pid_t rpid = waitpid(pid, &status, 0);
    if (rpid == pid) {
        if (WIFEXITED(status))
            return WEXITSTATUS(status);
    }
    return -3;
}

void
test_drop_unneeded_caps(void *data)
{
    uint32_t cap;
    (void)data;

    cap = 0;
    tt_int_op(fork_and_set_cap_with_res(cap), ==, 255);
    cap = 1<<CAP_NET_ADMIN|1<<CAP_SETGID|1<<CAP_SETUID|1<<CAP_SYS_ADMIN;
    tt_int_op(fork_and_set_cap_with_res(cap), ==, 255);
    cap = ~0;
    tt_int_op(fork_and_set_cap_with_res(cap), ==, 0);

end:
    return;
}

void
test_get_group_id(void *data)
{
    gid_t gid;

    tt_int_op(chmaddr_get_group_id("nosuchalias", &gid), ==, -1);
    tt_int_op(chmaddr_get_group_id("system", &gid), ==, 0);
    tt_int_op(gid, !=, 0);

end:
    return;
}
  

void
test_get_users_groups(void *data)
{
    const gid_t *gids = NULL;

    (void)data;

    gids = chmaddr_get_users_groups(1);
    tt_ptr_op(gids, ==, NULL);
    gids = chmaddr_get_users_groups(0);
    tt_assert(gids != NULL);
    tt_int_op(gids[0], ==, 0);
end:
    return;
}

int
fork_and_switch_user_with_res(uid_t uid, uid_t ruid, uid_t euid, uid_t suid, int sec)
{
    pid_t pid;

    pid = fork();
    if (!pid) {
        if (sec && (ruid || euid || suid)) {
            if (prctl(PR_SET_SECUREBITS, SECBIT_KEEP_CAPS|SECBIT_NO_SETUID_FIXUP)) {
                fprintf(stderr, "Failed to KEEP_CAPS after fork(). %s\n",
                        strerror(errno));
                exit(-2);
            }
        }
        if (setresuid(ruid, euid, suid)) {
            fprintf(stderr, "setuid to %u failed: %s.\n", uid,
                    strerror(errno));
            return -2;
        }
        exit(chmaddr_switch_user(uid));
    }
 
    int status;
    pid_t rpid = waitpid(pid, &status, 0);
    if (rpid == pid) {
        if (WIFEXITED(status))
            return WEXITSTATUS(status);
    }
    return -3;
}
 
void
test_switch_user(void *data)
{
    tt_int_op(chmaddr_switch_user(0), ==, -1);
    tt_int_op(fork_and_switch_user_with_res(0, 0, 0, 0, 0), ==, 255);
    tt_int_op(fork_and_switch_user_with_res(1, 313375, 0, 0, 0), ==, 255);
    tt_int_op(fork_and_switch_user_with_res(313376, 313375, 313375, 313375, 0), ==, 255);
    tt_int_op(fork_and_switch_user_with_res(313375, 313375, 0, 0, 0), ==, 255);
    tt_int_op(prctl(PR_SET_SECUREBITS, SECBIT_KEEP_CAPS|SECBIT_NO_SETUID_FIXUP), ==, 0);
    tt_int_op(fork_and_switch_user_with_res(313375, 0, 0, 0, 1), ==, 0);
    tt_int_op(fork_and_switch_user_with_res(313375, 313375, 0, 0, 1), ==, 0);
    tt_int_op(fork_and_switch_user_with_res(-1, 0, 0, 0, 1), ==, 255);
    
end:
    return;
}

int
for_and_finish_with_res(int argc, const char **argv)
{
    pid_t pid;
    int status;

    pid = fork();
    if (!pid) {
       exit(chmaddr_finish_what_we_started(argc, argv));
    }
    pid_t rpid = waitpid(pid, &status, 0);
    if (rpid == pid) {
        if (WIFEXITED(status))
            return WEXITSTATUS(status);
    }
    return -2;
}

void
test_finish_what_we_started(void *data)
{
    char uid[5];
    snprintf(uid, 5, "%u", getuid());
    const char *args[] = {"ignored", "wlan0", "aa:bb:cc:dd:ee:ff", "123456", NULL};

    tt_int_op(for_and_finish_with_res(3, args), ==, 255);
    tt_int_op(for_and_finish_with_res(4, args), ==, 0);

end:
    return;
}
    

struct testcase_t tests[] = {
        { "string_format", test_string_format, },
        { "string_hex_to_byte", test_convert_hex_to_byte, },
        { "get_uid", test_get_uid, },
        { "drop_unneeded_caps", test_drop_unneeded_caps, },
        { "get_group_id", test_get_group_id, },
        { "get_users_groups", test_get_users_groups, },
        { "switch_user", test_switch_user, },
        { "finish_what_we_started", test_finish_what_we_started, },
        END_OF_TESTCASES
};

struct testgroup_t groups[] = {
        { "chmacaddr/", tests, },
        END_OF_GROUPS
};

int
main(int argc, const char *argv[])
{
	return tinytest_main(argc, argv, groups);
}

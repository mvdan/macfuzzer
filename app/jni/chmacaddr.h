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

#ifndef _MACFUZZER_CHMACADDR_H
#define _MACFUZZER_CHMACADDR_H

#include <sys/types.h>
#include <unistd.h>

static const uint8_t mac_hex_length = 17;
static const uint8_t mac_byte_length = 6;

typedef struct clone_args clone_args;

int chmaddr_confirm_caps_dropped(void);
int chmaddr_can_drop_caps(void);
int chmaddr_verify_string_format(const char *str_mac);
int chmaddr_convert_hex_decode(const char *strmac, uint8_t *mac);
int chmaddr_finish_what_we_started(int argc, const char *argv[]);
uid_t chmaddr_get_uid(const char *id, int *err);
int chmaddr_drop_unneeded_caps(void);
int chmaddr_lock_it_down(void);
int chmaddr_clone_me(void *args);
int chmaddr_find_valid_clone(int (*fn)(void *), void *child_stack, int flags,
                     clone_args *arg);
int chmaddr_make_it_so(int argc, const char *argv[]);
int chmaddr_get_group_id(const char *grpname, gid_t *gid);
const gid_t* chmaddr_get_users_groups(uid_t uid);
int chmaddr_switch_user(uid_t uid);

#endif //_MACFUZZER_CHMACADDR_H

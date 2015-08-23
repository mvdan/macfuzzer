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

#include <errno.h>
#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <jni.h>
#include <sys/types.h>
#include <errno.h>

#include "native_ioctller.h"
#include "chmacaddr.h"

jbyteArray
Java_cc_mvdan_macfuzzer_NativeIOCtller_getCurrentMacAddr(JNIEnv* env,
                                                         jobject thiz)
{
    if (env == NULL || thiz == NULL) {
        return (*env)->NewByteArray(env, 6);
    }

    jclass nioc = (*env)->GetObjectClass(env, thiz);

    jfieldID interface_field = (*env)->GetFieldID(env, nioc,
            "mInterface", "Ljava/lang/String;");
    if (interface_field == NULL) {
        return (*env)->NewByteArray(env, 6);
    }

    jstring jdev = (jstring) (*env)->GetObjectField(env, thiz, interface_field);
    const char * iface = (*env)->GetStringUTFChars(env, jdev, JNI_FALSE);
    struct ifreq dev;
    strncpy(dev.ifr_name, iface, 6);
    (*env)->ReleaseStringUTFChars(env, jdev, iface);

    int sock = socket (AF_INET, SOCK_DGRAM, 0);
    if (sock < 0) {
        return (*env)->NewByteArray(env, 6);
    }
    if (ioctl(sock, SIOCGIFHWADDR, &dev) < 0) {
        close(sock);
        return (*env)->NewByteArray(env, 6);
    }

    jbyteArray currAddr = (*env)->NewByteArray(env, 6);
    char * macAddr = dev.ifr_hwaddr.sa_data;
    (*env)->SetByteArrayRegion(env, currAddr, 0, 6, macAddr);

    close(sock);
    return currAddr;
}

jstring
Java_cc_mvdan_macfuzzer_NativeIOCtller_getCurrentMacAddrError(JNIEnv* env,
                                                              jobject thiz)
{
    if (env == NULL || thiz == NULL) {
        return (*env)->NewByteArray(env, 6);
    }

    const char * iface = "wlan0";
    struct ifreq dev;
    strncpy(dev.ifr_name, iface, 6);
    int sock = socket (AF_INET, SOCK_DGRAM, 0);
    if (sock < 0) {
        char * badAddr = (char *) malloc(sizeof(badAddr) * 20);
        sprintf(badAddr, "socket(): %d: %s\n", errno, strerror(errno));
        /*jcharArray errMsg = (*env)->NewCharArray(env, 20);
        (*env)->SetCharArrayRegion(env, errMsg, 0, 19, (jchar *)badAddr);*/
        return (*env)->NewStringUTF(env, badAddr);
    }
    if (ioctl(sock, SIOCGIFHWADDR, &dev) < 0) {
        close(sock);
        char * badAddr = (char *) malloc(sizeof(badAddr) * 20);
        sprintf(badAddr, "ioctl(): %d: %s\n", errno, strerror(errno));
        return (*env)->NewStringUTF(env, badAddr);
    }

    return (*env)->NewStringUTF(env, "All good!");
}

/** Set the MAC address *mac* of network interface *iface*.
  * Ensure *mac* is the correct length. We will segfault if it's not.
  * Return -1 if we fail while opening an inet datagram socket.
  * Return -2 if we fail while getting or setting the interface's MAC
  *    address.
  * Return 0 on success.
  *
  * Note, errno is reset to 0 at the beginning of this function. You
  * may use it on failure.
  */
int nativeioc_set_mac_addr(const char *iface, const uint8_t *mac) {
    struct ifreq dev;
    int i;
    strncpy(dev.ifr_name, iface, 6);
    errno = 0;

    int sock = socket(AF_INET, SOCK_DGRAM, 0);
    if (sock < 0) {
        return -1;
    }
    if (ioctl(sock, SIOCGIFHWADDR, &dev) < 0) {
        close(sock);
        return -2;
    }
    for (i=0; i<mac_byte_length; i++) {
        dev.ifr_hwaddr.sa_data[i] = mac[i];
    }
    if (ioctl(sock, SIOCSIFHWADDR, &dev) < 0) {
        close(sock);
        return -2;
    }

    close(sock);
    return 0;
}

jint
Java_cc_mvdan_macfuzzer_NativeIOCtller_setMacAddr(JNIEnv* env,
                                                  jobject thiz,
                                                  jbyteArray mac)
{
    uint8_t new_mac[6];

    (*env)->GetByteArrayRegion(env, mac, 0, 6, new_mac);

    jclass nioc = (*env)->GetObjectClass(env, thiz);

    jfieldID interface_field = (*env)->GetFieldID(env, nioc,
            "mInterface", "Ljava/lang/String;");
    if (interface_field == NULL) {
        return -1;
    }

    /*uid_t me = geteuid();
    gid_t gme = getgid();
    if (setgid(0) || seteuid(0)) {
        return -2;
    }*/
    jstring jdev = (jstring) (*env)->GetObjectField(env, thiz, interface_field);
    const char * iface = (*env)->GetStringUTFChars(env, jdev, JNI_FALSE);
    (*env)->ReleaseStringUTFChars(env, jdev, iface);
    int retval = nativeioc_set_mac_addr(iface, new_mac);
    /*if (setgid(gme) || seteuid(me)) {
        return retval << 2 | 1;
    }*/
    return retval;
}

jstring
Java_cc_mvdan_macfuzzer_NativeIOCtller_getErrorString(JNIEnv* env,
                                                      jobject thiz,
                                                      jint errcode)
{
    char strerrmsg[40];
    snprintf(strerrmsg, 39, "%d: %s", (int)errcode, strerror((int)errcode));
    return (*env)->NewStringUTF(env, strerrmsg);
}

jint
Java_cc_mvdan_macfuzzer_NativeIOCtller_getCurrentUID(JNIEnv* env,
                                                     jobject thiz)
{
   return (jint)getuid();
}


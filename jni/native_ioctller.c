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

jbyteArray
Java_un_ique_macaddroid_NativeIOCtller_getCurrentMacAddr(JNIEnv* env,
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
Java_un_ique_macaddroid_NativeIOCtller_getCurrentMacAddrError(JNIEnv* env,
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

int nativeioc_set_mac_addr(const char * iface, const uint8_t * mac) {
    struct ifreq dev;
    int i;
    strncpy(dev.ifr_name, iface, 6);

    int sock = socket (AF_INET, SOCK_DGRAM, 0);
    if (sock < 0) {
        return errno;
    }
    if (ioctl(sock, SIOCGIFHWADDR, &dev) < 0) {
        close(sock);
        return errno;
    }
    for (i=0; i<6; i++) {
        dev.ifr_hwaddr.sa_data[i] = mac[i];
    }
    if (ioctl(sock, SIOCSIFHWADDR, &dev) < 0) {
        close(sock);
        return -errno;
    }

    close(sock);
    return 0;
}

jint
Java_un_ique_macaddroid_NativeIOCtller_setMacAddr(JNIEnv* env,
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
Java_un_ique_macaddroid_NativeIOCtller_getErrorString(JNIEnv* env,
                                                      jobject thiz,
                                                      jint errcode)
{
    char strerrmsg[40];
    snprintf(strerrmsg, 39, "%d: %s", (int)errcode, strerror((int)errcode));
    return (*env)->NewStringUTF(env, strerrmsg);
}

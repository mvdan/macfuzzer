#include <errno.h>
#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <jni.h>

jbyteArray
Java_un_ique_macaddroid_NativeIOCtller_getCurrentMacAddr(JNIEnv* env,
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
        return (*env)->NewByteArray(env, 6);
    }
    if (ioctl(sock, SIOCGIFHWADDR, &dev) < 0) {
        close(sock);
        return (*env)->NewByteArray(env, 6);
    }

    jbyteArray currAddr = (*env)->NewByteArray(env, 6);
    char * macAddr = dev.ifr_hwaddr.sa_data;
    (*env)->SetByteArrayRegion(env, currAddr, 0, 6, macAddr);

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

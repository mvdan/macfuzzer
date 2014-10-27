#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <stdio.h>
#include <sys/prctl.h>
#include <unistd.h>
#include <errno.h>
#include <linux/capability.h>

/* android-20 doesn't support these by default */
#ifndef PR_CAPBSET_READ
#define PR_CAPBSET_READ 23
#endif
#ifndef PR_CAPBSET_DROP
#define PR_CAPBSET_DROP 24
#endif

int set_mac_addr(char * iface, char * mac);
int drop_unneeded_caps();
int confirm_caps_dropped();

const uint8_t mac_hex_length = 17;
const uint8_t mac_byte_length = 6;

int main(int argc, char * argv[])
{
    char * iface;
    uint8_t mac[mac_byte_length];
    int i;

    if (drop_unneeded_caps()) {
        return -1;
    }

    if (confirm_caps_dropped()) {
        return -1;
    }

    if (argc > 3) {
        return -1;
    }
    if (argc == 2) {
        return accept_from_stdin(argv[1]);
    }
    iface = argv[1];

    fprintf(stderr, "Dev: %s. Beginning address format verification.\n", iface);
    if (verify_string_format(argv[2])) {
        fprintf(stderr, "Address format failed: %s, %zd.\n", argv[2], strlen(argv[2]));
        return -1;
    }
    fprintf(stderr, "Address format passed.\n");

    if (convert_hex_to_byte(argv[2], mac)) {
        fprintf(stderr, "Conversion from hex to byte failed.\n");
        return -1;
    }
    fprintf(stderr, "Conversion from hex to byte passed.\n");

    int retval = set_mac_addr(iface, mac);
    if (retval) {
        fprintf(stderr, "set_mac_addr() returned with %d, %s.\n", retval, strerror(errno));
        fprintf(stderr, "6 MAC octets: ");
        int i;
        for (i = 0; i < mac_byte_length; i++)
            fprintf(stderr, "%d ", mac[i]);
    }
    fprintf(stderr, "set_mac_addr() successful.\n");
    return retval;
}

int verify_string_format(const char * str_mac)
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

int convert_hex_to_byte(const char * strmac, uint8_t * mac)
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
            fprintf(stderr, "Found '%c' at %d when we expected a colon.\n", strmac[i], i);
            return -1;
        }
        fprintf(stderr, "octet reached %d, i = %d. %ld\n", octet, i, topfour);
        mac[octet++] = topfour & 0xFF;
        if (octet > 5) {
            fprintf(stderr, "octet reached 5, i = %d.\n", i);
            break;
        }
        
    }

    return 0;
}

int accept_from_stdin(const char * iface)
{
    return 0;
}

int drop_unneeded_caps()
{
    int i;
    for (i = 0; prctl(PR_CAPBSET_READ, i, 0, 0, 0) == 1; i++) {
        if (i == CAP_NET_ADMIN) {
            continue;
        }
        if (prctl(PR_CAPBSET_DROP, i, 0, 0, 0)) {
            fprintf(stderr, "Failed to drop cap: %d\n", i);
            return -1;
        }
    }
    return 0;
}


#define CHROOT_DIR "/tmp"
int confirm_caps_dropped()
{
    if (chroot(CHROOT_DIR)) {
        if (errno == EPERM) {
            return 0;
        }
    }
    fprintf(stderr, "We can chroot, cap drop failed!\n");
    return -1;
}
#undef CHROOT_DIR

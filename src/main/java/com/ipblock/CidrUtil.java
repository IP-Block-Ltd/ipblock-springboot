package com.ipblock;

import java.net.InetAddress;

/**
 * Minimal CIDR matcher for IPv4 and IPv6 whitelist entries.
 */
final class CidrUtil {

    private CidrUtil() {
    }

    static boolean matches(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }
            InetAddress address = InetAddress.getByName(ip);
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefix = Integer.parseInt(parts[1]);

            byte[] addrBytes = address.getAddress();
            byte[] netBytes = network.getAddress();
            if (addrBytes.length != netBytes.length) {
                return false;
            }

            int fullBytes = prefix / 8;
            int remBits = prefix % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (addrBytes[i] != netBytes[i]) {
                    return false;
                }
            }
            if (remBits > 0) {
                int mask = 0xFF << (8 - remBits) & 0xFF;
                if ((addrBytes[fullBytes] & mask) != (netBytes[fullBytes] & mask)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

package com.badbones69.vouchers.api.enums;

import com.badbones69.vouchers.api.CrazyManager;

public enum Version {

    TOO_OLD(-1),
    v1_7_R1(171), v1_7_R2(172), v1_7_R3(173), v1_7_R4(174),
    v1_8_R1(181), v1_8_R2(182), v1_8_R3(183),
    v1_9_R1(191), v1_9_R2(192),
    v1_10_R1(1101),
    v1_11_R1(1111),
    v1_12_R1(1121),
    v1_13_R2(1132),
    v1_14_R1(1141),
    v1_15_R1(1151),
    v1_16_R1(1161), v1_16_R2(1162), v1_16_R3(1163),
    v1_17_R1(1171),
    v1_18_R1(1181),
    v1_18_R2(1182),
    v1_19(1191),
    TOO_NEW(-2);

    private static Version currentProtocol;
    private static Version latest;

    private final int versionProtocol;

    private static final CrazyManager crazyManager = CrazyManager.getInstance();

    Version(int versionProtocol) {
        this.versionProtocol = versionProtocol;
    }

    public static Version getCurrentProtocol() {

        String serVer = crazyManager.getPlugin().getServer().getClass().getPackage().getName();

        int serProt = Integer.parseInt(
                serVer.substring(
                        serVer.lastIndexOf('.') + 1
                ).replace("_", "").replace("R", "").replace("v", "")
        );

        for (Version protocol : values()) {
            if (protocol.versionProtocol == serProt) {
                currentProtocol = protocol;
                break;
            }
        }

        if (currentProtocol == null) currentProtocol = Version.TOO_NEW;

        return currentProtocol;
    }

    public static boolean isLegacy() {
        return isOlder(Version.v1_13_R2);
    }

    public static Version getLatestProtocol() {

        if (latest != null) return latest;

        Version old = Version.TOO_OLD;

        for (Version protocol : values()) {
            if (protocol.compare(old) == 1) {
                old = protocol;
            }
        }

        return old;
    }

    public static boolean isNewer(Version protocol) {
        if (currentProtocol == null) getCurrentProtocol();
        int proto = currentProtocol.versionProtocol;
        return proto > protocol.versionProtocol || proto == -2;
    }

    public static boolean isAtLeast(Version protocol) {
        if (currentProtocol == null) getCurrentProtocol();
        int proto = currentProtocol.versionProtocol;
        return proto >= protocol.versionProtocol || proto == -2;
    }

    public static boolean isSame(Version protocol) {
        if (currentProtocol == null) getCurrentProtocol();
        return currentProtocol.versionProtocol == protocol.versionProtocol;
    }

    public static boolean isOlder(Version protocol) {
        if (currentProtocol == null) getCurrentProtocol();
        int proto = currentProtocol.versionProtocol;
        return proto < protocol.versionProtocol || proto == -1;
    }

    public int compare(Version protocol) {
        int result = -1;
        int current = versionProtocol;
        int check = protocol.versionProtocol;

        if (current > check || check == -2) {
            result = 1;
        } else if (current == check) {
            result = 0;
        }

        return result;
    }

}
package com.acme.onlineshop.utils;

/**
 * This class detects the currently used operating system at runtime.
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
public enum OperatingSystem {

    WINDOWS("Windows"),
    LINUX("Linux"), MAC("Mac OS"),
    FREE_BSD("FreeBSD"), OPEN_BSD("OpenBSD"), NET_BSD("NetBSD"),
    SOLARIS("Solaris"), SUN_OS("SunOS"),
    AIX("AIX"), HP_UX("HP-UX"), IRIX("IRIX"),
    OS_400("OS/400"), OS_2("OS/2"), Z_OS("z/OS");

    public final String name;

    OperatingSystem(String name) {
        this.name = name;
    }

    /**
     * System property for currently running operating system
     *
     * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.html#getProperties()">System properties</a>
     * @see <a href="https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/SystemUtils.java">Apache Commons Implementation</a>
     */
    public static OperatingSystem getCurrentSystem() {

        String systemIdentifier = System.getProperty("os.name").toLowerCase();

        if (systemIdentifier.startsWith("windows")) {
            return WINDOWS;
        } else if (systemIdentifier.startsWith("mac")) {
            return MAC;
        } else if (systemIdentifier.startsWith("linux")) {
            return LINUX;
        } else if (systemIdentifier.startsWith("freebsd")) {
            return FREE_BSD;
        } else if (systemIdentifier.startsWith("openbsd")) {
            return OPEN_BSD;
        } else if (systemIdentifier.startsWith("netbsd")) {
            return NET_BSD;
        } else if (systemIdentifier.startsWith("sunos")) {
            return SUN_OS;
        } else if (systemIdentifier.startsWith("solaris")) {
            return SOLARIS;
        } else if (systemIdentifier.startsWith("aix")) {
            return AIX;
        } else if (systemIdentifier.startsWith("hp-ux")) {
            return HP_UX;
        } else if (systemIdentifier.startsWith("irix")) {
            return IRIX;
        } else if (systemIdentifier.startsWith("os/400")) {
            return OS_400;
        } else if (systemIdentifier.startsWith("os/2")) {
            return OS_2;
        } else if (systemIdentifier.startsWith("z/os")) {
            return Z_OS;
        } else {
            throw new IllegalStateException("Unknown operating system");
        }
    }

    public static boolean isUnix() {
        return isUnix(getCurrentSystem());
    }

    public static boolean isUnix(OperatingSystem currentSystem) {
        return switch (currentSystem) {
            case LINUX, MAC, SOLARIS, SUN_OS, FREE_BSD, OPEN_BSD, NET_BSD, AIX, HP_UX, IRIX -> true;
            default -> false;
        };
    }
}

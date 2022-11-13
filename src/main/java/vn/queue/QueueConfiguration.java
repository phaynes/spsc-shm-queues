package vn.queue;

import vn.tech.OSUtility;

public class QueueConfiguration {
    final public String ramDiskQueueDir;

    public QueueConfiguration(String ramDiskQueueDir) {
        this.ramDiskQueueDir = ramDiskQueueDir;
    }

    private static QueueConfiguration current;

    public static QueueConfiguration getCurrent() {
        if (current == null)
            current = new QueueConfiguration(ramDiskQueueDirFromOsType(OSUtility.getCurrent().osType));
        return current;
    }

    public static String ramDiskQueueDirFromOsType(OSUtility.OSTYPE osType) {
        switch (osType) {
        case OS_UNKNOWN:
            return ".";
        case OS_WINDOW:
            return "r:";
        case OS_MAC:
            return "/Volumes/ram-disk/edb/";
        case OS_LINUX:
            return "/dev/shm/tmx/edb/";
        case OS_SOLARIS:
            return "/var/run/tmx/edb/";
        default:
            return ".";
        }
    }

}

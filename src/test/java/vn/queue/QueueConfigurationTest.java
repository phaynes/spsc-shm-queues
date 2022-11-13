package vn.queue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import vn.queue.QueueConfiguration;
import vn.tech.OSUtility;
import vn.tech.OSUtility.OSTYPE;

public class QueueConfigurationTest {

    /**
     * Test basic positioning.
     * 
     * @throws Exception
     *             File processing exception - probably need open a file.
     */
    @Test
    public void testOsTypeFromName() throws Exception {
        assertThat("Unknown", QueueConfiguration.ramDiskQueueDirFromOsType(OSTYPE.OS_UNKNOWN), equalTo("."));
        assertThat("Mac", QueueConfiguration.ramDiskQueueDirFromOsType(OSTYPE.OS_MAC),
                equalTo("/Volumes/ram-disk/edb/"));
        assertThat("Linux", QueueConfiguration.ramDiskQueueDirFromOsType(OSTYPE.OS_LINUX),
                equalTo("/dev/shm/tmx/edb/"));
        assertThat("SunOS", QueueConfiguration.ramDiskQueueDirFromOsType(OSTYPE.OS_SOLARIS),
                equalTo("/var/run/tmx/edb/"));
        assertThat("Windows", QueueConfiguration.ramDiskQueueDirFromOsType(OSTYPE.OS_WINDOW), equalTo("r:"));

    }

    @Test
    public void testCurrentOsType() throws Exception {
        OSTYPE osType = OSUtility.getCurrent().osType;
        switch (osType) {
        case OS_UNKNOWN:
            assertThat("Unknown", QueueConfiguration.getCurrent().ramDiskQueueDir, equalTo("."));
            break;
        case OS_MAC:
            assertThat("Mac", QueueConfiguration.getCurrent().ramDiskQueueDir, equalTo("/Volumes/ram-disk/edb/"));
            break;
        case OS_LINUX:
            assertThat("Linux", QueueConfiguration.getCurrent().ramDiskQueueDir, equalTo("/dev/shm/tmx/edb/"));
            break;
        case OS_SOLARIS:
            assertThat("SunOS", QueueConfiguration.getCurrent().ramDiskQueueDir, equalTo("/var/run/tmx/edb/"));
            break;
        case OS_WINDOW:
            assertThat("Windows", QueueConfiguration.getCurrent().ramDiskQueueDir, equalTo("r:"));
            break;
        }

    }
}

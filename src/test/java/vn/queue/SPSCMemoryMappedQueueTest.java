package vn.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import vn.queue.CacheLine;
import vn.queue.QueueMemoryMappedBuffer;
import vn.queue.SpscMemoryMappedCacheLineQueue;

public class SPSCMemoryMappedQueueTest {
    public static final String TEST_QUEUE_PATH =  buildRamDiskPath() + "test-queue.ipc";

    @Before
    public void setup() throws Exception {
    }

    public static String buildRamDiskPath() {
        final String os = System.getProperty("os.name").toLowerCase();
        final boolean isWindows = (os.indexOf("win") >= 0);
        final boolean isLinux = (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
        final boolean isMac = (os.indexOf("mac") >= 0);
        if (isWindows) {
            return "r:";
        }
       if (isMac) {
            return "/Volumes/ram-disk/edb/";
       }
        return isLinux? "/dev/shm/tmx/edb/" : "/var/run/tmx/edb/"; // Assume Solaris is running.
    }

    /**
     * Test basic positioning.
     * @throws Exception File processing exception - probably need open a file.
     */
    @Test
    public void testMessageQueueSize() throws Exception {
        try (QueueMemoryMappedBuffer mappedBuffer = new QueueMemoryMappedBuffer(TEST_QUEUE_PATH)) {
            mappedBuffer.createAndLoadMappedFile();
            SpscMemoryMappedCacheLineQueue queue = new SpscMemoryMappedCacheLineQueue(mappedBuffer, (byte) 0);
            assertEquals(mappedBuffer.getCapacity(), (1 << 12));
            assertEquals(queue.size(), 0);
            assertTrue(queue.isEmpty());
        }
    }

    /**
     * Test queue offset calculations.
     * @throws Exception File processing exception - probably need open a file.
     */
    @Test
    public void testQueuePositioning() throws Exception {
        try (QueueMemoryMappedBuffer mappedBuffer = new QueueMemoryMappedBuffer(TEST_QUEUE_PATH)) {
            mappedBuffer.createAndLoadMappedFile();
            SpscMemoryMappedCacheLineQueue queue = new SpscMemoryMappedCacheLineQueue(mappedBuffer, (byte) 0);
            assertTrue(queue.testOffsetCalculation());
        }
    }

    /**
     * Test adding and removing of elements.
     * @throws Exception File processing exception - probably need open a file.
     */
    @Test
    public void testAddAndPurge() throws Exception {
        try (QueueMemoryMappedBuffer mappedBuffer = new QueueMemoryMappedBuffer(TEST_QUEUE_PATH)) {
            mappedBuffer.createAndLoadMappedFile();
            SpscMemoryMappedCacheLineQueue queue = new SpscMemoryMappedCacheLineQueue(mappedBuffer,
                    (byte) (SpscMemoryMappedCacheLineQueue.PRODUCER | SpscMemoryMappedCacheLineQueue.CONSUMER));
            CacheLine cacheLine = queue.newCacheLine();
            for (int i=0; i< mappedBuffer.getCapacity() - 1; i++) {
               assertTrue(queue.preOffer(cacheLine));
               cacheLine.putInt(0, i);
               queue.offer();
               assertEquals(queue.size(), (i+1));
            }
            assertTrue(!queue.preOffer(cacheLine));

            CacheLine result;
            for (int i=0; i< mappedBuffer.getCapacity() - 1; i++) {
                result = queue.poll();
                assertTrue(result != null);
                assertEquals(result.getInt(0), i);
                queue.releaseCacheLine();
            }
            assertEquals(queue.size(), 0);
        }
    }

}

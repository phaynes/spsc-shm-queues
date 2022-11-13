package vn.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;

import org.agrona.BitUtil;
import org.agrona.IoUtil;
import org.agrona.concurrent.AtomicBuffer;
import org.agrona.concurrent.UnsafeBuffer;

/**
 * A shared memory buffer to support IPC via Queues.
 */
public class QueueMemoryMappedBuffer implements AutoCloseable {

    // The queue size + size of the queue header should sit within L1 cache.

    /**
     * Size of the queue.
     */
    public static final int DEFAULT_QUEUE_SIZE = 4096;

    /**
     * Additional cache lines to provide working memory for the queue to operate
     * within.
     */
    public static final int QUEUE_HEADER_SIZE = 8;

    /**
     * Shared memory location of the queue.
     */
    private String queueName;

    /**
     * Mapped file.
     */
    private MappedByteBuffer mappedFile;

    /**
     * Wrapping of mappedFile which supports off heap processing.
     */
    private UnsafeBuffer buffer;

    /**
     * Size of queue.
     */
    private final int capacity;

    /*
     * Size of the queue in bytes.
     */
    private int FILE_SIZE;

    /**
     * Create a memory mapped buffer queue.
     *
     * @param queueName
     *            SHM filename.
     */
    public QueueMemoryMappedBuffer(String queueName) {
        this(queueName, DEFAULT_QUEUE_SIZE);
    }

    /**
     * Create the memory backing for the queue. Currently private to keep the
     * queue operating within the L1 Cache.
     *
     * @param queueName
     *            SHM file name.
     * @param capacity
     *            Size of the queue.
     */
    public QueueMemoryMappedBuffer(String queueName, int capacity) {
        this.queueName = queueName;
        this.capacity = capacity;

        // We have two design options store bytes within a cache line size or
        // individually.
        // Try cache lines to start with
        FILE_SIZE = (this.capacity + QUEUE_HEADER_SIZE) * BitUtil.CACHE_LINE_LENGTH;
    }

    /**
     * Create a new file to support publication.
     *
     * @return The memory mapped file.
     * @throws FileNotFoundException
     *             File is not found and can't load.
     * @throws IOException
     *             An error exists in opening the queue.
     */
    public MappedByteBuffer createAndLoadMappedFile() throws FileNotFoundException, IOException {
        File ipcFile = new File(queueName);
        IoUtil.deleteIfExists(ipcFile);
        mappedFile = IoUtil.mapNewFile(ipcFile, FILE_SIZE);
        buffer = new UnsafeBuffer(mappedFile);
        buffer.setMemory(0, buffer.capacity(), (byte) 0);
        return mappedFile;
    }

    /**
     * Open a file to read queue results from.
     *
     * @return The memory mapped file.
     * @throws FileNotFoundException
     *             File is not found and can't load.
     * @throws IOException
     *             An error exists in opening the queue.
     */
    public MappedByteBuffer mapProducerBuffer() throws FileNotFoundException, IOException {
        File ipcFile = new File(queueName);
        return ipcFile.exists() ? mapBuffer() : createAndLoadMappedFile();
    }

    /**
     * Map to a buffer.
     *
     * @return The memory mapped file.
     * @throws FileNotFoundException
     *             File is not found and can't load.
     * @throws IOException
     *             An error exists in opening the queue.
     */
    public MappedByteBuffer mapBuffer() throws FileNotFoundException, IOException {
        if (mappedFile != null) {
            return mappedFile;
        }
        mappedFile = IoUtil.mapExistingFile(new File(queueName), queueName);
        buffer = new UnsafeBuffer(mappedFile);
        return mappedFile;
    }

    /**
     * Open a file to read queue results from.
     *
     * @return The memory mapped file.
     * @throws FileNotFoundException
     *             File is not found and can't load.
     * @throws IOException
     *             An error exists in opening the queue.
     */
    public MappedByteBuffer waitForFileCreateMappedBuffer() throws FileNotFoundException, IOException {
        File ipcFile = new File(queueName);
        while (!ipcFile.exists()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {  // NOSONAR
                e.printStackTrace(); // NOSONAR
            }
        }
        return mapBuffer();
    }

    /**
     * Close the file supporting the backing.
     */
    @Override
    public void close() {
        if (mappedFile != null) {
            IoUtil.unmap(mappedFile);
            mappedFile = null;
        }
    }

    /**
     * Return the queue capacity.
     *
     * @return Capacity of the queue.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Return the underlying buffer.
     *
     * @return underlying Buffer that can perform operations on the queue.
     */
    public AtomicBuffer getBuffer() {
        return buffer;
    }

    /**
     * Underlying memory mapped file.
     *
     * @return Underlying shared memory file.
     */
    public MappedByteBuffer getMappedFile() {
        return mappedFile;
    }

    /**
     * Return the size of the underlying file.
     * @return
     */
    public int getFileSize() {
        return FILE_SIZE;
    }

}

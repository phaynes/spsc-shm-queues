package vn.queue;

import java.util.AbstractQueue;
import java.util.Iterator;

import org.agrona.BitUtil;
import org.agrona.UnsafeAccess;
import org.agrona.concurrent.AtomicBuffer;

import sun.misc.Unsafe;

/**
 * A Single Producer / Single Consumer (SPSC) {@link java.util.Queue} whose
 * elements are a cache line (64 bytes).
 *
 * Designed to enable IPC between C++ processes as well as C++ and Java
 * processes. The queue uses a memory mapped file as a backing store.
 *
 * There is an equivalent C++ implementation to support full shared memory IPC.
 *
 * @see AbstractQueue
 * @author Philip Hayes
 */
public class SpscMemoryMappedCacheLineQueue extends AbstractQueue<CacheLine> {

    public static final byte PRODUCER = 1;
    public static final byte CONSUMER = 2;

    // Shared variable in the queue header to support initial synchronization of
    // the
    // writing and reading from the queue.
    public static final  int SYNC_ADDRESS_POS = 3 * BitUtil.CACHE_LINE_LENGTH;

    // The following are the offsets into the cache line storing the queue
    // positions.
    private final long headAddressPos;
    private final long tailCacheAddressPos;
    private final long tailAddressPos;
    private final long headCacheAddressPos;
    private final long arrayBaseAddresssPos;

    private QueueMemoryMappedBuffer memoryBuffer;

    private CacheLine peekCacheLine;
    private CacheLine pollCacheLine;

    private static final Unsafe UNSAFE = UnsafeAccess.UNSAFE;

    // This mask exists to support the following optimisation.
    // http://psy-lob-saw.blogspot.com.au/2014/11/the-mythical-modulo-mask.html
    int mask = 0;

    /*
     * Construct the queue and map pointers into the shared memory header.
     *
     * @param memoryBuffer Underlying memory mapped file.
     */
    public SpscMemoryMappedCacheLineQueue(QueueMemoryMappedBuffer memoryBuffer, byte viewMask) {
        this.memoryBuffer = memoryBuffer;
        headAddressPos = memoryBuffer.getBuffer().addressOffset();
        tailCacheAddressPos = headAddressPos + BitUtil.SIZE_OF_LONG;
        tailAddressPos = headAddressPos + 2 * BitUtil.CACHE_LINE_LENGTH;
        headCacheAddressPos = tailAddressPos + BitUtil.SIZE_OF_LONG;
        arrayBaseAddresssPos = headAddressPos + 4 * BitUtil.CACHE_LINE_LENGTH;
        // producer owns tail and headCache

        mask = memoryBuffer.getCapacity() - 1;

        peekCacheLine = newCacheLine();
        pollCacheLine = newCacheLine();

        // consumer owns head and tailCache
        if ((viewMask & CONSUMER) == CONSUMER) {
            clear();
        }
    }

    /**
     * Create a new cache line ready for processing.
     *
     * @return a new cache line
     */
    public CacheLine newCacheLine() {
        return new CacheLine(memoryBuffer.getMappedFile());
    }

    /**
     * Returns the underlying memory mapped buffer.
     *
     * @return Memory mapped buffer.
     */
    public AtomicBuffer buffer() {
        return this.memoryBuffer.getBuffer();
    }

    /**
     * Setup a cache line ready for writing too in with the correct position in
     * the memory mapped file.
     *
     * @param aCacheLine
     *            An empty cache line to be updated.
     * @return true if a CacheLine is mapped to a position in memory or false if
     *         the queue is full.
     */
    public boolean preOffer(CacheLine aCacheLine) {
        final long currentTail = getTailPlain();
        if (((currentTail + 1L - getHeadCache()) & mask) == 0) {
            setHeadCache(getHead());
            if (((currentTail + 1L - getHeadCache()) & mask) == 0) {
                return false;
            }
        }
        aCacheLine.updateWrap(calcElementOffset(currentTail));
        return true;
    }

    /**
     * Release the current cache line from the producer and make available to
     * the consumer.
     */
    public void offer() {
        setTail(getTailPlain() + 1);
    }

    @Override
    public boolean offer(CacheLine aCacheLine) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns back a handle to the current cache line.
     *
     * @return A cache line. If the CacheLine is null the queue is empty.
     */
    @Override
    public CacheLine poll() {
        final long currentHead = getHeadPlain();
        if (((currentHead - getTailCache()) & mask) == 0) {
            setTailCache(getTail());
            if (((currentHead - getTailCache()) & mask) == 0) {
                return null;
            }
        }
        pollCacheLine.updateWrap(calcElementOffset(currentHead));
        return pollCacheLine;
    }

    /**
     * Empty the queue.
     */
    @Override
    public void clear() {
        while (poll() != null) {
            releaseCacheLine();
        }
    }

    /**
     * Return the next cache line, spinning until an event is available.
     *
     * @return
     */
    public CacheLine nextCacheLine() {
        CacheLine result;
        while (null == (result = poll())) {
            Thread.yield();
        }
        return result;
    }

    /**
     * Release the cache line and advances to the next item on the queue. This
     * makes it available for the producer process.
     */
    public void releaseCacheLine() {
        setHead(getHeadPlain() + 1);
    }

    /**
     * Look at the next cache line in the queue
     *
     * @return The next cache line in the queue or false if empty.
     */
    @Override
    public CacheLine peek() {
        final long currentHead = getHeadPlain();
        if (currentHead >= getTailCache() && currentHead >= getTail()) {
            return null;

        }
        peekCacheLine.updateWrap(calcElementOffset(currentHead));
        return peekCacheLine;
    }

    /**
     * Test method to support navigating through the shared memory file for
     * testing.
     *
     * @param pos
     *            An index into the queue
     * @return A cache line pointing to a position within the mapped file.
     */
    public CacheLine peekPos(long pos) {
        peekCacheLine.updateWrap(calcElementOffset(pos));
        return peekCacheLine;
    }

    @Override
    public Iterator<CacheLine> iterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns number of items on the queue.
     */
    @Override
    public int size() {
        return (int) ((getTail() - getHead()) & mask);
    }

    /**
     * Returns true if the queue is empty.
     *
     * @return <tt>true</tt> if this queue is empty.
     */
    @Override
    public boolean isEmpty() {
        return ((getTail() - getHead()) & mask) == 0;
    }

    /**
     * Test function demonstrate offset calculation.
     *
     * @return true if offset calculation is successful.
     */
    public boolean testOffsetCalculation() {
        boolean ok = true;
        for (long i = 0; i < (10 * memoryBuffer.getCapacity()); i++) {
            ok &= (calcElementOffset(i) == calcElementOffsetModulus(i));
        }
        return ok;
    }

    /**
     * Calculate the offset in the array.
     *
     * @return The current cache line position in the memory mapped array.
     */
    private long calcElementOffset(final long currentHead) {
        return arrayBaseAddresssPos + ((currentHead & mask) << 6);
    }

    /**
     * Test member to show an alternate - but slower - address calculation.
     */
    private long calcElementOffsetModulus(final long currentHead) {
        return arrayBaseAddresssPos + ((currentHead % memoryBuffer.getCapacity()) << 6);
    }

    public long getHeadPlain() {
        return UNSAFE.getLong(null, headAddressPos);
    }

    public long getHead() {
        return UNSAFE.getLongVolatile(null, headAddressPos);
    }

    public void setHead(final long value) {
        UNSAFE.putOrderedLong(null, headAddressPos, value);
    }

    public long getTailPlain() {
        return UNSAFE.getLong(null, tailAddressPos);
    }

    public long getTail() {
        return UNSAFE.getLongVolatile(null, tailAddressPos);
    }

    public void setTail(final long value) {
        UNSAFE.putOrderedLong(null, tailAddressPos, value);
    }

    public long getHeadCache() {
        return UNSAFE.getLong(null, headCacheAddressPos);
    }

    public void setHeadCache(final long value) {
        UNSAFE.putLong(null, headCacheAddressPos, value);
    }

    public long getTailCache() {
        return UNSAFE.getLong(null, tailCacheAddressPos);
    }

    public void setTailCache(final long value) {
        UNSAFE.putLong(null, tailCacheAddressPos, value);
    }
}

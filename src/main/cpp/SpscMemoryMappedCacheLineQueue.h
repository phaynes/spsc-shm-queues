#ifndef INCLUDED_VN_SPSC_MEMORY_MAPPED_CACHE_LINE_QUEUE_
#define INCLUDED_VN_SPSC_MEMORY_MAPPED_CACHE_LINE_QUEUE_

#include "QueueMemoryMappedBuffer.h"
#include "Atomic64.h"
#include "CacheLine.h"
#include "util/BitUtil.h"

namespace vn { namespace common { namespace collections {

using namespace aeron::util;

/**
 * A Single Producer / Single Consumer (SPSC) queue whose elements are a
 * cache line (64 bytes).
 * Designed to enable IPC between C++ <==> C++ and C++ <==> Java the queue uses a memory
 * mapped file as a backing store.
 *
 * There is an equivalent Java implementation to support full shared memory IPC.
 */
class SpscMemoryMappedCacheLineQueue
{
public:
    // Shared variable in the queue header to support initial synchronization of the
    // writing and reading from the queue.
    static const std::int32_t SYNC_ADDRESS_POS = 3 * BitUtil::CACHE_LINE_LENGTH;

    enum
    {
        // Bit mask for calls to constructor: does caller produce and/or consume events in queue?
        IS_PRODUCER = 1,
        IS_CONSUMER = 2
    };

    SpscMemoryMappedCacheLineQueue(QueueMemoryMappedBuffer& aBuffer, std::uint32_t viewMask)
    : m_buffer(aBuffer), m_pollCacheLine(0, 0)
    {
        // Set calculation pointers up into cache line friendly locations.
        m_headAddressPos       = m_buffer.getBuffer()->getBuffer();
        m_tailCacheAddressPos  = m_headAddressPos + sizeof(std::uint8_t*);
        m_tailAddressPos       = m_headAddressPos + 2 * BitUtil::CACHE_LINE_LENGTH;
        m_headCacheAddressPos  = m_tailAddressPos + sizeof(std::uint8_t*);
        m_arrayBaseAddresssPos = m_headAddressPos + 4 * BitUtil::CACHE_LINE_LENGTH;

        // Set up a mask to optimise positioning calculation.
        m_mask = m_buffer.getCapacity() - 1;

        if ((viewMask && (bool)IS_CONSUMER) != 0)
        {
           clear();
        }
    }

    ~SpscMemoryMappedCacheLineQueue() {}

    /**
     * Returns the underlying memory mapped buffer.
     */
    inline CacheLine* getBuffer()
    {
        return m_buffer.getBuffer();
    }

    /**
     * Setup a cache line ready for writing to, with the correct
     * position in the memory mapped file.
     * @return true if a CacheLine is mapped to a position in memory or false if the queue is full.
     */
    inline bool preOffer(CacheLine& aCacheLine)
    {
        std::int64_t currentTail = getTailPlain();
        if (((currentTail + 1L - getHeadCache()) & m_mask) == 0)
        {
            setHeadCache(getHead());
            if (((currentTail + 1L - getHeadCache()) & m_mask) == 0)
                return false;
        }
        aCacheLine.updateWrap(calcElementOffset(currentTail), BitUtil::CACHE_LINE_LENGTH);
        return true;
    }

    /**
     * Release the current cache line from the producer and make available to
     * the consumer.
     */
    inline void offer()
    {
        setTail(getTailPlain() + 1);
    }

    /**
     * Returns back a handle to the current cache line.
     * @return A cache line. If the CacheLine is not valid the queue is empty.
     */
    inline CacheLine* poll()
    {
        std::int64_t currentHead = getHeadPlain();
        if (((currentHead - getTailCache()) & m_mask) == 0)
        {
            setTailCache(getTail());
            if (((currentHead - getTailCache()) & m_mask) == 0)
            {
                return 0;
            }
        }
        m_pollCacheLine.updateWrap(calcElementOffset(currentHead), BitUtil::CACHE_LINE_LENGTH);
        return &m_pollCacheLine;
    }

    /**
     * Release the cache line and advances to the next item on the queue.
     * This makes it available for the producer process.
     */
    inline void releaseCacheLine()
    {
        setHead(getHeadPlain() + 1);
    }

    /**
     * Returns the current cache line.
     */
    inline CacheLine& getCurrentCacheLine()
    {
        return m_pollCacheLine;
    }

    /**
     * Returns back the size of the queue.
     */
    inline int size()
    {
        return (int) ((getTail() - getHead()) & m_mask);
    }

    /**
     * Returns true if the queue is empty.
     */
    inline bool isEmpty()
    {
        return ((getTail() - getHead()) & m_mask) == 0;
    }

    /**
     * Removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     *
     * <p>This implementation repeatedly invokes {@link #poll poll} until it
     * returns <tt>isValid()</tt>.
     **/
    inline void clear()
    {
        while (poll() != 0) releaseCacheLine();
    }

    SpscMemoryMappedCacheLineQueue(SpscMemoryMappedCacheLineQueue const&) = delete;
    SpscMemoryMappedCacheLineQueue& operator=(SpscMemoryMappedCacheLineQueue const&) = delete;

    /**
     * Test function to demonstrate the offset calculation.
     */
    inline bool testOffsetCalculation()
    {
        bool ok = true;
        for (std::int64_t i = 0; i < (10*m_buffer.getCapacity()); i++)
        {
            ok &= (calcElementOffset(i) == calcElementOffsetModulus(i));
        }
        return ok;
    }

    inline bool testIncTail()
    {
        std::int64_t tail = getTailPlain();
        setTail(tail + 1);
        return (tail >= 0);
    }

private:
    // The following are the offsets into the cache line storing the queue positions.
    std::uint8_t* m_headAddressPos;
    std::uint8_t* m_tailCacheAddressPos;
    std::uint8_t* m_tailAddressPos;
    std::uint8_t* m_headCacheAddressPos;
    std::uint8_t* m_arrayBaseAddresssPos;

    // The underlying memory mapped buffer.
    QueueMemoryMappedBuffer& m_buffer;

    // A working cache line to avoid copying of memory.
    CacheLine m_pollCacheLine;

    // This mask exists to support this optimisation
    // http://psy-lob-saw.blogspot.com.au/2014/11/the-mythical-modulo-mask.html
    std::uint32_t m_mask;

    /**
     * Calculate the offset in the array.
     */
    inline std::uint8_t* calcElementOffset(std::int64_t currentHead)
    {
        return m_arrayBaseAddresssPos + ((m_mask & currentHead) << 6);
    }

    /**
     * Test member to show an alternate - but slower - address calculation.
     */
    inline std::uint8_t* calcElementOffsetModulus(std::int64_t currentHead)
    {
        return m_arrayBaseAddresssPos + ((currentHead % m_buffer.getCapacity()) << 6);
    }

    // Different getters / setter method to support different levels of update visibility.

    // OK across cache lines only.
    inline std::int64_t getHeadPlain()
    {
        return getInt64(m_headAddressPos);
    }

    // Strong getter.
    inline std::int64_t getHead()
    {
        return getInt64Atomic(m_headAddressPos);
    }

    // Relaxed atomic update.
    inline void setHead(std::int64_t value)
    {
        putInt64Ordered(m_headAddressPos, value);
    }

    inline std::int64_t getTailPlain()
    {
        return getInt64(m_tailAddressPos);
    }

    inline std::int64_t getTail()
    {
        return getInt64Atomic(m_tailAddressPos);
    }

    inline void setTail(std::int64_t value)
    {
        putInt64Ordered(m_tailAddressPos, value);
    }

    inline std::int64_t getHeadCache()
    {
        return getInt64(m_headCacheAddressPos);
    }

    inline void setHeadCache(std::int64_t value)
    {
        putInt64(m_headCacheAddressPos, value);
    }

    inline std::int64_t getTailCache()
    {
        return getInt64(m_tailCacheAddressPos);
    }

    inline void setTailCache(std::int64_t value)
    {
        putInt64(m_tailCacheAddressPos, value);
    }

    inline std::int64_t getInt64(std::uint8_t* location) const
    {
        return *reinterpret_cast<std::int64_t *>(location);
    }

    inline std::int64_t getInt64Atomic(std::uint8_t* location) const
    {
        return getInt64Volatile((std::int32_t*)location);
    }

    inline void putInt64Ordered(std::uint8_t* location, std::int64_t v)
    {
        ::putInt64Ordered((volatile std::int32_t*)location, v);

    }

    inline void putInt64(std::uint8_t* location, std::int64_t v)
    {
        ::putInt64((volatile std::int32_t*)location, v);
    }
};

}}}

#endif

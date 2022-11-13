#ifndef INCLUDED_VN_CONCURRENT_CACHE_LINE_SIZE_
#define INCLUDED_VN_CONCURRENT_CACHE_LINE_SIZE_

#include <stddef.h>

namespace vn { namespace common {

/**
 * To implement Cache Oblivious algorithms the CPU dimensions of the computer
 * running the application must be known.
 *
 * The purpose of this class is to the CPU dimension of the system.
 */
class CPUStats
{
public:
    CPUStats();

    ~CPUStats()
    {
    }

    /**
     * Returns the size of a cache line.
     */
    inline size_t getCacheLineSize()
    {
        return m_lineSize;
    }

    /**
     * Returns the size of l1 Cache in bytes.
     */
    inline size_t getL1CacheSize()
    {
        return m_l1CacheSize;
    }

    /**
     * Returns the size of the l2 Cache in bytes.
     */
    inline size_t getL2CacheSize()
    {
        return m_l2CacheSize;
    }

    /**
     * Returns the size of the l3 Cache in bytes or zero if there isn't one.
     */
    inline size_t getL3CacheSize()
    {
        return m_l3CacheSize;
    }

private:
    size_t m_lineSize = 0;
    size_t m_l1CacheSize = 0;
    size_t m_l2CacheSize = 0;
    size_t m_l3CacheSize = 0;

    void initStat();

};
}}

#endif


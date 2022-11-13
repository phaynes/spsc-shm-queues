#include "CPUStats.h"

#include <iostream>

#include <stddef.h>

#if defined(__APPLE__)

#include <sys/sysctl.h>

#elif defined(_WIN32)

#include <stdlib.h>
#include <windows.h>

#elif defined(linux)

#include <unistd.h>
#include <stdio.h>
#include <math.h>

#endif

namespace vn { namespace common {

CPUStats::CPUStats()
{
    initStat();
}

#if defined(__APPLE__)

void CPUStats::initStat()
{
    size_t sizeOfLineSize = sizeof(m_lineSize);
    sysctlbyname("hw.cachelinesize", &m_lineSize, &sizeOfLineSize, 0, 0);

    size_t sizeOfCacheSize = sizeof(m_l1CacheSize);
    sysctlbyname("hw.l1dcachesize", &m_l1CacheSize, &sizeOfCacheSize, 0, 0);
    sysctlbyname("hw.l2cachesize", &m_l2CacheSize, &sizeOfCacheSize, 0, 0);
    sysctlbyname("hw.l3cachesize", &m_l3CacheSize, &sizeOfCacheSize, 0, 0);
}

#elif defined(_WIN32)

void CPUStats::initStat()
{
    DWORD bufferSize = 0;
    DWORD i = 0;
    SYSTEM_LOGICAL_PROCESSOR_INFORMATION * buffer = 0;

    if (GetLogicalProcessorInformation(0, &bufferSize) == false) return;
    buffer = (SYSTEM_LOGICAL_PROCESSOR_INFORMATION *) malloc(bufferSize);
    GetLogicalProcessorInformation(&buffer[0], &bufferSize);

    for (i = 0; i != bufferSize / sizeof(SYSTEM_LOGICAL_PROCESSOR_INFORMATION); ++i)
    {
        if (buffer[i].Relationship == RelationCache && buffer[i].Cache.Level == 1)
        {
            m_lineSize = buffer[i].Cache.LineSize;
            m_l1CacheSize = buffer[i].Cache.Size;
            break;
        }
        if (buffer[i].Relationship == RelationCache && buffer[i].Cache.Level == 2)
        {
            m_l2CacheSize = buffer[i].Cache.Size;
            break;
        }
        if (buffer[i].Relationship == RelationCache && buffer[i].Cache.Level == 3)
        {
            m_l3CacheSize = buffer[i].Cache.Size;
            break;
        }
    }

    free(buffer);
}

#elif defined(linux)

// getconf LEVEL2_CACHE_SIZE
// getconf LEVEL1_DCACHE_SIZE
// getconf LEVEL1_DCACHE_LINESIZE
// sysconf (_SC_LEVEL1_DCACHE_LINESIZE)

// This is yet to be compiled and tested.
void CPUStats::initStat()
{
    m_lineSize = sysconf(_SC_LEVEL1_ICACHE_LINESIZE);
    m_l1CacheSize = sysconf(_SC_LEVEL1_DCACHE_SIZE);
    m_l2CacheSize = sysconf(_SC_LEVEL2_CACHE_SIZE);
    m_l3CacheSize = sysconf(_SC_LEVEL3_CACHE_SIZE);
}

#else
// Must be solaris
void CPUStats::initStat()
{
    // FIXME don't use hard-coded values.
    m_lineSize = 64; // FIXME sysconf(_SC_LEVEL1_ICACHE_LINESIZE);
    m_l1CacheSize = 32768; // FIXME sysconf(_SC_LEVEL1_DCACHE_SIZE);
    m_l2CacheSize = 4194304; // FIXME sysconf(_SC_LEVEL2_CACHE_SIZE);
    m_l3CacheSize = 0; // FIXME  sysconf(_SC_LEVEL3_CACHE_SIZE);
}
#endif

}
}


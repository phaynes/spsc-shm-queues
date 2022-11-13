#include <cstdint>

#include <util/ScopeUtils.h>
#include <util/StringUtil.h>
#include <iostream>

#include "SpscMemoryMappedCacheLineQueue.h"

#if defined(__APPLE__)

static const std::string TEST_QUEUE_PATH =  "/Volumes/ram-disk/test-queue.ipc";

#elif defined(linux)

static const std::string TEST_QUEUE_PATH =  "/dev/shm/tmx/test-queue.ipc";

#elif defined(_WIN32)

#else // Must be solaris

static const std::string TEST_QUEUE_PATH =  "/var/run/tmx/edb/test-queue.ipc";

#endif

using namespace vn::common::collections;
#define CATCH_CONFIG_RUNNER
#define CATCH_CONFIG_MAIN
#include <catch.hpp>

/**
 * Test Queue capacity functions.
 */
TEST_CASE("spscMMQueue queueSize", "queueSize")
{
    QueueMemoryMappedBuffer mappedBuffer(TEST_QUEUE_PATH.c_str());
    mappedBuffer.createAndLoadMappedFile();
    SpscMemoryMappedCacheLineQueue queue(mappedBuffer,
            SpscMemoryMappedCacheLineQueue::IS_PRODUCER | SpscMemoryMappedCacheLineQueue::IS_CONSUMER);

    REQUIRE(mappedBuffer.getCapacity() == (1 << 12));
    REQUIRE(queue.size() == 0);
    REQUIRE(queue.isEmpty());
}

TEST_CASE("spscMMQueue queuePositioning", "queuePositioning")
{
    QueueMemoryMappedBuffer mappedBuffer(TEST_QUEUE_PATH.c_str());
    mappedBuffer.createAndLoadMappedFile();
    SpscMemoryMappedCacheLineQueue queue(mappedBuffer,
            SpscMemoryMappedCacheLineQueue::IS_PRODUCER | SpscMemoryMappedCacheLineQueue::IS_CONSUMER);
    REQUIRE(queue.testOffsetCalculation());
}

TEST_CASE("spscMMQueue testAddAndPurge", "testAddAndPurge")
{
    QueueMemoryMappedBuffer mappedBuffer(TEST_QUEUE_PATH.c_str());
    mappedBuffer.createAndLoadMappedFile();

    SpscMemoryMappedCacheLineQueue queue(mappedBuffer,
            SpscMemoryMappedCacheLineQueue::IS_PRODUCER | SpscMemoryMappedCacheLineQueue::IS_CONSUMER);

    CacheLine cacheLine(0, 0);
    for (int i=0; i < mappedBuffer.getCapacity() - 1; i++)
    {
        REQUIRE(queue.preOffer(cacheLine));
        cacheLine.putInt32(0, i);
        queue.offer();
        REQUIRE(queue.size() == (i + 1));
    }
    REQUIRE(queue.preOffer(cacheLine) == false);

    for (int i=0; i < mappedBuffer.getCapacity() - 1; i++)
    {
        REQUIRE(queue.poll() != 0);
        REQUIRE(queue.getCurrentCacheLine().getInt32(0) == i);
        queue.releaseCacheLine();
    }
    REQUIRE(queue.size() == 0);

    for (int i=0; i < mappedBuffer.getCapacity() - 1; i++)
    {
        REQUIRE(queue.preOffer(cacheLine));
        cacheLine.putInt32(0, i);
        queue.offer();
        REQUIRE(queue.size() == (i + 1));
    }
    REQUIRE(queue.preOffer(cacheLine) == false);

    for (int i=0; i < mappedBuffer.getCapacity() - 1; i++)
    {
        REQUIRE(queue.poll() != 0);
        REQUIRE(queue.getCurrentCacheLine().getInt32(0) == i);
        queue.releaseCacheLine();
    }
    REQUIRE(queue.size() == 0);

    for (int i=0; i < mappedBuffer.getCapacity() - 1; i++)
    {
        REQUIRE(queue.preOffer(cacheLine));
        cacheLine.putInt32(0, i);
        queue.offer();
        REQUIRE(queue.size() == (i + 1));
    }
    queue.clear();
    REQUIRE(queue.size() == 0);
}


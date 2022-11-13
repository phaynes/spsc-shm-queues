#include "QueueMemoryMappedBuffer.h"

#include <thread>
#include <sys/stat.h>
#include "util/BitUtil.h"

#include "CPUStats.h"

namespace vn { namespace common { namespace collections {

// Additional space allocated for shared queue variables.
const int QUEUE_HEADER_SIZE = 8;

// Set a queue size so operation sits fully within L1 cache. By rights this should be
// determined programatically using the CacheLine class but this isn't currently
// supported in Java.

// This number must be a power of 2 for the masking operation to work.
const int DEFAULT_QUEUE_SIZE = 4096;

QueueMemoryMappedBuffer::QueueMemoryMappedBuffer(const char* aFilename) :
    m_filename (aFilename)
{
    CPUStats stats;
    m_capacity = DEFAULT_QUEUE_SIZE;
    m_fileSize = (QUEUE_HEADER_SIZE + m_capacity) * stats.getCacheLineSize();
    m_buffer = 0;
}

QueueMemoryMappedBuffer::~QueueMemoryMappedBuffer()
{
    if (m_buffer != NULL) delete m_buffer;
    m_buffer = 0;
}

MemoryMappedFile::ptr_t  QueueMemoryMappedBuffer::mapProducerBuffer()
{
    struct stat stat;
    return  (::stat(m_filename, &stat) == 0) ?  mapBuffer() : createAndLoadMappedFile();
}

MemoryMappedFile::ptr_t QueueMemoryMappedBuffer::mapBuffer()
{
    if (m_buffer != 0) return m_mmFile;
    m_mmFile = MemoryMappedFile::mapExisting(m_filename);
    m_buffer = new CacheLine(m_mmFile->getMemoryPtr(), m_mmFile->getMemorySize());
    return m_mmFile;
}

MemoryMappedFile::ptr_t QueueMemoryMappedBuffer::createAndLoadMappedFile()
{
    if (m_buffer != 0) return m_mmFile;
    m_mmFile = MemoryMappedFile::createNew(m_filename, m_fileSize);
    m_buffer = new CacheLine(m_mmFile->getMemoryPtr(), m_mmFile->getMemorySize());
    m_buffer->setMemory(0, m_mmFile->getMemorySize(), (std::uint8_t)0);
    return m_mmFile;
}

}}}

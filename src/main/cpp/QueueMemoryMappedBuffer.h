#ifndef INCLUDED_VN_CONCURRENT_QUEUE_MEMORY_MAPPED_BUFFER_
#define INCLUDED_VN_CONCURRENT_QUEUE_MEMORY_MAPPED_BUFFER_

#include <util/MemoryMappedFile.h>
#include <cstdint>
#include "CacheLine.h"

namespace vn { namespace common { namespace collections {

using aeron::util::MemoryMappedFile;
  /**
 * A memory mapped buffer suitable for processing of a shared queue.
 */
class QueueMemoryMappedBuffer
{
public:
    QueueMemoryMappedBuffer(const char* filename);
    ~QueueMemoryMappedBuffer();

    /**
     * Create a memory mapped file suitable for publication.
     */
    MemoryMappedFile::ptr_t createAndLoadMappedFile();

    /**
     * Either map to a producer buffer, creating one if need be.
     */
    MemoryMappedFile::ptr_t mapProducerBuffer();

    /**
     * Map to an existing producer buffer.
     */
    MemoryMappedFile::ptr_t mapBuffer();

    /**
     * Returns the capacity of the queue.
     */
    inline std::int64_t getCapacity() const
    {
        return m_capacity;
    }

    /**
     * Return the working memory buffer.
     */
    inline CacheLine* getBuffer() const
    {
        return m_buffer;
    }

    /**
     * Return a memory mapped file.
     */
    inline MemoryMappedFile::ptr_t getMappedFile() const
    {
        return m_mmFile;
    }

    QueueMemoryMappedBuffer(const QueueMemoryMappedBuffer &) = delete;
    QueueMemoryMappedBuffer& operator=(const QueueMemoryMappedBuffer &) = delete;

private:
    std::int64_t m_capacity;

    std::int64_t m_fileSize;

    CacheLine* m_buffer;

    MemoryMappedFile::ptr_t m_mmFile;

    const char* m_filename;
};


}}}


#endif

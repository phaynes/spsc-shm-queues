#ifndef INCLUDED_VN_CONCURRENT_CACHE_LINE_
#define INCLUDED_VN_CONCURRENT_CACHE_LINE_

#include <string.h>
#include "Atomic64.h"
namespace vn { namespace common { namespace collections {

/**
 * Supports operations across a CPU Cache Line.
 */
class CacheLine
{
public:
    CacheLine(std::uint8_t *buffer, std::int64_t length)
        : m_buffer (buffer), m_length(length)
    {
    }

    /**
     * Return whether this cache line points to a valid region of memory.
     */
    inline bool isValid()
    {
        return (getBuffer() != 0);
    }

    /**
     * Wrap the underlying queue.
     */
    inline void updateWrap(std::uint8_t *buffer, std::int64_t length)
    {
        setCapacity(length);
        setBuffer(buffer);
    }

    /**
     * Override the default implementations to remove bounds checking.
     */
    inline std::int8_t getInt8(std::int64_t offset) const
    {
        return *(volatile std::int8_t*)(m_buffer + offset);
    }

    /**
     * Override the default implementations to remove bounds checking.
     */
    inline std::int16_t getInt16(std::int64_t offset) const
    {
        return ::getInt16((volatile std::int16_t*)(m_buffer + offset));
    }

    /**
     * Override the default implementations to remove bounds checking.
     */
    inline std::int32_t getInt32(std::int64_t offset) const
    {
        return ::getInt32((volatile std::int32_t*)(m_buffer + offset));
    }
    
    /**
     * Override the default implementations to remove bounds checking.
     */
    inline std::int64_t getInt64(std::int64_t offset) const
    {
        return ::getInt64((volatile std::int32_t*)(m_buffer + offset));
    }

    /**
     * Override the default implementations to remove bounds checking.
     * Implement a local method rather than overriding to avoid a rather slow
     * virtual method dispatch.
     */
    inline void putInt8(std::int64_t offset, std::int8_t v)
    {
        ::putInt8((volatile std::int8_t*)(m_buffer + offset), v);
    }

    /**
     * Override the default implementations to remove bounds checking.
     * Implement a local method rather than overriding to avoid a rather slow
     * virtual method dispatch.
     */
    inline void putInt16(std::int64_t offset, std::int16_t v)
    {
        ::putInt16((volatile std::int16_t*)(m_buffer + offset), v);
    }

    /**
     * Override the default implementations to remove bounds checking.
     * Implement a local method rather than overriding to avoid a rather slow
     * virtual method dispatch.
     */
    inline void putInt32(std::int64_t offset, std::int32_t v)
    {
        ::putInt32((volatile std::int32_t*)(m_buffer + offset), v);
    }

    /**
     * Override the default implementations to remove bounds checking.
     * Implement a local method rather than overriding to avoid a rather slow
     * virtual method dispatch.
     */
    inline void putInt64Ordered(std::int64_t offset, std::int64_t v)
    {
        ::putInt64Ordered((volatile std::int32_t *)(m_buffer + offset), v);
    }

    /**
     * Override the default implementations to remove bounds checking.
     * Implement a local method rather than overriding to avoid a rather slow
     * virtual method dispatch.
     */
    inline void putInt64(std::int64_t offset, std::int64_t v)
    {
        ::putInt64((volatile std::int32_t *)(m_buffer + offset), v);
    }

    inline bool compareAndSetInt64(std::int32_t offset, std::int64_t expectedValue, std::int64_t updateValue)
    {
        std::int64_t original = cmpxchg((volatile std::int32_t*)(m_buffer + offset), expectedValue, updateValue);
        return (original == expectedValue);
    }

    inline void setMemory(std::int64_t offset , size_t length, std::uint8_t value)
    {
        memset(m_buffer + offset, value, length);
    }

    inline std::int64_t getCapacity() const
    {
         return m_length;
    }

     inline void setCapacity(int64_t length)
     {
         m_length = length;
     }

     inline std::uint8_t * getBuffer() const
     {
         return m_buffer;
     }

     inline void setBuffer(std::uint8_t *buffer)
     {
         m_buffer = buffer;
     }
private:
     std::uint8_t *m_buffer;
     std::int64_t  m_length;

};

}}}

#endif

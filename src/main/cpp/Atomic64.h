#ifndef INCLUDED_ATOMIC64_
#define INCLUDED_ATOMIC64_

#ifndef __LP64__

#error This code is 64 specific

#endif

#include <cstdint>

/**
 * Set of Operations to support Atomic operations in C++ that are
 * consistent with the same semantics in the JVM.
 *
 * std::int32_t* is used to specify memory addresses rather than void*  to provide compatibility
 * with Java.
 */

/**
 * A compiler directive not reorder instructions.
 */
inline void threadFence()
{
    __asm__ __volatile__("" ::: "memory");
}

/**
 * Fence operation that uses locked addl as mfence is sometimes expensive.
 *
 */
inline void fence()
{
    __asm__ volatile ("lock; addl $0,0(%%rsp)" : : : "cc", "memory");
}

inline void acquire()
{
    volatile std::int64_t* dummy;
    __asm__ volatile ("movq 0(%%rsp), %0" : "=r" (dummy) : : "memory");
}

inline void release()
{
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-variable"
    // Avoid hitting the same cache-line from different threads.
  volatile std::int64_t dummy = 0;
  (void) dummy; // Get Solaris to ignore unused variable too.
#pragma GCC diagnostic pop
}

/**
 * A more jitter friendly alternate to thread:yield in spin waits.
 */
inline void cpu_pause()
{
    asm volatile("pause\n": : :"memory");
}

/**
 * Returns a 16 bit integer without locking.
 */
inline std::int16_t getInt16(volatile void* source)
{
    return *reinterpret_cast<volatile std::int16_t *>(source);
}

/**
 * Returns a 32 bit integer without locking.
 */
inline std::int32_t getInt32(volatile std::int32_t* source)
{
    return *reinterpret_cast<volatile std::int32_t *>(source);
}

/**
 * Override the default implementations to remove bounds checking.
 * Implement a local method rather than overriding to avoid a rather slow
 * virtual method dispatch.
 */
inline void putInt8(volatile std::int8_t* source, std::int8_t value)
{
    *reinterpret_cast<volatile std::int8_t *>(source) = value;
}

/**
 * Override the default implementations to remove bounds checking.
 * Implement a local method rather than overriding to avoid a rather slow
 * virtual method dispatch.
 */
inline void putInt16(volatile std::int16_t* source, std::int16_t value)
{
    *reinterpret_cast<volatile std::int16_t *>(source) = value;
}

/**
 * Override the default implementations to remove bounds checking.
 * Implement a local method rather than overriding to avoid a rather slow
 * virtual method dispatch.
 */
inline void putInt32(volatile std::int32_t* source, std::int32_t value)
{
    *reinterpret_cast<volatile std::int32_t *>(source) = value;
}

/**
 * Returns a 64 bit integer.
 */
inline std::int64_t getInt64(volatile void* source)
{
    return *reinterpret_cast<volatile std::int64_t *>(source);
}

/**
 * Returns a 64 bit integer with ordered semantics.
 */
inline std::int64_t getInt64Ordered(volatile std::int32_t* source)
{
    int64_t sequence = *reinterpret_cast<volatile std::int64_t *>(source);
    threadFence();
    return sequence;
}

/**
 * Returns a 64 bit integer with volatile semantics.
 * On x64 MOV is a SC Atomic operation.
 */
inline std::int64_t getInt64Volatile(volatile std::int32_t* source)
{
    int64_t sequence = *reinterpret_cast<volatile std::int64_t *>(source);
    threadFence();
    return sequence;
}

/**
 * Put a 64 bit int without ordered semantics.
 */
inline void  putInt64(volatile std::int32_t*  address, std::int64_t value)
{
    *reinterpret_cast<volatile std::int64_t *>(address) = value;
}
/**
 * Put a 64 bit with ordered semantics.
 */
inline void  putInt64Ordered(volatile std::int32_t*  address, std::int64_t value)
{
    threadFence();
    *reinterpret_cast<volatile std::int64_t *>(address) = value;
}

/**
 * Put a 64 bit with Atomic semantics.
 **/
inline void putInt64Atomic(volatile std::int32_t*  address, std::int64_t value)
{
    __asm__ __volatile__ (  "xchgq (%2), %0"
                            : "=r" (value)
                            : "0" (value), "r" (address)
                            : "memory");
}

inline std::int64_t cmpxchg(volatile std::int32_t* destination,  std::int64_t expected, std::int64_t desired)
{
    uint64_t original;
    asm volatile("lock; cmpxchgq %2, %1"
                 : "=a"(original), "+m"(*destination)
                 : "q"(desired), "0"(expected));
    return original;
}

#endif

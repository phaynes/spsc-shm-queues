package vn.queue;

import static org.agrona.BitUtil.SIZE_OF_BYTE;
import static org.agrona.BitUtil.SIZE_OF_CHAR;
import static org.agrona.BitUtil.SIZE_OF_DOUBLE;
import static org.agrona.BitUtil.SIZE_OF_FLOAT;
import static org.agrona.BitUtil.SIZE_OF_INT;
import static org.agrona.BitUtil.SIZE_OF_LONG;
import static org.agrona.BitUtil.SIZE_OF_SHORT;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import sun.misc.Unsafe;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.UnsafeAccess;
import org.agrona.concurrent.AtomicBuffer;

/**
 * Models an off heap CacheLine on an Intel CPU (i.e. 64 bytes).
 *
 * This abstraction is designed to support CPU efficient processing by ensuring
 * data arriving at the CPU is always cache coherent.
 */
public class CacheLine implements AtomicBuffer {

    public static final String DISABLE_BOUNDS_CHECKS_PROP_NAME = "agrona.disable.bounds.checks";
    public static final boolean SHOULD_BOUNDS_CHECK = !Boolean.getBoolean(DISABLE_BOUNDS_CHECKS_PROP_NAME);

    private static final byte[] NULL_BYTES = "null".getBytes(StandardCharsets.UTF_8);
    private static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();
    private static final Unsafe UNSAFE = UnsafeAccess.UNSAFE;
    private static final long ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

    private byte[] byteArray;
    private ByteBuffer byteBuffer;
    public long addressOffset;

    private int capacity;

    public CacheLine(final ByteBuffer buffer) {
        wrap(buffer);
    }

    public void updateWrap(long offset) {
        addressOffset = offset;
    }

    @Override
    public void wrap(final ByteBuffer buffer)
    {
        byteBuffer = buffer;

        if (buffer.hasArray())
        {
            byteArray = buffer.array();
            addressOffset = ARRAY_BASE_OFFSET + buffer.arrayOffset();
        }
        else
        {
            byteArray = null;
            addressOffset = ((sun.nio.ch.DirectBuffer)buffer).address();
        }

        capacity = buffer.capacity();
    }

    @Override
    public void wrap(final DirectBuffer buffer)
    {
        addressOffset = buffer.addressOffset();
        capacity = buffer.capacity();
        byteArray = buffer.byteArray();
        byteBuffer = buffer.byteBuffer();
    }

    @Override
    public long addressOffset()
    {
        return addressOffset;
    }

    @Override
    public byte[] byteArray()
    {
        return byteArray;
    }

    @Override
    public ByteBuffer byteBuffer()
    {
        return byteBuffer;
    }

    @Override
    public void setMemory(final int index, final int length, final byte value)
    {
        boundsCheck(index, length);

        UNSAFE.setMemory(byteArray, addressOffset + index, length, value);
    }

    @Override
    public int capacity()
    {
        return capacity;
    }

    @Override
    public void checkLimit(final int limit)
    {
        if (limit > capacity)
        {
            final String msg = String.format("limit=%d is beyond capacity=%d", limit, capacity);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    public ByteBuffer duplicateByteBuffer()
    {
        if (null == byteBuffer)
        {
            return ByteBuffer.wrap(byteArray);
        }
        else
        {
            final ByteBuffer duplicate = byteBuffer.duplicate();
            duplicate.clear();

            return duplicate;
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public long getLong(final int index, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_LONG);

        long bits = UNSAFE.getLong(byteArray, addressOffset + index);
        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            bits = Long.reverseBytes(bits);
        }

        return bits;
    }

    @Override
    public void putLong(final int index, final long value, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_LONG);

        long bits = value;
        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            bits = Long.reverseBytes(bits);
        }

        UNSAFE.putLong(byteArray, addressOffset + index, bits);
    }

    @Override
    public long getLong(final int index)
    {
        boundsCheck(index, SIZE_OF_LONG);

        return UNSAFE.getLong(byteArray, addressOffset + index);
    }

    @Override
    public void putLong(final int index, final long value)
    {
        boundsCheck(index, SIZE_OF_LONG);

        UNSAFE.putLong(byteArray, addressOffset + index, value);
    }

    @Override
    public long getLongVolatile(final int index)
    {
        boundsCheck(index, SIZE_OF_LONG);

        return UNSAFE.getLongVolatile(byteArray, addressOffset + index);
    }

    @Override
    public void putLongVolatile(final int index, final long value)
    {
        boundsCheck(index, SIZE_OF_LONG);

        UNSAFE.putLongVolatile(byteArray, addressOffset + index, value);
    }

    @Override
    public void putLongOrdered(final int index, final long value)
    {
        boundsCheck(index, SIZE_OF_LONG);

        UNSAFE.putOrderedLong(byteArray, addressOffset + index, value);
    }

    @Override
    public long addLongOrdered(final int index, final long increment)
    {
        boundsCheck(index, SIZE_OF_LONG);

        final long offset = addressOffset + index;
        final byte[] byteArray = this.byteArray;
        final long value = UNSAFE.getLong(byteArray, offset);
        UNSAFE.putOrderedLong(byteArray, offset, value + increment);
        return value;
    }

    @Override
    public boolean compareAndSetLong(final int index, final long expectedValue, final long updateValue)
    {
        boundsCheck(index, SIZE_OF_LONG);

        return UNSAFE.compareAndSwapLong(byteArray, addressOffset + index, expectedValue, updateValue);
    }

    @Override
    public long getAndSetLong(final int index, final long value)
    {
        boundsCheck(index, SIZE_OF_LONG);

        return UNSAFE.getAndSetLong(byteArray, addressOffset + index, value);
    }

    @Override
    public long getAndAddLong(final int index, final long delta)
    {
        boundsCheck(index, SIZE_OF_LONG);

        return UNSAFE.getAndAddLong(byteArray, addressOffset + index, delta);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int getInt(final int index, final ByteOrder byteOrder)
    {
        return UNSAFE.getInt(byteArray, addressOffset + index);
    }

    @Override
    public void putInt(final int index, final int value, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_INT);

        int bits = value;
        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            bits = Integer.reverseBytes(bits);
        }

        UNSAFE.putInt(byteArray, addressOffset + index, bits);
    }

    @Override
    public int getInt(final int index)
    {
        return UNSAFE.getInt(byteArray, addressOffset + index);
    }

    @Override
    public void putInt(final int index, final int value)
    {
        UNSAFE.putInt(byteArray, addressOffset + index, value);
    }

    @Override
    public int getIntVolatile(final int index)
    {
        boundsCheck(index, SIZE_OF_INT);

        return UNSAFE.getIntVolatile(byteArray, addressOffset + index);
    }

    @Override
    public void putIntVolatile(final int index, final int value)
    {
        boundsCheck(index, SIZE_OF_INT);

        UNSAFE.putIntVolatile(byteArray, addressOffset + index, value);
    }

    @Override
    public void putIntOrdered(final int index, final int value)
    {
        boundsCheck(index, SIZE_OF_INT);

        UNSAFE.putOrderedInt(byteArray, addressOffset + index, value);
    }

    @Override
    public int addIntOrdered(final int index, final int increment)
    {
        boundsCheck(index, SIZE_OF_INT);

        final long offset = addressOffset + index;
        final byte[] byteArray = this.byteArray;
        final int value = UNSAFE.getInt(byteArray, offset);
        UNSAFE.putOrderedInt(byteArray, offset, value + increment);
        return value;
    }

    @Override
    public boolean compareAndSetInt(final int index, final int expectedValue, final int updateValue)
    {
        boundsCheck(index, SIZE_OF_INT);

        return UNSAFE.compareAndSwapInt(byteArray, addressOffset + index, expectedValue, updateValue);
    }

    @Override
    public int getAndSetInt(final int index, final int value)
    {
        boundsCheck(index, SIZE_OF_INT);

        return UNSAFE.getAndSetInt(byteArray, addressOffset + index, value);
    }

    @Override
    public int getAndAddInt(final int index, final int delta)
    {
        boundsCheck(index, SIZE_OF_INT);

        return UNSAFE.getAndAddInt(byteArray, addressOffset + index, delta);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public double getDouble(final int index, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_DOUBLE);

        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            final long bits = UNSAFE.getLong(byteArray, addressOffset + index);
            return Double.longBitsToDouble(Long.reverseBytes(bits));
        }
        else
        {
            return UNSAFE.getDouble(byteArray, addressOffset + index);
        }
    }

    @Override
    public void putDouble(final int index, final double value, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_DOUBLE);

        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            final long bits = Long.reverseBytes(Double.doubleToRawLongBits(value));
            UNSAFE.putLong(byteArray, addressOffset + index, bits);
        }
        else
        {
            UNSAFE.putDouble(byteArray, addressOffset + index, value);
        }
    }

    @Override
    public double getDouble(final int index)
    {
        boundsCheck(index, SIZE_OF_DOUBLE);

        return UNSAFE.getDouble(byteArray, addressOffset + index);
    }

    @Override
    public void putDouble(final int index, final double value)
    {
        boundsCheck(index, SIZE_OF_DOUBLE);

        UNSAFE.putDouble(byteArray, addressOffset + index, value);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public float getFloat(final int index, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_FLOAT);

        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            final int bits = UNSAFE.getInt(byteArray, addressOffset + index);
            return Float.intBitsToFloat(Integer.reverseBytes(bits));
        }
        else
        {
            return UNSAFE.getFloat(byteArray, addressOffset + index);
        }
    }

    @Override
    public void putFloat(final int index, final float value, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_FLOAT);

        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            final int bits = Integer.reverseBytes(Float.floatToRawIntBits(value));
            UNSAFE.putLong(byteArray, addressOffset + index, bits);
        }
        else
        {
            UNSAFE.putFloat(byteArray, addressOffset + index, value);
        }
    }

    @Override
    public float getFloat(final int index)
    {
        boundsCheck(index, SIZE_OF_FLOAT);

        return UNSAFE.getFloat(byteArray, addressOffset + index);
    }

    @Override
    public void putFloat(final int index, final float value)
    {
        boundsCheck(index, SIZE_OF_FLOAT);

        UNSAFE.putFloat(byteArray, addressOffset + index, value);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public short getShort(final int index, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_SHORT);

        short bits = UNSAFE.getShort(byteArray, addressOffset + index);
        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            bits = Short.reverseBytes(bits);
        }

        return bits;
    }

    @Override
    public void putShort(final int index, final short value, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_SHORT);

        short bits = value;
        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            bits = Short.reverseBytes(bits);
        }

        UNSAFE.putShort(byteArray, addressOffset + index, bits);
    }

    @Override
    public short getShort(final int index)
    {
        boundsCheck(index, SIZE_OF_SHORT);

        return UNSAFE.getShort(byteArray, addressOffset + index);
    }

    @Override
    public void putShort(final int index, final short value)
    {
        boundsCheck(index, SIZE_OF_SHORT);

        UNSAFE.putShort(byteArray, addressOffset + index, value);
    }

    @Override
    public short getShortVolatile(final int index)
    {
        boundsCheck(index, SIZE_OF_SHORT);

        return UNSAFE.getShortVolatile(byteArray, addressOffset + index);
    }

    @Override
    public void putShortVolatile(final int index, final short value)
    {
        boundsCheck(index, SIZE_OF_SHORT);

        UNSAFE.putShortVolatile(byteArray, addressOffset + index, value);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public byte getByte(final int index)
    {
        boundsCheck(index, SIZE_OF_BYTE);

        return UNSAFE.getByte(byteArray, addressOffset + index);
    }

    @Override
    public void putByte(final int index, final byte value)
    {
        boundsCheck(index, SIZE_OF_BYTE);

        UNSAFE.putByte(byteArray, addressOffset + index, value);
    }

    @Override
    public void getBytes(final int index, final byte[] dst)
    {
        getBytes(index, dst, 0, dst.length);
    }

    @Override
    public void getBytes(final int index, final byte[] dst, final int offset, final int length)
    {
        boundsCheck(index, length);
        boundsCheck(dst, offset, length);

        UNSAFE.copyMemory(byteArray, addressOffset + index, dst, ARRAY_BASE_OFFSET + offset, length);
    }

    @Override
    public void getBytes(final int index, final MutableDirectBuffer dstBuffer, final int dstIndex, final int length)
    {
        dstBuffer.putBytes(dstIndex, this, index, length);
    }

    @Override
    public void getBytes(final int index, final ByteBuffer dstBuffer, final int length)
    {
        boundsCheck(index, length);
        final int dstOffset = dstBuffer.position();
        boundsCheck(dstBuffer, dstOffset, length);

        final byte[] dstByteArray;
        final long dstBaseOffset;
        if (dstBuffer.hasArray())
        {
            dstByteArray = dstBuffer.array();
            dstBaseOffset = ARRAY_BASE_OFFSET + dstBuffer.arrayOffset();
        }
        else
        {
            dstByteArray = null;
            dstBaseOffset = ((sun.nio.ch.DirectBuffer)dstBuffer).address();
        }

        UNSAFE.copyMemory(byteArray, addressOffset + index, dstByteArray, dstBaseOffset + dstOffset, length);
        dstBuffer.position(dstBuffer.position() + length);
    }

    @Override
    public void putBytes(final int index, final byte[] src)
    {
        putBytes(index, src, 0, src.length);
    }

    @Override
    public void putBytes(final int index, final byte[] src, final int offset, final int length)
    {
        //boundsCheck(index, length);
        //boundsCheck(src, offset, length);

        UNSAFE.copyMemory(src, ARRAY_BASE_OFFSET + offset, byteArray, addressOffset + index, length);
    }

    @Override
    public void putBytes(final int index, final ByteBuffer srcBuffer, final int length)
    {
        boundsCheck(index, length);
        final int srcIndex = srcBuffer.position();
        boundsCheck(srcBuffer, srcIndex, length);

        putBytes(index, srcBuffer, srcIndex, length);
        srcBuffer.position(srcIndex + length);
    }

    @Override
    public void putBytes(final int index, final ByteBuffer srcBuffer, final int srcIndex, final int length)
    {
        boundsCheck(index, length);
        boundsCheck(srcBuffer, srcIndex, length);

        final byte[] srcByteArray;
        final long srcBaseOffset;
        if (srcBuffer.hasArray())
        {
            srcByteArray = srcBuffer.array();
            srcBaseOffset = ARRAY_BASE_OFFSET + srcBuffer.arrayOffset();
        }
        else
        {
            srcByteArray = null;
            srcBaseOffset = ((sun.nio.ch.DirectBuffer)srcBuffer).address();
        }

        UNSAFE.copyMemory(srcByteArray, srcBaseOffset + srcIndex, byteArray, addressOffset + index, length);
    }

    @Override
    public void putBytes(final int index, final DirectBuffer srcBuffer, final int srcIndex, final int length)
    {
        boundsCheck(index, length);
        srcBuffer.boundsCheck(srcIndex, length);

        UNSAFE.copyMemory(
            srcBuffer.byteArray(),
            srcBuffer.addressOffset() + srcIndex,
            byteArray,
            addressOffset + index,
            length);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public String getStringUtf8(final int offset, final ByteOrder byteOrder)
    {
        final int length = getInt(offset, byteOrder);

        return getStringUtf8(offset, length);
    }

    @Override
    public String getStringUtf8(final int offset, final int length)
    {
        final byte[] stringInBytes = new byte[length];
        getBytes(offset + SIZE_OF_INT, stringInBytes);

        return new String(stringInBytes, StandardCharsets.UTF_8);
    }

    @Override
    public int putStringUtf8(final int offset, final String value, final ByteOrder byteOrder)
    {
        return putStringUtf8(offset, value, byteOrder, Integer.MAX_VALUE);
    }

    @Override
    public int putStringUtf8(final int offset, final String value, final ByteOrder byteOrder, final int maxEncodedSize)
    {
        final byte[] bytes = value != null ? value.getBytes(StandardCharsets.UTF_8) : NULL_BYTES;
        if (bytes.length > maxEncodedSize)
        {
            throw new IllegalArgumentException("Encoded string larger than maximum size: " + maxEncodedSize);
        }

        putInt(offset, bytes.length, byteOrder);
        putBytes(offset + SIZE_OF_INT, bytes);

        return SIZE_OF_INT + bytes.length;
    }

    @Override
    public String getStringWithoutLengthUtf8(final int offset, final int length)
    {
        final byte[] stringInBytes = new byte[length];
        getBytes(offset, stringInBytes);

        return new String(stringInBytes, StandardCharsets.UTF_8);
    }

    @Override
    public int putStringWithoutLengthUtf8(final int offset, final String value)
    {
        final byte[] bytes = value != null ? value.getBytes(StandardCharsets.UTF_8) : NULL_BYTES;
        putBytes(offset, bytes);

        return bytes.length;
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void boundsCheck(final int index, final int length)
    {
        if (SHOULD_BOUNDS_CHECK)
        {
            if (index < 0 || length < 0 || (index + length) > capacity)
            {
                throw new IndexOutOfBoundsException(String.format("index=%d, length=%d, capacity=%d", index, length, capacity));
            }
        }
    }

    private static void boundsCheck(final byte[] buffer, final int index, final int length)
    {
        if (SHOULD_BOUNDS_CHECK)
        {
            final int capacity = buffer.length;
            if (index < 0 || length < 0 || (index + length) > capacity)
            {
                throw new IndexOutOfBoundsException(String.format("index=%d, length=%d, capacity=%d", index, length, capacity));
            }
        }
    }

    private static void boundsCheck(final ByteBuffer buffer, final int index, final int length)
    {
        if (SHOULD_BOUNDS_CHECK)
        {
            final int capacity = buffer.capacity();
            if (index < 0 || length < 0 || (index + length) > capacity)
            {
                throw new IndexOutOfBoundsException(String.format("index=%d, length=%d, capacity=%d", index, length, capacity));
            }
        }
    }
    @Override
    public void wrap(final byte[] buffer)
    {
        throw new UnsupportedOperationException();
    }
    @Override
    public void wrap(final long address, final int capacity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void wrap(byte[] buffer, int offset, int length) {

    }

    @Override
    public void wrap(ByteBuffer buffer, int offset, int length) {

    }

    @Override
    public void wrap(DirectBuffer buffer, int offset, int length) {

    }

    @Override
    public void verifyAlignment() {

    }

    @Override
    public byte getByteVolatile(final int index)
    {
        boundsCheck(index, SIZE_OF_BYTE);

        return UNSAFE.getByteVolatile(byteArray, addressOffset + index);
    }

    @Override
    public void putByteVolatile(final int index, final byte value)
    {
        boundsCheck(index, SIZE_OF_BYTE);

        UNSAFE.putByteVolatile(byteArray, addressOffset + index, value);
    }

    @Override
    public char getChar(final int index, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_SHORT);

        char bits = UNSAFE.getChar(byteArray, addressOffset + index);
        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            bits = (char)Short.reverseBytes((short)bits);
        }

        return bits;
    }

    @Override
    public void putChar(final int index, final char value, final ByteOrder byteOrder)
    {
        boundsCheck(index, SIZE_OF_SHORT);

        char bits = value;
        if (NATIVE_BYTE_ORDER != byteOrder)
        {
            bits = (char)Short.reverseBytes((short)bits);
        }

        UNSAFE.putChar(byteArray, addressOffset + index, bits);
    }

    @Override
    public char getChar(final int index)
    {
        boundsCheck(index, SIZE_OF_CHAR);

        return UNSAFE.getChar(byteArray, addressOffset + index);
    }

    @Override
    public void putChar(final int index, final char value)
    {
        boundsCheck(index, SIZE_OF_CHAR);

        UNSAFE.putChar(byteArray, addressOffset + index, value);
    }

    @Override
    public char getCharVolatile(final int index)
    {
        boundsCheck(index, SIZE_OF_CHAR);

        return UNSAFE.getCharVolatile(byteArray, addressOffset + index);
    }

    @Override
    public void putCharVolatile(final int index, final char value)
    {
        boundsCheck(index, SIZE_OF_CHAR);

        UNSAFE.putCharVolatile(byteArray, addressOffset + index, value);
    }

    @Override
    public boolean isExpandable() {
        // Cacheline is not expandable
        return false;
    }

    @Override
    public int putStringUtf8(int offset, String value) {
        return putStringUtf8(offset, value, ByteOrder.nativeOrder(), Integer.MAX_VALUE);
    }

    @Override
    public int putStringUtf8(int index, String value, int maxEncodedSize) {
        return putStringUtf8(index, value, ByteOrder.nativeOrder(), maxEncodedSize);
    }

    @Override
    public void getBytes(int index, ByteBuffer dstBuffer, int dstOffset, int length) {
        getBytes(index, dstBuffer, dstOffset, length);
    }

    @Override
    public String getStringUtf8(int index) {
        final int length = getInt(index);

        return getStringUtf8(index, length);
    }

    @Override
    public int compareTo(DirectBuffer o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int wrapAdjustment() {
        // TODO work out if this is correct
        return 0;
    }




}

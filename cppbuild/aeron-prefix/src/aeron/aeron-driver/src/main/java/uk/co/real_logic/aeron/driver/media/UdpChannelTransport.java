/*
 * Copyright 2014 - 2015 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.aeron.driver.media;

import uk.co.real_logic.aeron.driver.event.EventCode;
import uk.co.real_logic.aeron.driver.event.EventLogger;
import uk.co.real_logic.aeron.protocol.HeaderFlyweight;
import uk.co.real_logic.aeron.driver.Configuration;
import uk.co.real_logic.aeron.driver.LossGenerator;
import uk.co.real_logic.agrona.LangUtil;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import static uk.co.real_logic.aeron.logbuffer.FrameDescriptor.frameLength;
import static uk.co.real_logic.aeron.logbuffer.FrameDescriptor.frameVersion;

public abstract class UdpChannelTransport implements AutoCloseable
{
    private final UdpChannel udpChannel;
    private final LossGenerator lossGenerator;
    private final EventLogger logger;
    private final ByteBuffer receiveByteBuffer = ByteBuffer.allocateDirect(Configuration.RECEIVE_BYTE_BUFFER_LENGTH);
    private final UnsafeBuffer receiveBuffer = new UnsafeBuffer(receiveByteBuffer);
    private DatagramChannel sendDatagramChannel;
    private DatagramChannel receiveDatagramChannel;
    private SelectionKey selectionKey;
    private TransportPoller transportPoller;
    private InetSocketAddress bindSocketAddress;
    private InetSocketAddress endPointSocketAddress;
    private InetSocketAddress connectAddress;

    public UdpChannelTransport(
        final UdpChannel udpChannel,
        final InetSocketAddress endPointSocketAddress,
        final InetSocketAddress bindSocketAddress,
        final InetSocketAddress connectAddress,
        final LossGenerator lossGenerator,
        final EventLogger logger)
    {
        this.udpChannel = udpChannel;
        this.lossGenerator = lossGenerator;
        this.logger = logger;
        this.endPointSocketAddress = endPointSocketAddress;
        this.bindSocketAddress = bindSocketAddress;
        this.connectAddress = connectAddress;
    }

    /**
     * Create the underlying channel for reading and writing.
     */
    public void openDatagramChannel()
    {
        try
        {
            sendDatagramChannel = DatagramChannel.open(udpChannel.protocolFamily());
            receiveDatagramChannel = sendDatagramChannel;
            if (udpChannel.isMulticast())
            {
                final NetworkInterface localInterface = udpChannel.localInterface();

                if (null != connectAddress)
                {
                    receiveDatagramChannel = DatagramChannel.open(udpChannel.protocolFamily());
                }

                receiveDatagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                receiveDatagramChannel.bind(new InetSocketAddress(endPointSocketAddress.getPort()));
                receiveDatagramChannel.join(endPointSocketAddress.getAddress(), localInterface);
                sendDatagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, localInterface);

                if (null != connectAddress)
                {
                    sendDatagramChannel.connect(connectAddress);
                }
            }
            else
            {
                sendDatagramChannel.bind(bindSocketAddress);

                if (null != connectAddress)
                {
                    sendDatagramChannel.connect(connectAddress);
                }
            }

            if (0 != Configuration.SOCKET_SNDBUF_LENGTH)
            {
                sendDatagramChannel.setOption(StandardSocketOptions.SO_SNDBUF, Configuration.SOCKET_SNDBUF_LENGTH);
            }

            if (0 != Configuration.SOCKET_RCVBUF_LENGTH)
            {
                receiveDatagramChannel.setOption(StandardSocketOptions.SO_RCVBUF, Configuration.SOCKET_RCVBUF_LENGTH);
            }

            sendDatagramChannel.configureBlocking(false);
            receiveDatagramChannel.configureBlocking(false);
        }
        catch (final IOException ex)
        {
            throw new RuntimeException(String.format(
                "channel \"%s\" : %s", udpChannel.originalUriString(), ex.toString()), ex);
        }
    }

    /**
     * Register this transport for reading from a {@link TransportPoller}.
     *
     * @param transportPoller to register read with
     */
    public void registerForRead(final TransportPoller transportPoller)
    {
        this.transportPoller = transportPoller;
        selectionKey = transportPoller.registerForRead(this);
    }

    /**
     * Return underlying {@link UdpChannel}
     *
     * @return underlying channel
     */
    public UdpChannel udpChannel()
    {
        return udpChannel;
    }

    /**
     * The {@link DatagramChannel} for this transport channel.
     *
     * @return {@link DatagramChannel} for this transport channel.
     */
    public DatagramChannel receiveDatagramChannel()
    {
        return receiveDatagramChannel;
    }

    /**
     * Send contents of {@link ByteBuffer} to connected address
     *
     * @param buffer to send
     * @return number of bytes sent
     */
    public int send(final ByteBuffer buffer)
    {
        logger.logFrameOut(buffer, connectAddress);

        int byteSent = 0;
        try
        {
            byteSent = sendDatagramChannel.write(buffer);
        }
        catch (final PortUnreachableException ex)
        {
            // ignore
        }
        catch (final IOException ex)
        {
            LangUtil.rethrowUnchecked(ex);
        }

        return byteSent;
    }

    /**
     * Send contents of {@link java.nio.ByteBuffer} to remote address
     *
     * @param buffer        to send
     * @param remoteAddress to send to
     * @return number of bytes sent
     */
    public int sendTo(final ByteBuffer buffer, final InetSocketAddress remoteAddress)
    {
        logger.logFrameOut(buffer, remoteAddress);

        int bytesSent = 0;
        try
        {
            bytesSent = sendDatagramChannel.send(buffer, remoteAddress);
        }
        catch (final IOException ex)
        {
            LangUtil.rethrowUnchecked(ex);
        }

        return bytesSent;
    }

    /**
     * Close transport, canceling any pending read operations and closing channel
     */
    public void close()
    {
        try
        {
            if (null != selectionKey)
            {
                selectionKey.cancel();
            }

            if (null != transportPoller)
            {
                transportPoller.cancelRead(this);
            }

            sendDatagramChannel.close();

            if (receiveDatagramChannel != sendDatagramChannel)
            {
                receiveDatagramChannel.close();
            }
        }
        catch (final Exception ex)
        {
            logger.logException(ex);
        }
    }

    /**
     * Is transport representing a multicast media or unicast
     *
     * @return if transport is multicast media
     */
    public boolean isMulticast()
    {
        return udpChannel.isMulticast();
    }

    /**
     * Return socket option value
     *
     * @param name of the socket option
     * @param <T>  type of option
     * @return option value
     */
    public <T> T getOption(final SocketOption<T> name)
    {
        T option = null;
        try
        {
            option = sendDatagramChannel.getOption(name);
        }
        catch (final IOException ex)
        {
            LangUtil.rethrowUnchecked(ex);
        }

        return option;
    }

    /**
     * Return the capacity of the {@link ByteBuffer} used for reception
     *
     * @return capacity of receiving byte buffer
     */
    public int receiveBufferCapacity()
    {
        return receiveByteBuffer.capacity();
    }

    /**
     * Attempt to receive waiting data.
     *
     * @return number of bytes received.
     */
    public abstract int pollForData();

    public final boolean isValidFrame(final UnsafeBuffer receiveBuffer, final int length)
    {
        boolean isFrameValid = true;

        if (frameVersion(receiveBuffer, 0) != HeaderFlyweight.CURRENT_VERSION)
        {
            logger.log(EventCode.INVALID_VERSION, receiveBuffer, 0, frameLength(receiveBuffer, 0));
            isFrameValid = false;
        }
        else if (length < HeaderFlyweight.HEADER_LENGTH)
        {
            logger.log(EventCode.MALFORMED_FRAME_LENGTH, receiveBuffer, 0, length);
            isFrameValid = false;
        }

        return isFrameValid;
    }

    protected final UnsafeBuffer receiveBuffer()
    {
        return receiveBuffer;
    }

    protected final ByteBuffer receiveByteBuffer()
    {
        return receiveByteBuffer;
    }

    protected final EventLogger logger()
    {
        return logger;
    }

    protected final LossGenerator lossGenerator()
    {
        return lossGenerator;
    }

    protected final InetSocketAddress receive()
    {
        receiveByteBuffer.clear();

        InetSocketAddress address = null;
        try
        {
            address = (InetSocketAddress)receiveDatagramChannel.receive(receiveByteBuffer);
        }
        catch (final PortUnreachableException | ClosedByInterruptException ignored)
        {
            // do nothing
        }
        catch (final Exception ex)
        {
            LangUtil.rethrowUnchecked(ex);
        }

        return address;
    }
}

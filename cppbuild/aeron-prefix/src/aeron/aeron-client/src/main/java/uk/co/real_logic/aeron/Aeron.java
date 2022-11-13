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
package uk.co.real_logic.aeron;

import uk.co.real_logic.aeron.exceptions.DriverTimeoutException;
import uk.co.real_logic.agrona.*;
import uk.co.real_logic.agrona.concurrent.*;
import uk.co.real_logic.agrona.concurrent.broadcast.BroadcastReceiver;
import uk.co.real_logic.agrona.concurrent.broadcast.CopyBroadcastReceiver;
import uk.co.real_logic.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import uk.co.real_logic.agrona.concurrent.ringbuffer.RingBuffer;

import java.nio.MappedByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static uk.co.real_logic.agrona.IoUtil.mapExistingFile;

/**
 * Aeron entry point for communicating to the Media Driver for creating {@link Publication}s and {@link Subscription}s.
 * Use an {@link Aeron.Context} to configure the Aeron object.
 * <p>
 * A client application requires only one Aeron object per Media Driver.
 */
public final class Aeron implements AutoCloseable
{
    /**
     * The Default handler for Aeron runtime exceptions.
     * When a {@link uk.co.real_logic.aeron.exceptions.DriverTimeoutException} is encountered, this handler will
     * exit the program.
     * <p>
     * The error handler can be overridden by supplying an {@link Aeron.Context} with a custom handler.
     *
     * @see Aeron.Context#errorHandler(ErrorHandler)
     */
    public static final ErrorHandler DEFAULT_ERROR_HANDLER =
        (throwable) ->
        {
            throwable.printStackTrace();
            if (throwable instanceof DriverTimeoutException)
            {
                System.err.printf("\n***\n*** Timeout from the Media Driver - is it currently running? Exiting.\n***\n");
                System.exit(-1);
            }
        };


    private static final long IDLE_SLEEP_NS = TimeUnit.MILLISECONDS.toNanos(4);

    private final ClientConductor conductor;
    private final AgentRunner conductorRunner;
    private final Context ctx;

    Aeron(final Context ctx)
    {
        ctx.conclude();
        this.ctx = ctx;

        conductor = new ClientConductor(
            ctx.epochClock,
            ctx.nanoClock,
            ctx.toClientBuffer,
            ctx.logBuffersFactory,
            ctx.counterValuesBuffer(),
            new DriverProxy(ctx.toDriverBuffer),
            ctx.errorHandler,
            ctx.newImageHandler,
            ctx.inactiveImageHandler,
            ctx.mediaDriverTimeout());

        conductorRunner = new AgentRunner(ctx.idleStrategy, ctx.errorHandler, null, conductor);
    }

    /**
     * Create an Aeron instance and connect to the media driver.
     * <p>
     * Threads required for interacting with the media driver are created and managed within the Aeron instance.
     *
     * @param ctx for configuration of the client.
     * @return the new {@link Aeron} instance connected to the Media Driver.
     */
    public static Aeron connect(final Context ctx)
    {
        return new Aeron(ctx).start();
    }

    /**
     * Clean up and release all Aeron internal resources and shutdown threads.
     */
    public void close()
    {
        conductorRunner.close();
        ctx.close();
    }

    /**
     * Add a {@link Publication} for publishing messages to subscribers.
     *
     * @param channel   for receiving the messages known to the media layer.
     * @param streamId  within the channel scope.
     * @return the new Publication.
     */
    public Publication addPublication(final String channel, final int streamId)
    {
        return conductor.addPublication(channel, streamId);
    }

    /**
     * Add a new {@link Subscription} for subscribing to messages from publishers.
     *
     * @param channel  for receiving the messages known to the media layer.
     * @param streamId within the channel scope.
     * @return the {@link Subscription} for the channel and streamId pair.
     */
    public Subscription addSubscription(final String channel, final int streamId)
    {
        return conductor.addSubscription(channel, streamId);
    }

    private Aeron start()
    {
        final Thread thread = new Thread(conductorRunner);
        thread.setName("aeron-client-conductor");
        thread.start();

        return this;
    }

    /**
     * This class provides configuration for the {@link Aeron} class via the {@link Aeron#connect(Aeron.Context)}
     * method and its overloads. It gives applications some control over the interactions with the Aeron Media Driver.
     * It can also set up error handling as well as application callbacks for image information from the
     * Media Driver.
     */
    public static class Context extends CommonContext
    {
        private final AtomicBoolean isClosed = new AtomicBoolean(false);
        private EpochClock epochClock;
        private NanoClock nanoClock;
        private IdleStrategy idleStrategy;
        private CopyBroadcastReceiver toClientBuffer;
        private RingBuffer toDriverBuffer;
        private MappedByteBuffer cncByteBuffer;
        private DirectBuffer cncMetaDataBuffer;
        private LogBuffersFactory logBuffersFactory;
        private ErrorHandler errorHandler;
        private NewImageHandler newImageHandler;
        private InactiveImageHandler inactiveImageHandler;

        /**
         * This is called automatically by {@link Aeron#connect(Aeron.Context)} and its overloads.
         * There is no need to call it from a client application. It is responsible for providing default
         * values for options that are not individually changed through field setters.
         *
         * @return this Aeron.Context for method chaining.
         */
        public Context conclude()
        {
            super.conclude();

            try
            {
                if (null == epochClock)
                {
                    epochClock = new SystemEpochClock();
                }

                if (null == nanoClock)
                {
                    nanoClock = new SystemNanoClock();
                }

                if (null == idleStrategy)
                {
                    idleStrategy = new SleepingIdleStrategy(IDLE_SLEEP_NS);
                }

                if (cncFile() != null)
                {
                    cncByteBuffer = mapExistingFile(cncFile(), CncFileDescriptor.CNC_FILE);
                    cncMetaDataBuffer = CncFileDescriptor.createMetaDataBuffer(cncByteBuffer);

                    final int cncVersion = cncMetaDataBuffer.getInt(CncFileDescriptor.cncVersionOffset(0));

                    if (CncFileDescriptor.CNC_VERSION != cncVersion)
                    {
                        throw new IllegalStateException("aeron cnc file version not understood: version=" + cncVersion);
                    }
                }

                if (null == toClientBuffer)
                {
                    final BroadcastReceiver receiver = new BroadcastReceiver(
                        CncFileDescriptor.createToClientsBuffer(cncByteBuffer, cncMetaDataBuffer));
                    toClientBuffer = new CopyBroadcastReceiver(receiver);
                }

                if (null == toDriverBuffer)
                {
                    toDriverBuffer = new ManyToOneRingBuffer(
                        CncFileDescriptor.createToDriverBuffer(cncByteBuffer, cncMetaDataBuffer));
                }

                if (counterLabelsBuffer() == null)
                {
                    counterLabelsBuffer(CncFileDescriptor.createCounterLabelsBuffer(cncByteBuffer, cncMetaDataBuffer));
                }

                if (counterValuesBuffer() == null)
                {
                    counterValuesBuffer(CncFileDescriptor.createCounterValuesBuffer(cncByteBuffer, cncMetaDataBuffer));
                }

                if (null == logBuffersFactory)
                {
                    logBuffersFactory = new MappedLogBuffersFactory();
                }

                if (null == errorHandler)
                {
                    errorHandler = DEFAULT_ERROR_HANDLER;
                }

                if (null == newImageHandler)
                {
                    newImageHandler = (image, channel, streamId, sessionId, joiningPosition, sourceIdentity) -> { };
                }

                if (null == inactiveImageHandler)
                {
                    inactiveImageHandler = (image, channel, streamId, sessionId, position) -> { };
                }
            }
            catch (final Exception ex)
            {
                System.err.printf("\n***\n*** Failed to connect to the Media Driver - is it currently running?\n***\n");

                throw new IllegalStateException("Could not initialise communication buffers", ex);
            }

            return this;
        }

        /**
         * Set the {@link EpochClock} to be used for tracking wall clock time when interacting with the driver.
         *
         * @param clock {@link EpochClock} to be used for tracking wall clock time when interacting with the driver.
         * @return this Aeron.Context for method chaining
         */
        public Context epochClock(final EpochClock clock)
        {
            this.epochClock = clock;
            return this;
        }

        /**
         * Set the {@link NanoClock} to be used for tracking high resolution time.
         *
         * @param clock {@link NanoClock} to be used for tracking high resolution time.
         * @return this Aeron.Context for method chaining
         */
        public Context nanoClock(final NanoClock clock)
        {
            this.nanoClock = clock;
            return this;
        }

        /**
         * Provides an IdleStrategy for the thread responsible for communicating with the Aeron Media Driver.
         *
         * @param idleStrategy Thread idle strategy for communication with the Media Driver.
         * @return this Aeron.Context for method chaining.
         */
        public Context idleStrategy(final IdleStrategy idleStrategy)
        {
            this.idleStrategy = idleStrategy;
            return this;
        }

        /**
         * This method is used for testing and debugging.
         *
         * @param toClientBuffer Injected CopyBroadcastReceiver
         * @return this Aeron.Context for method chaining.
         */
        public Context toClientBuffer(final CopyBroadcastReceiver toClientBuffer)
        {
            this.toClientBuffer = toClientBuffer;
            return this;
        }

        /**
         * This method is used for testing and debugging.
         *
         * @param toDriverBuffer Injected RingBuffer.
         * @return this Aeron.Context for method chaining.
         */
        public Context toDriverBuffer(final RingBuffer toDriverBuffer)
        {
            this.toDriverBuffer = toDriverBuffer;
            return this;
        }

        /**
         * This method is used for testing and debugging.
         *
         * @param logBuffersFactory Injected LogBuffersFactory
         * @return this Aeron.Context for method chaining.
         */
        public Context bufferManager(final LogBuffersFactory logBuffersFactory)
        {
            this.logBuffersFactory = logBuffersFactory;
            return this;
        }

        /**
         * Handle Aeron exceptions in a callback method. The default behavior is defined by
         * {@link Aeron#DEFAULT_ERROR_HANDLER}.
         *
         * @param errorHandler Method to handle objects of type Throwable.
         * @return this Aeron.Context for method chaining.
         * @see uk.co.real_logic.aeron.exceptions.DriverTimeoutException
         * @see uk.co.real_logic.aeron.exceptions.RegistrationException
         */
        public Context errorHandler(final ErrorHandler errorHandler)
        {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Set up a callback for when a new {@link Image} is created.
         *
         * @param handler Callback method for handling new connection notifications.
         * @return this Aeron.Context for method chaining.
         */
        public Context newImageHandler(final NewImageHandler handler)
        {
            this.newImageHandler = handler;
            return this;
        }

        /**
         * Set up a callback for when a {@link Image} determined to be inactive.
         *
         * @param handler Callback method for handling inactive image notifications.
         * @return this Aeron.Context for method chaining.
         */
        public Context inactiveImageHandler(final InactiveImageHandler handler)
        {
            this.inactiveImageHandler = handler;
            return this;
        }

        /**
         * Set the amount of time, in milliseconds, that this client will wait until it determines the
         * Media Driver is unavailable. When this happens a
         * {@link uk.co.real_logic.aeron.exceptions.DriverTimeoutException} will be generated for the error handler.
         *
         * @param value Number of milliseconds.
         * @return this Aeron.Context for method chaining.
         * @see #errorHandler(ErrorHandler)
         */
        public Context mediaDriverTimeout(final long value)
        {
            driverTimeoutMs(value);
            return this;
        }

        /**
         * Get the amount of time, in milliseconds, that this client will wait until it determines the
         * Media Driver is unavailable.
         *
         * @return Number of milliseconds.
         */
        public long mediaDriverTimeout()
        {
            return driverTimeoutMs();
        }

        /**
         * Clean up all resources that the client uses to communicate with the Media Driver.
         */
        public void close()
        {
            if (isClosed.compareAndSet(false, true))
            {
                IoUtil.unmap(cncByteBuffer);

                super.close();
            }
        }
    }
}

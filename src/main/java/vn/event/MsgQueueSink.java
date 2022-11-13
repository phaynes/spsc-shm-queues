package vn.event;

import java.io.IOException;

import org.agrona.concurrent.AtomicBuffer;

import vn.queue.CacheLine;
import vn.queue.QueueMemoryMappedBuffer;
import vn.queue.SpscMemoryMappedCacheLineQueue;
import vn.tech.CommonCommandLine;

/**
 * Acts as a Sink and test source for an IPC message Queue - the purpose of
 * which is to:
 *   a) Support Development and testing.
 *   b) Provide a mechanism to remove C generated events from the system prior
 *      to the balance of the system being commissioned.
 *
 * This should be run with -XX:+UseCondCardMark -XX:CompileThreshold=100000
 *
 */
public class MsgQueueSink {

    // Set the number of times to send a message for test..
    public static int REPETITIONS = 60000000;

    private static String APP_VERSION = "0.0.1";
    public static String[][] options = { { "m", "mode", "true", "Mode", "true"}, {"q", "queueDir", "true", "QueueDirectory", "false" } };

    public static String QUEUE_NAME = "/Volumes/ram-disk/queue.ipc";

    public static AtomicBuffer sharedBuffer;
    
    public static int outOfOrderErrors = 0;

    public static void main(String[] args) throws Exception, IOException {

        CommonCommandLine commandLine = new CommonCommandLine("MsgQueueSink", APP_VERSION);
        if (commandLine.setCommandLine(args, options) == false) return;

        String mode = commandLine.getOption("m");
        String queueDir = commandLine.getOption("q");
        if (queueDir != null)
        	QUEUE_NAME = queueDir + "queue.ipc";
        try (QueueMemoryMappedBuffer buffer = new QueueMemoryMappedBuffer(QUEUE_NAME)) {
            System.out.println("Queue size is " + buffer.getCapacity());

            buffer.mapProducerBuffer();

            final byte viewMask = mode.equals("source") ? SpscMemoryMappedCacheLineQueue.PRODUCER : SpscMemoryMappedCacheLineQueue.CONSUMER;
            SpscMemoryMappedCacheLineQueue queue = new SpscMemoryMappedCacheLineQueue(buffer, viewMask);
            sharedBuffer = queue.buffer();
            if (mode.equals("source")) {
              sharedBuffer.putIntOrdered(SpscMemoryMappedCacheLineQueue.SYNC_ADDRESS_POS, 0);
            }
            for (int run = 0; run < 10; run++) {
                if (mode.equals("source")) {
                    producerRun(queue, run);
                } else {
                    consumerRun(queue, run);
                }
            }
        }
    }

    /**
     * Synchronize the running of the process so test consumer and
     * producer are run together.
     * @param stateToWaitTill
     * @param nextState
     */
    private static void waitTillNextState(int stateToWaitTill, int nextState) {
        while (!sharedBuffer.compareAndSetInt(SpscMemoryMappedCacheLineQueue.SYNC_ADDRESS_POS,
                stateToWaitTill, nextState)) {
            Thread.yield();
        }
    }

    /**
     * Produce data
     * @param queue
     * @param runNumber
     * @throws Exception
     */
    private static void producerRun(final SpscMemoryMappedCacheLineQueue queue, int runNumber) throws Exception {

        waitTillNextState(3 * runNumber, 3 * runNumber+1);
        final long start = System.nanoTime();
        int i = REPETITIONS;
        int f = 0;
        CacheLine cacheLine = queue.newCacheLine();
        do {
            while (!queue.preOffer(cacheLine)) {
                Thread.yield();
                f++;
            }
            cacheLine.putInt(0, i);
            queue.offer();
        } while (0 != --i);

        waitTillNextState(3 * runNumber + 2, 3 * runNumber + 3);

        final long duration = System.nanoTime() - start;
        final long ops = (REPETITIONS * 1000L * 1000L * 1000L) / duration;
        System.out.format("%d - ops/sec=%,d - %s result=%d f=%d\n", Integer.valueOf(runNumber), Long.valueOf(ops), queue.getClass()
                .getSimpleName(), queue.size(),f);
    }

    /**
     * Consume data
     * @param queue
     * @param runNumber
     * @throws Exception
     */
    private static void consumerRun(final SpscMemoryMappedCacheLineQueue queue, int runNumber) throws Exception {

        waitTillNextState(3 * runNumber + 1, 3 * runNumber + 1);

        long start = System.currentTimeMillis();
        CacheLine result;
        int i = REPETITIONS;
        int expectedValue = REPETITIONS;
        int queueEmpty = 0;
        do {
            while (null == (result = queue.poll())) {
                queueEmpty++;
                Thread.yield();
            }
            int resInteger = result.getInt(0);
            if (expectedValue != resInteger) {
            	outOfOrderErrors++;
            	expectedValue = resInteger;
           }
            queue.releaseCacheLine();
            expectedValue--;
        } while (0 != --i);

        waitTillNextState(3 * runNumber + 1, 3 * runNumber + 2);

        long end = System.currentTimeMillis() - start;
        System.out.println("Run time = " + end + "  " + queueEmpty);
        System.out.println("Out Of Order Errors = " + outOfOrderErrors);
    }
}

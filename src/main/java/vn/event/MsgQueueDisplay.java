package vn.event;

import java.io.IOException;

import vn.queue.CacheLine;
import vn.queue.QueueMemoryMappedBuffer;
import vn.queue.SpscMemoryMappedCacheLineQueue;

/**
 * Displays the structure of the message queue (At least the first int anyway).
 */
public class MsgQueueDisplay {
    public static void main(String[] args) throws Exception, IOException {
        try (QueueMemoryMappedBuffer buffer = new QueueMemoryMappedBuffer(MsgQueueSink.QUEUE_NAME)) {
            System.out.println("Queue capacity is " + buffer.getCapacity());
            buffer.mapProducerBuffer();
            SpscMemoryMappedCacheLineQueue queue = new SpscMemoryMappedCacheLineQueue(buffer, SpscMemoryMappedCacheLineQueue.CONSUMER);
            System.out.println ("Current state          : " + queue.buffer().getInt(SpscMemoryMappedCacheLineQueue.SYNC_ADDRESS_POS));
            System.out.println("");
            System.out.println("Head position is        : " + queue.getHead());
            System.out.println("Head cache position is  : " + queue.getHeadCache());
            System.out.println("Tail position is        : " + queue.getTail());
            System.out.println("Tail cache is           : " + queue.getTailCache());
            System.out.println("Element count in queue  : " + queue.size());
            CacheLine result;
            for (int i=0; i < QueueMemoryMappedBuffer.DEFAULT_QUEUE_SIZE; i++) {
                result =  queue.peekPos(i);
                System.out.println("Pos " + i + " contains a " + result.getInt(0));
            }
        }
    }
}

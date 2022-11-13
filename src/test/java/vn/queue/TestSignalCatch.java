package vn.queue;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.*;

import sun.misc.Signal;
import vn.queue.QueueMemoryMappedBuffer;

public class TestSignalCatch {

    @Test
    public void testSignal() throws FileNotFoundException, IOException {
        QueueMemoryMappedBuffer buffer = new QueueMemoryMappedBuffer(SPSCMemoryMappedQueueTest.TEST_QUEUE_PATH);

        buffer.createAndLoadMappedFile();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Here 1");
            }
        });

        Signal.handle(new Signal("INT"), (signal) -> {
            System.out.println("here");
        });
        try {
            System.out.println(buffer.getBuffer().getInt(buffer.getFileSize() + 4096));
        } catch (java.lang.Exception ex) {
            System.out.println("Here" + ex);
        }
    }

    public static void signalHandler(sun.misc.SignalHandler arg) {
        System.out.println("Signal happened");
    }

}

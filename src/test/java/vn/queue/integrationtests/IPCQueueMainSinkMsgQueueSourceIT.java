package vn.queue.integrationtests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IPCQueueMainSinkMsgQueueSourceIT {
    // Determine System Type
    final static String os = System.getProperty("os.name").toLowerCase();
    final static boolean isWindows = (os.indexOf("win") >= 0);
    final static boolean isLinux = (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    final static boolean isMac = (os.indexOf("mac") >= 0);
    static String ramDiskQueueDir;

    Process msgSink;
    Process msgSource;

    @BeforeClass
    public static void setupConfiguration() throws Exception {
        if (isWindows)
            ramDiskQueueDir = "r:";
        else if (isMac)
            ramDiskQueueDir = "/Volumes/ram-disk/edb/";
        else if (isLinux)
            ramDiskQueueDir = "/dev/shm/tmx/edb/";
        else
            ramDiskQueueDir = "/var/run/tmx/edb/"; // Assume Solaris is running.
        if (!Paths.get(ramDiskQueueDir).toFile().exists()) {
            throw new Exception("Queue directory does not exist:" + ramDiskQueueDir);
        }
    }

    @Before
    public void startUpDaemons() throws IOException, URISyntaxException, InterruptedException {

    }

    @After
    public void shutdownDaemons() throws IOException {

        if (msgSink != null && msgSink.isAlive()) msgSink.destroy();
        if (msgSource != null && msgSource.isAlive()) msgSource.destroy();
    }

    @Test
    public void publisherSubscriberIntegrationTest() throws IOException, URISyntaxException, InterruptedException {

        // Start Msg Sink
        msgSink = MsgQueueSinkLauncher.launchMsgSink(ramDiskQueueDir, "sink");

        assertFalse("Msg Sink not alive", msgSink.waitFor(2l, TimeUnit.SECONDS));

        // Start Msg Source
        msgSource = IPCQueueMainLauncher.launchIPCQueueMain(ramDiskQueueDir, "source");

        // Wait for source to finish sending
        assertTrue("Msg Source finished", msgSource.waitFor(180l, TimeUnit.SECONDS));
        assertTrue("Msg Sink finished", msgSink.waitFor(1l, TimeUnit.SECONDS));

        assertThat("Sink error", msgSink.exitValue(), equalTo(0));
        assertThat("Source error", msgSource.exitValue(), equalTo(0));
    }
}

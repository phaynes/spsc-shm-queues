package vn.queue.integrationtests;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Launch an IPCQueueMain process
 *
 */
public class IPCQueueMainLauncher {
    public static Process launchIPCQueueMain(String queueFileDir, String role) throws URISyntaxException, IOException {

        ProcessBuilder processBuilder = new ProcessBuilder();
        // ./cppbuild/binaries/IPCQueueMain -p /Volumes/ram-disk/queue.ipc -m sink

        List<String> command = new ArrayList<>();
        command.add("./target/binaries/IPCQueueMain");
        command.add("-p");
        command.add(queueFileDir + "queue.ipc");
        command.add("-m");
        command.add(role);
        processBuilder.command(command);
        processBuilder.redirectOutput(Redirect.INHERIT);
        processBuilder.redirectError(Redirect.INHERIT);
        return processBuilder.start();
    }
}

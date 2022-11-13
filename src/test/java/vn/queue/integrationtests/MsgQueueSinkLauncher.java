package vn.queue.integrationtests;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Launch an MsgQueueSink process
 *
 */
public class MsgQueueSinkLauncher {
    public static Process launchMsgSink(String queueFileDir, String role) throws URISyntaxException, IOException {

    	ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add("target/spscsmqueue-8.0.0-SNAPSHOT-jar-with-dependencies.jar");
        command.add("-q");
        command.add(queueFileDir);
        command.add("-m");
        command.add(role);
        processBuilder.command(command);
        processBuilder.redirectOutput(Redirect.INHERIT);
        processBuilder.redirectError(Redirect.INHERIT);
        return processBuilder.start();
    }
}

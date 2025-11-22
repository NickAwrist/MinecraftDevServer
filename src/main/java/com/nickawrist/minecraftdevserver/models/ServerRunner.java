package com.nickawrist.minecraftdevserver.models;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ServerRunner {

    private static final Logger LOG = Logger.getInstance(ServerRunner.class);

    private Path serverDir;
    private Path jarPath;

    private int allocatedMemoryMB = 1024;

    private OSProcessHandler processHandler;

    public ServerRunner(Path serverDir, Path jarPath) {
        this.serverDir = serverDir;
        this.jarPath = jarPath;
    }

    public void startServer() {
        if (isRunning()) {
            LOG.warn("Server is already running.");
            return;
        }

        GeneralCommandLine commandLine = new GeneralCommandLine()
                .withWorkDirectory(serverDir.toFile())
                .withExePath("java")
                .withParameters(String.format("-Xmx%dm", allocatedMemoryMB))
                .withParameters(String.format("-Xms%dm", allocatedMemoryMB))
                .withParameters("-jar")
                .withParameters(jarPath.getFileName().toString())
                .withParameters("nogui");

        try {
            processHandler = new OSProcessHandler(commandLine);

            processHandler.addProcessListener(new ProcessAdapter() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event,@NotNull Key outputType) {
                    String text = event.getText().trim();
                    if (text.isEmpty()) return;

                    if (outputType == ProcessOutputTypes.STDERR) {
                        LOG.warn("[Server ERR] " + text);
                    } else {
                        LOG.info("[Server OUT] " + text);
                    }
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    LOG.info("Server process terminated with exit code " + event.getExitCode());
                    processHandler = null;
                }
            });

            processHandler.startNotify();

            LOG.info("Server started successfully.");
        } catch (Exception e) {
            LOG.error("Failed to start server.", e);
        }
    }

    public void stopServer() {
        if (!isRunning()) {
            LOG.warn("Server is not running.");
            return;
        }

        try {
            processHandler.getProcessInput().write("stop\n".getBytes(StandardCharsets.UTF_8));
            processHandler.getProcessInput().flush();

        } catch (Exception e) {
            LOG.warn("Failed to send stop command, forcing kill.", e);
            processHandler.destroyProcess();
        }
    }


    public boolean isRunning(){
        return processHandler != null && !processHandler.isProcessTerminated();
    }

}

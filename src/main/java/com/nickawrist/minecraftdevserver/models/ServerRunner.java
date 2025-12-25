package com.nickawrist.minecraftdevserver.models;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.diagnostic.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ServerRunner {

    private static final Logger LOG = Logger.getInstance(ServerRunner.class);

    private final Path serverDir;
    private final Path jarPath;

    private int allocatedMemoryMB;

    private OSProcessHandler processHandler;

    public ServerRunner(Path serverDir, Path jarPath, int allocatedMemoryMB) {
        this.serverDir = serverDir;
        this.jarPath = jarPath;
        this.allocatedMemoryMB = allocatedMemoryMB;
    }

    public void setAllocatedMemoryMB(int allocatedMemoryMB) {
        this.allocatedMemoryMB = allocatedMemoryMB;
    }

    public void startServer(ServerConsole serverConsole) {
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

            serverConsole.getConsoleView().attachToProcess(processHandler);

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

    public void sendCommand(String command) {
        if (!isRunning()) {
            LOG.warn("Cannot send command, server is not running.");
            return;
        }

        try {
            String cmd = command.endsWith("\n") ? command : command + "\n";
            processHandler.getProcessInput().write(cmd.getBytes(StandardCharsets.UTF_8));
            processHandler.getProcessInput().flush();
            LOG.info("Sent command: " + command);
        } catch (Exception e) {
            LOG.error("Failed to send command to server.", e);
        }
    }


    public boolean isRunning(){
        return processHandler != null && !processHandler.isProcessTerminated();
    }

    public Path getServerDir() {
        return serverDir;
    }

}

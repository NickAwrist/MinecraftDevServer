package com.nickawrist.minecraftdevserver.models;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.nickawrist.minecraftdevserver.constants.PluginConstants;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class ServerRunner {

    private static final Logger LOG = Logger.getInstance(ServerRunner.class);
    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("\u001B\\[[;\\d]*[A-Za-z]");

    private final Path serverDir;
    private final Path jarPath;

    private int allocatedMemoryMB;

    private ProcessHandler processHandler;
    private boolean usingColoredHandler;

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
            processHandler = createProcessHandler(commandLine);
            attachAnsiDetectionListener(processHandler);
            serverConsole.getConsoleView().attachToProcess(processHandler);

            processHandler.startNotify();
            LOG.info("Server started successfully.");
        } catch (Exception e) {
            LOG.error("Failed to start server.", e);
        }
    }

    private ProcessHandler createProcessHandler(GeneralCommandLine commandLine) throws Exception {
        try {
            ColoredProcessHandler handler = new ColoredProcessHandler(commandLine);
            usingColoredHandler = true;
            return handler;
        } catch (Exception e) {
            LOG.warn("ColoredProcessHandler unavailable, falling back to OSProcessHandler. " +
                    "Console colors may not render correctly.", e);
            usingColoredHandler = false;
            notifyConsoleDegraded("Could not initialise ANSI-aware console handler. " +
                    "Server output may not display correctly.");
            return new OSProcessHandler(commandLine);
        }
    }

    private void attachAnsiDetectionListener(ProcessHandler handler) {
        AtomicBoolean notified = new AtomicBoolean(false);

        handler.addProcessListener(new ProcessListener() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                if (notified.get()) return;

                String text = event.getText();
                if (text != null && ANSI_ESCAPE_PATTERN.matcher(text).find()) {
                    LOG.info("ANSI escape sequences detected in server output.");

                    if (!usingColoredHandler && notified.compareAndSet(false, true)) {
                        notifyConsoleDegraded(
                                "Server output contains ANSI color codes that cannot be rendered. " +
                                "The console display may appear corrupted or show unexpected colours.");
                    }
                }
            }
        });
    }

    private void notifyConsoleDegraded(String message) {
        try {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("MinecraftDevServer.Console")
                    .createNotification("Dev server console", message, NotificationType.WARNING)
                    .notify(PluginConstants.project);
        } catch (Exception e) {
            LOG.warn("Failed to show console notification.", e);
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

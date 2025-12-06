package com.nickawrist.minecraftdevserver.models;

import javax.swing.JComponent;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerInstance {

    public interface ServerStateListener {
        void onServerStateChanged(boolean isRunning);
    }

    private final UUID uuid;
    private String serverName;
    private final String serverVersion;
    private Path jarPath;

    private ServerRunner serverRunner;

    ServerConsole serverConsole;

    private final List<ServerStateListener> stateListeners = new CopyOnWriteArrayList<>();


    public ServerInstance(String serverName, String serverVersion) {
        this.uuid = UUID.randomUUID();
        this.serverName = serverName;
        this.serverVersion = serverVersion;
    }

    public ServerInstance(UUID uuid, String serverName, String serverVersion, Path jarPath) {
        this.uuid = uuid;
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.jarPath = jarPath;
    }

    public String getServerName() {
        return serverName;
    }
    public String getServerVersion() {
        return serverVersion;
    }
    public UUID getUuid() {
        return uuid;
    }
    public JComponent getServerConsoleComponent() {
        if (serverConsole == null) { return null;}
        return serverConsole.getComponent();
    }
    public Path getServerDirPath() {
        if (serverRunner == null) { return null;}
        return serverRunner.getServerDir();
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    public void createServerRunner(Path jarDir) {
        this.jarPath = jarDir;
        this.serverRunner = new ServerRunner(jarDir.getParent(), jarDir);
        this.serverConsole = new ServerConsole();
        this.serverConsole.setCommandHandler(this::sendCommand);
    }

    public Path getJarPath() {
        return jarPath;
    }

    public boolean hasServerRunner() {
        return this.serverRunner != null;
    }

    public void addServerStateListener(ServerStateListener listener) {
        stateListeners.add(listener);
    }

    public void removeServerStateListener(ServerStateListener listener) {
        stateListeners.remove(listener);
    }

    private void notifyServerStateChanged(boolean isRunning) {
        for (ServerStateListener listener : stateListeners) {
            listener.onServerStateChanged(isRunning);
        }
    }

    public void startServer() {
        if (serverRunner == null) { return;}
        serverRunner.startServer(serverConsole);
        notifyServerStateChanged(true);
    }
    public void stopServer() {
        if (serverRunner == null) { return;}

        serverRunner.stopServer();
        notifyServerStateChanged(false);
    }
    public boolean isServerRunning() {
        if (serverRunner == null) { return false;}

        return serverRunner.isRunning();
    }

    public void sendCommand(String command) {
        if (serverRunner == null) { return; }

        if (command.equalsIgnoreCase("stop")) {
            stopServer();
            return;
        }

        if (command.equalsIgnoreCase("start")) {
            startServer();
            return;
        }

        serverRunner.sendCommand(command);
    }



}

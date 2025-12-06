package com.nickawrist.minecraftdevserver.models;

import javax.swing.*;
import java.nio.file.Path;
import java.util.UUID;

public class ServerInstance {

    private final UUID uuid;
    private String serverName;
    private String serverVersion;
    private Path jarPath;

    private ServerRunner serverRunner;

    ServerConsole serverConsole;


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
    }

    public Path getJarPath() {
        return jarPath;
    }

    public boolean hasServerRunner() {
        return this.serverRunner != null;
    }

    public void startServer() {
        if (serverRunner == null) { return;}
        serverRunner.startServer(serverConsole);
    }
    public void stopServer() {
        if (serverRunner == null) { return;}

        serverRunner.stopServer();
    }
    public boolean isServerRunning() {
        if (serverRunner == null) { return false;}

        return serverRunner.isRunning();
    }



}

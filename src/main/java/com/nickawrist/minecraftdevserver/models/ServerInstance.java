package com.nickawrist.minecraftdevserver.models;

import javax.swing.*;
import java.nio.file.Path;
import java.util.UUID;

public class ServerInstance {

    private final UUID uuid;
    private String serverName;
    private String serverVersion;

    private ServerRunner serverRunner;

    ServerConsole serverConsole;


    public ServerInstance(String serverName, String serverVersion) {
        this.uuid = UUID.randomUUID();
        this.serverName = serverName;
        this.serverVersion = serverVersion;

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

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }
    public void createServerRunner(Path jarDir) {
        this.serverRunner = new ServerRunner(jarDir.getParent(), jarDir);
        this.serverConsole = new ServerConsole();
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

package com.nickawrist.minecraftdevserver.models;

import java.util.UUID;

public class ServerInstance {

    private final UUID uuid;
    private String serverName;
    private String serverVersion;

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

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }



}

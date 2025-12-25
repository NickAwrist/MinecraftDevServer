package com.nickawrist.minecraftdevserver.models;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

/**
 * Serializable state object for a server instance.
 * Used by IntelliJ's XmlSerializer for persistence.
 */
@Tag("server")
public class ServerState {

    @Attribute("uuid")
    public String uuid;

    @Attribute("name")
    public String serverName;

    @Attribute("version")
    public String serverVersion;

    @Attribute("jarPath")
    public String jarPath;

    @Attribute("allocatedMemoryMB")
    public int allocatedMemoryMB = 1024;

    // Required for XML serialization
    public ServerState() {
    }

    public ServerState(String uuid, String serverName, String serverVersion, String jarPath, int allocatedMemoryMB) {
        this.uuid = uuid;
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.jarPath = jarPath;
        this.allocatedMemoryMB = allocatedMemoryMB;
    }
}


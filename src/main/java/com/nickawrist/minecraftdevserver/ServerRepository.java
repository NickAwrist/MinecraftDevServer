package com.nickawrist.minecraftdevserver;

import com.nickawrist.minecraftdevserver.models.ServerInstance;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ServerRepository {

    private static final HashMap<UUID, ServerInstance> repo = new HashMap<UUID, ServerInstance>();

    public static ServerInstance getServer(UUID uuid) {
        return repo.get(uuid);
    }

    public static ServerInstance createServer(String serverName, String serverVersion) {
        ServerInstance serverInstance = new ServerInstance(serverName, serverVersion);
        repo.put(serverInstance.getUuid(), serverInstance);
        return serverInstance;
    }

    public static List<ServerInstance> getServers() {
        return repo.values().stream().toList();
    }

}

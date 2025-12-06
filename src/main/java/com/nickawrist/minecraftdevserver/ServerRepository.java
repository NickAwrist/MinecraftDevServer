package com.nickawrist.minecraftdevserver;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.nickawrist.minecraftdevserver.models.ServerInstance;
import com.nickawrist.minecraftdevserver.models.ServerState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@State(
        name = "MinecraftDevServerRepository",
        storages = @Storage("MinecraftDevServerRepository.xml")
)
public final class ServerRepository implements PersistentStateComponent<ServerRepository.State> {

    private static final Logger LOG = Logger.getInstance(ServerRepository.class);

    private final Map<UUID, ServerInstance> repo = new HashMap<>();

    public static ServerRepository getInstance() {
        return ApplicationManager.getApplication().getService(ServerRepository.class);
    }

    /**
     * State class for XML serialization
     */
    public static class State {
        public List<ServerState> servers = new ArrayList<>();
    }

    @Override
    public @Nullable State getState() {
        State state = new State();
        for (ServerInstance server : repo.values()) {
            String jarPathStr = server.getJarPath() != null ? server.getJarPath().toString() : null;
            state.servers.add(new ServerState(
                    server.getUuid().toString(),
                    server.getServerName(),
                    server.getServerVersion(),
                    jarPathStr
            ));
        }
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        repo.clear();
        for (ServerState serverState : state.servers) {
            try {
                UUID uuid = UUID.fromString(serverState.uuid);
                Path jarPath = serverState.jarPath != null ? Paths.get(serverState.jarPath) : null;

                // Skip servers whose jar files no longer exist
                if (jarPath != null && !Files.exists(jarPath)) {
                    LOG.warn("Server jar not found, skipping: " + jarPath);
                    continue;
                }

                ServerInstance server = new ServerInstance(
                        uuid,
                        serverState.serverName,
                        serverState.serverVersion,
                        jarPath
                );

                // Re-create the server runner if jar path exists
                if (jarPath != null) {
                    server.createServerRunner(jarPath);
                }

                repo.put(uuid, server);
                LOG.info("Loaded server: " + serverState.serverName);
            } catch (Exception e) {
                LOG.error("Failed to load server state: " + serverState.serverName, e);
            }
        }
    }

    public ServerInstance getServer(UUID uuid) {
        return repo.get(uuid);
    }

    public ServerInstance createServer(String serverName, String serverVersion) {
        ServerInstance serverInstance = new ServerInstance(serverName, serverVersion);
        repo.put(serverInstance.getUuid(), serverInstance);
        return serverInstance;
    }

    public List<ServerInstance> getServers() {
        return repo.values().stream().toList();
    }

}

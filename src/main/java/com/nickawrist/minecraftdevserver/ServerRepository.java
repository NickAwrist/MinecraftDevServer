package com.nickawrist.minecraftdevserver;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.nickawrist.minecraftdevserver.constants.PluginConstants;
import com.nickawrist.minecraftdevserver.models.ServerInstance;
import com.nickawrist.minecraftdevserver.models.ServerState;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@State(
        name = "MinecraftDevServerRepository",
        storages = @Storage("MinecraftDevServerRepository.xml")
)
public final class ServerRepository implements PersistentStateComponent<ServerRepository.State> {

    private static final Logger LOG = Logger.getInstance(ServerRepository.class);

    private final Map<UUID, ServerInstance> repo = new HashMap<>();
    private final List<RepositoryChangeListener> changeListeners = new CopyOnWriteArrayList<>();

    public interface RepositoryChangeListener {
        void onRepositoryChanged();
    }

    public void addChangeListener(RepositoryChangeListener listener) {
        changeListeners.add(listener);
    }

    private void notifyRepositoryChanged() {
        for (RepositoryChangeListener listener : changeListeners) {
            listener.onRepositoryChanged();
        }
    }

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
    public @NotNull State getState() {
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

    public ServerInstance createServer(String serverName, String serverVersion) {
        ServerInstance serverInstance = new ServerInstance(serverName, serverVersion);
        repo.put(serverInstance.getUuid(), serverInstance);
        notifyRepositoryChanged();
        return serverInstance;
    }

    public List<ServerInstance> getServers() {
        return repo.values().stream().toList();
    }

    public void removeServer(UUID uuid) {
        ServerInstance server = repo.get(uuid);
        if (server != null) {
            // Try to get the directory from the server runner first
            Path serverDir = server.getServerDirPath();

            // If no server runner, calculate the directory path based on server name
            if (serverDir == null) {
                String destination = PathManager.getSystemPath();
                Path destinationPath = Paths.get(destination, PluginConstants.PLUGIN_ID);
                serverDir = destinationPath.resolve(server.getServerName());
            }

            // Delete the server directory if it exists
            if (Files.exists(serverDir)) {
                try {
                    FileUtils.deleteDirectory(serverDir.toFile());
                    LOG.info("Deleted server directory: " + serverDir);
                } catch (IOException e) {
                    LOG.error("Failed to delete server directory: " + serverDir, e);
                }
            }
        }

        repo.remove(uuid);
        notifyRepositoryChanged();
    }

}

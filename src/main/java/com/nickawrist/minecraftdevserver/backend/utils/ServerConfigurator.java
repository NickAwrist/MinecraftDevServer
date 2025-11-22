package com.nickawrist.minecraftdevserver.backend.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.nickawrist.minecraftdevserver.backend.models.ServerPropertyChanges;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerConfigurator {

    private static final Logger LOG = Logger.getInstance(ServerConfigurator.class);

    private static final String EULA_RESOURCE_PATH = "/templates/eula.txt";
    private static final String SERVER_PROPERTIES_RESOURCE_PATH = "/templates/server.properties";

    public static void configureServer(Path serverDir, ServerPropertyChanges changes) {
        writeResourceToFile(serverDir.resolve("eula.txt"));
        writePropertiesFile(changes, serverDir.resolve("server.properties"));
    }

    private static void writeResourceToFile(Path targetPath) {
        LOG.info("Writing resource " + EULA_RESOURCE_PATH + " to " + targetPath);

        try(InputStream inputStream = ServerConfigurator.class.getResourceAsStream(EULA_RESOURCE_PATH)) {
            if (inputStream == null) {
                LOG.error("Resource not found: " + EULA_RESOURCE_PATH);
                throw new RuntimeException("Resource not found: " + EULA_RESOURCE_PATH);
            }
            Files.copy(inputStream, targetPath);
            LOG.info("Successfully wrote resource " + EULA_RESOURCE_PATH + " to " + targetPath);
        } catch (Exception e) {
            LOG.error("Failed to write resource " + EULA_RESOURCE_PATH + " to " + targetPath, e);
            throw new RuntimeException(e);
        }
    }

    private static void writePropertiesFile(ServerPropertyChanges changes, Path targetPath) {
        LOG.info("Writing server.properties to " + targetPath);

        try(InputStream inputStream = ServerConfigurator.class.getResourceAsStream(SERVER_PROPERTIES_RESOURCE_PATH)) {
            if (inputStream == null) {
                LOG.error("Resource not found: " + SERVER_PROPERTIES_RESOURCE_PATH);
                throw new RuntimeException("Resource not found: " + SERVER_PROPERTIES_RESOURCE_PATH);
            }

           String content = new String(inputStream.readAllBytes());
            content = content.replace("%SERVER_NAME%", changes.motd());
            content = content.replace("%SERVER_PORT%", String.valueOf(changes.port()));
            Files.write(targetPath, content.getBytes());

            LOG.info("Successfully wrote server.properties to " + targetPath);
        } catch (Exception e) {
            LOG.error("Failed to write server.properties to " + targetPath, e);
            throw new RuntimeException(e);
        }
    }

}

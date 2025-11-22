package com.nickawrist.minecraftdevserver.backend.utils;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.nickawrist.minecraftdevserver.backend.apis.PaperApi;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuild;
import com.nickawrist.minecraftdevserver.constants.PluginConstants;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarInstaller {

    private static final Logger LOG = Logger.getInstance(JarInstaller.class);

    private static Path getOrCreateServerDirectory(String serverName) {
        String destination = PathManager.getSystemPath();
        Path destinationPath = Paths.get(destination, PluginConstants.PLUGIN_ID);
        Path newDirectory = destinationPath.resolve(serverName);

        LOG.info("Creating directory: " + newDirectory);
        if (!Files.exists(newDirectory)) {
            try {
                Files.createDirectories(newDirectory);
            } catch (IOException e) {
                LOG.error("Failed to create directory: " + newDirectory, e);
            }
        }
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(newDirectory.toFile());
        LOG.info("Created directory: " + newDirectory);
        return newDirectory;
    }

    public static Path downloadPaperServer(String serverName, PaperBuild build, String version) {
        String downloadURL = PaperApi.getDownloadUrl(build, version);

        Path targetDirectory = getOrCreateServerDirectory(serverName);

        String filename = build.downloads().application().name();
        filename = filename.replace(".", "-");
        filename = filename.replace("-jar", ".jar");

        LOG.info("Downloading " + downloadURL + " to " + targetDirectory.resolve(filename).toFile());
        try(InputStream is = new URI(downloadURL).toURL().openStream()){
            FileUtils.copyInputStreamToFile(is, targetDirectory.resolve(filename).toFile());
        } catch (IOException | URISyntaxException e) {
            LOG.error("Failed to download Paper server jar from: " + downloadURL, e);
            throw new RuntimeException(e);
        }

        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetDirectory.toFile());
        LOG.info("Successfully downloaded Paper server jar from: " + downloadURL);
        return targetDirectory.resolve(filename);
    }

}

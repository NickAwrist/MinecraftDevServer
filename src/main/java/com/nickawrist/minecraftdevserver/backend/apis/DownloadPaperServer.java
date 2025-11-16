package com.nickawrist.minecraftdevserver.backend.apis;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuild;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadPaperServer {

	private static final Logger LOG = Logger.getInstance(DownloadPaperServer.class);
	private static final String BASE_DOWNLOAD_URL = "https://api.papermc.io/v2/projects/paper/versions/%s/builds/%d/downloads/%s";

	private static Path getOrCreateServerDirectory(String serverName, String rootDirectory) {
		Path serverDirectory = Paths.get(rootDirectory, serverName);
		if (!Files.exists(serverDirectory)) {
			try {
				Files.createDirectory(serverDirectory);
			} catch (IOException e) {
				LOG.error("Failed to create server directory: " + serverDirectory, e);
			}
		}
		LocalFileSystem.getInstance().refreshAndFindFileByIoFile(serverDirectory.toFile());
		return serverDirectory;
	}

	public static void downloadPaperServer(String serverName, PaperBuild build, String version, String destination) {
		String downloadURL = String.format(BASE_DOWNLOAD_URL, version, build.build(), build.downloads().application().name());

		Path targetDirectory = getOrCreateServerDirectory(serverName, destination);

		String filename = build.downloads().application().name();
		filename = filename.replace(".", "-");
		filename = filename.replace("-jar", ".jar");

		try(InputStream is = new URL(downloadURL).openStream()){
			FileUtils.copyInputStreamToFile(is, targetDirectory.resolve(filename).toFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetDirectory.toFile());
	}


}

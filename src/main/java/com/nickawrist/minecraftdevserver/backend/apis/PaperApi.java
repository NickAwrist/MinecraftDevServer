package com.nickawrist.minecraftdevserver.backend.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.nickawrist.minecraftdevserver.backend.models.PaperApiException;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuild;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuildsResponse;
import com.nickawrist.minecraftdevserver.backend.models.PaperVersions;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PaperApi {

    private static final Logger LOG = Logger.getInstance(PaperApi.class);

    private static final String BASE_URL = "https://api.papermc.io/v2/projects/paper";

    private static final String BUILDS_ENDPOINT = "/versions/%s/builds";
    private static final String DOWNLOAD_ENDPOINT = BUILDS_ENDPOINT + "/%d/downloads/%s";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Get all available Paper versions.
     * @return A PaperVersions object containing available versions.
     * @throws PaperApiException If the http request fails or parsing fails.
     */
    public static PaperVersions getPaperVersions() throws PaperApiException {
        HttpRequest request = buildHttpGetRequest(BASE_URL);

        try {
            HttpResponse<String> response = sendRequest(request);
            if (response.statusCode() != 200) {
                logAndThrow("Failed to fetch Paper versions: " + response.statusCode());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.body(), PaperVersions.class);
        } catch (IOException | InterruptedException e) {
            logAndThrow("Failed to fetch Paper versions: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get Paper builds for a specific version.
     * @param version The Minecraft version (e.g., "1.16.5").
     * @return An array of PaperBuild objects.
     * @throws PaperApiException If the http request fails or parsing fails.
     */
    public static PaperBuild[] getPaperBuilds(String version) throws PaperApiException {
        String buildsUrl = String.format(BASE_URL+BUILDS_ENDPOINT, version);
        HttpRequest request = buildHttpGetRequest(buildsUrl);

        try {
            HttpResponse<String> response = sendRequest(request);
            if (response.statusCode() != 200) {
               logAndThrow("Failed to fetch Paper builds: " + response.statusCode());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            PaperBuildsResponse buildsResponse = objectMapper.readValue(response.body(), PaperBuildsResponse.class);
            return buildsResponse.builds();
        } catch (IOException | InterruptedException e) {
            logAndThrow("Failed to fetch Paper builds: " + e.getMessage());
            return null;
        }
    }

    public static String getDownloadUrl(PaperBuild build, String version) throws PaperApiException {
        return String.format(BASE_URL+DOWNLOAD_ENDPOINT, version, build.build(), build.downloads().application().name());
    }

    private static HttpRequest buildHttpGetRequest(String url) {
        return HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
    }

    private static HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void logAndThrow(String message) throws PaperApiException {
        LOG.error(message);
        throw new PaperApiException(message);
    }

}

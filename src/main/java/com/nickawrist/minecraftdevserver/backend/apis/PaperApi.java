package com.nickawrist.minecraftdevserver.backend.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import com.nickawrist.minecraftdevserver.backend.models.PaperApiException;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuild;
import com.nickawrist.minecraftdevserver.backend.models.PaperVersions;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PaperApi {

    private static final Logger LOG = Logger.getInstance(PaperApi.class);

    private static final String BASE_URL = "https://fill.papermc.io/v3/projects/paper";
    private static final String BUILDS_ENDPOINT = "/versions/%s/builds";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Get all available Paper versions.
     * @return A PaperVersions object containing available versions.
     * @throws PaperApiException If the http request fails or parsing fails.
     */
    public static PaperVersions getPaperVersions(boolean includePrereleases)
            throws PaperApiException {
        HttpRequest request = buildHttpGetRequest(BASE_URL);

        try {
            HttpResponse<String> response = sendRequest(request);
            if (response.statusCode() != 200) {
                logAndThrow("Failed to fetch Paper versions: " + response.statusCode());
            }

            PaperVersions paperVersions =
                    MAPPER.readValue(response.body(), PaperVersions.class);

            Map<String, String[]> filteredVersions = new LinkedHashMap<>();
            paperVersions.versions().forEach((version, versionsArray) -> {
                String[] filtered = Arrays.stream(versionsArray)
                        .filter(v -> !v.contains("-rc"))
                        .filter(v -> includePrereleases || !v.contains("-pre"))
                        .toArray(String[]::new);
                filteredVersions.put(version, filtered);
            });

            return new PaperVersions(filteredVersions);
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

            PaperBuild[] builds = MAPPER.readValue(response.body(), PaperBuild[].class);
            Arrays.sort(builds, Comparator.comparingInt(PaperBuild::build));
            return builds;
        } catch (IOException | InterruptedException e) {
            logAndThrow("Failed to fetch Paper builds: " + e.getMessage());
            return null;
        }
    }

    public static String getDownloadUrl(PaperBuild build) throws PaperApiException {
        return build.downloads().serverDefault().url();
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

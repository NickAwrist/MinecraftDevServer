package com.nickawrist.minecraftdevserver.backend.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nickawrist.minecraftdevserver.backend.models.PaperVersions;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PaperVersionsBackend {

	private static final String URL = "https://api.papermc.io/v2/projects/paper";

	public static PaperVersions getPaperVersions() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).GET().build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new IOException("Failed to fetch Paper versions: " + response.statusCode());
		}

		ObjectMapper objectMapper = new ObjectMapper();

		return objectMapper.readValue(response.body(), PaperVersions.class);
	}

}

package com.nickawrist.minecraftdevserver.backend.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuild;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuildsResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PaperBuildsBackend {

	private static final String URL = "https://api.papermc.io/v2/projects/paper/versions/%s/builds";

	public static PaperBuild[] getPaperBuilds(String version) throws Exception {
		String buildsUrl = String.format(URL, version);
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(buildsUrl)).GET().build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.body());
		if (response.statusCode() != 200) {
			throw new Exception("Failed to fetch Paper builds: " + response.statusCode());
		}
		ObjectMapper objectMapper = new ObjectMapper();
		PaperBuildsResponse buildsResponse = objectMapper.readValue(response.body(), PaperBuildsResponse.class);
		return buildsResponse.builds();
	}

}

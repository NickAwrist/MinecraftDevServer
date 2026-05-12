package com.nickawrist.minecraftdevserver.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaperBuild(
				@JsonProperty("id") int build,
				String time,
				Downloads downloads
) {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Downloads(
					@JsonProperty("server:default") DownloadInfo serverDefault
	) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record DownloadInfo(
					String name,
					String url
	) {}
}
package com.nickawrist.minecraftdevserver.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaperVersions(Map<String, String[]> versions) {
}

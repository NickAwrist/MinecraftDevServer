package com.nickawrist.minecraftdevserver.backend.models;

public record PaperBuildsResponse(String project_id, String project_name, String version, PaperBuild[] builds) {
}


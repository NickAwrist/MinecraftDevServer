package com.nickawrist.minecraftdevserver.backend.models;

public record PaperVersions(String project_id, String project_name, String[] version_groups, String[] versions) {
}

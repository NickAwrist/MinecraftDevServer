package com.nickawrist.minecraftdevserver.backend.models;

public record PaperBuild(int build, String time, String channel, boolean promoted, Changes[] changes, Downloads downloads ) {
	public record Changes(String commit, String summary, String message ){}
	public record Downloads(Application application){}
	public record Application(String name, String sha256){}
}

package com.example.tunepal;

import java.io.Serializable;

public class AudioModel implements Serializable {
    private String path;
    private String title;
    private String artist; // New field for artist
    private String albumArt; // New field for album art (optional)
    private String duration;

    // Updated constructor to include artist and albumArt
    public AudioModel(String path, String title, String artist, String albumArt, String duration) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.albumArt = albumArt;
        this.duration = duration;
    }

    // Getters and setters for path, title, artist, albumArt, and duration
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}

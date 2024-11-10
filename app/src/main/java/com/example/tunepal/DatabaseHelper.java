package com.example.tunepal;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    // Mock databases for playlists
    private static List<String> likeSongs = new ArrayList<>();
    private static List<String> yourPlaylistSongs = new ArrayList<>();

    // Methods to add a song to a playlist
    public static void addLikeSong(String song) {
        likeSongs.add(song);
    }

    public static void addYourPlaylistSong(String song) {
        yourPlaylistSongs.add(song);
    }

    // Methods to retrieve songs from playlists
    public static List<String> getLikeSongs() {
        return likeSongs;
    }

    public static List<String> getYourPlaylistSongs() {
        return yourPlaylistSongs;
    }
}

package com.example.tunepal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {

    private ListView listView;
    private List<String> playlistSongs; // Holds the song list for the playlist

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        // Retrieve the playlist name and audio paths from the intent
        String playlistName = getIntent().getStringExtra("PlaylistName");
        List<String> audioPaths = getIntent().getStringArrayListExtra("AudioPaths_list");

        if (audioPaths == null) {
            audioPaths = new ArrayList<>();
        }

        loadPlaylistSongs(playlistName, audioPaths);

        listView = findViewById(R.id.list_view);
        if (playlistSongs != null && !playlistSongs.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlistSongs);
            listView.setAdapter(adapter);

            // Set up item click listener for playing a song
            listView.setOnItemClickListener((parent, view, position, id) -> {
                // Start AudioPlayer activity with the selected song and full playlist
                Intent intent = new Intent(PlaylistActivity.this, AudioPlayer.class);
                intent.putExtra("AudioPaths_list", new ArrayList<>(playlistSongs)); // pass the playlist
                intent.putExtra("SelectedPosition", position); // pass the selected song's position
                startActivity(intent);
            });
        } else {
            Toast.makeText(this, "No songs available in this playlist.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPlaylistSongs(String playlistName, List<String> audioPaths) {
        if ("Liked Song".equals(playlistName)) {
            playlistSongs = DatabaseHelper.getLikeSongs();
        } else if ("Your Playlist".equals(playlistName)) {
            playlistSongs = DatabaseHelper.getYourPlaylistSongs();
        } else {
            playlistSongs = audioPaths;
        }
    }
}

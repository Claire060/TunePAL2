package com.example.tunepal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noMusicTextView;
    ArrayList<AudioModel> songsList = new ArrayList<>();
    ArrayList<String> audioPaths = new ArrayList<>();
    DrawerLayout layDL;
    NavigationView vNV;
    Toolbar toolbar;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layDL = findViewById(R.id.drawer_layout);
        vNV = findViewById(R.id.nav_view);
        View headerView = vNV.getHeaderView(0);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Initialize Toolbar
        setSupportActionBar(toolbar);

        TextView userNameTextView = headerView.findViewById(R.id.tv_user_name);
        TextView userEmailTextView = headerView.findViewById(R.id.tv_user_email);
        ImageView userAvatarImageView = headerView.findViewById(R.id.img_user_avatar);

        // Get current user from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmailTextView.setText(currentUser.getEmail());
            userNameTextView.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "No name set");
            // Optionally, set user avatar if available
        } else {
            userEmailTextView.setText("guest@example.com");
            userNameTextView.setText("Guest");
        }

        // Initialize DrawerLayout and set up the toggle button
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            vNV.setCheckedItem(R.id.nav_view);
        }
        NavClick();

        // Initialize RecyclerView and No Music TextView
        recyclerView = findViewById(R.id.rv_music_list);
        noMusicTextView = findViewById(R.id.no_song_text);

        // Initialize search bar
        EditText etSearch = findViewById(R.id.et_search); // Assuming et_search is the ID of your search bar
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSongs(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // No action needed after text changes
            }
        });

        // Check for Permissions and load music data
        if (!checkPermission()) {
            requestPermission();
            return;
        }

        loadMusicData();
    }

    private void NavClick() {
        vNV.setNavigationItemSelectedListener(item -> {
            Fragment frag = null;
            int val = 0;

            if (item.getItemId() == R.id.nav_profile) {
                val = 1;
            } else if (item.getItemId() == R.id.nav_home) {
                val = 2;
            } else if (item.getItemId() == R.id.nav_liked_songs) {
                val = 3;
            } else if (item.getItemId() == R.id.nav_playlist) {
                val = 4;
            }

            switch (val) {
                case 1:
                    Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(this, "Liked Song", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(MainActivity.this, PlaylistActivity.class);
                    intent1.putStringArrayListExtra("AudioPaths_list", audioPaths); // Pass audioPaths list
                    intent1.putExtra("PlaylistName", "Liked Song"); // Indicate the playlist name
                    startActivity(intent1);
                    break;

                case 4:
                    Toast.makeText(this, "Your Playlist", Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(MainActivity.this, PlaylistActivity.class);
                    intent2.putStringArrayListExtra("AudioPaths_list", audioPaths); // Pass audioPaths list
                    intent2.putExtra("PlaylistName", "Your Playlist"); // Indicate the playlist name
                    startActivity(intent2);
                    break;

            }
            layDL.closeDrawer(GravityCompat.START);
            return true;
        });

        View headerView = vNV.getHeaderView(0);
        Button loginButton = headerView.findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });

    }



    private void loadMusicData() {
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, // Add artist to projection
                MediaStore.Audio.Media.ALBUM_ID, // Add album ID to get album art later
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Fetch album art using the album ID (optional)
                String albumId = cursor.getString(2);
                String albumArt = getAlbumArt(albumId);

                AudioModel songData = new AudioModel(
                        cursor.getString(3), // Path
                        cursor.getString(0), // Title
                        cursor.getString(1), // Artist
                        albumArt,            // Album art
                        cursor.getString(4)  // Duration
                );

                if (new File(songData.getPath()).exists()) {
                    songsList.add(songData);
                    audioPaths.add(songData.getPath());
                }
            }
            cursor.close();
        }

        if (songsList.isEmpty()) {
            noMusicTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MusicListAdapter(getApplicationContext(), songsList));
        }
    }

    // Method to fetch album art from album ID (optional)
    private String getAlbumArt(String albumId) {
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{albumId},
                null
        );

        String albumArt = null;
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            if (columnIndex != -1) {
                albumArt = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return albumArt;
    }

    private boolean checkPermission() {
        // Check for Android 13 and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ we need to request READ_MEDIA_AUDIO for music files
            int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_AUDIO);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            // For Android 6 - 12, request READ_EXTERNAL_STORAGE
            int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        // For Android 13+ we request READ_MEDIA_AUDIO
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 123);
        } else {
            // For Android 6 - 12 we request READ_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMusicData(); // Reload music data after permission is granted
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to filter the list based on the search query
    private void filterSongs(String query) {
        ArrayList<AudioModel> filteredList = new ArrayList<>();

        for (AudioModel song : songsList) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(song);
            }
        }

        if (filteredList.isEmpty()) {
            noMusicTextView.setVisibility(View.VISIBLE);
        } else {
            noMusicTextView.setVisibility(View.GONE);
        }

        // Update the RecyclerView with the filtered list
        MusicListAdapter adapter = new MusicListAdapter(getApplicationContext(), filteredList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

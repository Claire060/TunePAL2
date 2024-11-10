package com.example.tunepal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AudioPlayer extends AppCompatActivity {
    private static final String TAG = "AudioPlayer";

    private List<String> audioPaths;
    private int currentPosition;
    private boolean isRepeat = false;
    private boolean isShuffle = false;

    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;

    private TextView playerpos, playerdur;
    private SeekBar seekBar;
    private ImageView rewind, play, pause, forward, play_next, play_previous, repeat, shuffle, addPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        // Retrieve the list of audio paths and selected position from the intent
        audioPaths = getIntent().getStringArrayListExtra("AudioPaths_list");
        currentPosition = getIntent().getIntExtra("SelectedPosition", 0);

        if (audioPaths == null || audioPaths.isEmpty()) {
            Toast.makeText(this, "No audio files to play", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Audio paths list is null or empty.");
            finish(); // End the activity if there's no audio to play
            return;
        }

        initializeViews();
        setupMediaPlayer();
        setupSeekBar();
        setupClickListeners();

        TextView titleTextView = findViewById(R.id.music_name);
        titleTextView.setSelected(true); // Set the TextView as selected for marquee
        playAudio();
    }

    private void initializeViews() {
        playerpos = findViewById(R.id.playerPosition);
        playerdur = findViewById(R.id.playerDuration);
        seekBar = findViewById(R.id.seek_bar);
        rewind = findViewById(R.id.btn_rew);
        pause = findViewById(R.id.btn_pause);
        play = findViewById(R.id.btn_ply);
        forward = findViewById(R.id.btn_frwd);
        play_next = findViewById(R.id.btn_next);
        play_previous = findViewById(R.id.btn_previous);
        repeat = findViewById(R.id.btn_repeat);
        shuffle = findViewById(R.id.btn_shuffle);
        addPlaylist = findViewById(R.id.btn_add_playlist);
    }

    private void setupMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        initializeMediaPlayer(audioPaths.get(currentPosition));

        mediaPlayer.setOnCompletionListener(mp -> {
            pause.setVisibility(View.GONE);
            play.setVisibility(View.VISIBLE);
            if (isRepeat) {
                mediaPlayer.seekTo(0);
                playAudio();
            } else {
                playNextAudio();
            }
        });
        int duration = mediaPlayer.getDuration();
        playerdur.setText(convertFormat(duration));
    }

    private void setupSeekBar() {
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    playerpos.setText(convertFormat(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        handler = new Handler();
        runnable = () -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                playerpos.setText(convertFormat(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(runnable, 500);
            }
        };
    }

    private void setupClickListeners() {
        play.setOnClickListener(view -> {
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            playAudio();
        });

        pause.setOnClickListener(view -> {
            pause.setVisibility(View.GONE);
            play.setVisibility(View.VISIBLE);
            mediaPlayer.pause();
            handler.removeCallbacks(runnable);
        });

        forward.setOnClickListener(view -> {
            int currentPos = mediaPlayer.getCurrentPosition();
            if (mediaPlayer.isPlaying() && currentPos + 5000 < mediaPlayer.getDuration()) {
                mediaPlayer.seekTo(currentPos + 5000);
                playerpos.setText(convertFormat(mediaPlayer.getCurrentPosition()));
            }
        });

        rewind.setOnClickListener(view -> {
            int currentPos = mediaPlayer.getCurrentPosition();
            if (mediaPlayer.isPlaying() && currentPos > 5000) {
                mediaPlayer.seekTo(currentPos - 5000);
                playerpos.setText(convertFormat(mediaPlayer.getCurrentPosition()));
            }
        });

        play_next.setOnClickListener(view -> playNextAudio());
        play_previous.setOnClickListener(view -> playPreviousAudio());

        repeat.setOnClickListener(view -> {
            isRepeat = !isRepeat;
            repeat.setImageResource(isRepeat ? R.drawable.repeat_on : R.drawable.repeat);
            Toast.makeText(AudioPlayer.this, isRepeat ? "Repeat ON" : "Repeat OFF", Toast.LENGTH_SHORT).show();
        });

        shuffle.setOnClickListener(view -> {
            isShuffle = !isShuffle;
            shuffle.setImageResource(isShuffle ? R.drawable.shuffle_on : R.drawable.shuffle);
            Toast.makeText(AudioPlayer.this, isShuffle ? "Shuffle ON" : "Shuffle OFF", Toast.LENGTH_SHORT).show();
        });

        addPlaylist.setOnClickListener(view -> {
            String[] playlistOptions = {"Like Songs", "Your Playlist"};
            new AlertDialog.Builder(this)
                    .setTitle("Add to Playlist")
                    .setItems(playlistOptions, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                addToPlaylist(currentPosition, "Like Songs");
                                break;
                            case 1:
                                addToPlaylist(currentPosition, "Your Playlist");
                                break;
                        }
                    })
                    .show();
        });
    }

    private void initializeMediaPlayer(String path) {
        File audioFile = new File(path);
        if (!audioFile.exists()) {
            Toast.makeText(this, "Audio file does not exist!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "File not found: " + path);
            return;
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();

            String songTitle = audioFile.getName();
            String artistName = "Unknown Artist";
            TextView titleTextView = findViewById(R.id.music_name);
            TextView artistTextView = findViewById(R.id.artist_name);
            titleTextView.setText(songTitle);
            artistTextView.setText(artistName);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing MediaPlayer: " + e.getMessage());
            Toast.makeText(this, "Cannot play this audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void playAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration());
            handler.postDelayed(runnable, 0);
        }
    }

    private void playNextAudio() {
        if (currentPosition < audioPaths.size() - 1) {
            currentPosition++;
            stopAudio();
            initializeMediaPlayer(audioPaths.get(currentPosition));
            playAudio();
            updateUI();
        } else {
            Toast.makeText(this, "No more songs in the playlist", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        seekBar.setMax(mediaPlayer.getDuration());
        playerdur.setText(convertFormat(mediaPlayer.getDuration()));
        handler.postDelayed(runnable, 0);
    }

    private void playPreviousAudio() {
        if (currentPosition > 0) {
            currentPosition--;
            stopAudio();
            initializeMediaPlayer(audioPaths.get(currentPosition));
            playAudio();
            updateUI();
        } else {
            Toast.makeText(this, "Already at the beginning of the playlist", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            handler.removeCallbacks(runnable);
        }
    }

    private void addToPlaylist(int audioIndex, String playlistName) {
        String audioPath = audioPaths.get(audioIndex);
        boolean success = false;

        if ("Like Songs".equals(playlistName)) {
            DatabaseHelper.addLikeSong(audioPath);
            success = DatabaseHelper.getLikeSongs().contains(audioPath); // Verify addition
        } else if ("Your Playlist".equals(playlistName)) {
            DatabaseHelper.addYourPlaylistSong(audioPath);
            success = DatabaseHelper.getYourPlaylistSongs().contains(audioPath); // Verify addition
        }

        if (success) {
            Toast.makeText(this, "Added to " + playlistName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add to " + playlistName, Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("DefaultLocale")
    private String convertFormat(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onBackPressed();
    }
}

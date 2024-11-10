package com.example.tunepal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.AudioViewHolder> {

    List<AudioModel> audios = new ArrayList<>();
    Context context;

    public MusicListAdapter(Context context, List<AudioModel> audios) {
        this.context = context;
        this.audios = audios;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, @SuppressLint("RecyclerView") int position) {
        AudioModel audioModel = audios.get(position);

        // Set the title and artist name
        holder.titleText.setText(audioModel.getTitle());
        holder.artistText.setText(audioModel.getArtist()); // Assuming artist info exists in AudioModel

        // If you have album art, you can load it here (optional)
        // Example: Glide.with(context).load(audioModel.getAlbumArtPath()).into(holder.albumArt);

        ArrayList<String> audioPaths = new ArrayList<>();
        for (AudioModel audio : audios) {
            audioPaths.add(audio.getPath());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AudioPlayer.class);
                // Pass the list of audio paths through the intent
                intent.putStringArrayListExtra("AudioPaths_list", audioPaths);
                // Pass the selected position to start playing from that position
                intent.putExtra("SelectedPosition", position);
                // Add FLAG_ACTIVITY_NEW_TASK to handle context not being an Activity
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return audios.size();
    }

    public void updateList(List<AudioModel> newList) {
        audios.clear();
        audios.addAll(newList);
        notifyDataSetChanged(); // Notify RecyclerView that data has changed
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, artistText;
        ImageView albumArt;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tv_music_title);
            artistText = itemView.findViewById(R.id.tv_artist_name);
            albumArt = itemView.findViewById(R.id.img_album_art);
        }
    }
}

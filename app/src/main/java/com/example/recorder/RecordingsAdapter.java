package com.example.recorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.ViewHolder> {

    private Context context;
    ArrayList<RecordInfo> recordings = new ArrayList<>();

    public RecordingsAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recording_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.duration.setText(recordings.get(position).size);
        holder.name.setText(recordings.get(position).name);
        holder.name.setSelected(true);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerMedia media = new PlayerMedia(context);
                media.setpath(recordings.get(position).path);
                media.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,duration;
        LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            name = itemView.findViewById(R.id.recording_name);
            duration  = itemView.findViewById(R.id.duration);
        }
    }

    public void setRecordings(ArrayList<RecordInfo> recordings) {
        this.recordings = recordings;
        notifyDataSetChanged();
    }
}

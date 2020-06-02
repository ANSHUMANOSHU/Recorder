package com.example.recorder;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import java.io.IOException;

public class PlayerMedia extends AlertDialog.Builder implements DialogInterface.OnDismissListener {

    private static MediaPlayer mediaPlayer;
    private View view;
    private String path;
    private Context context;
    private boolean isPlaying = true;
    private int current,total;


    public PlayerMedia(@NonNull Context context) {
        super(context);
        this.context = context;

    }

    private void initialize() {
        mediaPlayer  = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    attachAllListeners();
                    mp.start();
                    current = 0;
                    total = mp.getDuration();
                    isPlaying = true;

                }
            });





        } catch (IOException e) {}
    }

    private void attachAllListeners() {
        final ImageView playPause = view.findViewById(R.id.pause_play);
        final TextView starts = view.findViewById(R.id.current_position);
        TextView ends = view.findViewById(R.id.total_length);
        TextView record_name =  view.findViewById(R.id.details);
        final SeekBar seekBar = view.findViewById(R.id.seek);

        record_name.setText(path.split("/")[path.split("/").length-1]);
        record_name.setSelected(true);
        playPause.setImageResource(R.drawable.pause);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    playPause.setImageResource(R.drawable.play);
                    mediaPlayer.pause();
                    isPlaying = false;
                }else {
                    playPause.setImageResource(R.drawable.pause);
                    mediaPlayer.start();
                    isPlaying  = true;
                }
            }
        });
        ends.setText(convertDuration(mediaPlayer.getDuration()+""));
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(0);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while (isPlaying || current<total ){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        current = mediaPlayer.getCurrentPosition();
                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                starts.setText(convertDuration(mediaPlayer.getCurrentPosition() + ""));
                            }
                        });

                    }
                }catch (IllegalStateException e){}
            }
        });
        thread.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playPause.setImageResource(R.drawable.play);
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mediaPlayer.stop();
        mediaPlayer.release();
        isPlaying = false;
        current = total;
        dialog.dismiss();
    }

    public void setpath(String path){
        this.path = path;
        view = LayoutInflater.from(context).inflate(R.layout.media_layout,null,false);
        setView(view);
        initialize();
        setOnDismissListener(this);
    }


    private String convertDuration(String extractMetadata) {
        long duration = Long.parseLong(extractMetadata);
        int temp = (int) (duration / 1000);
        int minutes = temp / 60;
        int seconds = temp % 60;

        return String.format("%02d : %02d", minutes, seconds);
    }

}

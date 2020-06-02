package com.example.recorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    private ImageView recordBtn;
    private Chronometer chronometer;
    private MediaRecorder recorder;
    private RecyclerView recyclerView;
    private RecordingsAdapter adapter;
    private ArrayList<RecordInfo> recordings;
    private static boolean isRecording = false;
    public static final int RQ =1;
    String[] permissions ={Manifest.permission.READ_EXTERNAL_STORAGE
            ,Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //permissions check
        if(!chechforPermission()){
            requestPermission();

        }

        //view Initialized
        initViews();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,permissions,RQ);
    }

    private boolean chechforPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void initViews() {
        recordBtn = findViewById(R.id.recordbtn);
        chronometer = findViewById(R.id.meter);
        recyclerView = findViewById(R.id.recordingsRecyclerView);
        adapter = new RecordingsAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        fetchAllRecordings();
    }


    public void startStopRecording(View view) {
        if (!isRecording) {
            readyMediaRecorder();
            try {
                view.setBackground(getResources().getDrawable(R.drawable.background_red));
                recyclerView.setBackground(getResources().getDrawable(R.drawable.changable));
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                isRecording = true;
                recorder.prepare();
                recorder.start();
                Toast.makeText(this, "Recording Audio", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } else {
            view.setBackground(getResources().getDrawable(R.drawable.background_green));
            recyclerView.setBackground(getResources().getDrawable(R.drawable.appicon));
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.stop();
            recorder.stop();
            recorder.reset();
            recorder.release();
            Toast.makeText(this, "Recorded and saved", Toast.LENGTH_SHORT).show();
            isRecording = false;
            fetchAllRecordings();

        }

    }

    private void readyMediaRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/MyRecorder");
        if (!path.exists())
            path.mkdirs();
        path = new File(path.toString()+ "/"+new Date().getTime() + ".mp3");
        recorder.setOutputFile(path.getAbsolutePath());
    }

    private void fetchAllRecordings() {
        String name = "";
        recordings = new ArrayList<>();
        File fileManagerlocation = Environment.getExternalStorageDirectory();
        File dir = new File(fileManagerlocation.getAbsolutePath() + "/MyRecorder/");

        if (dir.exists()) {
            if (dir.listFiles() != null) {
                for (File f : Objects.requireNonNull(dir.listFiles())) {
                    if (f.isFile()) {
                        name = f.getName();
                    }
                    if (name.contains(".mp3") || name.contains(".MP3")) {

                        //create new media object
                        RecordInfo recordInfo = new RecordInfo();
                        recordInfo.name = name;
                        recordInfo.path = f.getAbsolutePath();
                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(f.getAbsolutePath());
                        recordInfo.size = convertDuration(mediaMetadataRetriever
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                        //add to list
                        recordings.add(recordInfo);
                    }
                }
                adapter.setRecordings(recordings);
            }
        }
    }

    private String convertDuration(String extractMetadata) {
        long duration = Long.parseLong(extractMetadata);
        int temp = (int) (duration / 1000);
        int minutes = temp / 60;
        int seconds = temp % 60;

        return String.format("%02d : %02d", minutes, seconds);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RQ){
            if( !(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            &&grantResults[2] == PackageManager.PERMISSION_GRANTED)){

                Toast.makeText(this, "Permission is Required ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}

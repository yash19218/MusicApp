package com.example.beatboxx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayingMusicActivity extends AppCompatActivity {

    Button btnPlay , btnNext , btnPrevious, btnFastForward , btnFastBackward;
    TextView txtSongName , txtSongStart,txtSongEnd;
    SeekBar seekMusicBar;
    BarVisualizer barVisualizer;
    ImageView imageView;
    String songName;
    public static final  String Extra_Name = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(barVisualizer != null){
            barVisualizer.release();
        }
        super.onDestroy();
    }

    Thread updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_music);

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.themeaction));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater =(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_banner,null);
        actionBar.setCustomView(view);

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnFastForward = findViewById(R.id.btnFastForward);
        btnFastBackward = findViewById(R.id.btnFastBackward);

        txtSongName = findViewById(R.id.txtSong);
        txtSongEnd = findViewById(R.id.txtSongEnd);
        txtSongStart = findViewById(R.id.txtSongStart);

        seekMusicBar  = findViewById(R.id.seekBar);

        imageView  = findViewById(R.id.imgView);

        barVisualizer = findViewById(R.id.wave);

        if(mediaPlayer != null){
            mediaPlayer.start();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String sName = intent.getStringExtra("songname");
        position = bundle.getInt("pos",0);
        txtSongName.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });

        seekBarAnimation();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    btnPlay.setBackgroundResource(R.drawable.vi_play);
                    mediaPlayer.pause();
                }else{
                    btnPlay.setBackgroundResource(R.drawable.vi_pause);
                    mediaPlayer.start();

                    TranslateAnimation moveDisc = new TranslateAnimation(-25,25,-25,25);
                    moveDisc.setInterpolator(new AccelerateInterpolator());
                    moveDisc.setDuration(600);
                    moveDisc.setFillEnabled(true);
                    moveDisc.setFillAfter(true);
                    moveDisc.setRepeatMode(Animation.REVERSE);
                    moveDisc.setRepeatCount(1);
                    imageView.startAnimation(moveDisc);
                }
            }
        });


        setVisualizer();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri uri = Uri.parse(mySongs.get(position).toString());
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);

                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                mediaPlayer.start();

                startAnimation(imageView,360f);
                seekBarAnimation();
                setVisualizer();
            }
        });



        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position<1)?mySongs.size()-1:position-1;//pos == 0!
                Uri uri = Uri.parse(mySongs.get(position).toString());
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);

                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                mediaPlayer.start();
                startAnimation(imageView,-360f);
                setVisualizer();
            }
        });

        btnFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        btnFastBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });

    }

    public void startAnimation(View view , Float degree){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView,"rotation",0f,degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String t="";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        t+=min+":";
        //append 0 for making two-digit number!
        t+=(sec<10)?"0"+sec:sec+"";
        return t;
    }

    public void  setVisualizer(){
        int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId != -1){
            barVisualizer.setAudioSessionId(audioSessionId);
        }
    }

    public void seekBarAnimation(){
        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currPos=0;
                while(currPos<totalDuration){
                    try {
                        sleep(500);
                        currPos = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(currPos);
                    }catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        seekMusicBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_700), PorterDuff.Mode.MULTIPLY);
        seekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_700),PorterDuff.Mode.SRC_IN);

        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);

        final Handler handler = new Handler();
        final int delay=1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currTime = createTime(mediaPlayer.getCurrentPosition());
                if(currTime.equals(endTime)||(mediaPlayer.getCurrentPosition()>=mediaPlayer.getDuration())){
                    btnNext.performClick();
                    seekMusicBar.setProgress(0);
                    handler.postDelayed(this,delay);
                }
                txtSongStart.setText(currTime);
                handler.postDelayed(this,delay);
            }
        },delay);
    }

}
package com.musicplayer.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.service.PlayService;
import com.musicplayer.ui.activity.PlayActivity;
import com.musicplayer.utils.StaticVariate;

import static android.content.Context.MODE_PRIVATE;

public class PlayBarLayout extends LinearLayout implements View.OnClickListener {

    private LinearLayout layoutPlayBar;
    private Context context;
    private ImageView imgPrev, imgPlay, imgNext, imgList;
    private TextView textTitle, textSinger;
    private ProgressBar progressBarPlayBar;
    private SharedPreferences preferencesPlayList;
    private static final int PLAY = 0;
    private static final int PAUSE = 1;

    public PlayBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.layout_play_bar, this);
        init();
        textTitle.setText(preferencesPlayList.getString("playTitle", "用心聆听"));
        textSinger.setText(preferencesPlayList.getString("playSinger", "用心聆听"));
        setOnClickListener();
        (new ChangeUIThread()).start();

    }

    private void setOnClickListener() {
        layoutPlayBar.setOnClickListener(this);
        imgPrev.setOnClickListener(this);
        imgPlay.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        imgList.setOnClickListener(this);
    }

    private void init() {
        preferencesPlayList = context.getSharedPreferences(StaticVariate.playList, MODE_PRIVATE);
        layoutPlayBar = findViewById(R.id.layout_play_bar);
        progressBarPlayBar = findViewById(R.id.progress_play_bar);
        imgPrev = findViewById(R.id.img_play_bar_prev);
        imgPlay = findViewById(R.id.img_play_bar_play);
        imgNext = findViewById(R.id.img_play_bar_next);
        imgList = findViewById(R.id.img_play_bar_list);
        textTitle = findViewById(R.id.text_title);
        textSinger = findViewById(R.id.text_singer);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_play_bar:
                Intent playBarIntent = new Intent(context, PlayActivity.class);
                context.startActivity(playBarIntent);
                break;
            case R.id.img_play_bar_prev:
                StaticVariate.isPrev = true;
                if (StaticVariate.isFistPlay) {
                    Intent intent = new Intent(context, PlayService.class);
                    context.startService(intent);
                }
                break;
            case R.id.img_play_bar_play:
                if (StaticVariate.isFistPlay) {
                    Intent intent = new Intent(context, PlayService.class);
                    context.startService(intent);
                } else {
                    StaticVariate.isPlayOrPause = true;
                    StaticVariate.isPause = true;
//                    Log.e("*****", "播放栏播放");
                }

                break;
            case R.id.img_play_bar_next:
                StaticVariate.isNext = true;
                if (StaticVariate.isFistPlay) {
                    Intent intent = new Intent(context, PlayService.class);
                    context.startService(intent);
                }
                break;
        }
    }

    class ChangeUIThread extends Thread {
        public void run() {
            while (true) {
                if (StaticVariate.isPlay) {
                    Message message = new Message();
                    message.what = PLAY;
                    changeUIHandler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = PAUSE;
                    changeUIHandler.sendMessage(message);
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private Handler changeUIHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLAY:
                    imgPlay.setImageResource(R.drawable.ic_pause);
                    progressBarPlayBar.setProgress(StaticVariate.playProgress * 100 / StaticVariate.playDuration);
                    if(textTitle.getText() != preferencesPlayList.getString(
                            StaticVariate.keyPlayTitle, StaticVariate.unKnown)&&
                            textSinger.getText() != preferencesPlayList.getString(
                                    StaticVariate.keyPlaySinger, StaticVariate.unKnown)){
                        textTitle.setText(preferencesPlayList.getString(
                                StaticVariate.keyPlayTitle, StaticVariate.unKnown));
                        textSinger.setText(preferencesPlayList.getString(
                                StaticVariate.keyPlaySinger, StaticVariate.unKnown));
                    }
//                    Log.e("*********", preferencesPlayList.getString("playTitle", "用心聆听"));
                    break;
                case PAUSE:
                    imgPlay.setImageResource(R.drawable.ic_play);
                    break;
            }
        }


    };
}

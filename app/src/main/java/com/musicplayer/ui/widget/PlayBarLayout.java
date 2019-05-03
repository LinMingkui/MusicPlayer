package com.musicplayer.ui.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
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
import com.musicplayer.utils.Variate;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

public class PlayBarLayout extends LinearLayout implements View.OnClickListener, PlayService.OnPlaySongChangeListener, PlayService.OnPlayStateChangeListener, PlayService.OnProgressListener {

    private String TAG = "*PlayBarLayout";
    private PlayService playService;
    private LinearLayout layoutPlayBar;
    private Context mContext;
    private ImageView imgPrev, imgPlay, imgNext, imgList;
    private TextView textName, textSinger;
    private ProgressBar progressBarPlayBar;
    private SharedPreferences preferencesPlayList;
    private boolean isBind = false;
    private OnPlaySongChangeListener onPlaySongChangeListener;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playService = ((PlayService.PlayBinder)service).getService();
            playService.setOnPlaySongChangeListener(PlayBarLayout.this);
            playService.setOnPlayStateChangeListener(PlayBarLayout.this);
            playService.setOnProgressListener(PlayBarLayout.this);
            if (playService.isPlay()){
                imgPlay.setImageResource(R.drawable.ic_pause);
            }
            progressBarPlayBar.setMax(playService.getDuration());
            progressBarPlayBar.setProgress(playService.getProgress());
            Log.e(TAG,"onServiceConnected");
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public PlayBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_play_bar, this);
        init();
        textName.setText(preferencesPlayList.getString(Variate.keySongName, "用心聆听"));
        textSinger.setText(preferencesPlayList.getString(Variate.keySinger, "用心聆听"));
        setOnClickListener();
    }

    private void setOnClickListener() {
        layoutPlayBar.setOnClickListener(this);
        imgPrev.setOnClickListener(this);
        imgPlay.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        imgList.setOnClickListener(this);
    }

    private void init() {
        preferencesPlayList = mContext.getSharedPreferences(Variate.playList, MODE_PRIVATE);
        layoutPlayBar = findViewById(R.id.layout_play_bar);
        progressBarPlayBar = findViewById(R.id.progress_play_bar);
        imgPrev = findViewById(R.id.img_play_bar_prev);
        imgPlay = findViewById(R.id.img_play_bar_play);
        imgNext = findViewById(R.id.img_play_bar_next);
        imgList = findViewById(R.id.img_play_bar_list);
        textName = findViewById(R.id.text_title);
        textSinger = findViewById(R.id.text_singer);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_play_bar:
                Intent playBarIntent = new Intent(mContext, PlayActivity.class);
                mContext.startActivity(playBarIntent);
                break;
            case R.id.img_play_bar_prev:
                if (!Variate.isFistPlay){
                    playService.prevSong();
                }
                break;
            case R.id.img_play_bar_play:
                if (Variate.isFistPlay) {
                    Intent intent = new Intent(mContext, PlayService.class);
                    mContext.startService(intent);
                } else {
                    if (playService.isPlay()){
                        playService.pause();
                    }else {
                        playService.play();
                    }
                }
                break;
            case R.id.img_play_bar_next:
                if (!Variate.isFistPlay){
                    playService.nextSong();
                }
                break;
        }
    }

    @Override
    public void OnPlaySongChange(Cursor cursor) {
        textName.setText(cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
        textSinger.setText(cursor.getString(cursor.getColumnIndex(Variate.keySinger)));
        if (onPlaySongChangeListener != null) {
            onPlaySongChangeListener.OnPlaySongChange();
        }
//        Log.e(TAG,"name:"+cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
    }
    @Override
    public void OnPlayStateChange(boolean isPlay) {
//        Log.e(TAG,mContext+"OnPlayStateChange");
        if(isPlay){
            imgPlay.setImageResource(R.drawable.ic_pause);
        }else {
            imgPlay.setImageResource(R.drawable.ic_play);
        }
    }
    @Override
    public void onProgress(int duration, int current) {
        progressBarPlayBar.setMax(duration);
        progressBarPlayBar.setProgress(current);
//        Log.e(TAG,"duration:"+duration+"  current"+current);
    }

    public interface OnPlaySongChangeListener{
        void OnPlaySongChange();
    }
    public void setOnPlaySongChangeListener(OnPlaySongChangeListener listener){
        onPlaySongChangeListener = listener;
    }
    public void mBindService(Context context){
        Log.e(TAG,"mBindService "+context);
        if (!isBind) {
            mContext = context;
            Intent intent = new Intent(context, PlayService.class);
            mContext.bindService(intent, connection, BIND_AUTO_CREATE);
            textName.setText(preferencesPlayList.getString(Variate.keySongName, "用心聆听"));
            textSinger.setText(preferencesPlayList.getString(Variate.keySinger, "用心聆听"));
            isBind = true;
        }
    }
    public void mUnBindService(Context context){
        Log.e(TAG,"mUnBindService "+context);
        if (isBind && connection != null) {
            context.unbindService(connection);
            isBind = false;
        }
    }
}

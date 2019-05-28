package com.musicplayer.ui.widget;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.musicplayer.R;
import com.musicplayer.service.PlayService;
import com.musicplayer.ui.activity.PlayActivity;
import com.musicplayer.utils.NetworkUtils;
import com.musicplayer.utils.Variate;

import java.io.File;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

public class PlayBarLayout extends LinearLayout implements View.OnClickListener, PlayService.OnPlaySongChangeListener, PlayService.OnPlayStateChangeListener, PlayService.OnProgressListener {

    private String TAG = "*PlayBarLayout";
    private PlayService playService;
    private LinearLayout layoutPlayBar;
    private Context mContext;
    private ImageView imgSinger, imgPrev, imgPlay, imgNext, imgList;
    private TextView textName, textSinger;
    private ProgressBar progressBarPlayBar;
    private SharedPreferences preferencesPlayList;
    private boolean isBind = false;
    private NetworkUtils networkUtils;
    private OnPlaySongChangeListener onPlaySongChangeListener;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playService = ((PlayService.PlayBinder) service).getService();
            playService.setOnPlaySongChangeListener(PlayBarLayout.this);
            playService.setOnPlayStateChangeListener(PlayBarLayout.this);
            playService.setOnProgressListener(PlayBarLayout.this);
            if (playService.isPlay()) {
                imgPlay.setImageResource(R.drawable.ic_pause);
            }
            progressBarPlayBar.setMax(playService.getDuration());
            progressBarPlayBar.setProgress(playService.getProgress());
            Log.e(TAG, "onServiceConnected");
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
        textSinger.setText(preferencesPlayList.getString(Variate.keySinger, "用心聆听")
                .equals("") ? "群星" : preferencesPlayList.getString(Variate.keySinger, "用心聆听"));
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
        imgSinger = findViewById(R.id.img_singer);
        imgPrev = findViewById(R.id.img_play_bar_prev);
        imgPlay = findViewById(R.id.img_play_bar_play);
        imgNext = findViewById(R.id.img_play_bar_next);
        imgList = findViewById(R.id.img_play_bar_list);
        textName = findViewById(R.id.text_title);
        textSinger = findViewById(R.id.text_singer);
        networkUtils = new NetworkUtils();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_play_bar:
                if (mContext != null) {
                    Intent playBarIntent = new Intent(mContext, PlayActivity.class);
                    mContext.startActivity(playBarIntent);
                }
                break;
            case R.id.img_play_bar_prev:
                if (!Variate.isFistPlay) {
                    playService.prevSong();
                }
                break;
            case R.id.img_play_bar_play:
                if (Variate.isFistPlay) {
                    if (mContext != null) {
                        Intent intent = new Intent(mContext, PlayService.class);
                        mContext.startService(intent);
                    }
                } else {
                    if (playService.isPlay()) {
                        playService.pause();
                    } else {
                        playService.play();
                    }
                }
                break;
            case R.id.img_play_bar_next:
                if (!Variate.isFistPlay) {
                    playService.nextSong();
                }
                break;
        }
    }

    @Override
    public void OnPlaySongChange() {
        textName.setText(preferencesPlayList.getString(Variate.keySongName, Variate.unKnown));
        textSinger.setText(preferencesPlayList.getString(Variate.keySinger, "用心聆听")
                .equals("") ? "群星" : preferencesPlayList.getString(Variate.keySinger, "用心聆听"));
        if (onPlaySongChangeListener != null) {
            onPlaySongChangeListener.OnPlaySongChange();
        }
        setImgSinger();
    }

    private void setImgSinger() {
        String path = preferencesPlayList.getString(Variate.keySinger, "").replace('/', ' ');
        String picPath = new StringBuffer().append(Variate.PIC_PATH)
                .append('/').append(path)
                .toString();
        Log.e(TAG, "picPath " + picPath);
        File filePic = new File(picPath);
        if (filePic.exists()) {
            Glide.with(this).load(filePic)
                    .error(R.mipmap.img_default_singer).into(imgSinger);
        } else {
            String keyWord = new StringBuilder(preferencesPlayList.getString(Variate.keySinger, "")
                    .replace('/', ' ')).append(" - ")
                    .append(preferencesPlayList.getString(Variate.keySongName, "")).toString();
            if (!keyWord.equals("")) {
                networkUtils.getSongInfo(keyWord, "qq", Variate.FILTER_NAME);
            }
        }
    }

    @Override
    public void OnPlayStateChange(boolean isPlay) {
        if (isPlay) {
            imgPlay.setImageResource(R.drawable.ic_pause);
        } else {
            imgPlay.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    public void onProgress(int duration, int current) {
        progressBarPlayBar.setMax(duration);
        progressBarPlayBar.setProgress(current);
    }

    //在activity调用的接口
    public interface OnPlaySongChangeListener {
        void OnPlaySongChange();
    }

    public void setOnPlaySongChangeListener(OnPlaySongChangeListener listener) {
        onPlaySongChangeListener = listener;
    }

    public void mBindService(Context context) {
        Log.e(TAG, "mBindService " + context);
        if (!isBind) {
            mContext = context;
            setImgSinger();
            networkUtils.setOnGetSongInfoListener(song -> {
                if (mContext != null) {
                    ((Activity) mContext).runOnUiThread(() -> {
                        if (mContext != null) {
                            Glide.with(mContext).load(song.getSingerUrl())
                                    .error(R.mipmap.img_default_singer).into(imgSinger);
                        }
                    });
                }
            });
            Intent intent = new Intent(context, PlayService.class);
            mContext.bindService(intent, connection, BIND_AUTO_CREATE);
            textName.setText(preferencesPlayList.getString(Variate.keySongName, "用心聆听"));
            textSinger.setText(preferencesPlayList.getString(Variate.keySinger, "用心聆听"));
            isBind = true;
        }
    }

    public void mUnBindService(Context context) {
        Log.e(TAG, "mUnBindService " + context);
        if (isBind && connection != null) {
            context.unbindService(connection);
            isBind = false;
            mContext = null;
        }
    }
}

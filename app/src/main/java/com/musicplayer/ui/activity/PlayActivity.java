package com.musicplayer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.commit451.nativestackblur.NativeStackBlur;
import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.service.PlayService;
import com.musicplayer.ui.fragment.LyricFragment;
import com.musicplayer.ui.fragment.SingerFragment;
import com.musicplayer.utils.BaseActivity;
import com.musicplayer.utils.BitmapUtils;
import com.musicplayer.utils.StaticVariate;
import com.musicplayer.utils.TimeUtils;

import crossoverone.statuslib.StatusUtil;

import static com.musicplayer.utils.MethodUtils.insertSong;

public class PlayActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    public static boolean isClickLrcView = false;
    private static final String TAG = "*PlayActivity";
    private static final int PLAY = 0;
    private static final int PAUSE = 1;
    private static final int SET_SINGER_IMG = 2;
    private static final int BACK_TO_SINGER = 3;
    private static boolean isSetSingerImg;
    private boolean run;
    private boolean isSetProgress = true;
    private boolean isFirstOpen = true;
    private int playView = 1;
    private int maxVolume;
    private int currentVolume;
    private DataBase dataBase;
    private SQLiteDatabase db;
    private ChangeUIThread changeUIThread;
    private Cursor playCursor;
    private Bitmap bitmap;

    private SharedPreferences preferencesPlayList;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;

    private LinearLayout linearLayoutPlay;
    private FrameLayout frameLayoutPlay;
    private SingerFragment singerFragment;
    private LyricFragment lyricFragment;

    private ImageView imgBackground;
    private ImageView imgBack;
    private ImageView imgFavorite;
    private ImageView imgPlayMode, imgPrev, imgPlayOrPause, imgNext, imgPlayList;
    private SeekBar seekBarPlay, seekBarVolume;
    private TextView textTitle, textSinger;
    private TextView textDuration, textProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //设置状态栏颜色
        if (Build.VERSION.SDK_INT >= 23) {
            StatusUtil.setSystemStatus(this, false, true);
        } else {
            StatusUtil.setUseStatusBarColor(this, getResources().getColor(R.color.notification),
                    getResources().getColor(R.color.notification));
        }

        init();
        setOnClickListener();

        setPlayModeIcon();
        textTitle.setText(preferencesPlayList.getString("playTitle", "用心聆听"));
        textSinger.setText(preferencesPlayList.getString("playSinger", "用心聆听"));
        //播放进度控制
        seekBarPlay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textProgress.setText(TimeUtils.transformTime(progress * StaticVariate.playDuration / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSetProgress = false;
                Log.e("*********", "seekBar开始");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                StaticVariate.setPlayProgress = seekBar.getProgress() * StaticVariate.playDuration / 1000;
                StaticVariate.isSetProgress = true;
                isSetProgress = true;
                Log.e("*********", "seekBar结束");
            }
        });

        //音量调节
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.e(TAG, "volumeProgress " + progress);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    currentVolume = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void init() {
        mContext = this;
        preferencesPlayList = getSharedPreferences(StaticVariate.playList, MODE_PRIVATE);
        preferencesSet = getSharedPreferences(StaticVariate.keySet, MODE_PRIVATE);
        editorSet = preferencesSet.edit();

        dataBase = new DataBase(this, StaticVariate.dataBaseName,
                null, 1);
        db = dataBase.getWritableDatabase();

        StaticVariate.isSetFavoriteIcon = true;
        isSetSingerImg = true;

        linearLayoutPlay = findViewById(R.id.ll_play);
        frameLayoutPlay = findViewById(R.id.fragment_play);

        seekBarPlay = findViewById(R.id.seekbar_play);
        seekBarVolume = findViewById(R.id.seekbar_volume);
        //获取系统最大媒体音量
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.e(TAG, "maxVolume " + maxVolume);
        seekBarVolume.setMax(maxVolume);
        seekBarVolume.setProgress(currentVolume);

        imgBackground = findViewById(R.id.img_background);
        imgBack = findViewById(R.id.img_back);
        imgFavorite = findViewById(R.id.img_favorite);
        imgPlayMode = findViewById(R.id.img_play_mode);
        imgPrev = findViewById(R.id.img_prev);
        imgPlayOrPause = findViewById(R.id.img_play_or_pause);
        imgNext = findViewById(R.id.img_next);
        imgPlayList = findViewById(R.id.img_play_list);
        textTitle = findViewById(R.id.text_title);
        textSinger = findViewById(R.id.text_singer);
        textDuration = findViewById(R.id.text_duration);
        textProgress = findViewById(R.id.text_progress);
    }

    //设置歌手图片尺寸
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (isFirstOpen) {
            super.onWindowFocusChanged(hasFocus);
            int width = linearLayoutPlay.getWidth();
            int height = linearLayoutPlay.getHeight();
            Log.e(TAG, "width " + width + "height " + height);
            int wh;
            if (width <= height) {
                wh = (int) (width * 6 / 7.0);
            } else if (height <= (int) (width * 6 / 7.0)) {
                wh = height;
            } else {
                wh = (int) (height * 6 / 7.0);
            }
//        ViewGroup.LayoutParams params = frameLayoutPlay.getLayoutParams();
//        params.height = wh;
//        params.width = wh;
//        frameLayoutPlay.setLayoutParams(params);
            singerFragment = SingerFragment.newInstance(wh);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_play, singerFragment).commitAllowingStateLoss();
            isFirstOpen = false;
        }
    }

    private void setOnClickListener() {
        imgBack.setOnClickListener(this);
        imgFavorite.setOnClickListener(this);
        imgPlayMode.setOnClickListener(this);
        imgPrev.setOnClickListener(this);
        imgPlayOrPause.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        imgPlayList.setOnClickListener(this);

        linearLayoutPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                PlayActivity.this.finish();
                break;
            case R.id.img_prev:
                StaticVariate.isPrev = true;
                if (StaticVariate.isFistPlay) {
                    Intent intent = new Intent(mContext, PlayService.class);
                    startService(intent);
                }
                break;
            case R.id.img_play_or_pause:
                if (StaticVariate.isFistPlay) {
                    Intent intent = new Intent(mContext, PlayService.class);
                    startService(intent);
                } else {
                    StaticVariate.isPlayOrPause = true;
                    StaticVariate.isPause = true;
                    Log.e("*****", "播放");
                }
                break;
            case R.id.img_next:
                StaticVariate.isNext = true;
                if (StaticVariate.isFistPlay) {
                    Intent intent = new Intent(mContext, PlayService.class);
                    startService(intent);
                }
                break;

            //收藏
            case R.id.img_favorite:
                Cursor favoriteCursor = db.rawQuery("select * from " + StaticVariate.
                                favoriteSongListTable + " where fileUrl = ?",
                        new String[]{preferencesPlayList.getString(StaticVariate.fileUrl, "")});
                if (favoriteCursor.getCount() == 0) {
                    favoriteCursor = db.rawQuery("select * from " + StaticVariate.localSongListTable
                                    + " where fileUrl = ?",
                            new String[]{preferencesPlayList.getString(StaticVariate.fileUrl, "")});
                    favoriteCursor.moveToFirst();
                    insertSong(db, StaticVariate.favoriteSongListTable, favoriteCursor, true);
                    favoriteCursor.close();
                    Toast.makeText(mContext, "已添加至我的收藏", Toast.LENGTH_SHORT).show();
                } else {
                    db.delete(StaticVariate.favoriteSongListTable, StaticVariate.fileUrl + " = ?",
                            new String[]{preferencesPlayList.getString(StaticVariate.fileUrl, "")});
                    Toast.makeText(mContext, "已从我的收藏移除", Toast.LENGTH_SHORT).show();
                }
                StaticVariate.isSetFavoriteIcon = true;
//                StaticVariate.isSetListPlayIcon = true;

                //发送更新通知广播
                sendBroadcast(new Intent(StaticVariate.ACTION_UPDATE));
                break;

            //切换播放模式
            case R.id.img_play_mode:
                int key = preferencesSet.getInt(StaticVariate.keyPlayMode, 0);
                if (key == StaticVariate.ORDER) {
                    editorSet.putInt(StaticVariate.keyPlayMode, StaticVariate.RANDOM);
                    editorSet.apply();
                    imgPlayMode.setImageResource(R.drawable.ic_random_play);
                    Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
                } else if (key == StaticVariate.RANDOM) {
                    editorSet.putInt(StaticVariate.keyPlayMode, StaticVariate.SINGLE);
                    editorSet.apply();
                    imgPlayMode.setImageResource(R.drawable.ic_single_cycle);
                    Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
                } else if (key == StaticVariate.SINGLE) {
                    editorSet.putInt(StaticVariate.keyPlayMode, StaticVariate.ORDER);
                    editorSet.apply();
                    imgPlayMode.setImageResource(R.drawable.ic_order_play);
                    Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
                }
                break;

            //切换歌手图片和歌词界面
            case R.id.ll_play:
                Log.e(TAG, "点击播放界面");
                if (playView == 1) {
                    if (lyricFragment == null) {
                        lyricFragment = new LyricFragment();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_play, lyricFragment).commitAllowingStateLoss();
                    playView = 2;
                } else if (playView == 2) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_play, singerFragment).commitAllowingStateLoss();
                    playView = 1;
                }

                break;

        }
    }

    class ChangeUIThread extends Thread {

        public void run() {
            Log.e(TAG, "changeUIThread start");
            while (run) {
                if (StaticVariate.isPlay) {
                    Message message = new Message();
                    message.what = PLAY;
                    changeUIHandler.sendMessage(message);

                } else {
                    Message message = new Message();
                    message.what = PAUSE;
                    changeUIHandler.sendMessage(message);
                }
                if (!isSetSingerImg) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    bitmap = setBackground();
                    Message message = new Message();
                    message.what = SET_SINGER_IMG;
                    changeUIHandler.sendMessage(message);
                }
                if (isClickLrcView){
                    Message message = new Message();
                    message.what = BACK_TO_SINGER;
                    changeUIHandler.sendMessage(message);
                    isClickLrcView = false;
                }
            }
            Log.e(TAG, "changeUIThread close");
        }

    }

    private Handler changeUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case PLAY:
                    imgPlayOrPause.setImageResource(R.drawable.ic_pause);
                    textDuration.setText(TimeUtils.transformTime(StaticVariate.playDuration));
                    if (isSetProgress) {
                        textProgress.setText(TimeUtils.transformTime(StaticVariate.playProgress));
                        seekBarPlay.setProgress(StaticVariate.playProgress * 1000 / StaticVariate.playDuration);
                    }
                    if (textTitle.getText() != preferencesPlayList.
                            getString(StaticVariate.keyPlayTitle, StaticVariate.unKnown) &&
                            textSinger.getText() != preferencesPlayList.
                                    getString(StaticVariate.keyPlaySinger, StaticVariate.unKnown)) {
                        textTitle.setText(preferencesPlayList.getString(
                                StaticVariate.keyPlayTitle, StaticVariate.unKnown));
                        textSinger.setText(preferencesPlayList.getString(
                                StaticVariate.keyPlaySinger, StaticVariate.unKnown));
                    }
                    //设置收藏图标
                    synchronized (this) {
                        if (StaticVariate.isSetFavoriteIcon && run) {
                            playCursor = db.rawQuery("select * from " + StaticVariate.
                                            favoriteSongListTable + " where fileUrl = ?",
                                    new String[]{preferencesPlayList.getString(StaticVariate.fileUrl, "")});
                            if (playCursor.getCount() == 0) {
                                imgFavorite.setImageResource(R.drawable.ic_favorite_no);
                            } else {
                                imgFavorite.setImageResource(R.drawable.ic_favorite_yes);
                            }
                            StaticVariate.isSetFavoriteIcon = false;
                        }
                    }
                    break;
                case PAUSE:
                    imgPlayOrPause.setImageResource(R.drawable.ic_play);
                    //设置收藏图标
                    synchronized (this) {
                        if (StaticVariate.isSetFavoriteIcon && run) {
                            playCursor = db.rawQuery("select * from " + StaticVariate.
                                            favoriteSongListTable + " where fileUrl = ?",
                                    new String[]{preferencesPlayList.getString(StaticVariate.fileUrl, "")});
                            if (playCursor.getCount() == 0) {
                                imgFavorite.setImageResource(R.drawable.ic_favorite_no);
                            } else {
                                imgFavorite.setImageResource(R.drawable.ic_favorite_yes);
                            }
                            StaticVariate.isSetFavoriteIcon = false;
                        }
                    }
                    break;
                case SET_SINGER_IMG:
                    imgBackground.setImageBitmap(bitmap);
                    bitmap = null;
                    isSetSingerImg = false;
                    break;
                case BACK_TO_SINGER:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_play, singerFragment).commitAllowingStateLoss();
                    playView = 1;
                    break;
            }
            return false;
        }
    });

    //    private void initReceiver() {
//        //注册广播
//        receiver = new PlayService.UpdateNotificationReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(StaticVariate.ACTION_DOWN);
//        registerReceiver(receiver, intentFilter);
//        Intent favoriteIntent = new Intent(StaticVariate.ACTION_DOWN);
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (currentVolume > 0) {
                    seekBarVolume.setProgress(--currentVolume);
                    Log.e(TAG, "setProgress" + currentVolume);
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (currentVolume < maxVolume) {
                    seekBarVolume.setProgress(++currentVolume);
                    Log.e(TAG, "setProgress" + currentVolume);
                }
                break;

        }
        return super.onKeyDown(keyCode, event);
    }

    public static class UIReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case StaticVariate.ACTION_PREV:
                    Log.e(TAG, "广播接收 上一曲");
                case StaticVariate.ACTION_NEXT:
                    Log.e(TAG, "广播接收 下一曲");
                    isSetSingerImg = true;
                    break;
            }
        }
    }


    private Bitmap setBackground() {

        return NativeStackBlur.process(BitmapFactory.decodeResource(getResources(),
                R.drawable.img_default_play_background), 25);

    }

    private void setPlayModeIcon() {
        int key = preferencesSet.getInt(StaticVariate.keyPlayMode, 0);
        if (key == StaticVariate.ORDER) {
            imgPlayMode.setImageResource(R.drawable.ic_order_play);
        } else if (key == StaticVariate.RANDOM) {
            imgPlayMode.setImageResource(R.drawable.ic_random_play);
        } else if (key == StaticVariate.SINGLE) {
            imgPlayMode.setImageResource(R.drawable.ic_single_cycle);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        run = true;
        changeUIThread = new ChangeUIThread();
        changeUIThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        run = false;
    }

    protected void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            db.close();
            dataBase.close();
        }
        if (playCursor != null) {
            playCursor.close();
        }
        imgBackground.setImageBitmap(null);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        Log.e(TAG, "关闭Activity");
    }
}

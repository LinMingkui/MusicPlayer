package com.musicplayer.ui.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import com.musicplayer.utils.DownloadUtils;
import com.musicplayer.utils.TimeUtils;
import com.musicplayer.utils.ToastUtils;
import com.musicplayer.utils.Variate;

import java.lang.reflect.Field;

import crossoverone.statuslib.StatusUtil;

import static com.musicplayer.utils.MethodUtils.addSongMenu;
import static com.musicplayer.utils.MethodUtils.addToFavorite;
import static com.musicplayer.utils.Song.getPlayingSong;
import static com.musicplayer.utils.Song.songToCursor;
import static java.lang.Thread.sleep;

public class PlayActivity extends BaseActivity implements View.OnClickListener, PlayService.OnPlaySongChangeListener, PlayService.OnPlayStateChangeListener, PlayService.OnProgressListener {

    private Context mContext;
    private PlayService playService;
    private IBinder iBinder;
    public static boolean isClickLrcView = false;
    public boolean run = true;
    public boolean isBind = false;

    private static final String TAG = "*PlayActivity";
    private boolean isFirstOpen = true;
    private int playView = 1;
    private int maxVolume;
    private int currentVolume;
    private DataBase dataBase;
    private SQLiteDatabase db;

    private SharedPreferences preferencesPlayList;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;

    private LinearLayout linearLayoutPlay;
    private SingerFragment singerFragment;
    private LyricFragment lyricFragment;

    private ImageView imgBackground;
    private ImageView imgBack;
    private ImageView imgFavorite, imgDownload, imgMenu;
    private ImageView imgPlayMode, imgPrev, imgPlay, imgNext, imgPlayList;
    private SeekBar seekBarPlay, seekBarVolume;
    private TextView textName, textSinger;
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
        setFavoriteIcon();

        textName.setText(preferencesPlayList.getString(Variate.keySongName, "用心聆听"));
        textSinger.setText(preferencesPlayList.getString(Variate.keySinger, "用心聆听")
                .equals("") ? "群星" : preferencesPlayList.getString(Variate.keySinger, "用心聆听"));
        new Thread(() -> {
            while (run) {
                if (isClickLrcView) {
                    switchFragment();
                    isClickLrcView = false;
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //播放进度控制
        seekBarPlay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && playService != null) {
                    playService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
        mContext = PlayActivity.this;
        preferencesPlayList = getSharedPreferences(Variate.playList, MODE_PRIVATE);
        preferencesSet = getSharedPreferences(Variate.set, MODE_PRIVATE);
        editorSet = preferencesSet.edit();
        editorSet.apply();

        dataBase = new DataBase(this, Variate.dataBaseName,
                null, 1);
        db = dataBase.getWritableDatabase();

        Variate.isSetFavoriteIcon = true;

        linearLayoutPlay = findViewById(R.id.ll_play);

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
        imgDownload = findViewById(R.id.img_download);
        imgMenu = findViewById(R.id.img_menu);
        imgPlayMode = findViewById(R.id.img_play_mode);
        imgPrev = findViewById(R.id.img_prev);
        imgPlay = findViewById(R.id.img_play);
        imgNext = findViewById(R.id.img_next);
        imgPlayList = findViewById(R.id.img_play_list);
        textName = findViewById(R.id.text_title);
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
            singerFragment = SingerFragment.newInstance(wh);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_play, singerFragment)
                    .show(singerFragment)
                    .commitAllowingStateLoss();
            isFirstOpen = false;
        }
    }

    private void setOnClickListener() {
        imgBack.setOnClickListener(this);
        imgFavorite.setOnClickListener(this);
        imgMenu.setOnClickListener(this);
        imgDownload.setOnClickListener(this);
        imgPlayMode.setOnClickListener(this);
        imgPrev.setOnClickListener(this);
        imgPlay.setOnClickListener(this);
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
                if (!Variate.isFistPlay) {
                    playService.prevSong();
                }
                break;
            case R.id.img_play:
                if (Variate.isFistPlay) {
                    Intent intent = new Intent(mContext, PlayService.class);
                    startService(intent);
                } else {
                    if (playService.isPlay()) {
                        playService.pause();
                    } else {
                        playService.play();
                    }
                }
                break;
            case R.id.img_next:
                if (!Variate.isFistPlay) {
                    playService.nextSong();
                }
                break;
            //收藏
            case R.id.img_favorite:
                addToFavorite(mContext, db, preferencesPlayList);
                setFavoriteIcon();
                Variate.isSetFavoriteIcon = true;
                //发送更新通知广播
                sendBroadcast(new Intent(Variate.ACTION_UPDATE));
                break;
            //切换播放模式
            case R.id.img_play_mode:
                int key = preferencesSet.getInt(Variate.keyPlayMode, 0);
                if (key == Variate.ORDER) {
                    editorSet.putInt(Variate.keyPlayMode, Variate.RANDOM);
                    editorSet.apply();
                    imgPlayMode.setImageResource(R.drawable.ic_random_play);
                    Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
                } else if (key == Variate.RANDOM) {
                    editorSet.putInt(Variate.keyPlayMode, Variate.SINGLE);
                    editorSet.apply();
                    imgPlayMode.setImageResource(R.drawable.ic_single_cycle);
                    Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
                } else if (key == Variate.SINGLE) {
                    editorSet.putInt(Variate.keyPlayMode, Variate.ORDER);
                    editorSet.apply();
                    imgPlayMode.setImageResource(R.drawable.ic_order_play);
                    Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
                }
                break;
            //切换歌手图片和歌词界面
            case R.id.ll_play:
                Log.e(TAG, "点击播放界面");
                switchFragment();
                break;
            case R.id.img_download:
                if (preferencesPlayList.getInt(Variate.keySongType, Variate.SONG_TYPE_LOCAL) != Variate.SONG_TYPE_LOCAL) {
                    DownloadUtils downloadUtils = new DownloadUtils(mContext, null, getPlayingSong(preferencesPlayList));
                    downloadUtils.startDownload();
                } else {
                    ToastUtils.show(mContext, "已下载");
                }
                break;
            case R.id.img_menu:
                showMenu(v);
                break;
        }
    }

    //音乐列表item imgMenu点击事件
    @SuppressLint("RestrictedApi")
    public void showMenu(View view) {
        PopupMenu pm = new PopupMenu(mContext, view);
        pm.getMenuInflater().inflate(R.menu.memu_pm_local_song_list, pm.getMenu());
        pm.getMenu().getItem(0).setVisible(false);
        pm.getMenu().getItem(2).setVisible(false);
        pm.getMenu().getItem(3).setVisible(false);
        pm.setOnMenuItemClickListener(menuItem -> {
            menuItemClick(menuItem.getItemId());
            return false;
        });
        //使用反射，强制显示菜单图标
        try {
            Field field = pm.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(pm);
            mHelper.setForceShowIcon(true);
            mHelper.show();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    //音乐列表菜单弹出dialog点击事件
    private void menuItemClick(int dialogItemId) {
        switch (dialogItemId) {
            //添加到歌单
            case R.id.item_add_song_menu:
                View viewAddSongMenuDialog = getLayoutInflater().inflate(R.layout.dialog_add_song_menu, null);
                addSongMenu(mContext, db, songToCursor(getPlayingSong(preferencesPlayList)), viewAddSongMenuDialog);
                break;
        }
    }

    private void switchFragment() {
        if (playView == 1) {
            if (lyricFragment == null) {
                lyricFragment = new LyricFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_play, lyricFragment)
                        .hide(singerFragment)
                        .show(lyricFragment)
                        .commitAllowingStateLoss();
            }
            getSupportFragmentManager().beginTransaction()
                    .hide(singerFragment)
                    .show(lyricFragment)
                    .commitAllowingStateLoss();
            playView = 2;
        } else if (playView == 2) {
            getSupportFragmentManager().beginTransaction()
                    .hide(lyricFragment)
                    .show(singerFragment)
                    .commitAllowingStateLoss();
            playView = 1;
        }
    }

    @Override
    public void OnPlaySongChange() {
        Log.e(TAG, "OnPlaySongChange");
        textName.setText(preferencesPlayList.getString(Variate.keySongName, Variate.unKnown));
        textSinger.setText(preferencesPlayList.getString(Variate.keySinger, "用心聆听")
                .equals("") ? "群星" : preferencesPlayList.getString(Variate.keySinger, "用心聆听"));
        setFavoriteIcon();
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
        runOnUiThread(() -> {
            textDuration.setText(TimeUtils.transformTime(duration));
            textProgress.setText(TimeUtils.transformTime(current));
            seekBarPlay.setProgress(current);
            seekBarPlay.setMax(duration);
        });
    }

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

    private void setFavoriteIcon() {
        Cursor cursor;
        if (preferencesPlayList.getInt(Variate.keySongType, Variate.SONG_TYPE_LOCAL) == Variate.SONG_TYPE_LOCAL) {
            imgDownload.setImageResource(R.drawable.ic_download_yes);
            cursor = db.rawQuery("select * from " + Variate.
                            favoriteSongListTable + " where " + Variate.keySongUrl + " = ?",
                    new String[]{preferencesPlayList.getString(Variate.keySongUrl, "")});
        } else {
            imgDownload.setImageResource(R.mipmap.img_download_no);
            cursor = db.rawQuery("select * from " + Variate.
                            favoriteSongListTable + " where " + Variate.keySongMid + " = ?",
                    new String[]{preferencesPlayList.getString(Variate.keySongMid, "")});
        }
        if (cursor.getCount() == 0) {
            imgFavorite.setImageResource(R.drawable.ic_favorite_no);
        } else {
            imgFavorite.setImageResource(R.drawable.ic_favorite_yes);
        }
        cursor.close();
    }

    private void setPlayModeIcon() {
        int key = preferencesSet.getInt(Variate.keyPlayMode, 0);
        if (key == Variate.ORDER) {
            imgPlayMode.setImageResource(R.drawable.ic_order_play);
        } else if (key == Variate.RANDOM) {
            imgPlayMode.setImageResource(R.drawable.ic_random_play);
        } else if (key == Variate.SINGLE) {
            imgPlayMode.setImageResource(R.drawable.ic_single_cycle);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBinder = service;
            playService = ((PlayService.PlayBinder) service).getService();
            playService.setOnPlaySongChangeListener(PlayActivity.this);
            playService.setOnPlayStateChangeListener(PlayActivity.this);
            playService.setOnProgressListener(PlayActivity.this);
            if (playService.isPlay()) {
                imgPlay.setImageResource(R.drawable.ic_pause);
            }
            seekBarPlay.setMax(playService.getDuration());
            seekBarPlay.setProgress(playService.getProgress());
            setFavoriteIcon();
            isBind = true;
            Log.e(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(mContext, PlayService.class);
        bindService(intent, connection, BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onRestart() {
        dataBase = new DataBase(this, Variate.dataBaseName,
                null, 1);
        db = dataBase.getWritableDatabase();
        super.onRestart();
    }

    @Override
    protected void onPause() {
        if (isBind) {
            unbindService(connection);
        }
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        run = false;
        db.close();
        dataBase.close();
        Log.e(TAG, "关闭Activity");
    }
}

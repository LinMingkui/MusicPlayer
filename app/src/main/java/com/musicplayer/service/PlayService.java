package com.musicplayer.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.ui.activity.PlayActivity;
import com.musicplayer.utils.MyApplication;
import com.musicplayer.utils.NetworkUtils;
import com.musicplayer.utils.ToastUtils;
import com.musicplayer.utils.Variate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import static com.musicplayer.utils.MethodUtils.addPlaySongToTable;
import static com.musicplayer.utils.MethodUtils.addSong;
import static com.musicplayer.utils.MethodUtils.addToFavorite;
import static com.musicplayer.utils.MethodUtils.getSqlBaseOrder;
import static com.musicplayer.utils.MethodUtils.savePic;
import static com.musicplayer.utils.MethodUtils.setPlayMessage;
import static com.musicplayer.utils.Song.getTypeByInt;

public class PlayService extends Service {

    private OnProgressListener onProgressListener;
    private OnPlayStateChangeListener onPlayStateChangeListener;
    private OnPlaySongChangeListener onPlaySongChangeListener;
    private NotificationReceiver receiver;
    private static PendingIntent favoritePI, prevPI, playPI, nextPI, lrcPI, closePI, notificationPI;
    private static Uri uri;
    private NetworkUtils networkUtils;
    private Context context = this;
    private DataBase dataBase;
    private static SQLiteDatabase db;
    private MediaPlayer mediaPlayer;
    private static Cursor cursor;
    private boolean run = true;
    private boolean isSend = false;
    private static boolean isPlay = false;
    private static SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;
    private String tableName;
    private static int position;
    private static String TAG = "*PlayService";
    private static Notification notification;

    public void onCreate() {
        super.onCreate();
        initReceiver();
        networkUtils = new NetworkUtils();
        preferencesPlayList = getSharedPreferences(Variate.playList, MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();
        editorPlayList.apply();
        preferencesSet = getSharedPreferences(Variate.set, MODE_PRIVATE);
        editorSet = preferencesSet.edit();
        editorSet.apply();
        dataBase = new DataBase(this, Variate.dataBaseName,
                null, 1);
        db = dataBase.getWritableDatabase();
        try {
            mediaPlayer = new MediaPlayer();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isSend = false;
        tableName = preferencesPlayList.getString(Variate.keyTableName, "")
                .isEmpty() ? Variate.localSongListTable :
                preferencesPlayList.getString(Variate.keyTableName, "");
        Log.e(TAG, tableName);
        position = preferencesPlayList.getInt("position", 0);
        Log.e(TAG, "position " + position);
        String sql = getSqlBaseOrder(tableName, preferencesSet);
        cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            cursor.moveToPosition(position);
        }
        if (intent.getBooleanExtra(Variate.keyIsLocal, true)) {
            if (cursor.getCount() != 0) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    isSend = false;
                    Log.e(TAG, "setOnCompletionListener");
                    nextSong();
                });
                initMediaPlayer();
            }
        } else {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(Variate.song.getSongUrl());
                editorPlayList.putString(Variate.keySongName, Variate.song.getSongName());
                editorPlayList.putString(Variate.keySinger, Variate.song.getSinger());
                editorPlayList.putString(Variate.keySongUrl, "");
                editorPlayList.putString(Variate.keySongMid, Variate.song.getSongMid());
                editorPlayList.putInt(Variate.keySongType, Variate.song.getType());
                editorPlayList.apply();
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnCompletionListener(mp -> {
                    isSend = false;
                    Log.e(TAG, "setOnCompletionListener");
                    if (preferencesSet.getInt(Variate.keyPlayMode, Variate.ORDER) == Variate.SINGLE || cursor.getCount() == 0) {
                        mediaPlayer.reset();
                        try {
                            mediaPlayer.setDataSource(Variate.song.getSongUrl());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mediaPlayer.prepareAsync();
                    } else if (cursor.getCount() != 0) {
                        nextSong();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mediaPlayer.setOnPreparedListener(mp -> {
            play();
            insertToRecentlyList();
            updateNotification();
            if (Variate.isFistPlay) {
                startForeground(1, notification);
                Variate.isFistPlay = false;
            }
//            showNotification(context, isPlay);
            isSend = true;
            Variate.isInitLyric = true;
            Variate.isInitSingleLyric = true;
            if (onPlaySongChangeListener != null) {
                onPlaySongChangeListener.OnPlaySongChange();
            }
        });
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            if (what == -38 && extra == 0) {
                initMediaPlayer();
                return true;
            }
            return false;
        });
        networkUtils.setOnGetSongInfoListener(song -> {
            try {
                Log.e(TAG, "networkUtilsPic singerUrl " + song.getSingerUrl());
                Bitmap bitmap = Glide.with(context).asBitmap().load(song.getSingerUrl())
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                if (bitmap != null) {
                    Log.e(TAG, "networkUtilsPic bitmap != null");
                    File file = new File(new StringBuilder(Variate.PIC_PATH)
                            .append('/')
                            .append(preferencesPlayList.getString(Variate.keySongName, "")
                                    .replace('/',' '))
                            .toString());
                    savePic(context, song.getSingerUrl(), file);
//                    uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
//                            bitmap, null, null));
                    uri = Uri.parse(file.toString());
                    Log.e(TAG, "networkUtilsPic uri.getPath()" + uri.getPath());
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            showNotification(context, isPlay());
        });
        new sentProgressThread().start();
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            if (onPlayStateChangeListener != null) {
                onPlayStateChangeListener.OnPlayStateChange(mediaPlayer.isPlaying());
            }
        }
        isPlay = isPlay();
        showNotification(context, isPlay);
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (onPlayStateChangeListener != null) {
                onPlayStateChangeListener.OnPlayStateChange(mediaPlayer.isPlaying());
            }
        }
        isPlay = isPlay();
        showNotification(context, isPlay);
    }

    public boolean isPlay() {
        if (mediaPlayer != null) {
            boolean b;
            try {
                b = mediaPlayer.isPlaying();
            } catch (IllegalStateException e) {
                b = false;
            }
            return b;
        } else {
            return false;
        }
    }

    public void seekTo(int progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progress);
        }
    }

    public void initMediaPlayer() {
        isSend = false;
        mediaPlayer.reset();
        setPlayMessage(editorPlayList, cursor, tableName, position);

        int type = cursor.getInt(cursor.getColumnIndex(Variate.keySongType));
        if (type == Variate.SONG_TYPE_LOCAL) {
            try {
                mediaPlayer.setDataSource(cursor.getString(cursor.getColumnIndex(Variate.keySongUrl)));
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String mid = cursor.getString(cursor.getColumnIndex(Variate.keySongMid));
            String SType = getTypeByInt(type);
            NetworkUtils networkUtils = new NetworkUtils();
            networkUtils.getSongInfo(mid, SType, Variate.FILTER_ID);
            networkUtils.setOnGetSongInfoListener(song -> {
                try {
                    mediaPlayer.setDataSource(song.getSongUrl());
                    Log.e(TAG, song.getSongUrl());
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            });
        }
        if (onPlaySongChangeListener != null) {
            onPlaySongChangeListener.OnPlaySongChange();
        }
    }

    private void updateNotification() {
        StringBuilder builder = new StringBuilder(Variate.PIC_PATH).append('/')
                .append(preferencesPlayList.getString(Variate.keySinger, "").replace('/', ' '));
        File file = new File(builder.toString());
        if (file.exists()) {
//            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
//            uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
//                    bitmap, null, null));
            uri = Uri.parse(file.toString());
            showNotification(context, isPlay());
        } else {
            builder = new StringBuilder(preferencesPlayList.getString(Variate.keySinger, ""))
                    .append(' ').append(preferencesPlayList.getString(Variate.keySongName, ""));
            networkUtils.getSongInfo(builder.toString(), "qq", Variate.FILTER_NAME);
        }

    }

    public int getProgress() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    //上一曲
    public void prevSong() {
        onPlayStateChangeListener.OnPlayStateChange(mediaPlayer.isPlaying());
        if (cursor.getCount() == 0) {
            ToastUtils.show(context, "您必须先播放一次本地列表的音乐");
        } else {
            int playMode = preferencesSet.getInt(Variate.keyPlayMode, Variate.ORDER);
            if (playMode == Variate.ORDER) {
                //顺序播放
                if (cursor.isFirst()) {
                    cursor.moveToLast();
                    position = cursor.getCount() - 1;
                } else {
                    cursor.moveToPosition(--position);
                }
            } else if (playMode == Variate.RANDOM) {
                //随机播放
                do {
                    position = (int) (Math.random() * cursor.getCount());
                } while (position == cursor.getCount());
                Log.e(TAG, "random position:" + position);
                cursor.moveToPosition(position);
            }
            Log.e(TAG, "上一曲" + position);
            initMediaPlayer();
        }
    }

    //下一曲
    public void nextSong() {
        onPlayStateChangeListener.OnPlayStateChange(mediaPlayer.isPlaying());
        if (cursor.getCount() == 0) {
            ToastUtils.show(context, "您必须先播放一次本地列表的音乐");
        } else {
            int playMode = preferencesSet.getInt(Variate.keyPlayMode, Variate.ORDER);
            if (playMode == Variate.ORDER) {
                //顺序播放
                if (cursor.isLast()) {
                    cursor.moveToFirst();
                    position = 0;
                } else {
                    cursor.moveToPosition(++position);
                }
            } else if (playMode == Variate.RANDOM) {
                //随机播放
                do {
                    position = (int) (Math.random() * cursor.getCount());
                } while (position == cursor.getCount());
                Log.e(TAG, "random position:" + position);
                cursor.moveToPosition(position);
            }
            Log.e(TAG, "下一曲" + position);
            initMediaPlayer();
        }
    }

    //发送进度
    private class sentProgressThread extends Thread {
        @Override
        public void run() {
            while (run) {
                if (isSend) {
                    if (onProgressListener != null) {
                        onProgressListener.onProgress(mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition());
                    }
                    Variate.playProgress = mediaPlayer.getCurrentPosition();
                }
                if (Variate.isSetProgress) {
                    seekTo(Variate.setPlayProgress);
                    Variate.playProgress = mediaPlayer.getCurrentPosition();
                    Variate.isSetProgress = false;
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //添加到最近播放
    private void insertToRecentlyList() {
        Cursor recentlyCursor;
        if (preferencesPlayList.getInt(Variate.keySongType, Variate.SONG_TYPE_LOCAL) == Variate.SONG_TYPE_LOCAL) {
            recentlyCursor = db.rawQuery("select * from " + Variate.
                            recentlySongListTable + " where " + Variate.keySongUrl + " = ?",
                    new String[]{cursor.getString(cursor.getColumnIndex(Variate.keySongUrl))});
            if (recentlyCursor.getCount() != 0) {
                db.delete(Variate.recentlySongListTable, Variate.keySongUrl + " = ?",
                        new String[]{cursor.getString(cursor.getColumnIndex(Variate.keySongUrl))});
            }
            addSong(db, Variate.recentlySongListTable, cursor);
        } else {
            recentlyCursor = db.rawQuery("select * from " + Variate.
                            recentlySongListTable + " where " + Variate.keySongMid + " = ?",
                    new String[]{preferencesPlayList.getString(Variate.keySongMid, "")});
            if (recentlyCursor.getCount() != 0) {
                db.delete(Variate.recentlySongListTable, Variate.keySongMid + " = ?",
                        new String[]{preferencesPlayList.getString(Variate.keySongMid, "")});
            }
            addPlaySongToTable(db, preferencesPlayList, Variate.recentlySongListTable);
        }
        recentlyCursor.close();
    }

    //歌曲进度接口
    public interface OnProgressListener {
        void onProgress(int duration, int current);
    }

    public void setOnProgressListener(OnProgressListener listener) {
        this.onProgressListener = listener;
    }

    //播放状态改变接口
    public interface OnPlayStateChangeListener {
        void OnPlayStateChange(boolean isPlay);
    }

    public void setOnPlayStateChangeListener(OnPlayStateChangeListener listener) {
        this.onPlayStateChangeListener = listener;
    }

    //播放歌曲改变接口
    public interface OnPlaySongChangeListener {
        void OnPlaySongChange();
    }

    public void setOnPlaySongChangeListener(OnPlaySongChangeListener listener) {
        this.onPlaySongChangeListener = listener;
    }

    //注册广播
    private void initReceiver() {
        receiver = new PlayService.NotificationReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Variate.ACTION_DOWN);
        registerReceiver(receiver, intentFilter);

        Intent favoriteIntent = new Intent(Variate.ACTION_DOWN);
        favoriteIntent.putExtra("id", Variate.ID_FAVORITE);
        favoritePI = PendingIntent.getBroadcast(this,
                Variate.ID_FAVORITE, favoriteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(Variate.ACTION_DOWN);
        prevIntent.putExtra("id", Variate.ID_PREV);
        prevPI = PendingIntent.getBroadcast(this,
                Variate.ID_PREV, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(Variate.ACTION_DOWN);
        playIntent.putExtra("id", Variate.ID_PLAY_OR_PAUSE);
        playPI = PendingIntent.getBroadcast(this,
                Variate.ID_PLAY_OR_PAUSE, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(Variate.ACTION_DOWN);
        nextIntent.putExtra("id", Variate.ID_NEXT);
        nextPI = PendingIntent.getBroadcast(this,
                Variate.ID_NEXT, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent lrcIntent = new Intent(Variate.ACTION_DOWN);
        lrcIntent.putExtra("id", Variate.ID_LRC);
        lrcPI = PendingIntent.getBroadcast(this,
                Variate.ID_LRC, lrcIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(Variate.ACTION_DOWN);
        closeIntent.putExtra("id", Variate.ID_CLOSE);
        closePI = PendingIntent.getBroadcast(this,
                Variate.ID_CLOSE, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//        Intent intent = new Intent(context, PlayActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Intent notificationIntent = new Intent(Variate.ACTION_DOWN);
        notificationIntent.putExtra("id", Variate.ID_NOTIFICATION);
        notificationPI = PendingIntent.getBroadcast(this,
                Variate.ID_NOTIFICATION, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    //显示通知
    public static void showNotification(Context context, boolean isPlay) {
        RemoteViews viewNormal = new RemoteViews(context.getPackageName(), R.layout.layout_play_notification_normal);
        RemoteViews viewBig = new RemoteViews(context.getPackageName(), R.layout.layout_play_notification_big);

        //设置控件监听事件
        viewBig.setOnClickPendingIntent(R.id.img_prev, prevPI);
        viewBig.setOnClickPendingIntent(R.id.img_favorite, favoritePI);
        viewBig.setOnClickPendingIntent(R.id.img_play_or_pause, playPI);
        viewBig.setOnClickPendingIntent(R.id.img_next, nextPI);
        viewBig.setOnClickPendingIntent(R.id.img_lrc, lrcPI);
        viewBig.setOnClickPendingIntent(R.id.img_close, closePI);
        viewBig.setOnClickPendingIntent(R.id.linear_layout_notification, notificationPI);

        viewNormal.setOnClickPendingIntent(R.id.img_prev, prevPI);
        viewNormal.setOnClickPendingIntent(R.id.img_play_or_pause, playPI);
        viewNormal.setOnClickPendingIntent(R.id.img_next, nextPI);
        viewNormal.setOnClickPendingIntent(R.id.img_close, closePI);
        viewNormal.setOnClickPendingIntent(R.id.linear_layout_notification, notificationPI);

        String title, singer;
        title = preferencesPlayList.getString(Variate.keySongName, Variate.unKnown);
        singer = preferencesPlayList.getString(Variate.keySinger, Variate.unKnown);
        viewBig.setTextViewText(R.id.tv_song_name, title);
        viewBig.setTextViewText(R.id.tv_singer, singer);
        viewNormal.setTextViewText(R.id.tv_song_name, title);
        viewNormal.setTextViewText(R.id.tv_singer, singer);
        if (uri != null) {
            viewBig.setImageViewUri(R.id.img_singer, uri);
            viewNormal.setImageViewUri(R.id.img_singer, uri);
//            viewBig.setimag
        }

        Cursor cursorFavorite = db.rawQuery("select " + Variate.keySongId + " from "
                        + Variate.favoriteSongListTable
                        + " where " + Variate.keySongUrl + " = ?",
                new String[]{preferencesPlayList.getString(Variate.keySongUrl, Variate.unKnown)});
        if (cursorFavorite.getCount() != 0) {
            cursorFavorite.close();
            viewBig.setImageViewResource(R.id.img_favorite,
                    R.drawable.ic_favorite_yes);
        } else {
            viewBig.setImageViewResource(R.id.img_favorite,
                    R.drawable.ic_favorite_no);
        }
        if (isPlay) {
            viewBig.setImageViewResource(R.id.img_play_or_pause,
                    R.drawable.ic_pause);
            viewNormal.setImageViewResource(R.id.img_play_or_pause,
                    R.drawable.ic_pause);
        } else {
            viewBig.setImageViewResource(R.id.img_play_or_pause,
                    R.drawable.ic_play);
            viewNormal.setImageViewResource(R.id.img_play_or_pause,
                    R.drawable.ic_play);
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "PLAY", "PLAY", NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.e(TAG, "createNotificationChannel");
            }
        }
        notification = new NotificationCompat.Builder(context, "PLAY")
                .setContentTitle("") // 创建通知的标题
                .setContentText("") // 创建通知的内容
                .setSmallIcon(R.mipmap.ic_launcher) // 创建通知的小图标
                .setContent(viewNormal)
                .setCustomBigContentView(viewBig)// 通过设置RemoteViews对象来设置通知的布局，这里我们设置为自定义布局
                .setOngoing(true)//无法划去
                .build();
        notificationManager.notify(1, notification);
        // 前台服务
    }

    //通知点击事件
    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Variate.ACTION_DOWN)) {
                int id = intent.getIntExtra("id", 0);
                Log.e(TAG, "id " + id);
                switch (id) {
                    case Variate.ID_FAVORITE:
                        Log.e(TAG, "通知栏收藏");
//                        addFavorite(context, db, cursor);
                        addToFavorite(context, db, preferencesPlayList);
                        Variate.isSetFavoriteIcon = true;
                        showNotification(context, isPlay());
                        if (onPlaySongChangeListener != null) {
                            onPlaySongChangeListener.OnPlaySongChange();
                        }
                        break;
                    case Variate.ID_PREV:
                        Log.e(TAG, "通知栏上一曲");
                        prevSong();
                        break;
                    case Variate.ID_PLAY_OR_PAUSE:
                        Log.e(TAG, "通知播放暂停");
                        if (isPlay()) {
                            pause();
                        } else {
                            play();
                        }
                        showNotification(context, isPlay);
                        break;
                    case Variate.ID_NEXT:
                        Log.e(TAG, "通知栏下一曲");
                        nextSong();
                        break;
                    case Variate.ID_LRC:
                        Log.e(TAG, "通知栏歌词");

                        break;
                    case Variate.ID_CLOSE:
                        Log.e(TAG, "通知关闭");
//                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                        manager.cancelAll();
//                        collapseStatusBar();
                        stopForeground(true);
                        MyApplication.getInstance().AppExit();
                        break;
                    case Variate.ID_NOTIFICATION:
                        Log.e(TAG, "从通知打开播放界面");
                        collapseStatusBar();
                        try {
                            MyApplication.getInstance().finishActivity(
                                    Class.forName("com.musicplayer.ui.activity.PlayActivity"));

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(context, PlayActivity.class));
                        break;
                }
            }
        }
    }

    //更新通知
    public static class UpdateNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Variate.ACTION_UPDATE)) {
                Log.e(TAG, "更新通知");
                showNotification(context, isPlay);
            }
        }

    }

    //收起通知
    public void collapseStatusBar() {
        @SuppressLint("WrongConstant")
        Object service = getSystemService("statusbar");
//        Object service = getSystemService(Context.VIBRATOR_SERVICE);
        try {
            Method collapse;
            collapse = service.getClass().getMethod("collapsePanels");
            collapse.setAccessible(true);
            collapse.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //停止
    public void onDestroy() {
        run = false;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        unregisterReceiver(receiver);
//        Variate.isFistPlay = true;
//        Variate.isPlay = false;
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
////        manager.cancelAll();
        if (notification != null) {
            stopForeground(true);
        }
        if (dataBase != null) {
            dataBase.close();
        }
        if (db != null) {
            db.close();
        }
        if (cursor != null) {
            cursor.close();
        }
        super.onDestroy();
    }
}

package com.musicplayer.service;

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
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.ui.activity.PlayActivity;
import com.musicplayer.utils.MyApplication;
import com.musicplayer.utils.StaticVariate;

import java.io.IOException;
import java.lang.reflect.Method;

import static com.musicplayer.utils.MethodUtils.addFavorite;
import static com.musicplayer.utils.MethodUtils.insertSong;

public class PlayService extends Service {


    private NotificationReceiver receiver;
    private static PendingIntent favoritePI, prevPI, playPI, nextPI, lrcPI, closePI, notificationPI;

    private Context context = this;
    private DataBase dataBase;
    private static SQLiteDatabase db;
    private PlaySongThread playSongThread;
    private MediaPlayer mediaPlayer;
    private static Cursor cursor;
    private boolean run = true;
    private static SharedPreferences preferencesPlayList;
    private SharedPreferences.Editor editorPlayList;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;
    private String listName = StaticVariate.localSongListTable;
    private static int position;
    private static String TAG = "*PlayService";


    public void onCreate() {
        super.onCreate();
        Log.e(TAG, listName);
        initReceiver();

        preferencesPlayList = getSharedPreferences(StaticVariate.playList, MODE_PRIVATE);
        editorPlayList = preferencesPlayList.edit();
        preferencesSet = getSharedPreferences(StaticVariate.keySet, MODE_PRIVATE);
        editorSet = preferencesSet.edit();
        listName = preferencesPlayList.getString(StaticVariate.keyListName, "")
                .isEmpty() ? StaticVariate.localSongListTable :
                preferencesPlayList.getString(StaticVariate.keyListName, "");

        Log.e(TAG, listName);
        position = preferencesPlayList.getInt("position", 0);
        Log.e(TAG, String.valueOf(position));
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        StaticVariate.isFistPlay = false;
        mediaPlayer = new MediaPlayer();
        dataBase = new DataBase(this, StaticVariate.dataBaseName,
                null, 1);
        db = dataBase.getWritableDatabase();
        cursor = db.query(listName, null, null,
                null, null, null, null);
        Log.e(TAG, "总大小" + cursor.getCount());
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "请先进本地音乐列表进行初始化", Toast.LENGTH_LONG).show();
        } else {
            playSongThread = new PlaySongThread();
            playSongThread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //播放线程
    class PlaySongThread extends Thread {
        public void run() {
            cursor.moveToPosition(position);
            insertToRecentlyList();
            setUIMessage();
            initMediaPlayer();
            Log.e(TAG, cursor.getString(1));
            while (run) {
                if (StaticVariate.isSetProgress) {                                          //快进
                    mediaPlayer.seekTo(StaticVariate.setPlayProgress);
                    Log.e(TAG, "seekTo " + StaticVariate.setPlayProgress);
                    StaticVariate.isPause = false;
                    StaticVariate.isPlayOrPause = false;
                    StaticVariate.isSetProgress = false;
                } else if (StaticVariate.isPlayOrPause) {                           //播放、暂停
                    if (StaticVariate.isPause) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            StaticVariate.isPause = false;
                            Log.e(TAG, "暂停");
                            showNotification(context);
                        } else {
                            mediaPlayer.start();
                            StaticVariate.isPause = false;
                            StaticVariate.isPlayOrPause = false;
                            Log.e(TAG, "播放");
                            showNotification(context);
                        }
                    }
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (StaticVariate.isNext) {                                        //下一曲
                    nextSong();
                    initMediaPlayer();
                    StaticVariate.isNext = false;
                } else if (StaticVariate.isPrev) {                                         //上一曲
                    prevSong();
                    initMediaPlayer();
                    StaticVariate.isPrev = false;
                } else if (mediaPlayer != null && !mediaPlayer.isPlaying()) {               //自动播放下一首
                    nextSong();
                    initMediaPlayer();
                }

                //删除当前列表音乐时
                if (StaticVariate.isDelete) {
                    cursor = db.rawQuery("select * from " + listName, null);
                    if (StaticVariate.isSubPosition) {
                        position--;
                        StaticVariate.isSubPosition = false;
                    } else if (StaticVariate.isEqualPosition) {
                        cursor.moveToPosition(position);
                        initMediaPlayer();
                    }
                    cursor.moveToPosition(position);
                    StaticVariate.isDelete = false;
                    StaticVariate.isSubPosition = false;
                    StaticVariate.isEqualPosition = false;
                    setUIMessage();
                    Log.e(TAG, "删除");
                }
                //播放进度
                try {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        StaticVariate.isPlay = true;
                        StaticVariate.playProgress = mediaPlayer.getCurrentPosition();
                    } else {
                        StaticVariate.isPlay = false;
                    }
                } catch (IllegalStateException e) {
                    Log.e(TAG, e.toString());
                }

                try {
                    sleep(100);                                                     //休息
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //上一曲
    private void prevSong() {
        int key = preferencesSet.getInt(StaticVariate.keyPlayMode, 0);
        if (key == StaticVariate.ORDER) {
            //顺序播放
            if (cursor.isFirst()) {
                cursor.moveToLast();
                position = cursor.getCount() - 1;
            } else {
                cursor.moveToPosition(--position);
            }
        } else if (key == StaticVariate.RANDOM) {
            //随机播放
            do {
                position = (int) (Math.random() * cursor.getCount());
            } while (position == cursor.getCount());
            Log.e(TAG, "random position:" + position);
            cursor.moveToPosition(position);
        }
//        else if (key == StaticVariate.SINGLE) {
            //单曲循环
//        }
        setUIMessage();
        insertToRecentlyList();
        sendBroadcast(new Intent(StaticVariate.ACTION_PREV));
        Log.e(TAG, "上一曲" + position);
    }

    //下一曲
    private void nextSong() {
        int key = preferencesSet.getInt(StaticVariate.keyPlayMode, 0);
        if (key == StaticVariate.ORDER) {
            //顺序播放
            if (cursor.isLast()) {
                cursor.moveToFirst();
                position = 0;
            } else {
                cursor.moveToPosition(++position);
            }
        } else if (key == StaticVariate.RANDOM) {
            //随机播放
            do {
                position = (int) (Math.random() * cursor.getCount());
            } while (position == cursor.getCount());
            Log.e(TAG, "random position:" + position);
            cursor.moveToPosition(position);
        }
//        else if (key == StaticVariate.SINGLE) {
            //单曲循环
//        }
        setUIMessage();
        insertToRecentlyList();
        sendBroadcast(new Intent(StaticVariate.ACTION_NEXT));
        Log.e(TAG, "下一曲" + position);
    }

    //保存当前播放音乐的信息
    private void setUIMessage() {
        editorPlayList.putString(StaticVariate.keyListName, listName);
        editorPlayList.putInt("position", position);
        editorPlayList.putString(StaticVariate.keyPlayTitle,
                cursor.getString(cursor.getColumnIndex(StaticVariate.title)));
        editorPlayList.putString(StaticVariate.keyPlaySinger,
                cursor.getString(cursor.getColumnIndex(StaticVariate.singer)));
        editorPlayList.putString(StaticVariate.fileUrl,
                cursor.getString(cursor.getColumnIndex(StaticVariate.fileUrl)));
        editorPlayList.apply();
        StaticVariate.isSetListPlayIcon = true;
        StaticVariate.isSetFavoriteIcon = true;
        StaticVariate.isInitLyric = true;
        showNotification(context);
    }

    private void initMediaPlayer() {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(cursor.getString(cursor.getColumnIndex(StaticVariate.fileUrl)));
            mediaPlayer.prepare();
            StaticVariate.playDuration = mediaPlayer.getDuration();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    //添加到最近播放
    private void insertToRecentlyList() {
        if (!listName.equals(StaticVariate.recentlySongListTable)) {
            Cursor recentlyCursor = db.rawQuery("select * from " + StaticVariate.
                            recentlySongListTable + " where fileUrl = ?",
                    new String[]{cursor.getString(cursor.getColumnIndex(StaticVariate.fileUrl))});
            if (recentlyCursor.getCount() != 0) {
                db.delete(StaticVariate.recentlySongListTable, StaticVariate.fileUrl + " = ?",
                        new String[]{cursor.getString(cursor.getColumnIndex(StaticVariate.fileUrl))});
            }
            insertSong(db, StaticVariate.recentlySongListTable, cursor, true);
            recentlyCursor.close();
        }
    }

    //注册广播
    private void initReceiver() {

        receiver = new NotificationReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StaticVariate.ACTION_DOWN);
        registerReceiver(receiver, intentFilter);

        Intent favoriteIntent = new Intent(StaticVariate.ACTION_DOWN);
        favoriteIntent.putExtra("id", StaticVariate.ID_FAVORITE);
        favoritePI = PendingIntent.getBroadcast(this,
                StaticVariate.ID_FAVORITE, favoriteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(StaticVariate.ACTION_DOWN);
        prevIntent.putExtra("id", StaticVariate.ID_PREV);
        prevPI = PendingIntent.getBroadcast(this,
                StaticVariate.ID_PREV, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(StaticVariate.ACTION_DOWN);
        playIntent.putExtra("id", StaticVariate.ID_PLAY_OR_PAUSE);
        playPI = PendingIntent.getBroadcast(this,
                StaticVariate.ID_PLAY_OR_PAUSE, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(StaticVariate.ACTION_DOWN);
        nextIntent.putExtra("id", StaticVariate.ID_NEXT);
        nextPI = PendingIntent.getBroadcast(this,
                StaticVariate.ID_NEXT, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent lrcIntent = new Intent(StaticVariate.ACTION_DOWN);
        lrcIntent.putExtra("id", StaticVariate.ID_LRC);
        lrcPI = PendingIntent.getBroadcast(this,
                StaticVariate.ID_LRC, lrcIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(StaticVariate.ACTION_DOWN);
        closeIntent.putExtra("id", StaticVariate.ID_CLOSE);
        closePI = PendingIntent.getBroadcast(this,
                StaticVariate.ID_CLOSE, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//        Intent intent = new Intent(context, PlayActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Intent notificationIntent = new Intent(StaticVariate.ACTION_DOWN);
        notificationIntent.putExtra("id", StaticVariate.ID_NOTIFICATION);
        notificationPI = PendingIntent.getBroadcast(this,
                StaticVariate.ID_NOTIFICATION, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    //显示通知
    public static void showNotification(Context context) {
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
        if (cursor != null) {
            title = cursor.getString(cursor.getColumnIndex(StaticVariate.title));
            singer = cursor.getString(cursor.getColumnIndex(StaticVariate.singer));
        } else {
            title = preferencesPlayList.getString(StaticVariate.keyPlayTitle, StaticVariate.unKnown);
            singer = preferencesPlayList.getString(StaticVariate.keyPlaySinger, StaticVariate.unKnown);
        }
        viewBig.setTextViewText(R.id.tv_song_name, title);
        viewBig.setTextViewText(R.id.tv_singer, singer);
        viewNormal.setTextViewText(R.id.tv_song_name, title);
        viewNormal.setTextViewText(R.id.tv_singer, singer);

        Cursor cursorFavorite = db.rawQuery("select id from "
                        + StaticVariate.favoriteSongListTable
                        + " where " + StaticVariate.fileUrl + " = ?",
                new String[]{cursor.getString(cursor.getColumnIndex(StaticVariate.fileUrl))});
        if (cursorFavorite.getCount() != 0) {
            cursorFavorite.close();
            viewBig.setImageViewResource(R.id.img_favorite,
                    R.drawable.ic_favorite_yes);
        } else {
            viewBig.setImageViewResource(R.id.img_favorite,
                    R.drawable.ic_favorite_no);
        }
        if (StaticVariate.isPlayOrPause) {
            viewBig.setImageViewResource(R.id.img_play_or_pause,
                    R.drawable.ic_play);
            viewNormal.setImageViewResource(R.id.img_play_or_pause,
                    R.drawable.ic_play);
        } else {
            viewBig.setImageViewResource(R.id.img_play_or_pause,
                    R.drawable.ic_pause);
            viewNormal.setImageViewResource(R.id.img_play_or_pause,
                    R.drawable.ic_pause);
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
        Notification notification = new NotificationCompat.Builder(context, "PLAY")
                .setContentTitle("") // 创建通知的标题
                .setContentText("") // 创建通知的内容
                .setSmallIcon(R.mipmap.ic_launcher) // 创建通知的小图标
                .setContent(viewNormal)
                .setCustomBigContentView(viewBig) // 通过设置RemoteViews对象来设置通知的布局，这里我们设置为自定义布局
                .setOngoing(true)//无法划去
                .build();
        notificationManager.notify(1, notification);

    }

    //通知点击事件
    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(StaticVariate.ACTION_DOWN)) {
                int id = intent.getIntExtra("id", 0);
                Log.e(TAG, "id " + id);
                switch (id) {
                    case StaticVariate.ID_FAVORITE:
                        Log.e(TAG, "通知栏收藏");
                        addFavorite(context, db, cursor);
                        StaticVariate.isSetFavoriteIcon = true;
                        showNotification(context);
                        break;
                    case StaticVariate.ID_PREV:
                        Log.e(TAG, "通知栏上一曲");
                        StaticVariate.isPrev = true;
                        break;
                    case StaticVariate.ID_PLAY_OR_PAUSE:
                        Log.e(TAG, "通知播放暂停");
                        StaticVariate.isPlayOrPause = true;
                        StaticVariate.isPause = true;
                        break;
                    case StaticVariate.ID_NEXT:
                        Log.e(TAG, "通知栏下一曲");
                        StaticVariate.isNext = true;
                        break;
                    case StaticVariate.ID_LRC:
                        Log.e(TAG, "通知栏歌词");

                        break;
                    case StaticVariate.ID_CLOSE:
                        Log.e(TAG, "通知关闭");
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        manager.cancelAll();
                        collapseStatusBar();
                        MyApplication.getInstance().AppExit();
                        break;
                    case StaticVariate.ID_NOTIFICATION:
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
            if (action.equals(StaticVariate.ACTION_UPDATE)) {
                Log.e(TAG, "更新通知");
                if (!StaticVariate.isFistPlay) {
                    showNotification(context);
                }
            }
        }
    }

    //停止
    public void onDestroy() {
        run = false;
        mediaPlayer.stop();
        mediaPlayer.release();
        unregisterReceiver(receiver);
        StaticVariate.isFistPlay = true;
        StaticVariate.isPlay = false;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();
        super.onDestroy();
    }

    //收起通知
    public void collapseStatusBar() {
        Object service = getSystemService("statusbar");
//        Object service = getSystemService(Context.VIBRATOR_SERVICE);
        try {
            Method collapse;
//            if (Build.VERSION.SDK_INT <= 16) {
//                collapse = service.getClass().getMethod("collapse");
//            } else {
                collapse = service.getClass().getMethod("collapsePanels");
//            }
            collapse.setAccessible(true);
            collapse.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
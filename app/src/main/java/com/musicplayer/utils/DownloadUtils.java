package com.musicplayer.utils;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.ui.activity.DownloadSongActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * 文件下载工具类（单例模式）
 * Created on 2017/10/16.
 */

public class DownloadUtils {
    private static DownloadUtils downloadUtil;
    private String TAG = "*DownloadUtils";
    private OkHttpClient okHttpClient;
    private static int number = 2;
    private Context mContext;
    private NotificationManager notificationManager;
    //    private SQLiteDatabase db;
    private String filePath;
    private String fileName;
    private String songName;
    private String singer;
    private String songUrl;
    private String songMid;
    private int songType;
    private String table;
    private Cursor cursor = null;
    private Song song = null;

    public DownloadUtils(Context context, String table, Cursor cursor) {
        okHttpClient = new OkHttpClient();
        mContext = context;
        number++;
//        this.db = db;
        this.table = table;
        notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "DOWNLOAD", "DOWNLOAD", NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
//                Log.e(TAG, "createNotificationChannel");
            }
        }
        songName = cursor.getString(cursor.getColumnIndex(Variate.keySongName));
        singer = cursor.getString(cursor.getColumnIndex(Variate.keySinger));
        songUrl = Variate.SONG_PATH + "/" + singer.replace('/', ' ') + " - " + songName + ".mp3";
        songMid = cursor.getString(cursor.getColumnIndex(Variate.keySongMid));
        songType = cursor.getInt(cursor.getColumnIndex(Variate.keySongType));
        fileName = singer.replace('/', ' ') + " - " + songName + ".mp3";
        Log.e(TAG, "songMid " + songMid);
    }

    public DownloadUtils(Context context, String table, Song song) {
        okHttpClient = new OkHttpClient();
        mContext = context;
        number++;
//        this.db = db;
        this.table = table;
        notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "DOWNLOAD", "DOWNLOAD", NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
//                Log.e(TAG, "createNotificationChannel");
            }
        }
        songName = song.getSongName();
        singer = song.getSinger();
        songUrl = Variate.SONG_PATH + "/" + singer.replace('/', ' ') + " - " + songName + ".mp3";
        songMid = song.getSongMid();
        songType = song.getType();
        fileName = singer.replace('/', ' ') + " - " + songName + ".mp3";
        Log.e(TAG, "path " + songUrl);
    }

    //添加音乐
    private void updateDataBase() {
        DataBase dataBase = new DataBase(mContext, Variate.dataBaseName, null, 1);
        SQLiteDatabase db = dataBase.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Variate.keySongName, songName);
        values.put(Variate.keySinger, singer);
        values.put(Variate.keySongUrl, songUrl);
        values.put(Variate.keySongType, Variate.SONG_TYPE_LOCAL);
        values.put(Variate.keySongMid, songMid);
        db.insert(Variate.downloadSongListTable, null, values);
        db.insert(Variate.localSongListTable, null, values);
        Cursor sMId = db.rawQuery("select " + Variate.keySongMenuId + " from " + Variate.songMenuNameTable, null);
        List<String> tableList = new ArrayList<>();
        tableList.add(Variate.favoriteSongListTable);
        tableList.add(Variate.recentlySongListTable);
        if (sMId.getCount() != 0) {
//            int songMenuId[] = new int[sMId.getCount()];
            for (int i = 0; i < sMId.getCount(); i++) {
                sMId.moveToPosition(i);
//                songMenuId[i] = sMId.getInt(0);
                tableList.add(Variate.songMenuTable + "_" + sMId.getInt(0));
            }
        }
        for (String table : tableList) {
//            db.execSQL("update " + table + " set " + Variate.keySongUrl + " = " + songUrl
//                    + "," + Variate.keySongType + " = " + Variate.SONG_TYPE_LOCAL
//                    + " where " + Variate.keySongMid + " = " + songMid + " and "
//                    + Variate.keySongType + " != " + Variate.SONG_TYPE_LOCAL);
            db.update(table,values,Variate.keySongMid + " = ? and "
                    + Variate.keySongType + " != ?",new String[]{songMid,String.valueOf(Variate.SONG_TYPE_LOCAL)});
        }
        dataBase.close();
        db.close();
    }

    public void download(final String url) {
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败监听回调
//                listener.onDownloadFailed(e);
                Log.e(TAG, "onFailure");
                ((Activity) mContext).runOnUiThread(() -> ToastUtils.show(mContext, songName + "onFailure 下载失败"));
            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                File dir = new File(Variate.SONG_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, fileName);
                Log.e(TAG, file.getPath());

                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中更新进度条
                        notificationManager.notify(number, getNotification("正在下载", progress));
                    }
                    fos.flush();
                    updateDataBase();
                    Log.e(TAG, "下载完成 ");
                    notificationManager.notify(number, getNotification("下载完成", -1));
                    ((Activity) mContext).runOnUiThread(() ->
                            ToastUtils.show(mContext, songName + " 下载完成")
                    );
                } catch (IOException e) {
                    ((Activity) mContext).runOnUiThread(() -> ToastUtils.show(mContext, songName + "IOException 下载失败"));
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

//    public interface OnDownloadListener {
//        /**
//         * @param file 下载成功后的文件
//         */
//        void onDownloadSuccess(File file);
//
//        /**
//         * @param progress 下载进度
//         */
//        void onDownloading(int progress);
//
//        /**
//         * @param e 下载异常信息
//         */
//        void onDownloadFailed(Exception e);
//    }

    public void startDownload() {
        ToastUtils.show(mContext, "开始下载");
        NetworkUtils networkUtils = new NetworkUtils();
        networkUtils.getSongInfo(songMid, Song.getTypeByInt(songType), Variate.FILTER_ID);
        networkUtils.setOnGetSongInfoListener(song -> {
            Log.e(TAG, "songUrl " + song.getSongUrl());
            download(song.getSongUrl());
        });

    }

    private Notification getNotification(String title, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "DOWNLOAD");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(title);
        if (progress >= 0) {
            builder.setContentText(songName);
            builder.setProgress(100, progress, false);
        } else {
            Intent intent = new Intent(mContext, DownloadSongActivity.class);
            PendingIntent pI = PendingIntent.getActivity(mContext, 0, intent, 0);
            builder.setContentIntent(pI);
            builder.setAutoCancel(true);
        }
        return builder.build();
    }
}

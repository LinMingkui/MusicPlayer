package com.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.util.Log;

import com.musicplayer.database.DataBase;

import java.io.IOException;

public class PlayUtils {
    private String TAG = "*PlayUtils";
    private static PlayUtils playUtils;
    private static Context mContext;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private DataBase dataBase = new DataBase(mContext, Variate.dataBaseName, null, 1);
    private SQLiteDatabase db = dataBase.getWritableDatabase();
    private SharedPreferences preferencesPlayList = mContext.getSharedPreferences(Variate.playList, Context.MODE_PRIVATE);
    private SharedPreferences preferencesSet = mContext.getSharedPreferences(Variate.set, Context.MODE_PRIVATE);
    private SharedPreferences.Editor editorPlayList = preferencesPlayList.edit();
    private String tableName = Variate.localSongListTable;
    private String songName,singer,songUrl;
    private int position = 0;
    private Cursor cursor;

    public static PlayUtils getInstance(Context context) {
        if (playUtils == null || mContext == null) {
            playUtils = new PlayUtils();
            mContext = context;
        }
        return playUtils;
    }

    private void setPlayMessage(Context context, String tableName, int position) {
        editorPlayList.putString(Variate.keyTableName, tableName);
        editorPlayList.putInt("position", position);
        editorPlayList.apply();
        this.tableName = tableName;
        this.position = position;
    }

    private void init() {
        cursor = db.rawQuery("select * from " + tableName, null);
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            songUrl = cursor.getString(cursor.getColumnIndex(Variate.keySongUrl));
            songName = cursor.getString(cursor.getColumnIndex(Variate.keySongName));
            singer = cursor.getString(cursor.getColumnIndex(Variate.keySinger));
        }
    }

    private void start(){
        class ThreadPlay extends Thread{
            @Override
            public void run() {
//                if

            }
        }
    }
    private void play() {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void prev(){
        int key = preferencesSet.getInt(Variate.keyPlayMode, 0);
        if (key == Variate.ORDER) {
            //顺序播放
            if (cursor.isFirst()) {
                cursor.moveToLast();
                position = cursor.getCount() - 1;
            } else {
                cursor.moveToPosition(--position);
            }
        } else if (key == Variate.RANDOM) {
            //随机播放
            do {
                position = (int) (Math.random() * cursor.getCount());
            } while (position == cursor.getCount());
            Log.e(TAG, "random position:" + position);
            cursor.moveToPosition(position);
        }
    }
}

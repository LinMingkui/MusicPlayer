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
    private DataBase dataBase = new DataBase(mContext, StaticVariate.dataBaseName, null, 1);
    private SQLiteDatabase db = dataBase.getWritableDatabase();
    private SharedPreferences preferencesPlayList = mContext.getSharedPreferences(StaticVariate.playList, Context.MODE_PRIVATE);
    private SharedPreferences preferencesSet = mContext.getSharedPreferences(StaticVariate.keySet, Context.MODE_PRIVATE);
    private SharedPreferences.Editor editorPlayList = preferencesPlayList.edit();
    private String listName = StaticVariate.localSongListTable;
    private String songName,singer,fileUrl;
    private int position = 0;
    private Cursor cursor;

    public static PlayUtils getInstance(Context context) {
        if (playUtils == null || mContext == null) {
            playUtils = new PlayUtils();
            mContext = context;
        }
        return playUtils;
    }

    private void setPlayMessage(Context context, String listName, int position) {
        editorPlayList.putString(StaticVariate.keyListName, listName);
        editorPlayList.putInt("position", position);
        editorPlayList.apply();
        this.listName = listName;
        this.position = position;
    }

    private void init() {
        cursor = db.rawQuery("select * from " + listName, null);
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            fileUrl = cursor.getString(cursor.getColumnIndex(StaticVariate.fileUrl));
            songName = cursor.getString(cursor.getColumnIndex(StaticVariate.title));
            singer = cursor.getString(cursor.getColumnIndex(StaticVariate.singer));
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
            mediaPlayer.setDataSource(fileUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void prev(){
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
    }
}

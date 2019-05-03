package com.musicplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBase extends SQLiteOpenHelper {

    //本地音乐列表
    private static final String CREATE_BOOK_LOCAL = "create table localSongListTable("
            + "songId integer primary key, "
            + "songName text, "
            + "singer text, "
            + "songUrl text, "
            + "songType integer)";
    //下载音乐列表
    private static final String CREATE_BOOK_DOWNLOAD = "create table downloadSongListTable("
            + "songId integer primary key AUTOINCREMENT, "
            + "songName text, "
            + "singer text, "
            + "songUrl text, "
            + "songType integer)";
    //收藏音乐列表
    private static final String CREATE_BOOK_FAVORITE = "create table favoriteSongListTable("
            + "songId integer primary key AUTOINCREMENT, "
            + "songName text, "
            + "singer text, "
            + "songUrl text, "
            + "songType integer)";
    //最近播放音乐列表
    private static final String CREATE_BOOK_RECENTLY = "create table recentlySongListTable("
            + "songId integer primary key AUTOINCREMENT, "
            + "songName text, "
            + "singer text, "
            + "songUrl text, "
            + "songType integer)";
    //本地搜索音乐列表
    private static final String CREATE_BOOK_SEARCH = "create table localSearchSongListTable("
            + "songId integer primary key AUTOINCREMENT, "
            + "songName text, "
            + "singer text, "
            + "songUrl text, "
            + "songType integer)";
    //正在播放音乐列表
    private static final String CREATE_BOOK_PLAY = "create table playListTable("
            + "songId integer primary key AUTOINCREMENT, "
            + "songName text, "
            + "singer text, "
            + "songUrl text, "
            + "songType integer)";

    //歌单列表
    private static final String CREATE_BOOK_SONG_MENU = "create table songMenuNameTable("
            + "songMenuid integer primary key AUTOINCREMENT, "
            + "songMenuName text, "
            + "songNumber integer)";

    public DataBase(Context context, String name,
                    SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK_LOCAL);
        db.execSQL(CREATE_BOOK_DOWNLOAD);
        db.execSQL(CREATE_BOOK_FAVORITE);
        db.execSQL(CREATE_BOOK_RECENTLY);
        db.execSQL(CREATE_BOOK_SONG_MENU);
        db.execSQL(CREATE_BOOK_SEARCH);
        db.execSQL(CREATE_BOOK_PLAY);
        Log.e("DataBase.java", "数据库" + db + "创建成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

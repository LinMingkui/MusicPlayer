package com.musicplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBase extends SQLiteOpenHelper {

    private static final String CREATE_BOOK_LOCAL = "create table localSongListTable("
            + "id integer primary key AUTOINCREMENT, "
            + "title text, "
            + "singer text, "
            + "fileUrl text, "
            + "addTime integer)";
    private static final String CREATE_BOOK_DOWNLOAD = "create table downloadSongListTable("
            + "id integer primary key AUTOINCREMENT, "
            + "title text, "
            + "singer text, "
            + "fileUrl text,"
            + "addTime integer)";
    private static final String CREATE_BOOK_FAVORITE = "create table favoriteSongListTable("
            + "id integer primary key AUTOINCREMENT, "
            + "title text, "
            + "singer text, "
            + "fileUrl text,"
            + "addTime integer)";
    private static final String CREATE_BOOK_RECENTLY = "create table recentlySongListTable("
            + "id integer primary key AUTOINCREMENT, "
            + "title text, "
            + "singer text, "
            + "fileUrl text,"
            + "addTime integer)";
    private static final String CREATE_BOOK_SEARCH = "create table localSearchSongListTable("
            + "id integer primary key AUTOINCREMENT, "
            + "title text, "
            + "singer text, "
            + "fileUrl text,"
            + "addTime integer)";

    private static final String CREATE_BOOK_SONG_MENU = "create table songMenuNameTable("
            + "id integer primary key AUTOINCREMENT, "
            + "songMenuName text, "
            + "songNumber integer,"
            + "addTime integer)";

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
        Log.e("DataBase.java", "数据库" + db + "创建成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

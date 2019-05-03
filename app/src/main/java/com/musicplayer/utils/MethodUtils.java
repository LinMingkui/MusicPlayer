package com.musicplayer.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.SongListAdapter;

import java.util.List;

public class MethodUtils {


    //添加音乐
    public static void addSong(SQLiteDatabase db, String table, Cursor cursor) {
        ContentValues values = new ContentValues();
        values.put(Variate.keySongName, cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
        values.put(Variate.keySinger, cursor.getString(cursor.getColumnIndex(Variate.keySinger)));
        values.put(Variate.keySongUrl, cursor.getString(cursor.getColumnIndex(Variate.keySongUrl)));
        values.put(Variate.keySongType, cursor.getString(cursor.getColumnIndex(Variate.keySongType)));
        db.insert(table, null, values);
    }

    //添加本地音乐到数据库
    public static void saveSong(SQLiteDatabase db, List<Song> songs) {
        ContentValues values;
        for (int i = 0; i < songs.size(); i++) {
            values = new ContentValues();
            values.put(Variate.keySongName, songs.get(i).getSongName());
            values.put(Variate.keySinger, songs.get(i).getSinger());
            values.put(Variate.keySongUrl, songs.get(i).getSongUrl());
            values.put(Variate.keySongType, Variate.SONG_TYPE_LOCAL);
            db.insert(Variate.localSongListTable, null, values);
            values.clear();
            Log.e("*insertSong", songs.get(i).getSongName());
        }
    }

    //添加至播放列表
    public static void savePlayList(SQLiteDatabase db, Cursor cursor) {
        ContentValues values = new ContentValues();
        db.execSQL("delete from " + Variate.playListTable);
//        Log.e("*savePlayList",""+cursor.getCount());
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            new Thread(() -> {
                Log.e("*savePlayList", "" + cursor.getCount());
                for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
                    values.put(Variate.keySongName, cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
                    values.put(Variate.keySinger, cursor.getString(cursor.getColumnIndex(Variate.keySinger)));
                    values.put(Variate.keySongUrl, cursor.getString(cursor.getColumnIndex(Variate.keySongUrl)));
                    values.put(Variate.keySongType, cursor.getString(cursor.getColumnIndex(Variate.keySongType)));
                    db.insert(Variate.playListTable, null, values);
                    Log.e("*savePlayList", cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
                    values.clear();
                }
            }).start();
        }
    }

    //保存当前播放音乐的信息
    public static void setPlayMessage(SharedPreferences.Editor editor, Cursor cursor, String tableName, int position) {
        editor.putString(Variate.keyTableName, tableName);
        editor.putInt("position", position);
        editor.putString(Variate.keySongName,
                cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
        editor.putString(Variate.keySinger,
                cursor.getString(cursor.getColumnIndex(Variate.keySinger)));
        editor.putString(Variate.keySongUrl,
                cursor.getString(cursor.getColumnIndex(Variate.keySongUrl)));
        editor.putString(Variate.keySongType,
                cursor.getString(cursor.getColumnIndex(Variate.keySongType)));
        editor.apply();
    }


    //添加收藏
    public static void addFavorite(Context context, SQLiteDatabase db, Cursor cursorSong) {
        Cursor favoriteCursor = db.rawQuery
                ("select * from " + Variate.favoriteSongListTable + " where " +
                                Variate.keySongUrl + " = ?",
                        new String[]{cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongUrl))});
        if (favoriteCursor.getCount() == 0) {
            addSong(db, Variate.favoriteSongListTable, cursorSong);
            Toast.makeText(context, "已添加至我的收藏", Toast.LENGTH_SHORT).show();
        } else {
            db.delete(Variate.favoriteSongListTable, Variate.keySongUrl + " = ?",
                    new String[]{cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongUrl))});
            Toast.makeText(context, "已从我i的收藏移除", Toast.LENGTH_SHORT).show();
        }
    }

    //添加到歌单对话框
    public static void addSongMenu(final Context context, final SQLiteDatabase db,
                                   final Cursor cursorSong, final View viewAddSongMenuDialog) {
        Log.e("****添加歌单", cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongName)));
        Cursor cursorSongMenuName = db.rawQuery("select * from " + Variate.songMenuNameTable, null);
        final String[] songMenuName = new String[cursorSongMenuName.getCount()];
        for (int i = 0; i < cursorSongMenuName.getCount(); i++) {
            cursorSongMenuName.moveToNext();
            songMenuName[i] = cursorSongMenuName.getString(
                    cursorSongMenuName.getColumnIndex(Variate.keySongMenuName));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("添加到歌单")
                .setItems(songMenuName, (dialog, which) -> {
                    Log.e("***歌单名", songMenuName[which]);
                    //获取歌单id
                    Cursor cursorSongMenuId = db.rawQuery("select " + Variate.keySongMenuId + " from "
                            + Variate.songMenuNameTable
                            + " where "
                            + Variate.keySongMenuName
                            + " = ?", new String[]{songMenuName[which]});
                    if (cursorSongMenuId.getCount() == 0) {
                        Toast.makeText(context,
                                "获取歌单id失败",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        cursorSongMenuId.moveToFirst();
                        int songMenuId = cursorSongMenuId.getInt(0);
                        Log.e("***歌单id", "" + songMenuId);
                        String tableName = Variate.songMenuTable
                                + "_" + songMenuId;
                        //判断是否已存在
                        Cursor cursorSongMenuSong = db.rawQuery("select "
                                        + Variate.keySongUrl
                                        + " from " + tableName
                                        + " where " + Variate.keySongUrl + " = ?",
                                new String[]{cursorSong.getString(
                                        cursorSong.getColumnIndex(Variate.keySongUrl))});
                        if (cursorSongMenuSong.getCount() == 0) {
                            //添加音乐
                            addSongToSongMenu(db, cursorSong, songMenuId);
                            Toast.makeText(context,
                                    "已添加到歌单“" + songMenuName[which] + "”",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context,
                                    "歌曲已存在",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setPositiveButton("新建歌单", (dialog, which) ->
                addSongMenuDialog(context, db, viewAddSongMenuDialog, cursorSong))
                .setNegativeButton("取消", null).show();
    }

    //创建歌单对话框
    public static void addSongMenuDialog(final Context context, final SQLiteDatabase db,
                                         View viewAddSongMenuDialog, final Cursor cursorSong) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(viewAddSongMenuDialog);
        builder.create();
        final AlertDialog dialog = builder.show();

        final EditText editSongMenuName = viewAddSongMenuDialog.
                findViewById(R.id.edit_song_menu_name);
        Button btnOK = viewAddSongMenuDialog.findViewById(R.id.btn_ok);
        Button btnCancel = viewAddSongMenuDialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnOK.setOnClickListener(v -> {
            if (!editSongMenuName.getText().toString().isEmpty()) {
                Cursor cursorSongMenuName = db.rawQuery("select " + Variate.keySongMenuName +
                                " from " + Variate.songMenuNameTable +
                                " where " + Variate.keySongMenuName +
                                " = ?",
                        new String[]{editSongMenuName.getText().toString()});
                //把歌单名插入表
                if (cursorSongMenuName.getCount() == 0) {
                    ContentValues values = new ContentValues();
                    Log.e("****", editSongMenuName.getText().toString());
                    values.put(Variate.keySongMenuName, editSongMenuName.getText().toString());
                    values.put(Variate.keySongNumber, 0);
                    db.insert(Variate.songMenuNameTable, null, values);
                    dialog.dismiss();
                    //获取歌单Id
                    Cursor cursorSongMenuId = db.rawQuery("select " + Variate.keySongMenuId + " from "
                                    + Variate.songMenuNameTable + " where "
                                    + Variate.keySongMenuName + " = ?",
                            new String[]{editSongMenuName.getText().toString()});
                    cursorSongMenuId.moveToFirst();
                    int songMenuId = cursorSongMenuId.getInt(0);
                    //根据歌单Id创建表
                    db.execSQL("create table songMenuTable"
                            + "_" + songMenuId
                            + "(songId integer primary key AUTOINCREMENT, "
                            + "songName text, "
                            + "singer text, "
                            + "songUrl text,"
                            + "songType interger)");
                    addSongToSongMenu(db, cursorSong, songMenuId);
                    Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "歌单已存在", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "歌单名不能为空", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //把音乐添加至歌单
    public static void addSongToSongMenu(SQLiteDatabase db, Cursor cursorSong, int songMenuId) {
        //往表了添加歌
        String tableName = Variate.songMenuTable
                + "_" + songMenuId;
        addSong(db, tableName, cursorSong);
        //设置歌单音乐数量
        Cursor cursorSongNumber = db.rawQuery("select " + Variate.keySongName + " from "
                + tableName, new String[]{});
        db.execSQL("update " + Variate.songMenuNameTable
                + " set " + Variate.keySongNumber + " = "
                + cursorSongNumber.getCount()
                + " where " + Variate.keySongMenuId + " = " + songMenuId);
        Log.e("****歌单音乐数量", "" + cursorSongNumber.getCount());
    }

    //删除音乐
    public static void deleteSong(Context context, SQLiteDatabase db,
                                  String table, Cursor cursorSong, SongListAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("删除").setMessage("确定要删除“"
                + cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongName))
                + "”？").setPositiveButton("确定", (dialog, which) -> {
            db.delete(table, Variate.keySongUrl
                    + " = ?", new String[]{cursorSong.getString(
                    cursorSong.getColumnIndex(Variate.keySongUrl))});
            adapter.changeData();
            adapter.notifyDataSetChanged();
            Log.e("*deleteSong ", table.substring(table.lastIndexOf('_') + 1));
            if (table.lastIndexOf('_') != -1) {
                Cursor cursor = db.rawQuery("select * from " + table, null);
                db.execSQL("update " + Variate.songMenuNameTable
                        + " set " + Variate.keySongNumber + " = "
                        + cursor.getCount()
                        + " where " + Variate.keySongMenuId + " = " + table.substring(table.lastIndexOf('_') + 1));
            }
            //更新当前播放列表
//            if (preferencesPlayList.getString(Variate.keyTableName, "")
//                    .equals(Variate.localSongListTable)) {
//                if (songListItemPosition < preferencesPlayList.getInt("position", 0)) {
//                    Variate.isSubPosition = true;
//                } else if (songListItemPosition == preferencesPlayList.getInt("position", 0)) {
//                    Variate.isEqualPosition = true;
//                }
//                Variate.isDelete = true;
//            }
            Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
        }).setNegativeButton("取消", null).show();
    }

    //根据排序设置获取SQL查询语句
    public static String getSqlBaseOrder(String table, SharedPreferences preferencesSet) {
        StringBuilder sql = new StringBuilder();
        int order;
        switch (table) {
            case Variate.localSongListTable:
                order = preferencesSet.getInt(Variate.keyLocalSort, Variate.SORT_TIME_DESC);
                break;
            case Variate.favoriteSongListTable:
                order = preferencesSet.getInt(Variate.keyFavoriteSort, Variate.SORT_TIME_DESC);
                break;
            case Variate.downloadSongListTable:
                order = preferencesSet.getInt(Variate.keyDownloadSort, Variate.SORT_TIME_DESC);
                break;
            case Variate.recentlySongListTable:
                order = preferencesSet.getInt(Variate.keyRecentlySort, Variate.SORT_TIME_DESC);
                break;
            case Variate.localSearchSongListTable:
                order = preferencesSet.getInt(Variate.keyLocalSearchSort, Variate.SORT_NAME_ASC);
                break;
            default:
                order = preferencesSet.getInt(Variate.keySongMenuSort
                        + "_" + table.substring(table.indexOf("_") + 1), Variate.SORT_TIME_DESC);
                break;

        }
        sql.append("select * from ").append(table).append(" order by ");
        switch (order) {
            case Variate.SORT_NAME_ASC:
                sql.append(Variate.keySongName + " collate localized asc");
                break;
            case Variate.SORT_NAME_DESC:
                sql.append(Variate.keySongName + " collate localized desc");
                break;
            case Variate.SORT_SINGER_ASC:
                sql.append(Variate.keySinger + " collate localized asc");
                break;
            case Variate.SORT_SINGER_DESC:
                sql.append(Variate.keySinger + " collate localized desc");
                break;
            case Variate.SORT_TIME_ASC:
                sql.append(Variate.keySongId + "  asc");
                break;
            case Variate.SORT_TIME_DESC:
                sql.append(Variate.keySongId + " desc");
                break;
            default:
                sql.append(Variate.keySongId + "  asc");
                break;
        }
        return sql.toString();
    }

    // 获取屏幕宽度
    public static int getDbWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}




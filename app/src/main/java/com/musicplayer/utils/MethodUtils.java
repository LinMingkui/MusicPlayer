package com.musicplayer.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.musicplayer.R;
import com.musicplayer.adapter.SongListAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MethodUtils {


    //添加音乐
    public static void addSong(SQLiteDatabase db, String table, Cursor cursor) {
        ContentValues values = new ContentValues();
        values.put(Variate.keySongName, cursor.getString(cursor.getColumnIndex(Variate.keySongName)));
        values.put(Variate.keySinger, cursor.getString(cursor.getColumnIndex(Variate.keySinger)));
        values.put(Variate.keySongUrl, cursor.getString(cursor.getColumnIndex(Variate.keySongUrl)));
        values.put(Variate.keySongType, cursor.getInt(cursor.getColumnIndex(Variate.keySongType)));
        values.put(Variate.keySongMid, cursor.getString(cursor.getColumnIndex(Variate.keySongMid)));
        db.insert(table, null, values);
    }

    //添加在线音乐
    public static void addNetWorkSong(SQLiteDatabase db, String table, Song song) {
        ContentValues values = new ContentValues();
        values.put(Variate.keySongName, song.getSongName());
        values.put(Variate.keySinger, song.getSinger());
        values.put(Variate.keySongUrl, song.getSongUrl());
        values.put(Variate.keySongType, song.getType());
        values.put(Variate.keySongMid, song.getSongMid());
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
            values.put(Variate.keySongMid, "");
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
                    values.put(Variate.keySongType, cursor.getInt(cursor.getColumnIndex(Variate.keySongType)));
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
        editor.putInt(Variate.keySongType,
                cursor.getInt(cursor.getColumnIndex(Variate.keySongType)));
        editor.putString(Variate.keySongMid,
                cursor.getString(cursor.getColumnIndex(Variate.keySongMid)));
        editor.apply();
    }


    //添加本地列表的音乐到收藏
    public static void addToFavorite(Context context, SQLiteDatabase db, Cursor cursorSong) {
        Cursor favoriteCursor = db.rawQuery
                ("select * from " + Variate.favoriteSongListTable + " where " +
                                Variate.keySongUrl + " = ?",
                        new String[]{cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongUrl))});
        if (favoriteCursor.getCount() == 0) {
            addSong(db, Variate.favoriteSongListTable, cursorSong);
            Toast.makeText(context, "已添加至我的收藏", Toast.LENGTH_SHORT).show();
        } else {
//            db.delete(Variate.favoriteSongListTable, Variate.keySongUrl + " = ?",
//                    new String[]{cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongUrl))});
            Toast.makeText(context, "歌曲已存在", Toast.LENGTH_SHORT).show();
        }
    }

    //添加网络音乐到收藏
    public static void addToFavorite(Context context, SQLiteDatabase db, Song song) {
        Cursor favoriteCursor = db.rawQuery
                ("select * from " + Variate.favoriteSongListTable + " where " +
                                Variate.keySongMid + " = ?",
                        new String[]{song.getSongMid()});
        if (favoriteCursor.getCount() == 0) {
            addNetWorkSong(db, Variate.favoriteSongListTable, song);
            Toast.makeText(context, "已添加至我的收藏", Toast.LENGTH_SHORT).show();
        } else {
//            db.delete(Variate.favoriteSongListTable, Variate.keySongMid + " = ?",
//                    new String[]{song.getSongMid()});
            Toast.makeText(context, "歌曲已存在", Toast.LENGTH_SHORT).show();
        }
    }

    //添加正在播放的音乐到收藏
    public static void addToFavorite(Context context, SQLiteDatabase db, SharedPreferences pPlayList) {
        Cursor favoriteCursor;
        if (pPlayList.getInt(Variate.keySongType, Variate.SONG_TYPE_LOCAL) == Variate.SONG_TYPE_LOCAL) {
            favoriteCursor = db.rawQuery
                    ("select * from " + Variate.favoriteSongListTable + " where " +
                                    Variate.keySongUrl + " = ?",
                            new String[]{pPlayList.getString(Variate.keySongUrl, Variate.unKnown)});
            if (favoriteCursor.getCount() == 0) {
                addPlaySongToTable(db, pPlayList, Variate.favoriteSongListTable);
                Toast.makeText(context, "已添加至我的收藏", Toast.LENGTH_SHORT).show();
            } else {
                db.delete(Variate.favoriteSongListTable, Variate.keySongUrl + " = ?",
                        new String[]{pPlayList.getString(Variate.keySongUrl, Variate.unKnown)});
                Toast.makeText(context, "已从我的收藏移除", Toast.LENGTH_SHORT).show();
            }
        } else {
            favoriteCursor = db.rawQuery
                    ("select * from " + Variate.favoriteSongListTable + " where " +
                                    Variate.keySongMid + " = ?",
                            new String[]{pPlayList.getString(Variate.keySongMid, Variate.unKnown)});
            if (favoriteCursor.getCount() == 0) {
                addPlaySongToTable(db, pPlayList, Variate.favoriteSongListTable);
                Toast.makeText(context, "已添加至我的收藏", Toast.LENGTH_SHORT).show();
            } else {
                db.delete(Variate.favoriteSongListTable, Variate.keySongMid + " = ?",
                        new String[]{pPlayList.getString(Variate.keySongMid, Variate.unKnown)});
                Toast.makeText(context, "已从我的收藏移除", Toast.LENGTH_SHORT).show();
            }
        }
        favoriteCursor.close();
    }

    //把正在播放的音乐添加到某个表
    public static void addPlaySongToTable(SQLiteDatabase db, SharedPreferences pPlayList, String table) {
        ContentValues values = new ContentValues();
        values.put(Variate.keySongName, pPlayList.getString(Variate.keySongName, Variate.unKnown));
        values.put(Variate.keySinger, pPlayList.getString(Variate.keySinger, Variate.unKnown));
        values.put(Variate.keySongUrl, pPlayList.getString(Variate.keySongUrl, Variate.unKnown));
        values.put(Variate.keySongType, pPlayList.getInt(Variate.keySongType, Variate.SONG_TYPE_LOCAL));
        values.put(Variate.keySongMid, pPlayList.getString(Variate.keySongMid, Variate.unKnown));
        db.insert(table, null, values);
    }

    //添加到歌单对话框
    public static void addSongMenu(Context context, SQLiteDatabase db,
                                   Cursor cursorSong, View viewAddSongMenuDialog) {
        Log.e("****添加歌单", cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongName)));
        Cursor cursorSongMenuName = db.rawQuery("select * from " + Variate.songMenuNameTable, null);
        String[] songMenuName = new String[cursorSongMenuName.getCount()];
        for (int i = 0; i < cursorSongMenuName.getCount(); i++) {
            cursorSongMenuName.moveToNext();
            songMenuName[i] = cursorSongMenuName.getString(
                    cursorSongMenuName.getColumnIndex(Variate.keySongMenuName));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("添加到歌单").setItems(songMenuName, (dialog, which) -> {
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
                Cursor cursorSongMenuSong;
                if (cursorSong.getInt(cursorSong.getColumnIndex(Variate.keySongType)) == Variate.SONG_TYPE_LOCAL) {
                    cursorSongMenuSong = db.rawQuery("select "
                                    + Variate.keySongUrl
                                    + " from " + tableName
                                    + " where " + Variate.keySongUrl + " = ?",
                            new String[]{cursorSong.getString(
                                    cursorSong.getColumnIndex(Variate.keySongUrl))});
                } else {
                    cursorSongMenuSong = db.rawQuery("select "
                                    + Variate.keySongUrl
                                    + " from " + tableName
                                    + " where " + Variate.keySongMid + " = ?",
                            new String[]{cursorSong.getString(
                                    cursorSong.getColumnIndex(Variate.keySongMid))});
                }
                if (cursorSongMenuSong.getCount() == 0) {

                    //添加音乐
                    addSongToSongMenu(db, cursorSong, songMenuId);
                    Toast.makeText(context, "已添加到歌单“" + songMenuName[which] + "”", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "歌曲已存在", Toast.LENGTH_SHORT).show();
                }
                cursorSongMenuSong.close();
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
                            + "songUrl text, "
                            + "songType integer, "
                            + "songMid text)");
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
        ContentValues values = new ContentValues();
        values.put(Variate.keySinger, cursorSong.getString(cursorSong.getColumnIndex(Variate.keySinger)));
        db.update(Variate.songMenuNameTable, values,
                Variate.keySongMenuId + "=?", new String[]{String.valueOf(songMenuId)});
        Log.e("****歌单音乐数量", "" + cursorSongNumber.getCount());
    }

    //删除音乐
    public static void deleteSong(Context context, SQLiteDatabase db,
                                  String table, Cursor cursorSong, SongListAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("删除").setMessage("确定要删除“"
                + cursorSong.getString(cursorSong.getColumnIndex(Variate.keySongName))
                + "”？").setPositiveButton("确定", (dialog, which) -> {
            if (cursorSong.getInt(cursorSong.getColumnIndex(Variate.keySongType)) == Variate.SONG_TYPE_LOCAL) {
                db.delete(table, Variate.keySongUrl
                        + " = ?", new String[]{cursorSong.getString(
                        cursorSong.getColumnIndex(Variate.keySongUrl))});
            } else {
                db.delete(table, Variate.keySongMid
                        + " = ?", new String[]{cursorSong.getString(
                        cursorSong.getColumnIndex(Variate.keySongMid))});
            }
            adapter.changeData();
            adapter.notifyDataSetChanged();
            Log.e("*deleteSong ", table.substring(table.lastIndexOf('_') + 1));
            //歌单
            if (table.lastIndexOf('_') != -1) {
                Cursor cursor = db.rawQuery("select * from " + table, null);
                String songMenuId = table.substring(table.lastIndexOf('_') + 1);
                db.execSQL("update " + Variate.songMenuNameTable
                        + " set " + Variate.keySongNumber + " = "
                        + cursor.getCount()
                        + " where " + Variate.keySongMenuId + " = " + songMenuId);
                cursor = db.rawQuery("select max(songId) from " + Variate.songMenuTable + "_" + songMenuId, null);
                cursor.moveToFirst();
                int maxid = cursor.getInt(0);
                cursor = db.rawQuery("select " + Variate.keySinger + " from " + table + " where "
                        + Variate.keySongId + "=?", new String[]{String.valueOf(maxid)});
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    ContentValues values = new ContentValues();
                    values.put(Variate.keySinger, cursor.getString(0));
                    db.update(Variate.songMenuNameTable, values,
                            Variate.keySongMenuId + "=?", new String[]{songMenuId});
                }
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

    public static void savePic(Context context, String url, File filePic) {

        Bitmap bitmap = null;
        try {
            bitmap = Glide.with(context).asBitmap().load(url)
                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
            if (bitmap != null) {
                File file = new File(Variate.PIC_PATH);
                if (!file.exists()) {
                    file.mkdirs();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(filePic);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}




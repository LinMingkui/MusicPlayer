package com.musicplayer.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.SongListAdapter;

import static com.musicplayer.utils.TimeUtils.getMSTime;

public class MethodUtils {

    //添加音乐
    public static void insertSong(SQLiteDatabase db, String table, Cursor cursor, boolean setTime) {
        ContentValues values = new ContentValues();
        values.put(StaticVariate.title, cursor.getString(cursor.getColumnIndex(StaticVariate.title)));
        values.put(StaticVariate.singer, cursor.getString(cursor.getColumnIndex(StaticVariate.singer)));
        values.put(StaticVariate.fileUrl, cursor.getString(cursor.getColumnIndex(StaticVariate.fileUrl)));
        if (setTime) {
            values.put(StaticVariate.addTime, getMSTime());
        } else {
            values.put(StaticVariate.addTime, cursor.getLong(cursor.getColumnIndex(StaticVariate.addTime)));
        }
        db.insert(table, null, values);
    }

    //添加音乐
    public static void insertSong(SQLiteDatabase db, String table, Song song) {
        ContentValues values = new ContentValues();
        values.put(StaticVariate.title, song.getTitle());
        values.put(StaticVariate.singer, song.getSinger());
        values.put(StaticVariate.fileUrl, song.getFileUrl());
        values.put(StaticVariate.addTime, getMSTime());
        db.insert(table, null, values);
    }

    //本地音乐搜索
    public static Cursor searchLocalSong(SQLiteDatabase db, String table, String searchText) {
        Cursor cursor = db.rawQuery(
                "select * from "
                        + table
                        + " where "
                        + StaticVariate.title + " like ?"
                        + " or "
                        + StaticVariate.singer + " like ?"
                , new String[]{"%" + searchText + "%", "%" + searchText + "%"});
        cursor.moveToFirst();
        return cursor;
    }

    //添加收藏
    public static void addFavorite(Context context, SQLiteDatabase db, Cursor cursorSong) {
        Cursor favoriteCursor = db.rawQuery
                ("select * from " + StaticVariate.favoriteSongListTable + " where " +
                                StaticVariate.fileUrl + " = ?",
                        new String[]{cursorSong.getString(cursorSong.getColumnIndex(StaticVariate.fileUrl))});
        if (favoriteCursor.getCount() == 0) {
            insertSong(db, StaticVariate.favoriteSongListTable, cursorSong, true);
            Toast.makeText(context, "已添加至我的收藏", Toast.LENGTH_SHORT).show();
        } else {
            db.delete(StaticVariate.favoriteSongListTable, StaticVariate.fileUrl + " = ?",
                    new String[]{cursorSong.getString(cursorSong.getColumnIndex(StaticVariate.fileUrl))});
            Toast.makeText(context, "已从我的收藏移除", Toast.LENGTH_SHORT).show();
        }
    }

    //添加到歌单
    public static void addSongMenu(final Context context, final SQLiteDatabase db,
                                   final Cursor cursorSong, final View viewAddSongMenuDialog) {
        Log.e("****添加歌单", cursorSong.getString(cursorSong.getColumnIndex(StaticVariate.title)));
        Cursor cursorSongMenuName = db.query(StaticVariate.songMenuNameTable,
                null, null, null, null, null, null);
        final String[] songMenuName = new String[cursorSongMenuName.getCount()];
        for (int i = 0; i < cursorSongMenuName.getCount(); i++) {
            cursorSongMenuName.moveToNext();
            songMenuName[i] = cursorSongMenuName.getString(
                    cursorSongMenuName.getColumnIndex(StaticVariate.songMenuName));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("添加到歌单")
                .setItems(songMenuName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("***歌单名", songMenuName[which]);
                        //获取歌单id
                        Cursor cursorSongMenuId = db.rawQuery("select id from "
                                + StaticVariate.songMenuNameTable
                                + " where "
                                + StaticVariate.songMenuName
                                + " = ?", new String[]{songMenuName[which]});
                        if (cursorSongMenuId.getCount() == 0) {
                            Toast.makeText(context,
                                    "获取歌单id失败",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            cursorSongMenuId.moveToFirst();
                            int id = cursorSongMenuId.getInt(cursorSongMenuId.getColumnIndex("id"));
                            Log.e("***歌单id", "" + id);
                            String tableName = StaticVariate.songMenuTable
                                    + "_" + id;
                            //判断是否已存在
                            Cursor cursorSongMenuSong = db.rawQuery("select "
                                            + StaticVariate.fileUrl
                                            + " from " + tableName
                                            + " where " + StaticVariate.fileUrl + " = ?",
                                    new String[]{cursorSong.getString(
                                            cursorSong.getColumnIndex(StaticVariate.fileUrl))});
                            if (cursorSongMenuSong.getCount() == 0) {
                                //添加音乐
                                addSong(db, cursorSong, id);
                                Toast.makeText(context,
                                        "已添加到歌单“" + songMenuName[which] + "”",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context,
                                        "歌曲已存在",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).setPositiveButton("新建歌单", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addSongMenuDialog(context, db, viewAddSongMenuDialog, cursorSong);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        }).show();
    }

    //创建歌单对话框
    public static void addSongMenuDialog(final Context context, final SQLiteDatabase db,
                                         View viewAddSongMenuDialog, final Cursor cursorSong) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(viewAddSongMenuDialog);
        builder.create();
        final AlertDialog dialog = builder.show();

//        WindowManager windowManager = getWindowManager();
//        Display display = windowManager.getDefaultDisplay();
//        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
//        lp.width = (int) (display.getWidth() * 0.7); //设置宽度
//        dialog.getWindow().setAttributes(lp);

        final EditText editSongMenuName = viewAddSongMenuDialog.
                findViewById(R.id.edit_song_menu_name);
        Button btnOK = viewAddSongMenuDialog.findViewById(R.id.btn_ok);
        Button btnCancel = viewAddSongMenuDialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editSongMenuName.getText().toString().isEmpty()) {
                    Cursor cursorSongMenuName = db.rawQuery("select " + StaticVariate.songMenuName +
                                    " from " + StaticVariate.songMenuNameTable +
                                    " where " + StaticVariate.songMenuName +
                                    " = ?",
                            new String[]{editSongMenuName.getText().toString()});
                    //把歌单名插入表
                    if (cursorSongMenuName.getCount() == 0) {
                        ContentValues values = new ContentValues();
                        Log.e("****", editSongMenuName.getText().toString());
                        values.put(StaticVariate.songMenuName, editSongMenuName.getText().toString());
                        values.put(StaticVariate.songNumber, 0);
                        db.insert(StaticVariate.songMenuNameTable,
                                null, values);
                        dialog.dismiss();
                        //获取歌单Id
                        Cursor cursorSongMenuId = db.rawQuery("select id from "
                                        + StaticVariate.songMenuNameTable + " where "
                                        + StaticVariate.songMenuName + " = ?",
                                new String[]{editSongMenuName.getText().toString()});
                        cursorSongMenuId.moveToFirst();
                        int id = cursorSongMenuId.getInt(cursorSongMenuId.getColumnIndex("id"));
                        //根据歌单Id创建表
                        db.execSQL("create table songMenuTable"
                                + "_" + id
                                + "(id integer primary key AUTOINCREMENT, "
                                + "title text, "
                                + "singer text, "
                                + "fileUrl text,"
                                + "addTime interger)");
                        addSong(db, cursorSong, id);
                        Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "歌单已存在", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "歌单名不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public static void addSong(SQLiteDatabase db, Cursor cursorSong, int id) {
        //往表了添加歌
        String tableName = StaticVariate.songMenuTable
                + "_" + id;
        insertSong(db, tableName, cursorSong, true);
        //设置歌单音乐数量
        Cursor cursorSongNumber = db.rawQuery("select id from "
                + tableName, new String[]{});
        db.execSQL("update " + StaticVariate.songMenuNameTable
                + " set " + StaticVariate.songNumber + " = "
                + cursorSongNumber.getCount()
                + " where id = " + id);
        Log.e("****歌单音乐数量", "" + cursorSongNumber.getCount());
    }

    //删除音乐
    public static void deleteSong(final Context context, final SQLiteDatabase db,
                                  final String table, final Cursor cursorSong,
                                  final SharedPreferences preferencesPlayList,
                                  final int songListItemPosition,
                                  final SongListAdapter songListAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("删除").setMessage("确定要删除“"
                + cursorSong.getString(cursorSong.getColumnIndex(StaticVariate.title))
                + "”？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.delete(table, StaticVariate.fileUrl
                        + " = ?", new String[]{cursorSong.getString(
                        cursorSong.getColumnIndex(StaticVariate.fileUrl))});
                //更新当前播放列表
                if (preferencesPlayList.getString(StaticVariate.keyListName, "")
                        .equals(StaticVariate.localSongListTable)) {
                    if (songListItemPosition < preferencesPlayList.getInt("position", 0)) {
                        StaticVariate.isSubPosition = true;
                    } else if (songListItemPosition == preferencesPlayList.getInt("position", 0)) {
                        StaticVariate.isEqualPosition = true;
                    }
                    StaticVariate.isDelete = true;
                }
                songListAdapter.changeData();
                songListAdapter.notifyDataSetChanged();
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    //排序
    public static void showOrderPopupMenu(Context context, View view,
                                          final SQLiteDatabase db,
                                          final String table,
                                          SharedPreferences preferencesSet,
                                          final SharedPreferences.Editor editorSet,
                                          final Handler changeUIHandler,
                                          final ProgressDialog progressDialog,
                                          final String keyOrder) {
        final PopupMenu pm = new PopupMenu(context, view);
        pm.getMenuInflater().inflate(R.menu.menu_popupmenu_order_way, pm.getMenu());
        final int order = preferencesSet.getInt(keyOrder, -1);
        if (order == -1) {
            pm.getMenu().getItem(2).setChecked(true);
            editorSet.putInt(keyOrder, StaticVariate.ORDER_ADD_TIME_ASC);
            editorSet.apply();
        }
        if (order == StaticVariate.ORDER_TITLE_ASC || order == StaticVariate.ORDER_TITLE_DESC) {
            pm.getMenu().getItem(0).setChecked(true);
        } else if (order == StaticVariate.ORDER_SINGER_ASC || order == StaticVariate.ORDER_SINGER_DESC) {
            pm.getMenu().getItem(1).setChecked(true);
        } else if (order == StaticVariate.ORDER_ADD_TIME_ASC || order == StaticVariate.ORDER_ADD_TIME_DESC) {
            pm.getMenu().getItem(2).setChecked(true);
        }

        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Cursor cursorSong;
                int ORDER = -1;
                String sql = "";
                switch (item.getItemId()) {
                    case R.id.item_base_title:
                        if (order == StaticVariate.ORDER_TITLE_ASC) {
                            sql = "select * from " + table + " order by "
                                    + StaticVariate.title + " collate localized desc";
                            ORDER = StaticVariate.ORDER_TITLE_DESC;
                        } else {
                            sql = "select * from " + table + " order by " + StaticVariate.title
                                    + " collate localized asc";
                            ORDER = StaticVariate.ORDER_TITLE_ASC;
                        }
                        break;
                    case R.id.item_base_singer:
                        if (order == StaticVariate.ORDER_SINGER_ASC) {
                            sql = "select * from " + table + " order by " + StaticVariate.singer
                                    + " collate localized desc";
                            ORDER = StaticVariate.ORDER_SINGER_DESC;
                        } else {
                            sql = "select * from " + table + " order by " + StaticVariate.singer
                                    + " collate localized asc";
                            ORDER = StaticVariate.ORDER_SINGER_ASC;
                        }
                        break;
                    case R.id.item_base_add_time:
                        if (order == StaticVariate.ORDER_ADD_TIME_ASC) {
                            sql = "select * from " + table + " order by "
                                    + StaticVariate.addTime + " desc";
                            ORDER = StaticVariate.ORDER_ADD_TIME_DESC;
                        } else {
                            sql = "select * from " + table + " order by "
                                    + StaticVariate.addTime + " asc";
                            ORDER = StaticVariate.ORDER_ADD_TIME_ASC;
                        }
                        break;
                }
                progressDialog.show();
                cursorSong = db.rawQuery(sql, null);
                editorSet.putInt(keyOrder, ORDER);
                editorSet.apply();
                cursorSong.moveToFirst();
                SaveSongThread saveSongThread = new SaveSongThread(cursorSong);
                saveSongThread.start();
                pm.dismiss();
                return false;
            }

            //把音乐列表存入数据库
            class SaveSongThread extends Thread {

                Cursor cursorSong;

                public SaveSongThread(Cursor cursorSong) {
                    this.cursorSong = cursorSong;
                }

                public void run() {
                    db.execSQL("delete from " + table);
                    for (int i = 0; i < cursorSong.getCount(); i++) {
                        cursorSong.moveToPosition(i);
                        insertSong(db, table, cursorSong, false);
                    }
                    Message message = new Message();
                    message.what = StaticVariate.ORDER_COMPLETE;
                    changeUIHandler.sendMessage(message);
                }

            }
        });
        pm.show();
    }

    //根据排序设置获取SQL查询语句
    public static String getSqlBaseOrder(String table, SharedPreferences preferencesSet) {
        String sql;
        int order;
        String tempTable;

        if(table.lastIndexOf("_") == -1){
            tempTable = table;
        }else {
            tempTable = table.substring(0,table.lastIndexOf("_"));
            Log.e("***",table.substring(0,table.lastIndexOf("_")));
            Log.e("***",StaticVariate.keySongMenuOrder
                    + "_" + table.substring(table.indexOf("_")+1));
        }
        switch (tempTable) {
            case StaticVariate.localSongListTable:
                order = preferencesSet.getInt(StaticVariate.keyLocalOrder, -1);
                break;
            case StaticVariate.favoriteSongListTable:
                order = preferencesSet.getInt(StaticVariate.keyFavoriteOrder, -1);
                break;
            case StaticVariate.songMenuTable:
                order = preferencesSet.getInt(StaticVariate.keySongMenuOrder
                        + "_" + table.substring(table.indexOf("_")+1), -1);
                break;
            default:
                sql = "select * from " + table;
                return sql;

        }
        switch (order) {
            case StaticVariate.ORDER_TITLE_ASC:
                sql = "select * from " + table + " order by "
                        + StaticVariate.title + " collate localized asc";
                break;
            case StaticVariate.ORDER_TITLE_DESC:
                sql = "select * from " + table + " order by "
                        + StaticVariate.title + " collate localized desc";
                break;
            case StaticVariate.ORDER_SINGER_ASC:
                sql = "select * from " + table + " order by "
                        + StaticVariate.singer + " collate localized asc";
                break;
            case StaticVariate.ORDER_SINGER_DESC:
                sql = "select * from " + table + " order by "
                        + StaticVariate.singer + " collate localized desc";
                break;
            case StaticVariate.ORDER_ADD_TIME_ASC:
                sql = "select * from " + table + " order by "
                        + StaticVariate.addTime + "  asc";
                break;
            case StaticVariate.ORDER_ADD_TIME_DESC:
                sql = "select * from " + table + " order by "
                        + StaticVariate.addTime + " desc";
                break;
            default:
                sql = "select * from " + table + " order by "
                        + StaticVariate.addTime + "  asc";
        }
        return sql;
    }

    // 获取屏幕宽度
    public static int getDbWidth(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}




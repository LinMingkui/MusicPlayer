package com.musicplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import com.musicplayer.service.PlayService;

import java.io.File;
import java.util.ArrayList;


public class AudioUtils {

    /**
     * 获取sd卡所有的音乐文件
     */
    public static ArrayList<Song> getAllSongs(Context context) {

        ArrayList<Song> songs;

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[]{"audio/mpeg", "audio/x-ms-wma"}, null);

        songs = new ArrayList<>();

        if (cursor.moveToFirst()) {

            Song song;

            do {
                song = new Song();
                // 文件名
                song.setFileName(cursor.getString(1));
                // 歌曲名
                song.setTitle(cursor.getString(2));
                // 时长
                song.setDuration(cursor.getInt(3));
                // 歌手名
                song.setSinger(cursor.getString(4));
                // 专辑名
                song.setAlbum(cursor.getString(5));
                // 年代
                if (cursor.getString(6) != null) {
                    song.setYear(cursor.getString(6));
                } else {
                    song.setYear("未知");
                }
                // 歌曲格式
                if ("audio/mpeg".equals(cursor.getString(7).trim())) {
                    song.setType("mp3");
                } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
                    song.setType("wma");
                }
                // 文件大小
                if (cursor.getString(8) != null) {
                    float size = cursor.getInt(8) / 1024f / 1024f;
                    song.setSize((size + "").substring((size + "").indexOf("."), (size + "").indexOf(".") + 2) + "M");
                } else {
                    song.setSize("未知");
                }
                // 文件路径
                if (cursor.getString(9) != null) {
                    song.setFileUrl(cursor.getString(9));
                }
                songs.add(song);
            } while (cursor.moveToNext());

            cursor.close();

        }
        return songs;
    }

    public static void startPlay(Context context, SharedPreferences.Editor editor, String table, int position) {
        editor.putString(StaticVariate.keyListName, table);
        editor.putInt("position", position);
        editor.apply();
        Intent intent = new Intent(context, PlayService.class);
        context.stopService(intent);
        context.startService(intent);
    }

    public static Song getSong(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Song song = new Song();
        try {
            File file = new File(path);
            float size = file.length() / 1024f / 1024f;
            song.setSize((size + "").substring((size + "").indexOf("."), (size + "").indexOf(".") + 2) + "M");
            mmr.setDataSource(path);
            if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) != null) {
                song.setTitle(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            } else {
                song.setTitle(path.substring(path.lastIndexOf("/") + 1));
            }
            song.setFileName(path.substring(path.lastIndexOf("/") + 1));
//            Log.e("fileName",path.substring(path.lastIndexOf("/") + 1));
            song.setFileUrl(path);
            if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) != null) {
                song.setSinger(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            } else {
                song.setSinger("未知");
            }
            song.setDuration(Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
            if ("audio/mpeg".equals(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE))) {
                song.setType("mp3");
            } else if ("audio/x-ms-wma".equals(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE))) {
                song.setType("wma");
            }
            if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR) != null) {
                song.setYear(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR));
            } else {
                song.setYear("未知");
            }
            song.setAlbum(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));

//          byte[] pic = mmr.getEmbeddedPicture();  // 图片，可以通过BitmapFactory.decodeByteArray转换为bitmap图片
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("e", "错");
        }
        return song;
    }
}


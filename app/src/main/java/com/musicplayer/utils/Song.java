package com.musicplayer.utils;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;

public class Song {

    private String fileName;
    private String songName;
    private int duration;
    private String singer;
    private String singerUrl;
    private String album;
    private String year;
    private int type;
    private String size;
    private String songUrl;
    private String songMid;
    private String lrc;

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }
    public String getSingerUrl() {
        return singerUrl;
    }

    public void setSingerUrl(String singerUrl) {
        this.singerUrl = singerUrl;
    }
    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getSongMid() {
        return songMid;
    }

    public void setSongMid(String QQSongMid) {
        this.songMid = QQSongMid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Song() {
        super();
    }

    public Song(String fileName, String songName, int duration, String singer,
                String album, String year, int type, String size, String songUrl) {
        super();
        this.fileName = fileName;
        this.songName = songName;
        this.duration = duration;
        this.singer = singer;
        this.album = album;
        this.year = year;
        this.type = type;
        this.size = size;
        this.songUrl = songUrl;
    }

    @Override
    public String toString() {
        return "Song [fileName=" + fileName + ", songName=" + songName
                + ", duration=" + duration + ", singer=" + singer + ", album="
                + album + ", year=" + year + ", type=" + type + ", size="
                + size + ", songUrl=" + songUrl + "]";
    }

    public static int getTypeByString(String SType) {
        int IType;
        switch (SType) {
            case "qq":
                IType = Variate.SONG_TYPE_QQ;
                break;
            case "kugou":
                IType = Variate.SONG_TYPE_KG;
                break;
            case "netease":
                IType = Variate.SONG_TYPE_WYY;
                break;
            default:
                IType = Variate.SONG_TYPE_LOCAL;
                break;
        }
        return IType;
    }

    public static String getTypeByInt(int IType) {
        String SType;
        switch (IType) {
            case Variate.SONG_TYPE_QQ:
                SType = "qq";
                break;
            case Variate.SONG_TYPE_KG:
                SType = "kugou";
                break;
            case Variate.SONG_TYPE_WYY:
                SType = "netease";
                break;
            default:
                SType = "local";
                break;
        }
        return SType;
    }

    public static Cursor songToCursor(Song song){
        String[] COLUMN_NAME = {Variate.keySongId,Variate.keySongName,Variate.keySinger,
                Variate.keySongUrl,Variate.keySongType,Variate.keySongMid};
        MatrixCursor cursor = new MatrixCursor(COLUMN_NAME);
        MatrixCursor.RowBuilder builder = cursor.newRow();
        builder.add(Variate.keySongName,song.getSongName());
        builder.add(Variate.keySinger,song.getSinger());
        builder.add(Variate.keySongUrl,song.getSongUrl());
        builder.add(Variate.keySongType,song.getType());
        builder.add(Variate.keySongMid,song.getSongMid());
        cursor.moveToFirst();
        return cursor;
    }

    public static Song getPlayingSong(SharedPreferences sP){
        Song song = new Song();
        song.setSongName(sP.getString(Variate.keySongName, ""));
        song.setSinger(sP.getString(Variate.keySinger, ""));
        song.setSongUrl(sP.getString(Variate.keySongUrl, ""));
        song.setType(sP.getInt(Variate.keySongType, Variate.SONG_TYPE_LOCAL));
        song.setSongMid(sP.getString(Variate.keySongMid, ""));
        return song;
    }


}

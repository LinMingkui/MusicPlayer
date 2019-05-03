package com.musicplayer.utils;

public class Song {

    private String fileName;
    private String songName;
    private int duration;
    private String singer;
    private String album;
    private String year;
    private String type;
    private String size;
    private String songUrl;

    private String QQSongMid;

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getQQSongMid() {
        return QQSongMid;
    }
    public void setQQSongMid(String QQSongMid) {
        this.QQSongMid = QQSongMid;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
                String album, String year, String type, String size, String songUrl) {
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


}

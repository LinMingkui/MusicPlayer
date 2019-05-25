package com.musicplayer.utils;

import android.os.Environment;

public class Variate {
    //音乐列表数据库名
    public static final String dataBaseName = "DataBase.db";
    //本地音乐列表数据库表名
    public static final String localSongListTable = "localSongListTable";
    //下载管理音乐列表数据库表名
    public static final String downloadSongListTable = "downloadSongListTable";
    //我的收藏音乐列表数据库表名
    public static final String favoriteSongListTable = "favoriteSongListTable";
    //最近播放音乐列表数据库表名
    public static final String recentlySongListTable = "recentlySongListTable";
    //本地音乐搜索列表数据库表名
    public static final String localSearchSongListTable = "localSearchSongListTable";
    //歌单名数据库表名
    public static final String songMenuNameTable = "songMenuNameTable";
    //播放列表数据库表名
    public static final String playListTable = "playListTable";
    //歌单数据库表名
    public static final String songMenuTable = "songMenuTable";

    public static final String SONG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/juhemusic/song";
    public static final String LRC_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/juhemusic/lrc";
    public static final String PIC_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/juhemusic/pic";

    public static final String ACTION_DOWN = "com.musicplay.NOTIFICATION_VIEW_CLICK";

    public static final String ACTION_UPDATE = "com.musicplay.NOTIFICATION_UPDATE";

    public static final String ACTION_PREV = "com.musicplay.UI_PREV";

    public static final String ACTION_NEXT = "com.musicplay.UI_NEXT";

    public static final int ID_FAVORITE = 1;
    public static final int ID_PREV = 2;
    public static final int ID_PLAY_OR_PAUSE = 3;
    public static final int ID_NEXT = 4;
    public static final int ID_LRC = 5;
    public static final int ID_CLOSE = 6;
    public static final int ID_NOTIFICATION = 7;

    //歌曲类型
    public static final int SONG_TYPE_LOCAL = 1;
    public static final int SONG_TYPE_QQ = 2;
    public static final int SONG_TYPE_KG = 3;
    public static final int SONG_TYPE_WYY = 4;

    //按什么（Mid、Name）搜索
    public static final String FILTER_ID = "id";
    public static final String FILTER_NAME = "name";
    //保持播放信息的文件名
    public static final String playList = "playList";
    //保存软件配置的文件名
    public static final String set = "set";
    //音乐地址
    public static final String keySongUrl = "songUrl";
    //歌手
    public static final String keySinger = "singer";
    //歌曲名
    public static final String keySongName = "songName";
    //歌曲id
    public static final String keySongId = "songId";
    //歌曲数量
    public static final String keySongNumber = "songNumber";
    //歌曲类型
    public static final String keySongType = "songType";
    //歌曲类型
    public static final String keySongMid = "songMid";

    //表名
    public static final String keyTableName = "tableName";
    //歌单id
    public static final String keySongMenuId = "songMenuId";
    //歌单名
    public static final String keySongMenuName = "songMenuName";

    //播放模式
    public static final String keyPlayMode = "playMode";
    //本地音乐排序方式
    public static final String keyLocalSort = "localSort";
    //我的收藏排序方式
    public static final String keyFavoriteSort = "favoriteSort";
    //下载列表排序方式
    public static final String keyDownloadSort = "downloadSort";
    //最近播放排序方式
    public static final String keyRecentlySort = "recentlySort";
    //歌单里音乐排序方式
    public static final String keySongMenuSort = "songMenuSort";
    //本地搜索列表排序方式
    public static final String keyLocalSearchSort = "localSearchSort";
    //首页歌单是否展开
    public static final String keySongMenuExpand = "songMenuExpand";
    //播放的音乐是否为本地音乐
    public static final String keyIsLocal = "isLocal";
    //是否初始化歌词
    public static final String keyIsInitLocalList = "isInitLocalList";

    //根据音乐名升序排序
    public static final int SORT_NAME_ASC = 0;
    //根据音乐名降序排序
    public static final int SORT_NAME_DESC = 1;
    //根据歌手名升序排序
    public static final int SORT_SINGER_ASC = 2;
    //根据歌手名降序排序
    public static final int SORT_SINGER_DESC = 3;
    //根据添加时间升序排序
    public static final int SORT_TIME_ASC = 4;
    //根据添加时间降序排序
    public static final int SORT_TIME_DESC = 5;

    public static Song song;
    //列表循环
    public static final int ORDER = 0;
    //随机播放
    public static final int RANDOM = 1;
    //单曲循环
    public static final int SINGLE = 2;

    //未知歌名或歌手
    public static final String unKnown = "未知";

    //音乐播放进度
    public static int playProgress = 0;
    //是否第一次播放
    public static boolean isFistPlay = true;
    //是否设置播放进度
    public static boolean isSetProgress = false;

    //设置播放进度
    public static int setPlayProgress = 0;

    //是否更新播放播放页收藏图标
    public static boolean isSetFavoriteIcon = false;

    //是否初始化歌词
    public static boolean isInitLyric = false;
    //是否初始化歌词
    public static boolean isInitSingleLyric = false;

}

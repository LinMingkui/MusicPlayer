package com.musicplayer.utils;

public class StaticVariate {
    //音乐列表数据库名
    public static final String dataBaseName = "DataBase.db";

    //本地音乐列表数据库表名
    public static final String localSongListTable = "localSongListTable";

    //下载管理音乐列表数据库表名
    public static String downloadSongListTable = "downloadSongListTable";

    //我的收藏音乐列表数据库表名
    public static final String favoriteSongListTable = "favoriteSongListTable";

    //最近播放音乐列表数据库表名
    public static final String recentlySongListTable = "recentlySongListTable";

    //本地音乐搜索列表数据库表名
    public static final String localSearchSongListTable = "localSearchSongListTable";

    //歌单名数据库表名
    public static final String songMenuNameTable = "songMenuNameTable";

    //歌单数据库表名
    public static final String songMenuTable = "songMenuTable";

    //歌单名
    public static final String songMenuName = "songMenuName";

    //歌单里音乐数量
    public static final String songNumber = "songNumber";


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

    public static final String playList = "playList";
    //音乐文件名
    public static String fileName = "fileName";

    //音乐路径
    public static final String fileUrl = "fileUrl";

    //
    public static final String singer = "singer";

    //
    public static final String title = "title";

    //
    public static final String addTime = "addTime";

    //列表中音乐id
    public static String id = "id";

    //播放列表名
    public static final String keyListName = "listName";

    //音乐名
    public static final String keyPlayTitle = "playTitle";

    //歌手名
    public static final String keyPlaySinger = "playSinger";

    //设置文件名
    public static final String keySet = "set";

    //设置文件名
    public static final String keySongMenuId = "songMenuId";

    //播放模式
    public static final String keyPlayMode = "playMode";

    //本地音乐排序方式
    public static final String keyLocalOrder = "localOrder";

    //我的收藏排序方式
    public static final String keyFavoriteOrder = "favoriteOrder";

    //歌单里音乐排序方式
    public static final String keySongMenuOrder = "songMenuOrder";

    //首页歌单是否展开
    public static final String keySongMenuExpand = "songMenuExpand";

    //根据音乐名升序排序
    public static final int ORDER_TITLE_ASC = 0;

    //根据音乐名降序排序
    public static final int ORDER_TITLE_DESC = 1;

    //根据歌手名升序排序
    public static final int ORDER_SINGER_ASC = 3;

    //根据歌手名降序排序
    public static final int ORDER_SINGER_DESC = 4;

    //根据添加时间升序排序
    public static final int ORDER_ADD_TIME_ASC = 5;

    //根据添加时间降序排序
    public static final int ORDER_ADD_TIME_DESC = 6;

    //更新首页歌单数量信息
    public static final int ORDER_COMPLETE = 0;


    //列表循环
    public static final int ORDER = 0;

    //随机播放
    public static final int RANDOM = 1;

    //单曲循环
    public static final int SINGLE = 2;

    //未知歌名或歌手
    public static final String unKnown = "未知";

    //当前音乐长度
    public static int playDuration = 0;

    //音乐播放进度
    public static int playProgress = 0;

    //是否暂停
    public static boolean isPause = false;

    //是否上一曲
    public static boolean isPrev = false;

    //是否下一曲
    public static boolean isNext = false;

    //是否进入播放控制状态
    public static boolean isPlayOrPause = false;

    //是否正在播放
    public static boolean isPlay = false;

    //是否第一次播放
    public static boolean isFistPlay = true;

    //是否设置播放进度
    public static boolean isSetProgress = false;

    //设置播放进度
    public static int setPlayProgress = 0;

    //是否更新播放列表播放图标
    public static boolean isSetListPlayIcon = false;

    //是否更新播放播放页收藏图标
    public static boolean isSetFavoriteIcon = false;

    //是否执行了删除操作
    public static boolean isDelete = false;

    //是否需要执行position减一操作
    public static boolean isSubPosition = false;

    //删除位置的position与当前播放的position是否相等
    public static boolean isEqualPosition = false;

    //是否初始化歌词
    public static boolean isInitLyric = false;



}

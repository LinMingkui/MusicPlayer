package com.musicplayer.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lauzy.freedom.library.Lrc;
import com.musicplayer.R;
import com.musicplayer.ui.widget.SingleLrcView;
import com.musicplayer.utils.LrcHelper;
import com.musicplayer.utils.NetworkUtils;
import com.musicplayer.utils.Variate;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static com.musicplayer.utils.MethodUtils.savePic;


public class SingerFragment extends Fragment {

    private static final String ARG_WIDTH_HEIGHT = "wh";
    private static String TAG = "*SingerFragment";
    //歌手图片宽高
    private int wh;
    private SharedPreferences preferencesPlayList;
    private boolean run;
    private boolean updateLrc = false;
    private boolean isSaveLrc = false;
    private boolean isSavePic = false;
    private View view;
    private ImageView imgSinger;
    private CardView cardViewSinger;
    private SingleLrcView singleLrcView;
    private List<Lrc> lrc;
    private NetworkUtils networkUtils;
    private File fileLrc;
    private File filePic;

    public static SingerFragment newInstance(int wh) {
        SingerFragment fragment = new SingerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_WIDTH_HEIGHT, wh);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            wh = getArguments().getInt(ARG_WIDTH_HEIGHT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_singer, container, false);
        Log.e(TAG, "onCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initLyric();
        singleLrcView.setEmptyContent("暂无歌词");
        networkUtils.setOnGetSongInfoListener(song -> {
            if (isSaveLrc) {
                Log.e(TAG, "saveLrc");
                lrc = LrcHelper.parseLrcFromString(song.getLrc());
                if (getActivity() != null) {
                    (getActivity()).runOnUiThread(() -> singleLrcView.setLrcData(lrc));
                }
                updateLrc = true;
//                Log.e(TAG, song.getLrc());
                saveLrc(song.getLrc());
                isSaveLrc = false;
            }
            if (isSavePic) {
                savePic(getActivity(), song.getSingerUrl(), filePic);
                isSavePic = false;
                if (getActivity() != null) {
                    (getActivity()).runOnUiThread(() -> {
                        Glide.with(this).load(filePic)
                                .error(R.mipmap.img_default_singer).into(imgSinger);
                    });
                }
            }
        });
        ChangeUI changeUI = new ChangeUI();
        changeUI.start();
    }

    private void initView() {
        preferencesPlayList = getActivity().getSharedPreferences(Variate.playList, Context.MODE_PRIVATE);
        networkUtils = new NetworkUtils();
        Variate.isInitLyric = true;
        run = true;
        cardViewSinger = view.findViewById(R.id.cardview_singer);
        imgSinger = view.findViewById(R.id.img_singer);
        ViewGroup.LayoutParams params = cardViewSinger.getLayoutParams();
        params.width = wh;
        params.height = wh;
        cardViewSinger.setLayoutParams(params);
        singleLrcView = view.findViewById(R.id.single_lrc_view);
    }

    private void initLyric() {
        Variate.isInitSingleLyric = false;
        String path, lrcPath;
        if (preferencesPlayList.getInt(Variate.keySongType, Variate.SONG_TYPE_LOCAL) == Variate.SONG_TYPE_LOCAL) {
            path = preferencesPlayList.getString(Variate.keySongUrl, "");
            Log.e(TAG, "songPath " + path);
            lrcPath = new StringBuilder(Variate.LRC_PATH)
                    .append('/').append(path.substring(path.lastIndexOf('/'), path.lastIndexOf('.')))
                    .append(".lrc").toString();
        } else {
            String songName = preferencesPlayList.getString(Variate.keySongName, "");
            String singer = preferencesPlayList.getString(Variate.keySinger, "");
            singer = singer.replace('/', ' ');
            lrcPath = new StringBuilder(Variate.LRC_PATH).append('/').append(singer).append(" - ")
                    .append(songName).append(".lrc").toString();
        }
        Log.e(TAG, "lrcPath " + lrcPath);
        path = preferencesPlayList.getString(Variate.keySinger, "").replace('/', ' ');
        String picPath = new StringBuffer().append(Variate.PIC_PATH)
                .append('/').append(path)
                .toString();
        Log.e(TAG, "picPath " + picPath);
        fileLrc = new File(lrcPath);
        if (fileLrc.exists()) {
            lrc = LrcHelper.parseLrcFromFile(fileLrc);
            singleLrcView.setLrcData(lrc);
            updateLrc = true;
        } else {
            isSaveLrc = true;
        }
        filePic = new File(picPath);
        if (filePic.exists()) {
            Glide.with(this).load(filePic)
                    .error(R.mipmap.img_default_singer).into(imgSinger);
        } else {
            isSavePic = true;
        }
        if (isSaveLrc || isSavePic) {
            String keyWord = new StringBuilder(preferencesPlayList.getString(Variate.keySinger, "")
                    .replace('/', ' ')).append(" ")
                    .append(preferencesPlayList.getString(Variate.keySongName, "")).toString();
            if (!keyWord.equals("")) {
                networkUtils.getSongInfo(keyWord, "qq", Variate.FILTER_NAME);
            }
        }
    }

    private void saveLrc(String str) {
        try {
            File file = new File(Variate.LRC_PATH);
            if (!file.exists()) {
                file.mkdir();
            }
            FileWriter fw = new FileWriter(fileLrc.getPath());
            fw.flush();
            fw.write(str);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ChangeUI extends Thread {
        @Override
        public void run() {
            while (run) {
                if (Variate.isInitSingleLyric) {
                    handler.sendEmptyMessage(2);
                    updateLrc = false;
                } else if (updateLrc) {
                    handler.sendEmptyMessage(1);
                }
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    singleLrcView.updateTime(Variate.playProgress);
                    break;
                case 2:
                    initLyric();
                    break;
            }
            return false;
        }
    });

    @Override
    public void onDetach() {
        super.onDetach();
        run = false;
    }
}

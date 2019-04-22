package com.musicplayer.ui.fragment;

import android.net.Uri;
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
import android.widget.TextView;

import com.lauzy.freedom.library.Lrc;
import com.lauzy.freedom.library.LrcHelper;
import com.musicplayer.R;
import com.musicplayer.ui.widget.LrcView;
import com.musicplayer.utils.StaticVariate;

import java.io.File;
import java.util.List;

public class SingerFragment extends Fragment {

    private static final String ARG_WIDTH_HEIGHT = "wh";
    private static String TAG = "*SingerFragment";
    //歌手图片宽高
    private int wh;
    //歌词索引
    private int index;
    private boolean run;
    private OnFragmentInteractionListener mListener;

    private View view;
    private ImageView imgSinger;
    private CardView cardViewSinger;
    private TextView textViewLrc;
    private List<Lrc> lrc;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_singer,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initLyric();
        ChangeUI changeUI = new ChangeUI();
        changeUI.start();
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
//                R.drawable.img_default_play_background_jpg);
//        bitmap = BitmapUtils.rsBlur(getContext(),bitmap,25);
//        bitmap = BitmapUtils.rsBlur(getContext(),bitmap,25);
//        imgPlay.setImageBitmap(bitmap);
    }

    private void initView() {
        StaticVariate.isInitLyric = true;
        run = true;
        index = 0;

        textViewLrc = view.findViewById(R.id.tv_lrc);
        cardViewSinger = view.findViewById(R.id.cardview_singer);
        imgSinger = view.findViewById(R.id.img_singer);
        ViewGroup.LayoutParams params = cardViewSinger.getLayoutParams();
        params.width = wh;
        params.height = wh;
        cardViewSinger.setLayoutParams(params);
    }


    private String initLyric(){
        File file = new File("/sdcard/不要命.lrc");
        Log.e(TAG,"file path" + file.getPath());
        //从文件读取:
        lrc = LrcHelper.parseLrcFromFile(file);
        index = 0;
        return lrc.get(index).getText();
    }

    private String parseLrc(int currentTime){
        while (index < lrc.size()) {
            long time = lrc.get(index).getTime();
            if (index == lrc.size()-1){
                return lrc.get(lrc.size()-1).getText();
            }else if(time >= currentTime && time < lrc.get(index + 1).getTime()){
                return lrc.get(index).getText();
            }else {
                index ++;
            }
        }
        return lrc.get(lrc.size()-1).getText();
    }

    private class ChangeUI extends Thread{
        @Override
        public void run() {
            while (run){
                if (StaticVariate.isPlay){
                    handler.sendEmptyMessage(1);
                }else if (StaticVariate.isInitLyric){
                    handler.sendEmptyMessage(2);
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.e(TAG,parseLrc(StaticVariate.playProgress));
                    textViewLrc.setText(parseLrc(StaticVariate.playProgress));
                    break;
                case 2:
                    Log.e(TAG,initLyric());
                    textViewLrc.setText(initLyric());
                    StaticVariate.isInitLyric = false;
                    break;
            }
            return false;
        }
    });


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        run = false;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

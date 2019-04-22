package com.musicplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lauzy.freedom.library.Lrc;
import com.lauzy.freedom.library.LrcHelper;
import com.musicplayer.R;
import com.musicplayer.ui.widget.LrcView;
import com.musicplayer.utils.StaticVariate;

import java.io.File;
import java.util.List;

import me.zhengken.lyricview.LyricView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LyricFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LyricFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class LyricFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private OnFragmentInteractionListener mListener;


    private Context context;
    private String TAG = "*LyricFragment";
    private View view;
    private LyricView lyricView;
    private LrcView lrcView;
    private FrameLayout frameLayoutLyric;
    private List<Lrc> lrc;
    private boolean run = true;

    // TODO: Rename and change types and number of parameters
//    public static LyricFragment newInstance(String param1, String param2) {
//        LyricFragment fragment = new LyricFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lyric, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = getContext();
        run = true;
//        lyricView = view.findViewById(R.id.lyric_view);
        lrcView = view.findViewById(R.id.lrc_view);
        frameLayoutLyric = view.findViewById(R.id.fl_lyric);
//        frameLayoutLyric.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e(TAG,"点击事件");
//            }
//        });
        initLyric();

        lrcView.setOnPlayIndicatorLineListener(new LrcView.OnPlayIndicatorLineListener() {
            @Override
            public void onPlay(long var1, String var3) {
                Log.e(TAG,"歌词跳转播放");
//                Log.e(TAG,"var1 "+var1 +"var3 " +var3);
                StaticVariate.setPlayProgress = (int)(var1);
                StaticVariate.isSetProgress = true;
            }
        });

        ChangeUI changeUI = new ChangeUI();
        changeUI.start();
    }

    private class ChangeUI extends Thread{
        @Override
        public void run() {
            while (run){
                if(StaticVariate.isPlay){
                    handler.sendEmptyMessage(1);
                }else if (StaticVariate.isInitLyric) {
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
                    lrcView.updateTime(StaticVariate.playProgress);
                    break;
                case 2:
                    Log.e(TAG,"初始化歌词");
                    initLyric();
                    StaticVariate.isInitLyric = false;
                    break;
            }
            return false;
        }
    });

    private void initLyric(){
//        String uriStr = "android.resource://" + context.getPackageName() + "/"+R.raw.lyric;
//        Uri uri=Uri.parse(uriStr);
//        File file = new File(String.valueOf(uri));
        File file = new File("/sdcard/不要命.lrc");
        Log.e(TAG,"file path" + file.getPath());
        //从文件读取:
        lrc = LrcHelper.parseLrcFromFile(file);
        //设置歌词数据：
        lrcView.setLrcData(lrc);
    }
    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

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
//        mListener = null;
        run = false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}

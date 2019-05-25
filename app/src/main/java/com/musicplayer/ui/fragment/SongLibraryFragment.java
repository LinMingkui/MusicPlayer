package com.musicplayer.ui.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.ui.activity.SongLibrarySearchActivity;
import com.musicplayer.utils.ToastUtils;

import java.util.regex.Pattern;

import static android.webkit.WebSettings.LayoutAlgorithm.NARROW_COLUMNS;

public class SongLibraryFragment extends Fragment implements View.OnClickListener {

    private String TAG = "*SongLibraryFragment";
    private View view;
    private WebView web;
    private TextView tvBack, tvForward, tvRefresh, tvSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.fragment_song_library, container, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setAppCacheEnabled(true);
        web.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        web.getSettings().setSupportZoom(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setLayoutAlgorithm(NARROW_COLUMNS);
        web.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

        });
//        web.loadUrl("https://music.163.com/");
        web.loadUrl("https://y.qq.com/");
    }

    private void initView() {
        web = view.findViewById(R.id.web);
        tvBack = view.findViewById(R.id.tv_back);
        tvForward = view.findViewById(R.id.tv_forward);
        tvRefresh = view.findViewById(R.id.tv_refresh);
        tvSearch = view.findViewById(R.id.tv_search);
        tvBack.setOnClickListener(this);
        tvForward.setOnClickListener(this);
        tvRefresh.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                if (web.canGoBack()) {
                    web.goBack();
                } else {
                    ToastUtils.show(getActivity(), "当前页无法后退");
                }
                break;
            case R.id.tv_forward:
                if (web.canGoForward()) {
                    web.goForward();
                } else {
                    ToastUtils.show(getActivity(), "当前页无法前进");
                }
                break;
            case R.id.tv_refresh:
                web.reload();
                ToastUtils.show(getActivity(), "正在刷新");
                break;
            case R.id.tv_search:
                Log.e(TAG, "web.getUrl()" + web.getUrl());
                String url = web.getUrl();
//                String pattern = "https://music.163.com/m/song\\?id=\\d*";
                if (url.lastIndexOf(".html") != -1) {
                    url = url.substring(0, url.indexOf(".html"));
                    Log.e(TAG, "url " + url);
                    String pattern = "https://y.qq.com/n/yqq/song/\\w+";
                    if (Pattern.matches(pattern, url)) {
                        String id = url.substring(url.lastIndexOf('/') + 1);
                        Log.e(TAG, "id " + id);
                        Intent intent = new Intent(getActivity(), SongLibrarySearchActivity.class);
                        intent.putExtra("id", id);
                        intent.putExtra("type", "qq");
                        startActivity(intent);
                    } else {
                        ToastUtils.show(getActivity(), "请在歌曲详情页面进行此操作");
                    }
                } else {
                    ToastUtils.show(getActivity(), "请在歌曲详情页面进行此操作");
                }
                break;
        }
    }

}

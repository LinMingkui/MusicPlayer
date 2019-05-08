package com.musicplayer.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import com.musicplayer.R;
import com.musicplayer.adapter.TabAdapter;
import com.musicplayer.ui.fragment.KGSearchListFragment;
import com.musicplayer.ui.fragment.QQSearchListFragment;
import com.musicplayer.ui.fragment.WYYSearchListFragment;
import com.musicplayer.ui.widget.PlayBarLayout;
import com.musicplayer.utils.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class NetworkSearchActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = "*NetworkSearchActivity";
    private Context mContext;
    private PlayBarLayout playBarLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView imgBack, imgSearch;
    private EditText edtSearch;
    private QQSearchListFragment qqFragment;
    private KGSearchListFragment kgFragment;
    private WYYSearchListFragment wyyFragmnt;
    private List<Fragment> listFragment = new ArrayList<>();
    private String[] mTitles;
    private String keyWord;
    private TabAdapter tabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_search);
        initView();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    imgSearch.setVisibility(View.VISIBLE);
                } else {
                    imgSearch.setVisibility(View.GONE);
                }
                keyWord = s.toString();
            }
        });
        //监听回车
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search();
                return true;
            }
            return false;
        });
    }

    private void initView() {
        mContext = NetworkSearchActivity.this;
        playBarLayout = findViewById(R.id.play_bar_layout);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        imgBack = findViewById(R.id.img_title_back);
        imgSearch = findViewById(R.id.img_search);
        edtSearch = findViewById(R.id.edt_title_search);

        imgBack.setOnClickListener(this);
        imgSearch.setOnClickListener(this);

        mTitles = new String[]{"QQ音乐", "酷狗音乐", " 网易云音乐"};
        qqFragment = new QQSearchListFragment();
        kgFragment = new KGSearchListFragment();
        wyyFragmnt = new WYYSearchListFragment();
        listFragment.add(qqFragment);
        listFragment.add(kgFragment);
        listFragment.add(wyyFragmnt);
        tabAdapter = new TabAdapter(getSupportFragmentManager(), listFragment, mTitles);
        viewPager.setAdapter(tabAdapter);
        //将TabLayout和ViewPager关联
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_back:
                this.finish();
                break;
            case R.id.img_search:
                search();
                break;
        }
    }

    private void search() {
        int item = viewPager.getCurrentItem();
        listFragment.clear();
        listFragment.add(QQSearchListFragment.newInstance(keyWord));
        listFragment.add(KGSearchListFragment.newInstance(keyWord));
        listFragment.add(WYYSearchListFragment.newInstance(keyWord));
        viewPager.setAdapter(tabAdapter);
        viewPager.setCurrentItem(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        playBarLayout.mBindService(mContext);
    }

    @Override
    protected void onPause() {
        super.onPause();
        playBarLayout.mUnBindService(mContext);
    }
}

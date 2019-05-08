package com.musicplayer.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.TabAdapter;
import com.musicplayer.database.DataBase;
import com.musicplayer.ui.fragment.MyFragment;
import com.musicplayer.ui.fragment.SongLibraryFragment;
import com.musicplayer.ui.widget.PlayBarLayout;
import com.musicplayer.utils.BaseActivity;
import com.musicplayer.utils.MyApplication;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private String TAG = "*MainActivity";
    private DrawerLayout mainDrawerLayout;

//    private DataBase dataBase;
//    private SQLiteDatabase db;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;

    private PlayBarLayout playBarLayout;
    private LinearLayout linearLayoutExit;
    private ImageView imgMenu, imgSearch;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<Fragment> fragmentArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        //检查必要权限
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        init();
        setOnClickListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        playBarLayout.mBindService(mContext);
    }

    private void setOnClickListener() {
        imgMenu.setOnClickListener(this);
        imgSearch.setOnClickListener(this);
        linearLayoutExit.setOnClickListener(this);
    }

    private void init() {
        mainDrawerLayout = findViewById(R.id.drawer_layout_main);
        linearLayoutExit = findViewById(R.id.ll_exit);
        playBarLayout = findViewById(R.id.play_bar_layout);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        imgMenu = findViewById(R.id.img_title_left_menu);
        imgSearch = findViewById(R.id.img_search);

        //添加fragment
        fragmentArrayList = new ArrayList<>();
        fragmentArrayList.add(new MyFragment());
        fragmentArrayList.add(new SongLibraryFragment());
        viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), fragmentArrayList,new String[]{"我的","乐库"}));
        tabLayout.setupWithViewPager(viewPager);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_left_menu:
                mainDrawerLayout.openDrawer(Gravity.START);
                break;
            case R.id.img_search:
                Intent intent = new Intent(MainActivity.this,NetworkSearchActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_exit:
                MyApplication.getInstance().AppExit();
                break;
        }
    }

    //请求权限结果
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] granResult) {
        switch (requestCode) {
            case 1:
                if (granResult.length <= 0 || granResult[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "拒绝权限将无法使用程序", Toast.LENGTH_LONG).show();
                    MainActivity.this.finish();
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        playBarLayout.mUnBindService(mContext);
    }

    //返回后台
    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
//        super.onBackPressed();
    }
}

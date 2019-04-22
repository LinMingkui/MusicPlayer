package com.musicplayer.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.database.DataBase;
import com.musicplayer.ui.fragment.DiscoverFragment;
import com.musicplayer.ui.fragment.MyFragment;
import com.musicplayer.utils.BaseActivity;
import com.musicplayer.utils.MyApplication;

import java.util.ArrayList;

import static com.musicplayer.utils.MethodUtils.getDbWidth;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private String TAG = "*MainActivity";
    private DrawerLayout mainDrawerLayout;

    private DataBase dataBase;
    private SQLiteDatabase db;
    private SharedPreferences preferencesSet;
    private SharedPreferences.Editor editorSet;

    private LinearLayout linearLayoutExit;
    private ImageView imgMenu, imgSearch;

    private TextView tvMyMusic, tvSongLibrary;

    private ViewPager viewPager;
    private ImageView imgIndicator;
    private ArrayList<Fragment> fragmentArrayList;
    private FragmentManager fragmentManager;
    //指示器宽度
    private int bmpW;
    private int pageIndex = 0;
    private int offset;
    private Animation animation;

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

        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());


    }

    private void setOnClickListener() {
        imgMenu.setOnClickListener(this);
        tvMyMusic.setOnClickListener(this);
        tvSongLibrary.setOnClickListener(this);
        linearLayoutExit.setOnClickListener(this);
    }

    private void init() {
        mainDrawerLayout = findViewById(R.id.drawer_layout_main);
        linearLayoutExit = findViewById(R.id.ll_exit);

        viewPager = findViewById(R.id.view_pager);
        tvMyMusic = findViewById(R.id.tv_my_music);
        tvSongLibrary = findViewById(R.id.tv_song_library);
        imgMenu = findViewById(R.id.img_title_left_menu);
        imgIndicator = findViewById(R.id.img_indicator);

        //添加fragment
        fragmentArrayList = new ArrayList<>();
        fragmentArrayList.add(new MyFragment());
        fragmentArrayList.add(new DiscoverFragment());
        fragmentManager = getSupportFragmentManager();

        //指示器宽度为屏幕宽度的1/4
        bmpW = (getDbWidth(MainActivity.this) / 4);

        //设置动画图片宽度
        ViewGroup.LayoutParams para;
        para = imgIndicator.getLayoutParams();
        para.width = bmpW;
        imgIndicator.setLayoutParams(para);

        //把指示器放在“我的”下面
        animation = new TranslateAnimation(0, bmpW, 0, 0);
        animation.setFillAfter(true);// true:图片停在动画结束位置
        animation.setDuration(0);
        imgIndicator.startAnimation(animation);

        viewPager.setAdapter(new MFragmentPagerAdapter(fragmentManager, fragmentArrayList));
        viewPager.setOffscreenPageLimit(1);
        //显示第一个fragment
        viewPager.setCurrentItem(0);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_title_left_menu:
                mainDrawerLayout.openDrawer(Gravity.START);
                break;
            case R.id.tv_my_music:
                viewPager.setCurrentItem(0);
                break;
            case R.id.tv_song_library:
                viewPager.setCurrentItem(1);
                break;
            case R.id.ll_exit:
                MyApplication.getInstance().AppExit();
                break;
        }
    }


    //ViewPage滑动事件
    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int i, float v, int i1) {
//            Log.e(TAG, "i = " + i);
//            Log.e(TAG, "v = " + v);
//            Log.e(TAG, "i1 = " + i1);
//            if (v != 0) {
//                if (pageIndex == 0) {
//                    animation = new TranslateAnimation(offset,
//                            offset + (int) (v * bmpW), 0, 0);
//                    offset = offset + (int) (v * bmpW);
//                }
//                animation.setFillAfter(true);// true:图片停在动画结束位置
//                animation.setDuration(300);
//                imgIndicator.startAnimation(animation);
//            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }

        @Override
        public void onPageSelected(int i) {
            switch (i) {
                //当前为页卡1
                case 0:
                    animation = new TranslateAnimation(2 * bmpW, bmpW, 0, 0);
                    tvMyMusic.setTextColor(getResources().getColor(R.color.white));
                    tvSongLibrary.setTextColor(getResources().getColor(R.color.gray));
                    break;
                //当前为页卡2
                case 1:
                    animation = new TranslateAnimation(bmpW, 2 * bmpW, 0, 0);
                    tvMyMusic.setTextColor(getResources().getColor(R.color.gray));
                    tvSongLibrary.setTextColor(getResources().getColor(R.color.white));
                    break;
            }
            animation.setFillAfter(true);// true:图片停在动画结束位置
            animation.setDuration(300);
            imgIndicator.startAnimation(animation);
            pageIndex = i;
        }

    }

    public class MFragmentPagerAdapter extends FragmentPagerAdapter {

        //存放Fragment的数组
        private ArrayList<Fragment> fragmentsList;

        public MFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragmentsList) {
            super(fm);
            this.fragmentsList = fragmentsList;
        }

        @Override
        public Fragment getItem(int position) {

            return fragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentsList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
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

    //返回后台
    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
//        super.onBackPressed();
    }
}

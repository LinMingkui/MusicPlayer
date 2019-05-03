package com.musicplayer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class TabAdapter extends FragmentStatePagerAdapter {

    private String TAG = "*TabAdapter";
    private List<Fragment> mFragments;
    private String[] mTitles;

    public TabAdapter(FragmentManager fm, List<Fragment> fragments, String[] titles) {
        super(fm);
        mFragments = fragments;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
//        int i = 0;
//        switch (position){
//            case 0:
//                for (i = 0;i < mFragments.size();i++){
////                    Log.e(TAG,mFragments.get(i).getClass().toString());
//                    if(mFragments.get(i).getClass().toString().equals("class com.musicplayer.ui.fragment.QQSearchListFragment")){
//                        break;
//                    }
//                }
//                break;
//            case 1:
//                for (i = 0;i < mFragments.size();i++){
//                    if(mFragments.get(i).getClass().toString().equals("class com.musicplayer.ui.fragment.KGSearchListFragment")){
//                        break;
//                    }
//                }
//                break;
//            case 2:
//                for (i = 0;i < mFragments.size();i++){
//                    Log.e(TAG,mFragments.get(i).getClass().toString());
//                    if(mFragments.get(i).getClass().toString().equals("class com.musicplayer.ui.fragment.WYYSearchListFragment")){
//                        break;
//                    }
//                }
//                break;
//
//        }
//        Log.e(TAG,"i:"+i);
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}

package com.musicplayer.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static void show(Context mContext,String str){
        Toast.makeText(mContext,str,Toast.LENGTH_SHORT).show();
    }
}

package com.musicplayer.broadcastreceiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.musicplayer.utils.Variate;

public class BroadcastReceiver222 extends android.content.BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        if (action.equals(Variate.ACTION_DOWN)) {
        //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
        int id = intent.getIntExtra("id", 0);
        Log.e("BroadcastReceiver222", "通知栏收藏");

        switch (id) {
            case Variate.ID_FAVORITE:
//                        onDownLoadBtnClick();
                break;
            default:
                break;
        }
//        }
    }
}

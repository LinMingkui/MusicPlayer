package com.musicplayer.utils;

import android.app.Application;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.musicplayer.service.PlayService;

import java.util.Stack;

public class MyApplication extends Application {

    private static Stack<AppCompatActivity> activityStack;
    private static MyApplication singleton;
    @Override
    public void onCreate()
    {
        super.onCreate();
        singleton=this;
    }
    // Returns the application instance
    public static MyApplication getInstance() {
        return singleton;
    }

    /**
     * add Activity 添加Activity到栈
     */
    public void addActivity(AppCompatActivity activity){
        if(activityStack ==null){
            activityStack =new Stack<AppCompatActivity>();
        }
        activityStack.add(activity);
    }
    /**
     * get current Activity 获取当前Activity（栈中最后一个压入的）
     */
    public AppCompatActivity currentActivity() {
        AppCompatActivity activity = activityStack.lastElement();
        return activity;
    }
    /**
     * 结束当前Activity（栈中最后一个压入的）
     */
    public void finishActivity() {
        AppCompatActivity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(AppCompatActivity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (AppCompatActivity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 退出应用程序
     */
    public void AppExit() {
        try {
            Intent intent = new Intent(this, PlayService.class);
            stopService(intent);
            finishAllActivity();
        } catch (Exception e) {
        }
    }

}

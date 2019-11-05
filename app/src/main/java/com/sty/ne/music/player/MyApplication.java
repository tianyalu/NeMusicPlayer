package com.sty.ne.music.player;

import android.app.Application;

import com.sty.ne.music.player.ui.UIUtils;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UIUtils.getInstance(this);
    }
}

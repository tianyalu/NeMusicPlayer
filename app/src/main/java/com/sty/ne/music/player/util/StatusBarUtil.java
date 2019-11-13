package com.sty.ne.music.player.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sty.ne.music.player.ui.UIUtils;

public class StatusBarUtil {

    public static void setTranslateStateBar(Activity activity) {
        //4.0以下不支持沉浸式
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            int visibility = window.getDecorView().getSystemUiVisibility();
            //布局内容全屏显示
            visibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            //隐藏底部虚拟导航栏
            visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            //防止内容区域大小发生变化
            visibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

            window.getDecorView().setSystemUiVisibility(visibility);
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //设置状态栏透明，这样才能让ContentView向上
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public static void setStateBar(Activity activity, View toolBar){
        setTranslateStateBar(activity);
        if(toolBar != null) {
            toolBar.setPadding(toolBar.getPaddingLeft(), UIUtils.getInstance().getStatusBarHeight(activity),
                    toolBar.getPaddingRight(), toolBar.getPaddingBottom());
        }
    }
}

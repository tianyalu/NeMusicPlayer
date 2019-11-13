package com.sty.ne.music.player;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
//    private static Context mContext;
    private TextView textView;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mContext = this;
        textView = findViewById(R.id.text);
        //获取系统分配给应用的内存大小
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int heapSize = activityManager.getMemoryClass(); //M
        textView.setText("heapMemory: " + heapSize + "M");

        RecyclerView recyclerView;
        ListView listView;
    }
}

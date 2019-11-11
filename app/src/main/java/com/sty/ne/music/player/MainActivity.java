package com.sty.ne.music.player;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.sty.ne.music.player.model.MusicData;
import com.sty.ne.music.player.service.MusicService;
import com.sty.ne.music.player.view.DiscView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DiscView.IPlayInfo, View.OnClickListener{
    private ImageSwitcher bg_switcher; //背景切换
    private int bg_pic_res = -1; //背景资源ID
    private DiscView mDisc;
    private Toolbar mToolbar;
    private SeekBar mSeekBar;
    private ImageView mIvPlayOrPause, mIvNext, mIvPrevious; //播放/暂停按钮 下一曲 上一曲
    private TextView mTvMusicDuration, mTvTotalMusicDuration; //当前播放时长 总时长

    public static final int MUSIC_MESSAGE = 0;

    private Handler mMusicHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            mSeekBar.setProgress(mSeekBar.getProgress() + 1000);
            mTvMusicDuration.setText(duration2Time(mSeekBar.getProgress()));
            startUpdateSeekBarProgress();
            return false;
        }
    });

    private MusicReceiver mMusicReceiver = new MusicReceiver(); //广播接收者
    private List<MusicData> musicDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化音乐数据
        initMusicData();
        //初始化控件
        initView();
        //初始化广播接收者（跟service通讯）
        initMusicReceiver();
    }

    private void initMusicData() {
        MusicData musicData1 = new MusicData(R.raw.music1, R.raw.poster1, "我喜欢", "梁静茹");
        MusicData musicData2 = new MusicData(R.raw.music2, R.raw.poster2, "想把我唱给你听", "老狼");
        MusicData musicData3 = new MusicData(R.raw.music3, R.raw.poster3, "风再起时", "张国荣");
        //添加歌曲
        musicDataList.add(musicData1);
        musicDataList.add(musicData2);
        musicDataList.add(musicData3);
        //开启播放服务
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(MusicService.PARAM_MUSIC_LIST, (Serializable)(musicDataList));
        startService(intent);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onMusicInfoChanged(String musicName, String musicAuthor) {

    }

    @Override
    public void onMusicPicChanged(int musicPicRes) {

    }

    @Override
    public void onMusicChanged(DiscView.MusicChangedStatus musicChangedStatus) {

    }
}

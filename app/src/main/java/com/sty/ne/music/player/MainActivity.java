package com.sty.ne.music.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.sty.ne.music.player.model.MusicData;
import com.sty.ne.music.player.service.MusicService;
import com.sty.ne.music.player.ui.UIUtils;
import com.sty.ne.music.player.util.FastBlurUtil;
import com.sty.ne.music.player.util.StatusBarUtil;
import com.sty.ne.music.player.util.ToolbarUtils;
import com.sty.ne.music.player.view.DiscView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.sty.ne.music.player.view.DiscView.DURATION_NEEDLE_ANIMATOR;

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

    private void initView() {
        mDisc = findViewById(R.id.discview);
        mIvNext = findViewById(R.id.iv_next);
        mIvPrevious = findViewById(R.id.iv_previous);
        mIvPlayOrPause = findViewById(R.id.iv_play_or_pause);
        mTvMusicDuration = findViewById(R.id.tv_current_time);
        mTvTotalMusicDuration = findViewById(R.id.tv_total_time);
        mSeekBar = findViewById(R.id.music_seek_bar);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("歌单");
        ToolbarUtils.setMarqueeForToolbarTitleView(mToolbar);
        //设置沉浸式，并且toolbar设置padding
        StatusBarUtil.setStateBar(this, mToolbar);
        //ImageSwitcher(注意：只能有两个ImageView）
        bg_switcher = findViewById(R.id.bg_switcher);
        bg_switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                //调用两次
                ImageView imageView = new ImageView(MainActivity.this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); //设置保持纵横比居中缩放图像
                imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                return imageView;
            }
        });
        bg_switcher.setImageResource(R.drawable.bg_defalut);
        Animation animationIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animationIn.setDuration(500);
        animationOut.setDuration(500);
        bg_switcher.setInAnimation(animationIn); //设置淡入动画
        bg_switcher.setOutAnimation(animationOut); //设置淡出动画
        //唱盘设置播放监听
        mDisc.setPlayInfoListener(this);
        mIvPrevious.setOnClickListener(this);
        mIvNext.setOnClickListener(this);
        mIvPlayOrPause.setOnClickListener(this);
        //监听进度条变化
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //改变当前显示的播放进度
                mTvMusicDuration.setText(duration2Time(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                startUpdateSeekBarProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //拖动进度结果（音乐进度更新）
                seekTo(seekBar.getProgress());
                stopUpdateSeekBarProgress();
            }
        });

        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
        mDisc.setMusicDataList(musicDataList);
    }

    private void initMusicReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_DURATION);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_COMPLETE);
        //注册本地广播
        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicReceiver, intentFilter);
    }
    @Override
    public void onClick(View view) {
        if(view == mIvPlayOrPause) {
            mDisc.playOrPause();
        }else if(view == mIvNext) {
            mDisc.next();
        }else if(view == mIvPrevious) {
            mDisc.previous();
        }
    }

    /**
     * 唱盘接口回调 切歌
     * @param musicName
     * @param musicAuthor
     */
    @Override
    public void onMusicInfoChanged(String musicName, String musicAuthor) {
        getSupportActionBar().setTitle(musicName);
        getSupportActionBar().setSubtitle(musicAuthor);
    }

    /**
     * 唱盘接口回调 换图
     * @param musicPicRes
     */
    @Override
    public void onMusicPicChanged(int musicPicRes) {
        try2UpdateMusicPicBackground(musicPicRes);
    }

    /**
     * 唱盘接口回调 音乐播放状态改变
     * @param musicChangedStatus
     */
    @Override
    public void onMusicChanged(DiscView.MusicChangedStatus musicChangedStatus) {
        switch (musicChangedStatus) {
            case PLAY: {
                play();
                break;
            }
            case PAUSE: {
                pause();
                break;
            }
            case NEXT: {
                next();
                break;
            }
            case PREVIOUS: {
                previous();
                break;
            }
            case STOP: {
                stop();
                break;
            }
            default:
                break;
        }
    }

    private void try2UpdateMusicPicBackground(final int musicPicRes) {
        if(isNeed2UpdateBackground(musicPicRes)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Drawable foregroundDrawable = getForegroundDrawable(musicPicRes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bg_switcher.setImageDrawable(foregroundDrawable);
                        }
                    });
                }
            }).start();
        }
    }

    public boolean isNeed2UpdateBackground(int musicPicRes) {
        return this.bg_pic_res == -1 || musicPicRes != this.bg_pic_res;
    }

    private Drawable getForegroundDrawable(int musicPicRes) {
        //得到屏幕的宽高比，以便按照比例切割图片一部分
        final float widthHeightSize = (float) (UIUtils.getScreenWidth(MainActivity.this)
                * 1.0 / UIUtils.getScreenHeight(this) * 1.0);

        Bitmap bitmap = getForegroundBitmap(musicPicRes);
        int cropBitmapWidth = (int) (widthHeightSize * bitmap.getHeight());
        int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);

        //切割部分图片
        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth,
                bitmap.getHeight());
        //缩小图片
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.getWidth() / 50,
                bitmap.getHeight() / 50, false);
        //模糊化
        final Bitmap blurBitmap = FastBlurUtil.doBlur(scaleBitmap, 8, true);

        final Drawable foregroundDrawable = new BitmapDrawable(getResources(), blurBitmap);
        //加入灰色遮罩层，避免图片过亮影响其它控件
        foregroundDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        return foregroundDrawable;
    }

    private void stopUpdateSeekBarProgress() {
        mMusicHandler.removeMessages(MUSIC_MESSAGE);
    }

    private Bitmap getForegroundBitmap(int musicPicRes) {
        int screenWidth = UIUtils.getScreenWidth(this);
        int screenHeight = UIUtils.getScreenHeight(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), musicPicRes, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        if(imageWidth < screenWidth && imageHeight < screenHeight) {
            return BitmapFactory.decodeResource(getResources(), musicPicRes);
        }

        int sample = 2;
        int sampleX = imageWidth / screenWidth;
        int sampleY = imageHeight / screenHeight;

        if(sampleX > sampleY  && sampleY > 1) {
            sample = sampleX;
        }else if(sampleY > sampleX && sampleX > 1) {
            sample  = sampleY;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(getResources(), musicPicRes, options);
    }

    private void play() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
        startUpdateSeekBarProgress();
    }

    private void pause() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PAUSE);
        stopUpdateSeekBarProgress();
    }

    private void stop() {
        stopUpdateSeekBarProgress();;
        mIvPlayOrPause.setImageResource(R.drawable.ic_play);
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
        mSeekBar.setProgress(0);
    }

    /**
     * 下一曲 歌曲延迟切换 唱针指到碟片上开始播放
     */
    private void next() {
        bg_switcher.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_NEXT);
            }
        }, DURATION_NEEDLE_ANIMATOR);
        stopUpdateSeekBarProgress();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

    /**
     * 上一曲 歌曲延迟切换
     */
    private void previous() {
        bg_switcher.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_PREVIOUS);
            }
        }, DURATION_NEEDLE_ANIMATOR);
        stopUpdateSeekBarProgress();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

    private void complete(boolean isOver) {
        //逻辑需要改变
        if (isOver) {
            mDisc.stop();
        }else {
            mDisc.next();
        }
    }

    private void optMusic(final String action) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action));
    }

    private void seekTo(int position) {
        Intent intent = new Intent(MusicService.ACTION_OPT_MUSIC_SEEK_TO);
        intent.putExtra(MusicService.PARAM_MUSIC_SEEK_TO, position);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void startUpdateSeekBarProgress() {
        //避免重复发送Message
        stopUpdateSeekBarProgress();
        mMusicHandler.sendEmptyMessageDelayed(0, 1000);
    }

    //根据时长格式化成时间文本
    private static String duration2Time(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        return (min < 10 ? "0" + min : min + "") + ":" + (sec < 10 ? "0" + sec : sec + "");
    }

    private void updateMusicDurationInfo(int totalDuration) {
        mSeekBar.setProgress(0);
        mSeekBar.setMax(totalDuration);
        mTvTotalMusicDuration.setText(duration2Time(totalDuration));
        mTvMusicDuration.setText(duration2Time(0));
        startUpdateSeekBarProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMusicReceiver);
        //关闭服务
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);

        if(mMusicHandler != null) {
            mMusicHandler.removeCallbacksAndMessages(null);
            mMusicHandler = null;
            System.gc();
        }
    }

    class MusicReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收从MusicService发出的动作
            String action = intent.getAction();
            assert action != null; //断言 true继续 false抛出异常并退出
            switch (action) {
                case MusicService.ACTION_STATUS_MUSIC_PLAY: //播放
                    mIvPlayOrPause.setImageResource(R.drawable.ic_pause); //图标改成暂停
                    int currentPosition = intent.getIntExtra(MusicService.PARAM_MUSIC_CURRENT_POSITION, 0);
                    mSeekBar.setProgress(currentPosition);
                    //碟片状态改变
                    if(!mDisc.isPlaying()) {
                        mDisc.playOrPause();
                    }
                    break;
                case MusicService.ACTION_STATUS_MUSIC_PAUSE: //暂停
                    mIvPlayOrPause.setImageResource(R.drawable.ic_play); //图标改成播放
                    if(mDisc.isPlaying()) {
                        mDisc.playOrPause();
                    }
                    break;
                case MusicService.ACTION_STATUS_MUSIC_DURATION: //重新播放音乐
                    int duration = intent.getIntExtra(MusicService.PARAM_MUSIC_DURATION, 0);
                    updateMusicDurationInfo(duration);
                    break;
                case MusicService.ACTION_STATUS_MUSIC_COMPLETE: //音乐播放完成
                    boolean isOver = intent.getBooleanExtra(MusicService.PARAM_MUSIC_IS_OVER, true);
                    complete(isOver);
                    break;
                default:
                    break;
            }
        }
    }

}

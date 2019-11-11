package com.sty.ne.music.player.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sty.ne.music.player.R;
import com.sty.ne.music.player.model.MusicData;
import com.sty.ne.music.player.ui.UIUtils;
import com.sty.ne.music.player.ui.ViewCalculateUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class DiscView extends RelativeLayout {
    private ImageView mIvNeedle;
    private MyViewFlipper myViewFlipper;
    private ObjectAnimator mNeedleAnimator;

    private List<MusicData> mMusicData = new ArrayList<>();
    private List<ObjectAnimator> mDiscAnimators = new ArrayList<>();
    /*标记ViewFlipper是否处于偏移的状态*/
    private boolean mViewFlipperIsOffset = false;

    //标记唱针复位后，是否需要重新偏移到唱片处
    private boolean mIsNeed2StartPlayAnimator = false;

    public static final int DURATION_NEEDLE_ANIMATOR = 300;
    private int needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;
    private int musicStatus = MusicStatus.STOP;
    private IPlayInfo mIPlayInfo;
    private int otherPosterRes = -1;


    //唱针当前所处的状态
    @IntDef({
            NeedleAnimatorStatus.TO_FAR_END,
            NeedleAnimatorStatus.TO_NEAR_END,
            NeedleAnimatorStatus.IN_FAR_END,
            NeedleAnimatorStatus.IN_NEAR_END
    })

    /**
     * RetentionPolicy.SOURCE 源码注解，编译成.class文件后注解就不存在，用来提示开发者
     * RetentionPolicy.CLASS CLASS汇编注解，编译成.class文件后注解也还存在，用于自动生成代码
     * RetentionPolicy.RUNTIME 运行时动态注解，生命周期一直程序运行时都存在，常用于自动注入
     */
    @Retention(RetentionPolicy.SOURCE)
    private @interface NeedleAnimatorStatus{
        //移动时：从唱盘往远处移动
        int TO_FAR_END = 0;
        //移动时：从远处往唱盘移动
        int TO_NEAR_END = 1;
        //静止时：离开唱盘
        int IN_FAR_END = 2;
        //静止时：贴近唱盘
        int IN_NEAR_END = 3;
    }

    //音乐当前的状态：播放、暂停、停止
    @IntDef({
            MusicStatus.PLAY,
            MusicStatus.PAUSE,
            MusicStatus.STOP
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface MusicStatus{
        int PLAY = 0;
        int PAUSE = 1;
        int STOP = 2;
    }

    //DiscView需要出发的音乐切换状态：播放、暂停、上/下一首、停止
    public enum MusicChangedStatus {
        PLAY, PAUSE, NEXT, PREVIOUS, STOP
    }

    public interface IPlayInfo{
        //用于更新标题栏变化
        void onMusicInfoChanged(String musicName, String musicAuthor);

        //用于更新背景图片
        void onMusicPicChanged(int musicPicRes);

        //用于更新音乐播放状态
        void onMusicChanged(MusicChangedStatus musicChangedStatus);
    }

    public DiscView(Context context) {
        this(context, null);
    }

    public DiscView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiscView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //数据初始化
        //白色背景
        initDiscBackground();
        //ViewFlipper
        initViewFlipper();
        //唱针
        initNeedle();
        //动画
        initAnimation();
    }

    private void initNeedle() {
        mIvNeedle = findViewById(R.id.iv_needle);

        ViewCalculateUtil.setViewRelativeLayoutParam(mIvNeedle, UIUtils.NEEDLE_WIDTH, UIUtils.NEEDLE_HEIGHT,
                -UIUtils.NEEDLE_MARGIN_TOP, 0, UIUtils.NEEDLE_MARGIN_LEFT,
                0, false);
        int pivotX = UIUtils.getInstance().getWidth(UIUtils.NEEDLE_PIVOT_X);
        int pivotY = UIUtils.getInstance().getHeight(UIUtils.NEEDLE_PIVOT_Y);

        mIvNeedle.setPivotX(pivotX);
        mIvNeedle.setPivotY(pivotY);
        mIvNeedle.setRotation(UIUtils.ROTATION_INIT_NEEDLE);
    }

    private void initAnimation() {
        mNeedleAnimator = ObjectAnimator.ofFloat(mIvNeedle, View.ROTATION,
                UIUtils.ROTATION_INIT_NEEDLE, 0);
        mNeedleAnimator.setDuration(DURATION_NEEDLE_ANIMATOR);
        mNeedleAnimator.setInterpolator(new AccelerateInterpolator());
        mNeedleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                //根据动画开始前NeedleAnimatorStatus的状态，即可得出动画进行时NeedleAnimatorStatus的状态
                if(needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_NEAR_END;
                }else if(needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_NEAR_END;
                    playDiscAnimator();
                    musicStatus = MusicStatus.PLAY;
                }else if(needleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;
                    if(musicStatus == MusicStatus.STOP) {
                        mIsNeed2StartPlayAnimator = true;
                    }
                }

                if(mIsNeed2StartPlayAnimator) {
                    mIsNeed2StartPlayAnimator = false;
                    //只有在ViewFlipper不处于偏移状态时，才开始唱盘旋转动画
                    if(!mViewFlipperIsOffset) {
                        //延时50ms
                        DiscView.this.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                playAnimator();
                            }
                        }, 50);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void initViewFlipper() {
        myViewFlipper = findViewById(R.id.vf_disc_contain);
        myViewFlipper.setOverScrollMode(View.OVER_SCROLL_NEVER); //关闭渐变（触底或触顶）
        int marginTop = UIUtils.getInstance().getHeight(UIUtils.DISC_MARGIN_TOP);
        int height = UIUtils.getInstance().getWidth(UIUtils.DISC_BG_WIDTH);
        ViewCalculateUtil.setViewRelativeLayoutParam(myViewFlipper,
                1080, height, marginTop, 0, 0, 0);

        myViewFlipper.setOnPageChangeListener(new MyViewFlipper.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, float positionOffsetPixels) {
                Log.i("滑动", "" + positionOffsetPixels);
                if(positionOffsetPixels > 0) {
                    //上一曲
                    int previous = myViewFlipper.getPreviousItem();
                    if(otherPosterRes != mMusicData.get(previous).getmMusicPicRes()){
                        otherPosterRes = mMusicData.get(previous).getmMusicPicRes();
                        myViewFlipper.getOtherPosterView().setImageDrawable(
                                getDiscPosterDrawable(otherPosterRes));
                    }
                    if(positionOffset > 0.5) {
                        notifyMusicInfoChanged(previous);
                    }else {
                        notifyMusicInfoChanged(position);
                    }
                }else {
                    int next = myViewFlipper.getNextItem();
                    if(otherPosterRes != mMusicData.get(next).getmMusicPicRes()){
                        otherPosterRes = mMusicData.get(next).getmMusicPicRes();
                        myViewFlipper.getOtherPosterView().setImageDrawable(
                                getDiscPosterDrawable(otherPosterRes));
                    }
                    if(positionOffset < 0.5) {
                        notifyMusicInfoChanged(position);
                    }else {
                        notifyMusicInfoChanged(next);
                    }
                }
            }

            @Override
            public void onPageSelected(int position, boolean isNext) {
                resetOtherDiscAnimation();
                notifyMusicPicChanged(position);
                notifyMusicInfoChanged(position);
                playOrPause();
                if(isNext) {
                    notifyMusicStatusChanged(MusicChangedStatus.NEXT);
                }else {
                    notifyMusicStatusChanged(MusicChangedStatus.PREVIOUS);
                }
            }

            @Override
            public void onPageScrollStateChangeed(int state) {
                doWithAnimatorOnPageScroll(state);
            }
        });
    }

    //取消其他页面上的动画，并将图片旋转角度复原
    private void resetOtherDiscAnimation() {
        mDiscAnimators.get(myViewFlipper.getOtherItem()).cancel();
        myViewFlipper.getOtherView().setRotation(0);
    }

    private void doWithAnimatorOnPageScroll(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_SETTLING:
                break;
            case ViewPager.SCROLL_STATE_IDLE:
                mViewFlipperIsOffset = false;
                if(musicStatus == MusicStatus.PLAY) {
                    playAnimator();
                }
                break;
            case ViewPager.SCROLL_STATE_DRAGGING:
                mViewFlipperIsOffset = true;
                pauseAnimator();
                break;
            default:
                break;
        }
    }
    private void initDiscBackground() {
        ImageView mDiscBlackGround = findViewById(R.id.iv_disc_black_ground);
        mDiscBlackGround.setImageDrawable(getDiscBlackGroundDrawable());
        int marginTop = UIUtils.getInstance().getHeight(UIUtils.DISC_MARGIN_TOP);
        int width = UIUtils.getInstance().getWidth(UIUtils.DISC_BG_WIDTH);
        ViewCalculateUtil.setViewRelativeLayoutParam(mDiscBlackGround,
                width, width, marginTop, 0, 0, 0);
    }

    //得到唱盘背后透明的圆形背景
    public Drawable getDiscBlackGroundDrawable() {
        int discSize = UIUtils.getInstance().getWidth(UIUtils.DISC_BG_WIDTH);
        Bitmap bitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_disc_blackground), discSize, discSize, false);
        return RoundedBitmapDrawableFactory.create(getResources(), bitmapDisc);
    }

    public void setPlayInfoListener(IPlayInfo listener) {
        this.mIPlayInfo = listener;
    }

    //得到唱盘图片
    private  Bitmap getDiscDrawable() {
        int discSize = UIUtils.getInstance().getWidth(UIUtils.DISC_BLACK_WIDTH);
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_disc), discSize, discSize, false);
    }

    //获取封面，切圆角
    private Drawable getDiscPosterDrawable(int musicPicRes) {
        int musicPicSize = UIUtils.getInstance().getWidth(UIUtils.DISC_POSTER_WIDTH);
        Bitmap bitmapMusicPic = getMusicPicBitmap(musicPicSize, musicPicRes);
        RoundedBitmapDrawable roundMusicDrawable = RoundedBitmapDrawableFactory.create(
                getResources(), bitmapMusicPic);
        roundMusicDrawable.setCornerRadius(musicPicSize / 2);
        roundMusicDrawable.setAntiAlias(true);
        return roundMusicDrawable;
    }

    private Bitmap getMusicPicBitmap(int musicPicSize, int musicPicRes) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), musicPicRes, options);
        int imageWidth = options.outWidth;

        int sample = imageWidth / musicPicSize;
        int dstSample = 1;
        if(sample > dstSample) {
            dstSample = sample;
        }
        options.inJustDecodeBounds = false;
        //设置图片采样率
        options.inSampleSize = dstSample;
        //设置图片解码格式
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                musicPicRes, options), musicPicSize, musicPicSize, true);

    }

    public void setMusicDataList(List<MusicData> musicDataList) {
        if(musicDataList.isEmpty()) {
            return;
        }

        mMusicData.clear();
        mDiscAnimators.clear();
        mMusicData.addAll(musicDataList);
        myViewFlipper.setMusicSize(musicDataList.size());
        //获取第一首歌曲信息
        MusicData musicData = mMusicData.get(0);
        otherPosterRes = musicData.getmMusicPicRes();
        //添加碟片
        for (int i = 0; i < 2; i++) {
            View discLayout = LayoutInflater.from(getContext()).inflate(R.layout.layout_disc,
                    myViewFlipper, false);
            ImageView disc = discLayout.findViewById(R.id.iv_disc);
            disc.setImageBitmap(getDiscDrawable());
            ImageView poster = discLayout.findViewById(R.id.iv_poster);
            poster.setImageDrawable(getDiscPosterDrawable(otherPosterRes));
            ViewCalculateUtil.setViewRelativeLayoutParam(disc, UIUtils.DISC_BLACK_WIDTH,
                    UIUtils.DISC_BLACK_WIDTH, 3, 0, 0, 0,
                    true);
            ViewCalculateUtil.setViewRelativeLayoutParam(poster, UIUtils.DISC_POSTER_WIDTH,
                    UIUtils.DISC_POSTER_WIDTH, 0, 0, 0, 0,
                    true);
            mDiscAnimators.add(getDiscObjectAnimator(discLayout));
            myViewFlipper.addView(discLayout);
        }

        //接口回调通知图片更新
        if(mIPlayInfo != null) {
            mIPlayInfo.onMusicInfoChanged(musicData.getmMusicName(), musicData.getmMusicAuthor());
            mIPlayInfo.onMusicPicChanged(musicData.getmMusicPicRes());
        }
    }

    private ObjectAnimator getDiscObjectAnimator(View disc) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(disc, View.ROTATION, 0, 360);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setDuration(20 * 1000);
        objectAnimator.setInterpolator(new LinearInterpolator());

        return objectAnimator;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    //播放动画
    private void playAnimator() {
        //唱针处于远端，直接播放动画
        if(needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
            mNeedleAnimator.start();
            //唱针处于往远端移动时，设置标记，等待动画结束后再播放动画
        }else if(needleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END) {
            mIsNeed2StartPlayAnimator = true;
        }
    }

    //暂停动画
    private void pauseAnimator() {
        //播放是暂停动画
        if(needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
            pauseDiscAnimator();
            //唱针往唱盘移动时暂停动画
        }else if(needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
            mNeedleAnimator.reverse();
            //若动画在没结束时执行reverse方法，则不会执行监听器的onStart方法，此时需要手动设置
            needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
        }
        //动画可能执行多次，只有音乐处于停止/暂停状态时才执行暂停命令
        if(musicStatus == MusicStatus.STOP) {
            notifyMusicStatusChanged(MusicChangedStatus.STOP);
        }else if(musicStatus == MusicStatus.PAUSE) {
            notifyMusicStatusChanged(MusicChangedStatus.PAUSE);
        }
    }

    //播放唱盘动画
    private void playDiscAnimator() {
        ObjectAnimator objectAnimator = mDiscAnimators.get(myViewFlipper.getDisplayedChild());
        if(objectAnimator.isPaused()) {
            objectAnimator.resume();
        }else {
            objectAnimator.start();
        }
        //唱盘动画可能执行多次，只有音乐不是播放状态时，才执行播放
        if(musicStatus != MusicStatus.PLAY) {
            notifyMusicStatusChanged(MusicChangedStatus.PLAY);
        }
    }

    //暂停唱盘动画
    private void pauseDiscAnimator() {
        ObjectAnimator objectAnimator = mDiscAnimators.get(myViewFlipper.getDisplayedChild());
        objectAnimator.pause();
        mNeedleAnimator.reverse();
    }

    public void notifyMusicInfoChanged(int position) {
        if(mIPlayInfo != null) {
            MusicData musicData = mMusicData.get(position);
            mIPlayInfo.onMusicInfoChanged(musicData.getmMusicName(), musicData.getmMusicAuthor());
        }
    }

    public void notifyMusicPicChanged(int position) {
        if(mIPlayInfo != null) {
            MusicData musicData = mMusicData.get(position);
            mIPlayInfo.onMusicPicChanged(musicData.getmMusicPicRes());
        }
    }

    public void notifyMusicStatusChanged(MusicChangedStatus musicChangedStatus) {
        if(mIPlayInfo != null) {
            mIPlayInfo.onMusicChanged(musicChangedStatus);
        }
    }

    private void play() {
        playAnimator();
    }

    private void pause() {
        musicStatus = MusicStatus.PAUSE;
        pauseAnimator();
    }

    public void stop() {
        musicStatus = MusicStatus.STOP;
        pauseAnimator();
    }

    public void playOrPause() {
        if(musicStatus == MusicStatus.PLAY) {
            pause();
        }else {
            play();
        }
    }

    public void next() {
        int next = myViewFlipper.getNextItem();
        if(otherPosterRes != mMusicData.get(next).getmMusicPicRes()) {
            otherPosterRes = mMusicData.get(next).getmMusicPicRes();
            myViewFlipper.getOtherPosterView().setImageDrawable(getDiscPosterDrawable(otherPosterRes));
        }
        myViewFlipper.showNextWidthAnimation();;
        selectMusicWithButton();
    }

    public void previous() {
        int previous = myViewFlipper.getPreviousItem();
        if(otherPosterRes != mMusicData.get(previous).getmMusicPicRes()){
            otherPosterRes = mMusicData.get(previous).getmMusicPicRes();
            myViewFlipper.getOtherPosterView().setImageDrawable(getDiscPosterDrawable(otherPosterRes));
        }
        myViewFlipper.showPreviousWithAnimation();
        selectMusicWithButton();
    }

    public boolean isPlaying() {
        return musicStatus == MusicStatus.PLAY;
    }

    private void selectMusicWithButton() {
        if(musicStatus == MusicStatus.PLAY) {
            mIsNeed2StartPlayAnimator = true;
            pauseAnimator();
        }else if(musicStatus == MusicStatus.PAUSE) {
            play();
        }
    }
}

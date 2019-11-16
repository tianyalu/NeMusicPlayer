## NeMusicPlay 网易云音乐打碟界面
### 1. 框架结构图
![image](https://github.com/tianyalu/NeMusicPlayer/blob/master/show/frame_event_translate.png)
### 2. 说明
* ImageSwitcher: 用来做整个界面背景图片的切换(子控件有且只能有两个ImageView)
* ViewFlipper: 类似于ViewPager，用来做打碟图片承载View的切换(可添加多个子控件[本例子中其实也只是添加了两个
子控件]，子控件只有一个时显示，否则不显示)  

### 3. 示例效果图
![image](https://github.com/tianyalu/NeMusicPlayer/blob/master/show/show.gif)

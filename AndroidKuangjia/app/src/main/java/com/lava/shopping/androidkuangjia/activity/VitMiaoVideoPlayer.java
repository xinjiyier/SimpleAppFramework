package com.lava.shopping.androidkuangjia.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lava.shopping.androidkuangjia.R;
import com.lava.shopping.androidkuangjia.items.MediaItem;
import com.lava.shopping.androidkuangjia.utils.MediaUtils;
import com.lava.shopping.androidkuangjia.utils.TimeUtils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

public class VitMiaoVideoPlayer extends Activity implements View.OnClickListener{

    private VideoView mVideoView;
    private LinearLayout llMediaplayerTitlebarLine;
    private TextView tvMediaName;
    private ImageView ivMediaBattery;
    private TextView tvSystemTime;
    private LinearLayout llMediaplayerVoiceLine;
    private LinearLayout llMediaplayerDurationLine;
    private TextView tvMediaDurationComplete;
    private SeekBar sbMediaDuration;
    private ImageView ivVoiceSilence;
    private SeekBar sbMediaVoice;
    private ImageView ivSwitchPlayer;
    private TextView tvMediaDurationTotal;
    private LinearLayout llMediaplayerControlLine;
    private ImageView ivReturn;
    private ImageView ivBackward;
    private ImageView ivPlayPause;
    private ImageView ivNext;
    private ImageView ivFullScreen;
    private TextView tvInternetSpeed;
    private RelativeLayout pbCatch;
    private RelativeLayout rlMediaVideoController;

    private GestureDetector gestureDetector;
    private AudioManager audioManager;
    private TimeUtils timeUtils;
    private MediaUtils mediaUtils;
    private List<MediaItem> mediaItems;
    /**
     * 定义屏幕的长宽和视频的长宽
     */
    private int screenWidth;
    private int screenHeigth;
    private int mediaWidth;
    private int mediaHeight;
    /**
     * 当前播放视频在list中的位置
     */
    int mediaItemsPosition;
    /**
     * 当前播放视频是否是网络资源
     */
    boolean isWebVideo = false;
    /**
     * 当前视频控制界面是否显示
     */
    private boolean isMediaControllerVisiable;
    /**
     *
     */
    private boolean isFullScreen = false;
    private boolean isCatching = false;
    /**
     *用于刷新视频进度信息
     */
    private static final int SB_REFRESH = 0;
    /**
     * 用于让视频控制器隐藏
     */
    private static final int HIDE_MEDIA_CONTROLLER = 1;


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SB_REFRESH:
                    refreshMediaSeekBar();
                    if(isWebVideo){
                        refreshBufferProgress();
                    }
                    /**
                     * 如果视频出现卡顿
                     * 则显示当前网速
                     */
                    if(isCatching){
                        refreshInternetSpeed();
                    }
                    handler.removeMessages(SB_REFRESH);
                    handler.sendEmptyMessageDelayed(SB_REFRESH,1000);
                    break;
                case HIDE_MEDIA_CONTROLLER:
                    hideMediaController();
                    break;
            }
        }
    };
    private int maxVolume;
    private int currentVolume;
    private boolean isSlience = false;

    private void refreshSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm");
        Date date = new Date(System.currentTimeMillis());
        String currentTime = format.format(date);
        tvSystemTime.setText(currentTime);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case Intent.ACTION_BATTERY_CHANGED:
                    int batteryLevel = intent.getIntExtra("level",0);
                    refreshBatteryIcon(batteryLevel);
                    break;
                case Intent.ACTION_TIME_TICK:
                    refreshSystemTime();
                    break;
            }

        }
    };

    private void refreshBatteryIcon(int batteryLevel) {
        if(batteryLevel>90){
            ivMediaBattery.setBackgroundResource(R.drawable.ic_battery_100);
        }else if(batteryLevel>80){
            ivMediaBattery.setBackgroundResource(R.drawable.ic_battery_80);
        }else if(batteryLevel > 60){
            ivMediaBattery.setBackgroundResource(R.drawable.ic_battery_60);
        }else if(batteryLevel > 40){
            ivMediaBattery.setBackgroundResource(R.drawable.ic_battery_40);
        }else if(batteryLevel > 20){
            ivMediaBattery.setBackgroundResource(R.drawable.ic_battery_20);
        }else if(batteryLevel >10){
            ivMediaBattery.setBackgroundResource(R.drawable.ic_battery_10);
        }else{
            ivMediaBattery.setBackgroundResource(R.drawable.ic_battery_0);
        }
    }

    private void refreshMediaSeekBar() {
        int current = (int) mVideoView.getCurrentPosition();
        tvMediaDurationComplete.setText(timeUtils.stringForTime(current));
        sbMediaDuration.setProgress(current);
    }

    /**
     * 用于更新调节音量的seekbar的状态
     */
    private void refeshAudioSeekbar(boolean isSlience,int currentVolume){
        displayMediaController();
        if(isSlience){
            sbMediaVoice.setProgress(0);
        }else{
            //currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            sbMediaVoice.setProgress(currentVolume);
        }
    }

    /**
     * 如果是网络视频，则显示缓存进度
     */
    private void refreshBufferProgress(){
        int buffer = mVideoView.getBufferPercentage();
        int totalBuffer = buffer * sbMediaDuration.getMax();
        int secondaryProcess = totalBuffer/100;
        sbMediaDuration.setSecondaryProgress(secondaryProcess);
    }

    /**
     * 当卡顿的时候，进行更新网络速度
     * 以告知客户
     */
    private void refreshInternetSpeed(){
        tvInternetSpeed.setText(mediaUtils.getCurrentInternetSpeed(this));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(this);
        setContentView(R.layout.activity_vitmiao_video_player);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSystemTime();
    }

    private void initViews() {
        rlMediaVideoController = (RelativeLayout) findViewById(R.id.rl_media_video_controller);
        pbCatch = (RelativeLayout) findViewById(R.id.rl_catch);
        tvInternetSpeed = (TextView) findViewById(R.id.tv_internet_speed);
        /**
         * titleBar line views
         */
        llMediaplayerTitlebarLine = (LinearLayout)findViewById( R.id.ll_mediaplayer_titlebar_line );
        tvMediaName = (TextView)findViewById( R.id.tv_media_name );
        ivMediaBattery = (ImageView)findViewById( R.id.iv_media_battery );
        tvSystemTime = (TextView)findViewById( R.id.tv_system_time );
        /**
         * voice line views
         */
        llMediaplayerVoiceLine = (LinearLayout)findViewById( R.id.ll_mediaplayer_voice_line );
        ivVoiceSilence = (ImageView)findViewById( R.id.iv_voice_silence );
        sbMediaVoice = (SeekBar)findViewById( R.id.sb_media_voice );
        ivSwitchPlayer = (ImageView)findViewById( R.id.iv_switch_player );
        /**
         * duration line views
         */
        llMediaplayerDurationLine = (LinearLayout)findViewById( R.id.ll_mediaplayer_duration_line );
        tvMediaDurationComplete = (TextView)findViewById( R.id.tv_media_duration_complete );
        sbMediaDuration = (SeekBar)findViewById( R.id.sb_media_duration );
        tvMediaDurationTotal = (TextView)findViewById( R.id.tv_media_duration_total );
        /**
         * media control line views
         */
        llMediaplayerControlLine = (LinearLayout)findViewById( R.id.ll_mediaplayer_control_line );
        ivReturn = (ImageView)findViewById( R.id.iv_return );
        ivBackward = (ImageView)findViewById( R.id.iv_backward );
        ivPlayPause = (ImageView)findViewById( R.id.iv_play_pause );
        ivNext = (ImageView)findViewById( R.id.iv_next );
        ivFullScreen = (ImageView)findViewById( R.id.iv_full_screen );
        /**
         * 计算屏幕的长宽
         */
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeigth = dm.heightPixels;

        initOtherData();
    }

    private void initOtherData() {
        /**
         * 更新上一个和下一个视频的按钮
         * 隐藏控制栏
         * 初始化并更新音量条的数据
         */
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        sbMediaVoice.setMax(maxVolume);

        setLastAndNextIconState(mediaItemsPosition);
        hideMediaController();
        refeshAudioSeekbar(isSlience,audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }


    private void init() {
        timeUtils = new TimeUtils();
        mediaUtils = new MediaUtils();
        gestureDetector = new GestureDetector(this,new MyGestureDetector());
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mVideoView = (VideoView) this.findViewById(R.id.media_vv);
        mVideoView.setOnCompletionListener(new OnMediaCompleted());
        mVideoView.setOnErrorListener(new OnMediaErrored());
        mVideoView.setOnPreparedListener(new OnMediaPrepared());
        mediaItemsPosition = getIntent().getIntExtra("position",0);
        if(mVideoView!=null){
            mVideoView.setVideoURI(getData(mediaItemsPosition));
        }
        //mVideoView.setMediaController(new MediaController(ShoppingVideoPlayer.this));
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);

        registerReceiver(receiver,filter);
        initViews();
        initListener();
    }

    private void initListener() {
        ivVoiceSilence.setOnClickListener(this);
        ivSwitchPlayer.setOnClickListener(this);
        ivReturn.setOnClickListener(this);
        ivBackward.setOnClickListener(this);
        ivPlayPause.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        ivFullScreen.setOnClickListener(this);

        sbMediaDuration.setOnSeekBarChangeListener(new MyDurationSeekBarChangeListener());
        sbMediaVoice.setOnSeekBarChangeListener(new MyVoiceSeekBarChangeListener());
        mVideoView.setOnInfoListener(new MyOnInfoListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        Log.d("chenxiaoping","onTouch");
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(currentVolume > 0){
                    currentVolume --;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
                    refeshAudioSeekbar(false,currentVolume);
                }else if(currentVolume==0){
                    isSlience = true;
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                isSlience = false;
                if(currentVolume < maxVolume){
                    currentVolume ++;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
                    refeshAudioSeekbar(false,currentVolume);
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_voice_silence:
                break;
            case R.id.iv_switch_player:
                break;
            case R.id.iv_return:
                finish();
                break;
            case R.id.iv_backward:
                toLastVideo();
                break;
            case R.id.iv_play_pause:
                refeshPlayAndPauseIcon();
                break;
            case R.id.iv_next:
                toNextVideo();
                break;
            case R.id.iv_full_screen:
                setFullScreenOrDefault();
                break;
        }
        removeAndResendHideMessage();
    }

    private void toNextVideo() {
        int mediaSize = mediaItems.size();
        if(mediaItemsPosition == (mediaSize-1)){
            //如果当前为最后一个，则播放第一个视频
        }else{
            //就播放下一个视频
            mediaItemsPosition++;
            //mVideoView.setVideoURI(Uri.parse(mediaItems.get(mediaItemsPosition).getMediaData()));
            mVideoView.setVideoURI(getData(mediaItemsPosition));
            setLastAndNextIconState(mediaItemsPosition);
        }
        Toast.makeText(this, "mediaSize  "+mediaSize, Toast.LENGTH_SHORT).show();
    }

    private void setLastAndNextIconState(int position) {
        if(mediaItems!=null && mediaItems.size() > 0){
            ivNext.setEnabled(true);
            ivBackward.setEnabled(true);
            int mediaSize = mediaItems.size();
            if(++position > (mediaSize-1)){
                //当前为倒数第一个
                Log.d("ccc","当前为倒数第一个");
                ivNext.setEnabled(false);
            }
            if((position-2) < 0){
                //当前为第一个
                Log.d("ccc","当前为第一个");
                ivBackward.setEnabled(false);
            }
        }else{
            ivNext.setEnabled(false);
            ivBackward.setEnabled(false);
        }
    }

    private void toLastVideo() {
        int mediaSize = mediaItems.size();
        if(mediaItemsPosition == 0){
            //如果当前为第一个，则弹框提示
        }else{
            //就播放上一个视频
            mediaItemsPosition--;
            //mVideoView.setVideoURI(Uri.parse(mediaItems.get(mediaItemsPosition).getMediaData()));
            mVideoView.setVideoURI(getData(mediaItemsPosition));
            setLastAndNextIconState(mediaItemsPosition);
        }
        Toast.makeText(this, "mediaSize  "+mediaSize, Toast.LENGTH_SHORT).show();
    }

    private void refeshPlayAndPauseIcon(){
        if(mVideoView.isPlaying()){
            mVideoView.pause();
            ivPlayPause.setBackgroundResource(R.drawable.play_image);
        }else{
            mVideoView.start();
            ivPlayPause.setBackgroundResource(R.drawable.pause_image);
        }
    }

    public Uri getData(int position) {
        Uri uri;
        mediaItems = (List<MediaItem>) getIntent().getSerializableExtra("videolist");
        if(mediaItems != null && mediaItems.size() > 0){
            uri = Uri.parse(mediaItems.get(mediaItemsPosition).getMediaData());
        }else{
            uri = getIntent().getData();
        }
        if(mediaUtils.isWebVideo(uri)){
            isWebVideo = true;
        }
        return uri;
    }

    class OnMediaCompleted implements io.vov.vitamio.MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(io.vov.vitamio.MediaPlayer mp) {
            refeshPlayAndPauseIcon();
        }
    }

    class OnMediaErrored implements MediaPlayer.OnErrorListener, io.vov.vitamio.MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            return false;
        }

        @Override
        public boolean onError(io.vov.vitamio.MediaPlayer mp, int what, int extra) {
            return false;
        }
    }

    class OnMediaPrepared implements io.vov.vitamio.MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(io.vov.vitamio.MediaPlayer mediaPlayer) {
            //*计算视频的长宽
            //*
            mediaWidth = mediaPlayer.getVideoWidth();
            mediaHeight = mediaPlayer.getVideoHeight();

            int duration = (int) mediaPlayer.getDuration();
            sbMediaDuration.setMax(duration);
            tvMediaDurationTotal.setText(new TimeUtils().stringForTime(duration));
            mediaPlayer.start();
            handler.sendEmptyMessage(SB_REFRESH);
            //tvMediaName.setText(mVideoView.get);
            setAsDefaultScreen();
        }
    }

    /**
     * 手动拖动视频进度监听
     */
    class MyDurationSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
            Log.d("cxp","sadfasdfasdfas");
            if(fromUser){
                mVideoView.seekTo(i);
                sbMediaDuration.setProgress(i);
                handler.sendEmptyMessage(SB_REFRESH);
                removeAndResendHideMessage();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //进度条被认为触摸
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //进度条人为触摸结束
        }
    }

    /**
     * 手动音量调节监听
     */
    class MyVoiceSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //当拖动音量调节，改变音量的时候，对系统音量进行同步更新
            if(fromUser){
                //当只有是人为拖动的时候才会改变
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener{
        public MyGestureDetector() {
            super();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //Toast.makeText(VitMiaoVideoPlayer.this,"长按事件",Toast.LENGTH_SHORT).show();
            super.onLongPress(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //Toast.makeText(VitMiaoVideoPlayer.this,"双击事件",Toast.LENGTH_SHORT).show();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //Toast.makeText(VitMiaoVideoPlayer.this,"单击事件",Toast.LENGTH_SHORT).show();
            if(!isMediaControllerVisiable){
                displayMediaController();
            }else{
                handler.removeMessages(HIDE_MEDIA_CONTROLLER);
                hideMediaController();
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    class MyOnInfoListener implements io.vov.vitamio.MediaPlayer.OnInfoListener {
        @Override
        public boolean onInfo(io.vov.vitamio.MediaPlayer mp, int what, int extra) {
            switch (what){
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    isCatching = true;
                    pbCatch.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    isCatching = false;
                    pbCatch.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    }
    private void displayMediaController(){
        isMediaControllerVisiable = true;
        rlMediaVideoController.setVisibility(View.VISIBLE);
        removeAndResendHideMessage();
    }

    private void hideMediaController(){
        isMediaControllerVisiable = false;
        rlMediaVideoController.setVisibility(View.GONE);
    }

    private void removeAndResendHideMessage(){
        handler.removeMessages(HIDE_MEDIA_CONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER,4000);
    }

    /**
     * 该方法用于全屏和适应屏幕大小切换
     */
    private void setFullScreenOrDefault(){
        if(isFullScreen){
            setAsDefaultScreen();
        }else{
            setAsFullScreen();
        }
    }

    private void setAsDefaultScreen() {
        //当前是全屏
        //根据视频的长宽和屏幕的长宽来计算
        //视频应该显示的长宽
        /**
         * 计算应有的长宽
         */
        int width = screenWidth;
        int height = screenHeigth;
        int mVideoWidth = mediaWidth;
        int mVideoHeight = mediaHeight;

        if (mVideoWidth * height < width * mVideoHeight) {
            //Log.i("@@@", "image too wide, correcting");
            width = height * mVideoWidth / mVideoHeight;
        } else if (mVideoWidth * height > width * mVideoHeight) {
            //Log.i("@@@", "image too tall, correcting");
            height = width * mVideoHeight / mVideoWidth;
        }
        //mVideoView.setVideoSize(width,height);
        isFullScreen = false;
    }

    private void setAsFullScreen() {
        //当前不是全屏
        //设置为全屏
        //mVideoView.setVideoSize(screenWidth,screenHeigth);
        isFullScreen = true;
    }

    @Override
    protected void onDestroy() {
        if(receiver!=null){
            unregisterReceiver(receiver);
            receiver = null;
        }
        handler.removeCallbacksAndMessages(null);
        handler = null;
        super.onDestroy();
    }
}

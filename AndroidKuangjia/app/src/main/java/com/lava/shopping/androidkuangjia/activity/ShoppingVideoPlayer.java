package com.lava.shopping.androidkuangjia.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
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
import com.lava.shopping.androidkuangjia.utils.TimeUtils;
import com.lava.shopping.androidkuangjia.view.VideoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ShoppingVideoPlayer extends Activity implements View.OnClickListener{

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
    private RelativeLayout rlMediaVideoController;

    private GestureDetector gestureDetector;
    private TimeUtils timeUtils;
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
                    handler.removeMessages(SB_REFRESH);
                    handler.sendEmptyMessageDelayed(SB_REFRESH,1000);
                    break;
                case HIDE_MEDIA_CONTROLLER:
                    hideMediaController();
                    break;
            }
        }
    };

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
        int current = mVideoView.getCurrentPosition();
        tvMediaDurationComplete.setText(timeUtils.stringForTime(current));
        sbMediaDuration.setProgress(current);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_video_player);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSystemTime();
    }

    private void findViews() {
        rlMediaVideoController = (RelativeLayout) findViewById(R.id.rl_media_video_controller);
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

        setLastAndNextIconState(mediaItemsPosition);
        hideMediaController();
    }


    private void init() {
        timeUtils = new TimeUtils();
        gestureDetector = new GestureDetector(this,new MyGestureDetector());
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
        findViews();
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        Log.d("chenxiaoping","onTouch");
        return super.onTouchEvent(event);
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
        int mediaSize = mediaItems.size();
        ivNext.setEnabled(true);
        ivBackward.setEnabled(true);
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
        return uri;
    }

    class OnMediaCompleted implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Toast.makeText(ShoppingVideoPlayer.this,"",Toast.LENGTH_SHORT).show();
            refeshPlayAndPauseIcon();
        }
    }

    class OnMediaErrored implements MediaPlayer.OnErrorListener{
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            return false;
        }
    }

    class OnMediaPrepared implements MediaPlayer.OnPreparedListener{

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            /**
             * 计算视频的长宽
             */
            mediaWidth = mediaPlayer.getVideoWidth();
            mediaHeight = mediaPlayer.getVideoHeight();

            int duration = mediaPlayer.getDuration();
            sbMediaDuration.setMax(duration);
            tvMediaDurationTotal.setText(new TimeUtils().stringForTime(duration));
            mediaPlayer.start();
            handler.sendEmptyMessage(SB_REFRESH);
            //tvMediaName.setText(mVideoView.get);
            setAsDefaultScreen();
        }
    }

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

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener{
        public MyGestureDetector() {
            super();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Toast.makeText(ShoppingVideoPlayer.this,"长按事件",Toast.LENGTH_SHORT).show();
            super.onLongPress(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Toast.makeText(ShoppingVideoPlayer.this,"双击事件",Toast.LENGTH_SHORT).show();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Toast.makeText(ShoppingVideoPlayer.this,"单击事件",Toast.LENGTH_SHORT).show();
            if(!isMediaControllerVisiable){
                displayMediaController();
            }
            return super.onSingleTapConfirmed(e);
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
        mVideoView.setVideoSize(width,height);
        isFullScreen = false;
    }

    private void setAsFullScreen() {
        //当前不是全屏
        //设置为全屏
        mVideoView.setVideoSize(screenWidth,screenHeigth);
        isFullScreen = true;
    }

    @Override
    protected void onDestroy() {
        if(receiver!=null){
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}

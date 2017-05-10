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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.lava.shopping.androidkuangjia.R;
import com.lava.shopping.androidkuangjia.items.MediaItem;
import com.lava.shopping.androidkuangjia.utils.TimeUtils;

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

    private TimeUtils timeUtils;
    private List<MediaItem> mediaItems;
    int mediaItemsPosition;

    private static final int SB_REFRESH = 0;

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

        setLastAndNextIconState(mediaItemsPosition);
    }


    private void init() {
        timeUtils = new TimeUtils();
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

        sbMediaDuration.setOnSeekBarChangeListener(new MySeekBarChangeListener());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_voice_silence:
                break;
            case R.id.iv_switch_player:
                break;
            case R.id.iv_return:
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
                break;
        }
    }

    private void toNextVideo() {
        int mediaSize = mediaItems.size();
        if(mediaItemsPosition == (mediaSize-1)){
            //如果当前为最后一个，则播放第一个视频
        }else{
            //就播放下一个视频
            mediaItemsPosition++;
            mVideoView.setVideoURI(Uri.parse(mediaItems.get(mediaItemsPosition).getMediaData()));
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
            mVideoView.setVideoURI(Uri.parse(mediaItems.get(mediaItemsPosition).getMediaData()));
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
            int duration = mediaPlayer.getDuration();
            sbMediaDuration.setMax(duration);
            tvMediaDurationTotal.setText(new TimeUtils().stringForTime(duration));
            mediaPlayer.start();
            handler.sendEmptyMessage(SB_REFRESH);
            //tvMediaName.setText(mVideoView.get);
        }
    }

    class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
            Log.d("cxp","sadfasdfasdfas");
            if(fromUser){
                mVideoView.seekTo(i);
                sbMediaDuration.setProgress(i);
                handler.sendEmptyMessage(SB_REFRESH);
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

    @Override
    protected void onDestroy() {
        if(receiver!=null){
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}

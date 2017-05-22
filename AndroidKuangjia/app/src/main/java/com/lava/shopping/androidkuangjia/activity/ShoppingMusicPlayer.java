package com.lava.shopping.androidkuangjia.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lava.shopping.androidkuangjia.IMyMusicPlayerService;
import com.lava.shopping.androidkuangjia.IMyMusicPlayerServiceCallBack;
import com.lava.shopping.androidkuangjia.items.MediaItem;
import com.lava.shopping.androidkuangjia.service.MyMusicPlayerService;
import com.lava.shopping.androidkuangjia.R;
import com.lava.shopping.androidkuangjia.utils.TimeUtils;

import java.util.List;

public class ShoppingMusicPlayer extends Activity implements View.OnClickListener{
    private TextView tvMusicName;
    private TextView tvMusicArtist;
    private TextView tvMusicProgress;
    private SeekBar sbMusicProgress;
    private ImageView ivMusicImageBg;
    private Button btChangeMode;
    private Button btPlayPre;
    private Button btPalyAndPause;
    private Button btPalyNext;
    private Button btShowLyric;

    private StringBuffer sBuffer = new StringBuffer();
    private TimeUtils utils;
    private IMyMusicPlayerService mService;
    private List<MediaItem> mediaItems;
    private MediaItem mediaItem;
    private int mediaItemsPosition = 0 ;
    private boolean isBind = false;
    private boolean isServicePrepared = false;

    private static final int BUTTON_ENABLE = 0;
    private static final int INIT_MUSIC_STABLE_DATA = 1;
    private static final int INIT_MUSIC_CHANGING_DATA = 2;
    /**
     * 播放模式分别是
     * 列表循环 MUSIC_LOOP
     * 单曲循环 MUSIC_SINGLE
     * 顺序播放 MUSIC_ORDER
     * 随机播放 MUSIC_RANDOM
     */
    private static final int MUSIC_LOOP = 0;
    private static final int MUSIC_SINGLE = 1;
    private static final int MUSIC_ORDER = 2;
    private static final int MUSIC_RANDOM = 3;

    private int[] musicMode = new int[]{MUSIC_LOOP,MUSIC_SINGLE,MUSIC_ORDER};
    private int currentMode = MUSIC_LOOP;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case BUTTON_ENABLE:
                    setButtonsEnable(true);
                    break;
                case INIT_MUSIC_STABLE_DATA:
                    initMusicStableData();
                    break;
                case INIT_MUSIC_CHANGING_DATA:
                    initMusicChangingData();
                    handler.sendEmptyMessageDelayed(INIT_MUSIC_CHANGING_DATA,1000);
                    break;
            }
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = IMyMusicPlayerService.Stub.asInterface(iBinder);
            isBind = true;
            try {
                mService.registerCallback(callback);
                Log.d("xxx","registerCallback 1111111111");
                preparePlayMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBind = false;
        }
    };

    private void preparePlayMusic() {
        try {
            mService.prepare(mediaItemsPosition);
            setButtonsEnable(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    IMyMusicPlayerServiceCallBack.Stub callback = new IMyMusicPlayerServiceCallBack.Stub() {
        @Override
        public void Dataompleted() throws RemoteException {
            Log.d("xxx","dataComplete 1111111111");
            mediaItemsPosition = mService.getCurrentMusic();
            //preparePlayMusic();
            handler.sendEmptyMessage(BUTTON_ENABLE);
            handler.sendEmptyMessage(INIT_MUSIC_STABLE_DATA);
            handler.sendEmptyMessage(INIT_MUSIC_CHANGING_DATA);
        }
    };

    /**
     * 加载界面中不变的值（歌名，歌手,以及seekbar的最大值）
     */
    private void initMusicStableData(){
        mediaItem = mediaItems.get(mediaItemsPosition);
        try {
            if(mediaItem!=null){
                tvMusicName.setText(mediaItem.getMediaName());
                tvMusicArtist.setText(mediaItem.getMediaArtist());
                sbMusicProgress.setMax(mService.getMusicDuration());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载界面中随着歌曲不断变化的值
     * 比如说歌曲进度
     */
    private void initMusicChangingData() {
        try {
            if(mService!=null){
                sBuffer.setLength(0);
                sBuffer.append(utils.stringForTime(mService.getMusicCurrentPosition()));
                sBuffer.append("/");
                Log.d("chenxiaoping","aaaaaa  "+(sBuffer==null)+"       "+(mService==null));
                sBuffer.append(utils.stringForTime(mService.getMusicDuration()));
                sbMusicProgress.setProgress(mService.getMusicCurrentPosition());
                tvMusicProgress.setText(sBuffer);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void setButtonsEnable(boolean isEnable) {
        btChangeMode.setEnabled(isEnable);
        btPlayPre.setEnabled(isEnable);
        btPalyAndPause.setEnabled(isEnable);
        btPalyNext.setEnabled(isEnable);
        btShowLyric.setEnabled(isEnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("uuu","onCreate");
        setContentView(R.layout.activity_shopping_music_player);
        initViews();
        initDatas();
        bindToMusicService();
    }

    private void bindToMusicService() {
        Intent intent = new Intent(this, MyMusicPlayerService.class);
        intent.setAction("com.lava.shopping.androidkuangjia.service.MyMusicPlayerService");
        intent.setPackage(ShoppingMusicPlayer.this.getPackageName());
        bindService(intent,conn, Context.BIND_AUTO_CREATE);
        startService(intent);//防止实例化多个服务
    }

    private void initViews() {
        tvMusicName = (TextView)findViewById( R.id.tv_music_name );
        tvMusicArtist = (TextView)findViewById( R.id.tv_music_artist );
        tvMusicProgress = (TextView) findViewById(R.id.tv_music_progress);
        sbMusicProgress = (SeekBar) findViewById(R.id.sb_music_progress);
        btChangeMode = (Button)findViewById( R.id.bt_change_mode );
        btPlayPre = (Button)findViewById( R.id.bt_play_pre );
        btPalyAndPause = (Button)findViewById( R.id.bt_play_and_pause );
        btPalyNext = (Button)findViewById( R.id.bt_paly_next );
        btShowLyric = (Button)findViewById( R.id.bt_show_lyric );
        ivMusicImageBg = (ImageView) findViewById(R.id.iv_music_image_bg);

        /*ivMusicImageBg.setBackgroundResource(R.drawable.music_iv_bg);*/
        AnimationDrawable ad = (AnimationDrawable) ivMusicImageBg.getBackground();
        ad.start();

        btChangeMode.setOnClickListener( this );
        btPlayPre.setOnClickListener( this );
        btPalyAndPause.setOnClickListener( this );
        btPalyNext.setOnClickListener( this );
        btShowLyric.setOnClickListener( this );
        sbMusicProgress.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }

    private void initDatas() {
        utils = new TimeUtils();
        mediaItemsPosition = getIntent().getIntExtra("position",0);
        getData(mediaItemsPosition);
    }

    public Uri getData(int position) {
        Uri uri;
        mediaItems = (List<MediaItem>) getIntent().getSerializableExtra("videolist");
        if(mediaItems != null && mediaItems.size() > 0){
            Log.d("chenxiaoping","mediaItemsPosition  "+ mediaItemsPosition+"name := "+mediaItems.get(mediaItemsPosition).getMediaData());
            uri = Uri.parse(mediaItems.get(mediaItemsPosition).getMediaData());
        }else{
            uri = getIntent().getData();
        }
        return uri;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("uuu","ondestroy");
        unbindService(conn);
        try {
            //在销毁的时候取消回调注册
            if(callback!=null){
                mService.unRegisterCallback(callback);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        isServicePrepared = false;
        mService = null;
        isBind = false;
    }

    @Override
    public void onClick(View v) {
        if ( v == btChangeMode ) {
            // Handle clicks for btChangeMode
            resetMusicPlayMode();
        } else if ( v == btPlayPre ) {
            // Handle clicks for btPlayPre
            try {
                mService.playPreMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if ( v == btPalyAndPause ) {
            try {
                if(mService.isPlaying()){
                    mService.pause();
                    btPalyAndPause.setBackgroundResource(R.drawable.play_bt_bg);
                }else{
                    mService.start();
                    btPalyAndPause.setBackgroundResource(R.drawable.pause_bt_bg);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if ( v == btPalyNext ) {
            // Handle clicks for btPalyNext
            try {
                mService.playNextMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if ( v == btShowLyric ) {
            // Handle clicks for btShowLyric
        }
    }

    private void resetMusicPlayMode() {
        switch (currentMode){
            case MUSIC_LOOP:
                try {
                    currentMode = MUSIC_SINGLE;
                    btChangeMode.setBackgroundResource(R.drawable.singlemode_bt_bg);
                    mService.setPlayMode(MUSIC_SINGLE);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case MUSIC_SINGLE:
                try {
                    currentMode = MUSIC_ORDER;
                    btChangeMode.setBackgroundResource(R.drawable.ordermode_bt_bg);
                    mService.setPlayMode(MUSIC_ORDER);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case MUSIC_ORDER:
                try {
                    currentMode = MUSIC_LOOP;
                    btChangeMode.setBackgroundResource(R.drawable.loopmode_bt_bg);
                    mService.setPlayMode(MUSIC_LOOP);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                try {
                    mService.seekToProgress(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}

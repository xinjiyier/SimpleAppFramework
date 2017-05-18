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
import android.widget.TextView;

import com.lava.shopping.androidkuangjia.IMyMusicPlayerService;
import com.lava.shopping.androidkuangjia.IMyMusicPlayerServiceCallBack;
import com.lava.shopping.androidkuangjia.items.MediaItem;
import com.lava.shopping.androidkuangjia.service.MyMusicPlayerService;
import com.lava.shopping.androidkuangjia.R;

import java.util.List;

public class ShoppingMusicPlayer extends Activity implements View.OnClickListener{
    private TextView tvMusicName;
    private TextView tvMusicArtist;
    private ImageView ivMusicImageBg;
    private Button btChangeMode;
    private Button btPlayPre;
    private Button btPalyAndPause;
    private Button btPalyNext;
    private Button btShowLyric;

    private IMyMusicPlayerService mService;
    private List<MediaItem> mediaItems;
    private int mediaItemsPosition = 0 ;
    private boolean isBind = false;
    private boolean isServicePrepared = false;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setButtonsEnable(true);
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
            //preparePlayMusic();
            handler.sendEmptyMessage(0);
        }
    };

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
        btChangeMode = (Button)findViewById( R.id.bt_change_mode );
        btPlayPre = (Button)findViewById( R.id.bt_play_pre );
        btPalyAndPause = (Button)findViewById( R.id.bt_paly_and_pause );
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

    }

    private void initDatas() {
        mediaItemsPosition = getIntent().getIntExtra("position",0);
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
        } else if ( v == btPlayPre ) {
            // Handle clicks for btPlayPre
        } else if ( v == btPalyAndPause ) {
            try {
                if(mService.isPlaying()){
                    mService.pause();
                }else{
                    mService.start();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if ( v == btPalyNext ) {
            // Handle clicks for btPalyNext
        } else if ( v == btShowLyric ) {
            // Handle clicks for btShowLyric
        }
    }
}

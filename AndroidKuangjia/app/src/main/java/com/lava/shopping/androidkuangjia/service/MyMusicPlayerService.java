package com.lava.shopping.androidkuangjia.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.lava.shopping.androidkuangjia.IMyMusicPlayerService;
import com.lava.shopping.androidkuangjia.IMyMusicPlayerServiceCallBack;
import com.lava.shopping.androidkuangjia.items.MediaItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyMusicPlayerService extends Service {
    private boolean isDataComplete = false;
    private List<MediaItem> mediaItems;
    private MediaPlayer myMediaPlayer;
    private RemoteCallbackList<IMyMusicPlayerServiceCallBack> callbackList = new RemoteCallbackList<>();
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
    private static final int MUSIC_RANDOM = 1;

    private int playMode = MUSIC_ORDER;

    @Override
    public void onCreate() {
        super.onCreate();
        getLocalVideoData();
    }

    public MyMusicPlayerService() {
        //
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return iBinder;
    }
    public void getLocalVideoData() {
        isDataComplete = false;
        if(mediaItems == null){
            mediaItems = new ArrayList<>();
        }else{
            mediaItems.clear();
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                ContentResolver resolver = getContentResolver();
                String[] objs = new String[]{
                        MediaStore.Audio.Media.DISPLAY_NAME,//视频的名字
                        MediaStore.Audio.Media.DURATION,//视频的长度
                        MediaStore.Audio.Media.SIZE,//视频的大小
                        MediaStore.Audio.Media.DATA,//在sd卡的绝对地址
                        MediaStore.Audio.Media.ARTIST//艺术家
                };
                Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,objs,null,null,null);
                if(cursor != null){
                    while (cursor.moveToNext()){
                        MediaItem item = new MediaItem();
                        mediaItems.add(item);
                        item.setMediaName(cursor.getString(0));
                        item.setMediaDuration(cursor.getLong(1));
                        item.setMediaSize(cursor.getLong(2));
                        item.setMediaData(cursor.getString(3));
                        item.setMediaArtist(cursor.getString(4));
                        Log.d("chenxiaoping","data:=    "+cursor.getString(3));
                    }
                }
                //handler.sendEmptyMessage(0);
                cursor.close();
            }
        }.start();
    }

    IMyMusicPlayerService.Stub iBinder = new IMyMusicPlayerService.Stub() {
        MyMusicPlayerService service = MyMusicPlayerService.this;
        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void playPreMusic() throws RemoteException {
            service.playPreMusic();
        }

        @Override
        public void playNextMusic() throws RemoteException {
            service.playNextMusic();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            service.setPlayMode(playMode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public void prepare(int position) throws RemoteException {
            service.prepare(position);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void registerCallback(IMyMusicPlayerServiceCallBack callback) throws RemoteException {
            service.registerCallback(callback);
        }

        @Override
        public void unRegisterCallback(IMyMusicPlayerServiceCallBack callback) throws RemoteException {
            service.unRegisterCallback(callback);
        }
    };

    /**
     * 准备播放音乐
     */
    private void prepare(int position){
        Log.d("uuu","prepare  "+position);
        if(mediaItems!=null && mediaItems.size()>0){
            Log.d("uuu","mediaItems  "+mediaItems.size());
            if(myMediaPlayer!=null){
                Log.d("uuu","mediaPlayer != null ");
                //mediaPlayer.release();
                //myMediaPlayer.stop();
                myMediaPlayer.reset();
                myMediaPlayer = null;
            }
            try {
                Log.d("uuu","mediaPlayer != null ");
                if(myMediaPlayer==null){
                    Log.d("uuu","mediaPlayer == null ");
                    myMediaPlayer = new MediaPlayer();
                }
                myMediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                myMediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                myMediaPlayer.setOnErrorListener(new MyOnErrorListener());
                Log.d("uuu","mediaPlayer != null := "+mediaItems.get(position).getMediaData());

                myMediaPlayer.setDataSource(this, Uri.parse(mediaItems.get(position).getMediaData()));
                myMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this,"数据还未准备好，请耐心等待。",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 开始播放音乐
     */
    private void start(){
        if(!myMediaPlayer.isPlaying()){
            myMediaPlayer.start();
        }
    }
    /**
     * 停止播放音乐
     */
    private void stop(){

    }

    /**
     * 暂停播放音乐
     */
    private void pause(){
        if(myMediaPlayer.isPlaying()){
            myMediaPlayer.pause();
        }
    }

    /**
     *播放上一首音乐
     */
    private void playPreMusic(){

    }

    /**
     * 播放下一首音乐
     */
    private void playNextMusic(){

    }

    /**
     *设置音乐播放模式
     */
    private void setPlayMode(int playMode){

    }
    /**
     * 获取音乐播放模式
     */
    private int getPlayMode(){
        return playMode;
    }

    /**
     * 重置音乐列表
     */
    private void resetMusicList(List<MediaItem> mediaItems){

    }

    /**
     * 是否正在播放
     */
    private boolean isPlaying(){
        return myMediaPlayer.isPlaying();
    }

    /**
     * 注册回调
     * @param callback
     * @throws RemoteException
     */
    private void registerCallback(IMyMusicPlayerServiceCallBack callback) throws RemoteException {
        Log.d("xxx","registerCallback");
        if(callback!=null){
            callbackList.register(callback);
        }
    }

    /**
     * 取消回调注册
     * @param callback
     * @throws RemoteException
     */
    private void unRegisterCallback(IMyMusicPlayerServiceCallBack callback) throws RemoteException {
        if(callback!=null){
            callbackList.unregister(callback);
        }
    }

    private synchronized void dataComplete(){
        int count = callbackList.beginBroadcast();//准备通知callbacks
        Log.d("xxx","dataComplete count:= "+count);
        try {
            for(int i= 0 ;i < count ;i++){
                Log.d("xxx","dataComplete callbackList:= "+callbackList.toString());
                callbackList.getBroadcastItem(i).Dataompleted();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        callbackList.finishBroadcast();
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener{
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.d("uuu","onPrepared and started"+mediaPlayer.getDuration());
            myMediaPlayer.start();
            dataComplete();
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.d("uuu","MyOnCompletionListener");
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener{
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            Log.d("uuu","MyOnErrorListener");
            return false;
        }
    }

    @Override
    public void onDestroy() {
        callbackList.kill();
        super.onDestroy();
    }
}

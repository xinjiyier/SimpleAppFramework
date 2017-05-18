// IMyMusicPlayerService.aidl
package com.lava.shopping.androidkuangjia;
import com.lava.shopping.androidkuangjia.IMyMusicPlayerServiceCallBack;
// Declare any non-default types here with import statements

interface IMyMusicPlayerService {
    void start();
    void stop();
    void pause();
    void playPreMusic();
    void playNextMusic();
    void setPlayMode(int playMode);
    int getPlayMode();
    void prepare(int position);
    boolean isPlaying();
    int getMusicCurrentPosition();
    int getMusicDuration();
    void seekToProgress(int pro);

    void registerCallback(IMyMusicPlayerServiceCallBack callback);
    void unRegisterCallback(IMyMusicPlayerServiceCallBack callback);
}

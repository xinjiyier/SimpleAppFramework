// IMyMusicPlayerService.aidl
package com.lava.shopping.androidkuangjia;

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
}

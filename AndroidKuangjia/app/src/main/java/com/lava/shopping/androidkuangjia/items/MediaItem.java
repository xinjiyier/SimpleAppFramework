package com.lava.shopping.androidkuangjia.items;

import java.io.Serializable;

/**
 * Created by Xiaoping.Chen on 2017/5/8.
 */

public class MediaItem implements Serializable{
    private String mediaName;
    private long mediaSize;
    private long mediaDuration;
    private String mediaArtist;
    private String mediaData;//文件的绝对地址
    private String mediaImage;
    private String mediaHighData;

    public String getMediaImage() {
        return mediaImage;
    }

    public void setMediaImage(String mediaImage) {
        this.mediaImage = mediaImage;
    }

    public String getMediaHighData() {
        return mediaHighData;
    }

    public void setMediaHighData(String mediaHighData) {
        this.mediaHighData = mediaHighData;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public long getMediaSize() {
        return mediaSize;
    }

    public void setMediaSize(long mediaSize) {
        this.mediaSize = mediaSize;
    }

    public long getMediaDuration() {
        return mediaDuration;
    }

    public void setMediaDuration(long mediaDuration) {
        this.mediaDuration = mediaDuration;
    }

    public String getMediaArtist() {
        return mediaArtist;
    }

    public void setMediaArtist(String mediaArtist) {
        this.mediaArtist = mediaArtist;
    }

    public String getMediaData() {
        return mediaData;
    }

    public void setMediaData(String mediaData) {
        this.mediaData = mediaData;
    }

    @Override
    public String toString() {
        return mediaName+"  "+
                mediaData+"  "+
                mediaArtist+"  "+
                mediaDuration+"  "+
                mediaSize+"  "+
                mediaImage+"  "+
                mediaHighData+"  ";
    }
}

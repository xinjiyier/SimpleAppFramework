package com.lava.shopping.androidkuangjia.utils;

import android.net.Uri;

/**
 * Created by Administrator on 2017/5/11.
 */

public class MediaUtils {

    public MediaUtils() {

    }

    /**
     * 判断是否是网页中的文件
     * @param uri
     * @return
     */
    public boolean isWebVideo(Uri uri){
        String ur = uri.toString().toLowerCase();
        if(ur.startsWith("http")||ur.startsWith("rtsp")||ur.startsWith("mms")){
            return true;
        }
        return false;
    }


}

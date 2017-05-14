package com.lava.shopping.androidkuangjia.utils;

import android.content.Context;
import android.net.TrafficStats;
import android.net.Uri;

import java.text.DecimalFormat;

/**
 * Created by Administrator on 2017/5/11.
 */

public class MediaUtils {
    private DecimalFormat showFloatFormat = new DecimalFormat("0.00");
    private long rxtxTotal = 0;
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

    public String  getCurrentInternetSpeed(Context context) {
        rxtxTotal = TrafficStats.getTotalRxBytes()
                + TrafficStats.getTotalTxBytes();
        long tempSum = TrafficStats.getTotalRxBytes()
                + TrafficStats.getTotalTxBytes();
        long rxtxLast = tempSum - rxtxTotal;
        double totalSpeed = rxtxLast * 1000 / 2000d;
        rxtxTotal = tempSum;
        return showSpeed(totalSpeed);
    }

    private String showSpeed(double speed) {
        String speedString;
        if (speed >= 1048576d) {
            speedString = showFloatFormat.format(speed / 1048576d) + "MB/s";
        } else {
            speedString = showFloatFormat.format(speed / 1024d) + "KB/s";
        }
        return speedString;
    }
}

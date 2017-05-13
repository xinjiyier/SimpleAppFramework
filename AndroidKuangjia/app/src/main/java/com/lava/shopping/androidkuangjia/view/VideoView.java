package com.lava.shopping.androidkuangjia.view;

import android.app.ActionBar;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/5/11.
 */

public class VideoView extends android.widget.VideoView{
    /**
     * 在代码中创建的时候一般调用这个方法
     * @param context
     */
    public VideoView(Context context) {
        this(context,null);
    }

    /**
     * 在 布局文件 中调用的时候经常使用这个方法
     * @param context
     * @param attrs
     */
    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs ,0);
    }

    /**
     * 当需要设置布局洋时代额时候经常调用这个方法
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    public void setVideoSize(int width,int height){
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.width = width;
        params.height = height;
        setLayoutParams(params);
    }
}

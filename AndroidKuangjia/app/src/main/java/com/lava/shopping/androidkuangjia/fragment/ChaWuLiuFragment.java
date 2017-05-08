package com.lava.shopping.androidkuangjia.fragment;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.lava.shopping.androidkuangjia.R;
import com.lava.shopping.androidkuangjia.base.BaseFragment;

/**
 * Created by Xiaoping.Chen on 2017/4/26.
 */

public class ChaWuLiuFragment extends BaseFragment{
    private TextView textView;
    @Override
    public View initView() {
        textView = new TextView(mContext);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(20);
        textView.setTextColor(getResources().getColor(R.color.framework_text_color,null));
        return textView;
    }

    @Override
    protected void initData() {
        textView.setText("我是查物流");
    }
}

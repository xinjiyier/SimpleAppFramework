package com.lava.shopping.androidkuangjia.fragment;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.lava.shopping.androidkuangjia.R;
import com.lava.shopping.androidkuangjia.base.BaseFragment;

/**
 * Created by Xiaoping.Chen on 2017/4/26.
 */

public class GouWuCheFragment extends BaseFragment{
    private TextView textView;
    @Override
    public View initView() {
        textView = new TextView(mContext);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(20);
        //textView.setTextColor(getResources().getColor(R.color.framework_text_color,null));
        textView.setTextColor(getResources().getColorStateList(R.color.framework_text_color));
        return textView;
    }

    @Override
    protected void initData() {
        textView.setText("我是购物车");
    }
}

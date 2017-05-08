package com.lava.shopping.androidkuangjia.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.lava.shopping.androidkuangjia.R;

/**
 * Created by Xiaoping.Chen on 2017/5/8.
 */

public class AppHeaderView extends LinearLayout implements View.OnClickListener{
    private Context mContext;
    private View tbGame,tbRecode,tbSearch,tbSearchText;
    public AppHeaderView(Context context) {
        this(context,null);
    }

    public AppHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs ,0);
    }

    public AppHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        tbGame = findViewById(R.id.topbanner_game);
        tbRecode = findViewById(R.id.topbanner_record);
        tbSearchText = findViewById(R.id.topbanner_search_text);
        tbGame.setOnClickListener(this);
        tbRecode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.topbanner_game:
                Toast.makeText(mContext,"geme is clicked !!",Toast.LENGTH_SHORT).show();
                break;
            case R.id.topbanner_record:
                Toast.makeText(mContext,"record is clicked !!",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}

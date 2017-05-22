package com.lava.shopping.androidkuangjia;

import android.support.annotation.IdRes;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.lava.shopping.androidkuangjia.base.BaseFragment;
import com.lava.shopping.androidkuangjia.fragment.ChaWuLiuFragment;
import com.lava.shopping.androidkuangjia.fragment.GouWuCheFragment;
import com.lava.shopping.androidkuangjia.fragment.MyTaoBaoFragment;
import com.lava.shopping.androidkuangjia.fragment.WeiTaoFragment;
import com.lava.shopping.androidkuangjia.fragment.ZhuYeFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private RadioButton shouye;
    private List<BaseFragment> mBaseFragment;
    private RadioGroup mRadioGroup;
    private int position;
    private BaseFragment lastFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initFragment();
        initListener();
    }

    private void initListener() {
        mRadioGroup.setOnCheckedChangeListener(new RadioButtonListener());
        shouye.setChecked(true);
    }

    public BaseFragment getBaseFragment() {
        if(position!=-1){
             return mBaseFragment.get(position);
        }else {
             return lastFragment;
        }
    }

    class RadioButtonListener implements RadioGroup.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            switch (i){
                case R.id.shouye:
                    position = 0;
                    break;
                case R.id.weitao:
                    position = 1;
                    break;
                case R.id.wodetaobao:
                    position = 2;
                    break;
                case R.id.gouwuche:
                    position = 3;
                    break;
                case R.id.chawuliu:
                    position = 4;
                    break;
                default:
                    position = -1;
                    break;
            }
            BaseFragment to = getBaseFragment();
            SwitchFragment(lastFragment,to);
        }
    }

    private void SwitchFragment(BaseFragment from,BaseFragment to) {
/*        if(null!=bf){
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.framework_container,bf);
            ft.commit();
        }*/
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if(from != to){
            lastFragment = to;
            if(to.isAdded()){
                //隐藏from
                if(from != null){
                    ft.hide(from);
                }
                //显示to
                if(to !=null){
                    ft.show(to);
                }
            }else{
                //隐藏from
                if(from != null) {
                    ft.hide(from);
                }
                //添加to
                if(to !=null) {
                    ft.add(R.id.framework_container, to);
                }
            }
            ft.commit();
        }
    }

    private void initFragment() {
        mBaseFragment = new ArrayList<>();
        mBaseFragment.add(new ZhuYeFragment(MainActivity.this));
        mBaseFragment.add(new WeiTaoFragment(MainActivity.this));
        mBaseFragment.add(new MyTaoBaoFragment(MainActivity.this));
        mBaseFragment.add(new GouWuCheFragment());
        mBaseFragment.add(new ChaWuLiuFragment());
    }

    private void initView() {
        shouye = (RadioButton) this.findViewById(R.id.shouye);
        mRadioGroup = (RadioGroup) this.findViewById(R.id.bottom_radiogroup);
    }
}

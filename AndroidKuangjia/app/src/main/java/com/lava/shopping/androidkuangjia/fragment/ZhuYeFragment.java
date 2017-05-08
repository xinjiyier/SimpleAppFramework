package com.lava.shopping.androidkuangjia.fragment;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaDataSource;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lava.shopping.androidkuangjia.R;
import com.lava.shopping.androidkuangjia.base.BaseFragment;
import com.lava.shopping.androidkuangjia.items.MediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xiaoping.Chen on 2017/4/26.
 */

@SuppressLint("ValidFragment")
public class ZhuYeFragment extends BaseFragment{
    private ListView videoLv;
    private ProgressBar videoPb;
    private TextView videoTv;
    private Context mContext;
    private List<MediaItem> mediaItems;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mediaItems!=null&&mediaItems.size()>0){
                //有数据
                //progressbar 取消显示
                videoPb.setVisibility(View.GONE);
                //数据为空textView 不显示
                videoTv.setVisibility(View.GONE);
            }else{
                //没有数据
                //progressbar 取消显示
                videoPb.setVisibility(View.GONE);
                //数据为空textView 显示
                videoTv.setVisibility(View.VISIBLE);
            }
        }
    };

    @SuppressLint("ValidFragment")
    public ZhuYeFragment(Context context){
        this.mContext = context;
    }

    @Override
    public View initView() {
        View view = View.inflate(mContext,R.layout.video_list,null);
        videoLv = (ListView) view.findViewById(R.id.video_lv);
        videoPb = (ProgressBar) view.findViewById(R.id.video_pb);
        videoTv = (TextView) view.findViewById(R.id.video_tv);
        return view;
    }

    @Override
    protected void initData() {
        getLocalVideoData();
    }

    public void getLocalVideoData() {
        if(mediaItems != null){
            mediaItems = new ArrayList<>();
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                ContentResolver resolver = mContext.getContentResolver();
                String[] objs = new String[]{
                        MediaStore.Video.Media.DISPLAY_NAME,//视频的名字
                        MediaStore.Video.Media.DURATION,//视频的长度
                        MediaStore.Video.Media.SIZE,//视频的大小
                        MediaStore.Video.Media.DATA,//在sd卡的绝对地址
                        MediaStore.Video.Media.ARTIST//艺术家
                };
                Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,objs,null,null,null);
                if(cursor != null){
                    while (cursor.moveToNext()){
                        MediaItem item = new MediaItem();
                        mediaItems.add(item);
                        item.setMediaName(cursor.getString(0));
                        item.setMediaDuration(cursor.getLong(1));
                        item.setMediaSize(cursor.getLong(2));
                        item.setMediaData(cursor.getString(3));
                        item.setMediaArtist(cursor.getString(4));
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }


    class VideoListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return mediaItems.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view==null){
                view = View.inflate(mContext,R.layout.media_list_item,viewGroup);
            }
            return null;
        }
    }
    
    static class ViewHolder{

    }
}

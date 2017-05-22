package com.lava.shopping.androidkuangjia.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lava.shopping.androidkuangjia.R;
import com.lava.shopping.androidkuangjia.activity.ShoppingVideoPlayer;
import com.lava.shopping.androidkuangjia.base.BaseFragment;
import com.lava.shopping.androidkuangjia.items.MediaItem;
import com.lava.shopping.androidkuangjia.utils.Constants;
import com.lava.shopping.androidkuangjia.utils.TimeUtils;
import com.lava.shopping.androidkuangjia.view.XListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xiaoping.Chen on 2017/4/26.
 */

@SuppressLint("ValidFragment")
public class WeiTaoFragment extends BaseFragment implements XListView.IXListViewListener{
    private Context mContext;
    @ViewInject(R.id.xlv_media_data)
    private XListView mXListView;
    @ViewInject(R.id.rl_buffing)
    private RelativeLayout rlbuffing;
    private List<MediaItem> mediaItems;
    private List<MediaItem> moreMediaItems;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mediaItems!=null&&mediaItems.size()>0){
                //有数据
                //progressbar 取消显示
                rlbuffing.setVisibility(View.GONE);
                //数据为空textView 不显示
                rlbuffing.setVisibility(View.GONE);
            }else{
                //没有数据
                //progressbar 取消显示
                rlbuffing.setVisibility(View.GONE);
                //数据为空textView 显示
                rlbuffing.setVisibility(View.VISIBLE);
            }
            mXListView.deferNotifyDataSetChanged();
            mXListView.setAdapter(new VideoListAdapter());
            onLoad();
        }
    };

    public WeiTaoFragment(){}

    @SuppressLint("ValidFragment")
    public WeiTaoFragment(Context context){
        this.mContext = context;
    }

    @Override
    public View initView() {
        View view = View.inflate(mContext,R.layout.layout_media_list,null);
        x.view().inject(this,view);
        mXListView.setPullLoadEnable(true);
        mXListView.setXListViewListener(this);
        mXListView.setOnItemClickListener(new MyOnItemClickListener());
        return view;
    }

    @Override
    protected void initData() {
        getNetVideoData();
    }

    public void getNetVideoData() {
/*        if(mediaItems == null){
            mediaItems = new ArrayList<>();
        }else{
            mediaItems.clear();
        }*/
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new MyRequestNetCallbacks());
    }

    /**
     * 当下拉刷新的时候进行回调
     */
    @Override
    public void onRefresh() {
        handler.removeCallbacksAndMessages(null);
        //mediaItems.removeAll(null);
        mediaItems.clear();
        getNetVideoData();
        mediaItems.addAll(moreMediaItems);
        handler.sendEmptyMessageDelayed(0,2000);
    }

    /**
     * 当上拉加载更多的额时候回调
     */
    @Override
    public void onLoadMore() {
        handler.removeCallbacksAndMessages(null);
        getNetVideoData();
        mediaItems.addAll(moreMediaItems);
        handler.sendEmptyMessageDelayed(0,4000);
    }

    private void onLoad() {
        mXListView.stopRefresh();
        mXListView.stopLoadMore();
        mXListView.setRefreshTime(new TimeUtils().getCurrentTime());
    }

    class VideoListAdapter extends BaseAdapter {
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
            ViewHolder viewHolder;
            if(view==null){
                viewHolder = new ViewHolder();
                view = View.inflate(mContext,R.layout.media_list_item,null);
                viewHolder.mediaImageView = (ImageView) view.findViewById(R.id.video_im);
                viewHolder.mediaDuration = (TextView) view.findViewById(R.id.media_duration_tv);
                viewHolder.mediaName = (TextView) view.findViewById(R.id.media_name_tv);
                viewHolder.mediaSize = (TextView) view.findViewById(R.id.media_size_tv);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.mediaName.setText(mediaItems.get(i).getMediaName());
            viewHolder.mediaSize.setText(android.text.format.Formatter.formatFileSize(mContext,mediaItems.get(i).getMediaSize()));
            viewHolder.mediaDuration.setText(new TimeUtils().stringForTime((int) mediaItems.get(i).getMediaSize()));
            x.image().bind(viewHolder.mediaImageView,mediaItems.get(i).getMediaImage());
            return view;
        }
    }

    static class ViewHolder{
        private ImageView mediaImageView;
        private  TextView mediaName;
        private  TextView mediaDuration;
        private  TextView mediaSize;
    }

    class MyRequestNetCallbacks implements Callback.CommonCallback<String> {
        @Override
        public void onSuccess(String result) {
            if(mediaItems != null){
                processJsonData(result);
            }else{
                mediaItems = new ArrayList<>();
                mediaItems.addAll(processJsonData(result));
                handler.sendEmptyMessageDelayed(0,2000);
            }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {

        }

        @Override
        public void onCancelled(CancelledException cex) {

        }

        @Override
        public void onFinished() {

        }
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(mContext,ShoppingVideoPlayer.class);
            intent.putExtra("mediaName",mediaItems.get(position).getMediaName());
            //intent.setDataAndType(Uri.parse(mediaItems.get(position).getMediaData()),"video/*");
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", (Serializable) mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position-1);
            startActivity(intent);
        }
    }

    private List<MediaItem> processJsonData(String result) {
        moreMediaItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if(jsonArray!=null && jsonArray.length()>0){
                for(int i=0;i<jsonArray.length();i++){
                    MediaItem m = new MediaItem();
                    JSONObject item = jsonArray.optJSONObject(i);
                    m.setMediaName(item.optString("movieName"));
                    m.setMediaImage(item.optString("coverImg"));
                    m.setMediaData(item.optString("url"));
                    m.setMediaHighData(item.optString("hightUrl"));
                    m.setMediaDuration(item.optLong("videoLength"));
                    moreMediaItems.add(m);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return moreMediaItems;
    }
}

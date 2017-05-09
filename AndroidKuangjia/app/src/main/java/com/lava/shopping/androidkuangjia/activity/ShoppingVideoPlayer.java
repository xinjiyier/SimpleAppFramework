package com.lava.shopping.androidkuangjia.activity;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.lava.shopping.androidkuangjia.R;

public class ShoppingVideoPlayer extends AppCompatActivity {
private VideoView mVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_video_player);
        init();
    }

    private void init() {
        mVideoView = (VideoView) this.findViewById(R.id.media_vv);
        mVideoView.setOnCompletionListener(new OnMediaCompleted());
        mVideoView.setOnErrorListener(new OnMediaErrored());
        mVideoView.setOnPreparedListener(new OnMediaPrepared());
        if(mVideoView!=null){
            mVideoView.setVideoURI(getIntent().getData());
        }
        mVideoView.setMediaController(new MediaController(ShoppingVideoPlayer.this));
    }

    class OnMediaCompleted implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Toast.makeText(ShoppingVideoPlayer.this,"",Toast.LENGTH_SHORT).show();
        }
    }

    class OnMediaErrored implements MediaPlayer.OnErrorListener{
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            return false;
        }
    }

    class OnMediaPrepared implements MediaPlayer.OnPreparedListener{

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
        }
    }
}

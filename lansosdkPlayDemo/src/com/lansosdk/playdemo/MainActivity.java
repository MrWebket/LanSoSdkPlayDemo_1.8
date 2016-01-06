/*****************************************************************************
 * MainActivity.java
 * 
 *****************************************************************************/
/**
 * 
 * 杭州蓝松科技有限公司.LanSoSdk团队, 专业做多媒体音视频的方案公司.包括视频采集,编辑,编码, 传输,解码,处理,播放等.
 * 
 *  
 * 我们的部分视频高级播放功能如下: 欢迎商务合作
 * 1,设置视频下载缓冲器大小,设置视频缓冲时长.
 * 2,视频截屏,单帧播放.
 * 3,视频播放速度可调,任意速度可调.
 * 4,软硬解自动切换.完全支持软硬解.并软解功能支持NEON指令,多线程解码.
 * 5,视频录制.
 * 6,网络视频支持边播放、边下载功能. 支持快速全速下载.----网络不太好,或使用3G/4G情况下也可以流畅播放.
 * 7,网络视频,查看当前缓冲百分比, 查看当前网速.----
 * 8,支持对比度, 饱和度,色调,颜色提取,镜像,动态监测,分屏等12种功能,并可定制滤镜效果.  ----类似秒拍,美拍,快手的功能.
 * 9,支持左右3D, 红蓝3D播放.
 * 10,RTSP做视频直播时的延迟问题(定制).
 * 11,RTSP播放时马赛克严重的问题(定制).
 * 12,硬件在部分手机上不支持的问题(定制).
 * 13,M3U8网络播放时crash的问题(定制).
 * 14,playlist时自由拖动的问题(定制).
 * 15,解决您项目中遇到的各种视频网络等问题(定制).
 * 
 * Email:support@lansongtech.com.
 * @link https://github.com/LanSoSdk
 */

package com.lansosdk.playdemo;



import java.io.File;

import com.LanSoSdk.SdkInit.LanSoSdkInit;
import com.lansosdk.playdemo.R;
import com.lansosdk.playdemo.util.snoCrashHandler;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {

	private String videoPath="";
	EditText  etVideoPath;
	 
	//such as:		path = "/storage/sdcard1/chongchukabuer.mp4";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
        setContentView(R.layout.activity_main);
        
      
        etVideoPath=(EditText)findViewById(R.id.id_video_path_et);
        etVideoPath.setText("/sdcard/test.mp4");  //<----only example.
        
        findViewById(R.id.id_main_allcodec_btn).setOnClickListener(this);
    	findViewById(R.id.id_main_software_btn).setOnClickListener(this);
    	findViewById(R.id.id_main_3ddemo_btn).setOnClickListener(this);
    	findViewById(R.id.id_main_effectdemo_btn).setOnClickListener(this);
        
    	
    }
    @Override
    public void onClick(View v) {
    	// TODO Auto-generated method stub
    	String vpath=etVideoPath.getText().toString();
    	File file=new File(vpath);
    	if(file.exists()==false)
    	{
    		Toast.makeText(getApplicationContext(), R.string.video_path_check_error, Toast.LENGTH_LONG).show();
    		return ;
    	}
    	videoPath=vpath;
    	switch (v.getId()) {
			case R.id.id_main_allcodec_btn:
					playFullCodecDemo();
					break;
			case R.id.id_main_software_btn:
					playSoftWareDemo();
					break;
			case R.id.id_main_3ddemo_btn:
					play3DDemo();
					break;
			case R.id.id_main_effectdemo_btn:
				playVideoEffect();
				break;
		default:
			break;
		}
    }
    
    private void playFullCodecDemo()
    {
		Uri uri1=Uri.fromFile(new File(videoPath));
//		Uri uri1=Uri.parse("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8");//<-----------or http/rtsp/rtmp  URL
		
        Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.PLAY_LOCATION, uri1);
        intent.putExtra(VideoPlayerActivity.PLAY_IS_SOFTWARE_CODEC, false);
        startActivity(intent);
    }
    private void playSoftWareDemo()
    {
		Uri uri1=Uri.fromFile(new File(videoPath));
		
        Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.PLAY_LOCATION, uri1);
        intent.putExtra(VideoPlayerActivity.PLAY_IS_SOFTWARE_CODEC, true);  //<------------only here is different
        startActivity(intent);
    }
    private void play3DDemo()
    {
			Uri uri1=Uri.fromFile(new File(videoPath));
			Intent intent = new Intent(MainActivity.this, VideoPlay3DActivity.class);
		     intent.putExtra(VideoPlayerActivity.PLAY_LOCATION, uri1);
		     intent.putExtra(VideoPlayerActivity.PLAY_IS_SOFTWARE_CODEC, true);
		     startActivity(intent);
    }
    private void playVideoEffect()
    {
			Uri uri1=Uri.fromFile(new File(videoPath));
			Intent intent = new Intent(MainActivity.this, VideoEffectActivity.class);
		     intent.putExtra(VideoPlayerActivity.PLAY_LOCATION, uri1);
		     intent.putExtra(VideoPlayerActivity.PLAY_IS_SOFTWARE_CODEC, true);
		     startActivity(intent);
    }
    
}

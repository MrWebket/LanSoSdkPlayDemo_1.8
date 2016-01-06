/*****************************************************************************
 * LibPlay.java
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
package com.LanSoSdk.Play;

import java.io.File;
import java.util.ArrayList;

import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.LanSoSdk.Play.Util.HWDecoderUtil;
import com.LanSoSdk.SdkInit.LanSoSdkInit;

public class LibPlay extends PlayObject<LibPlay.Event> {
    private static final String TAG = "LanSoSdk";

    public static class Event extends PlayEvent {
        protected Event(int type) {
            super(type);
        }
    }

    /** Native crash handler */
    private static OnNativeCrashListener sOnNativeCrashListener;

    public interface HardwareAccelerationError {
        void eventHardwareAccelerationError(); // TODO REMOVE
    }

    /**
     * Create a libPlay withs options
     *
     * @param options
     */
    public LibPlay(ArrayList<String> options) {
        boolean setAout = true, setChroma = true;
        // check if aout/vout options are already set
        if (options != null) {
            for (String option : options) {
                if (option.startsWith("--aout="))
                    setAout = false;
                if (option.startsWith("--androidwindow-chroma"))
                    setChroma = false;
                if (!setAout && !setChroma)
                    break;
            }
        }

        // set aout/vout options if they are not set
        if (setAout || setChroma) {
            if (options == null)
                options = new ArrayList<String>();
            if (setAout) {
                final HWDecoderUtil.AudioOutput hwAout = HWDecoderUtil.getAudioOutputFromDevice();
                if (hwAout == HWDecoderUtil.AudioOutput.OPENSLES)
                    options.add("--aout=opensles");
                else
                    options.add("--aout=android_audiotrack");
            }
            if (setChroma) {
                options.add("--androidwindow-chroma");
                options.add("RV32");
            }
        }

        nativeNew(options.toArray(new String[options.size()]));
    }

 
    /**
     * *******************************************************************************************************************
     * @return
     */
    private  ArrayList<String> getLibOptions() 
    {
        ArrayList<String> options = new ArrayList<String>(50);
        
        final String subtitlesEncoding ="";
        final boolean frameSkip =false; 
        final boolean verboseMode = true; 
        int deblocking = -1;  //auto delete block

        int networkCaching = 60000;  ///software codec used; 
        
        options.add("--no-audio-time-stretch");  
        options.add("--avcodec-skiploopfilter");
        options.add("" + deblocking);
        options.add("--avcodec-skip-frame");
        options.add(frameSkip ? "2" : "0");
        options.add("--avcodec-skip-idct");
        options.add(frameSkip ? "2" : "0");
        options.add("--subsdec-encoding");
        options.add(subtitlesEncoding);
        options.add("--stats");
        options.add("--network-caching=" + networkCaching);
        
        options.add("--androidwindow-chroma");
        options.add("RV32"); 
        options.add(verboseMode ? "-vvv" : "-vv");
        return options;
    }
    public LibPlay()
    {
    	ArrayList<String> options=getLibOptions();
        boolean setAout = true, setChroma = true;
        if (options != null) 
        {
            for (String option : options) 
            {
                if (option.startsWith("--aout="))
                    setAout = false;
                if (option.startsWith("--androidwindow-chroma"))
                    setChroma = false;
                
                if (!setAout && !setChroma)
                    break;
            }
        }
        if (setAout || setChroma) 
        {
            if (options == null)
                options = new ArrayList<String>();
            
            if (setAout) 
            {
                final HWDecoderUtil.AudioOutput hwAout = HWDecoderUtil.getAudioOutputFromDevice();
                if (hwAout == HWDecoderUtil.AudioOutput.OPENSLES)
                    options.add("--aout=opensles");
                else
                    options.add("--aout=android_audiotrack");
            }
            if (setChroma) 
            {
                options.add("--androidwindow-chroma");
                options.add("RV32");
            }
        }
        nativeNew(options.toArray(new String[options.size()]));
    }
    
    public void setOnHardwareAccelerationError(HardwareAccelerationError error) {
        nativeSetOnHardwareAccelerationError(error);
    }
    private native void nativeSetOnHardwareAccelerationError(HardwareAccelerationError error);

    /**
     * Get the libPlay version
     * @return the libPlay version string
     */
    public native String version();

    @Override
    protected Event onEventNative(int eventType, long arg1, float arg2) {
        return null;
    }

    @Override
    protected void onReleaseNative() {
        nativeRelease();
    }

    public static interface OnNativeCrashListener {
        public void onNativeCrash();
    }

    public static void setOnNativeCrashListener(OnNativeCrashListener l) {
        sOnNativeCrashListener = l;
    }

    private static void onNativeCrash() {
        if (sOnNativeCrashListener != null)
            sOnNativeCrashListener.onNativeCrash();
    }

    /**
     * Sets the application name. LibPlay passes this as the user agent string
     * when a protocol requires it.
     *
     * @param name human-readable application name, e.g. "FooBar player 1.2.3"
     * @param http HTTP User Agent, e.g. "FooBar/1.2.3 Python/2.6.0"
     */
    public void setUserAgent(String name, String http){
        nativeSetUserAgent(name, http);
    }

    /* JNI */
    private native void nativeNew(String[] options);
    private native void nativeRelease();
    private native void nativeSetUserAgent(String name, String http);

    /* Load library before object instantiation */
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            try {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
                    System.loadLibrary("anw.10");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2)
                    System.loadLibrary("anw.13");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    System.loadLibrary("anw.14");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
                    System.loadLibrary("anw.18");
                else
                    System.loadLibrary("anw.21");
            } catch (Throwable t) {
                Log.w(TAG, "Unable to load the anw library: " + t);
            }

            try {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
                    System.loadLibrary("iomx.10");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2)
                    System.loadLibrary("iomx.13");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    System.loadLibrary("iomx.14");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
                    System.loadLibrary("iomx.18");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
                    System.loadLibrary("iomx.19");
            } catch (Throwable t) {
                // No need to warn if it isn't found, when we intentionally don't build these except for debug
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    Log.w(TAG, "Unable to load the iomx library: " + t);
            }
        }

        try {
            System.loadLibrary("lansosdkplay");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "Can't load lansosdkplay library: " + ule);
            /// FIXME Alert user
            System.exit(1);
        } catch (SecurityException se) {
            Log.e(TAG, "Encountered a security issue when loading lansosdkplay library: " + se);
            /// FIXME Alert user
            System.exit(1);
        }
    }
}

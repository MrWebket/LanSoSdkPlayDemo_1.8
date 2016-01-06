/*****************************************************************************
 * AndroidDevices.java
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

package com.lansosdk.playdemo.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.InputDevice;
import android.view.MotionEvent;



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import com.LanSoSdk.Play.Util.AndroidVersion;

public class AndroidDevices {
    public final static String TAG = "AndroidDevices";
    public final static String EXTERNAL_PUBLIC_DIRECTORY = Environment.getExternalStorageDirectory().getPath();

    final static boolean hasNavBar;
    final static boolean hasTsp;

    static {
        HashSet<String> devicesWithoutNavBar = new HashSet<String>();
        devicesWithoutNavBar.add("HTC One V");
        devicesWithoutNavBar.add("HTC One S");
        devicesWithoutNavBar.add("HTC One X");
        devicesWithoutNavBar.add("HTC One XL");
        hasNavBar = AndroidVersion.isICSOrLater()
                && !devicesWithoutNavBar.contains(android.os.Build.MODEL);
        hasTsp = LanSoDemoApplication.getAppContext().getPackageManager().hasSystemFeature("android.hardware.touchscreen");
    }

    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean hasNavBar()
    {
        return hasNavBar;
    }

    /** hasCombBar test if device has Combined Bar : only for tablet with Honeycomb or ICS */
    public static boolean hasCombBar() {
        return (!AndroidDevices.isPhone()
                && ((VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) &&
                    (VERSION.SDK_INT <= VERSION_CODES.JELLY_BEAN)));
    }

    public static boolean isPhone(){
        TelephonyManager manager = (TelephonyManager)LanSoDemoApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    public static boolean hasTsp(){
        return hasTsp;
    }
    @TargetApi(VERSION_CODES.HONEYCOMB_MR1)
    public static float getCenteredAxis(MotionEvent event,
            InputDevice device, int axis) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value = event.getAxisValue(axis);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }
}

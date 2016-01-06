/*****************************************************************************
 * snoCrashHandler.java
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

package com.lansosdk.playdemo.util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;


import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class snoCrashHandler implements UncaughtExceptionHandler {

    private static final String TAG = "LanSoSdkPlayHandler";

    private UncaughtExceptionHandler defaultUEH;

    public snoCrashHandler() {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        // Inject some info about android version and the device, since google can't provide them in the developer console
        StackTraceElement[] trace = ex.getStackTrace();
        StackTraceElement[] trace2 = new StackTraceElement[trace.length+3];
        System.arraycopy(trace, 0, trace2, 0, trace.length);
        trace2[trace.length+0] = new StackTraceElement("Android", "MODEL", android.os.Build.MODEL, -1);
        trace2[trace.length+1] = new StackTraceElement("Android", "VERSION", android.os.Build.VERSION.RELEASE, -1);
        trace2[trace.length+2] = new StackTraceElement("Android", "FINGERPRINT", android.os.Build.FINGERPRINT, -1);
        ex.setStackTrace(trace2);

        ex.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        Log.e(TAG, stacktrace);

        defaultUEH.uncaughtException(thread, ex);
    }

    private void writeLog(String log, String name) {
        CharSequence timestamp = DateFormat.format("yyyyMMdd_kkmmss", System.currentTimeMillis());
        String filename = name + "_" + timestamp + ".log";

        FileOutputStream stream;
        try {
            stream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        OutputStreamWriter output = new OutputStreamWriter(stream);
        BufferedWriter bw = new BufferedWriter(output);

        try {
            bw.write(log);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close(bw);
            close(output);
        }
    }

    private void writeLogcat(String name) {
        CharSequence timestamp = DateFormat.format("yyyyMMdd_kkmmss", System.currentTimeMillis());
        String filename = name + "_" + timestamp + ".log";
        try {
            writeLogcat2(filename);
        } catch (IOException e) {
            Log.e(TAG, "Cannot write logcat to disk");
        }
    }
    private  boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
                return false;
        }
    }
    public void writeLogcat2(String filename) throws IOException {
        String[] args = { "logcat", "-v", "time", "-d" };

        Process process = Runtime.getRuntime().exec(args);

        InputStreamReader input = new InputStreamReader(process.getInputStream());

        FileOutputStream fileStream;
        try {
            fileStream = new FileOutputStream(filename);
        } catch( FileNotFoundException e) {
            return;
        }

        OutputStreamWriter output = new OutputStreamWriter(fileStream);
        BufferedReader br = new BufferedReader(input);
        BufferedWriter bw = new BufferedWriter(output);

        try {
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
        }catch(Exception e) {}
        finally {
            close(bw);
            close(output);
            close(br);
            close(input);
        }
    }
}

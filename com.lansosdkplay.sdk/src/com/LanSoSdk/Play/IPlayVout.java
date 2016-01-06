/*****************************************************************************
 * public class IPlayVout.java
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

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.MainThread;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

@SuppressWarnings("unused")
public interface IPlayVout {
    interface Callback {
        /**
         * This callback is called when the native vout call request a new Layout.
         *
         * @param playVout playVout
         * @param width Frame width
         * @param height Frame height
         * @param visibleWidth Visible frame width
         * @param visibleHeight Visible frame height
         * @param sarNum Surface aspect ratio numerator
         * @param sarDen Surface aspect ratio denominator
         */
        @MainThread
        void onNewLayout(IPlayVout playVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen);

        /**
         * This callback is called when surfaces are created.
         */
        @MainThread
        void onSurfacesCreated(IPlayVout playVout);

        /**
         * This callback is called when surfaces are destroyed.
         */
        @MainThread
        void onSurfacesDestroyed(IPlayVout playVout);
    }

    /**
     * Set a surfaceView used for video out.
     * @see #attachViews()
     */
    @MainThread
    void setVideoView(SurfaceView videoSurfaceView);

    /**
     * Set a TextureView used for video out.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setVideoView(TextureView videoTextureView);

    /**
     * Set a surface used for video out.
     * @param videoSurface if surfaceHolder is null, this surface must be valid and attached.
     * @param surfaceHolder optional, used to configure buffers geometry before Android ICS
     * and to get notified when surface is destroyed.
     * @see #attachViews()
     */
    @MainThread
    void setVideoSurface(Surface videoSurface, SurfaceHolder surfaceHolder);

    /**
     * Set a SurfaceTexture used for video out.
     * @param videoSurfaceTexture this surface must be valid and attached.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setVideoSurface(SurfaceTexture videoSurfaceTexture);

    
    
    
    /**
     * Set a surfaceView used for video out.
     * @see #attachViews()
     */
    @MainThread
    void setVideoView2(SurfaceView videoSurfaceView);
    
    /**
     * Set a TextureView used for video out.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setVideoView2(TextureView videoTextureView);

    /**
     * Set a surface used for video out.
     * @param videoSurface if surfaceHolder is null, this surface must be valid and attached.
     * @param surfaceHolder optional, used to configure buffers geometry before Android ICS
     * and to get notified when surface is destroyed.
     * @see #attachViews()
     */
    @MainThread
    void setVideoSurface2(Surface videoSurface, SurfaceHolder surfaceHolder);

    /**
     * Set a SurfaceTexture used for video out.
     * @param videoSurfaceTexture this surface must be valid and attached.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setVideoSurface2(SurfaceTexture videoSurfaceTexture);


    void setVideoSurface2Showing(boolean isShow);
    /**
     * =====================================复制4个方法结束.
     */
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Set a surfaceView used for subtitles out.
     * @see #attachViews()
     */
    @MainThread
    void setSubtitlesView(SurfaceView subtitlesSurfaceView);

    /**
     * Set a TextureView used for subtitles out.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setSubtitlesView(TextureView subtitlesTextureView);

    /**
     * Set a surface used for subtitles out.
     * @param subtitlesSurface if surfaceHolder is null, this surface must be valid and attached.
     * @param surfaceHolder optional, used to configure buffers geometry before Android ICS
     * and to get notified when surface is destroyed.
     * @see #attachViews()
     */
    @MainThread
    void setSubtitlesSurface(Surface subtitlesSurface, SurfaceHolder surfaceHolder);

    /**
     * Set a SurfaceTexture used for subtitles out.
     * @param subtitlesSurfaceTexture this surface must be valid and attached.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setSubtitlesSurface(SurfaceTexture subtitlesSurfaceTexture);

    /**
     * Attach views previously set by setVideoView, setSubtitlesView, setVideoSurface, setSubtitleSurface
     * @see #setVideoView(SurfaceView)
     * @see #setVideoView(TextureView)
     * @see #setVideoSurface(Surface, SurfaceHolder)
     * @see #setSubtitlesView(SurfaceView)
     * @see #setSubtitlesView(TextureView)
     * @see #setSubtitlesSurface(Surface, SurfaceHolder)
     */
    @MainThread
    void attachViews();

    /**
     * Detach views previously attached.
     * This will be called automatically when surfaces are destroyed.
     */
    @MainThread
    void detachViews();

    /**
     * Return true if views are attached. If surfaces were destroyed, this will return false.
     */
    @MainThread
    boolean areViewsAttached();

    /**
     * Add a callback to receive {@link Callback#onNewLayout} events.
     */
    @MainThread
    void addCallback(Callback callback);

    /**
     * Remove a callback.
     */
    @MainThread
    void removeCallback(Callback callback);

    /**
     * Send a mouse event to the native vout.
     * @param action see ACTION_* in {@link android.view.MotionEvent}.
     * @param button see BUTTON_* in {@link android.view.MotionEvent}.
     * @param x x coordinate.
     * @param y y coordinate.
     */
    @MainThread
    void sendMouseEvent(int action, int button, int x, int y);

    /**
     * Send the the window size to the native vout.
     * @param width width of the window.
     * @param height height of the window.
     */
    @MainThread
    void setWindowSize(int width, int height);
}

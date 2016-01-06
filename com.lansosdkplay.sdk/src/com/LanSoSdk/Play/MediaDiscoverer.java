/*****************************************************************************
 * MediaDiscoverer.java
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

@SuppressWarnings("unused")
public class MediaDiscoverer extends PlayObject<MediaDiscoverer.Event> {
    private final static String TAG = "MediaDiscoverer";

    public static class Event extends PlayEvent {

        public static final int Started = 0x500;
        public static final int Ended   = 0x501;

        protected Event(int type) {
            super(type);
        }
    }

    public interface EventListener extends PlayEvent.Listener<MediaDiscoverer.Event> {}

    private MediaList mMediaList = null;

    /**
     * Create a MediaDiscover.
     *
     * @param LibPlay a valid LibPlay
     * @param name Name of the service discovery ("dsm", "upnp", "bonjour"...).
     */
    public MediaDiscoverer(LibPlay libplay, String name) {
        nativeNew(libplay, name);
    }

    /**
     * Starts the discovery. This MediaDiscoverer should be alive (not released).
     *
     * @return true the service is started
     */
    public boolean start() {
        if (isReleased())
            throw new IllegalStateException("MediaDiscoverer is released");
        return nativeStart();
    }

    /**
     * Stops the discovery. This MediaDiscoverer should be alive (not released).
     * (You can also call {@link #release() to stop the discovery directly}.
     */
    public void stop() {
        if (isReleased())
            throw new IllegalStateException("MediaDiscoverer is released");
        nativeStop();
    }

    public void setEventListener(EventListener listener) {
        super.setEventListener(listener);
    }

    @Override
    protected Event onEventNative(int eventType, long arg1, float arg2) {
        switch (eventType) {
            case Event.Started:
            case Event.Ended:
                return new Event(eventType);
        }
        return null;
    }

    /**
     * Get the MediaList associated with the MediaDiscoverer.
     * This MediaDiscoverer should be alive (not released).
     *
     * @return MediaList. This MediaList should be released with {@link #release()}.
     */
    public MediaList getMediaList() {
        synchronized (this) {
            if (mMediaList != null) {
                mMediaList.retain();
                return mMediaList;
            }
        }
        final MediaList mediaList = new MediaList(this);
        synchronized (this) {
            mMediaList = mediaList;
            mMediaList.retain();
            return mMediaList;
        }
    }

    @Override
    protected void onReleaseNative() {
        if (mMediaList != null)
            mMediaList.release();
        nativeRelease();
    }

    /* JNI */
    private native void nativeNew(LibPlay libplay, String name);
    private native void nativeRelease();
    private native boolean nativeStart();
    private native void nativeStop();
}

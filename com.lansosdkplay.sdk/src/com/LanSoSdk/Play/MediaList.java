/*****************************************************************************
 * MediaList.java
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

import android.util.SparseArray;

@SuppressWarnings("unused")
public class MediaList extends PlayObject<MediaList.Event> {
    private final static String TAG = "MediaList";

    public static class Event extends PlayEvent {

        public static final int ItemAdded              = 0x200;
        //public static final int WillAddItem            = 0x201;
        public static final int ItemDeleted            = 0x202;
        //public static final int WillDeleteItem         = 0x203;
        public static final int EndReached             = 0x204;

        /**
         * The media can be already released. If it's released, cached attributes are still
         * available (like media.getMrl()).
         * You should call {@link Media#retain()} and check the return value
         * before calling media native methods.
         */
        public final Media media;
        public final int index;

        protected Event(int type, Media media, int index) {
            super(type);
            this.media = media;
            this.index = index;
        }
    }

    public interface EventListener extends PlayEvent.Listener<MediaList.Event> {}

    private int mCount = 0;
    private final SparseArray<Media> mMediaArray = new SparseArray<Media>();
    private boolean mLocked = false;

    private void init() {
        lock();
        mCount = nativeGetCount();
        for (int i = 0; i < mCount; ++i)
            mMediaArray.put(i, new Media(this, i));
        unlock();
    }

    /**
     * Create a MediaList from LibPlay
     * @param LibPlay a valid LibPlay
     */
    public MediaList(LibPlay libplay) {
        nativeNewFromLibPlay(libplay);
        init();
    }

    /**
     *
     * @param md Should not be released
     */
    protected MediaList(MediaDiscoverer md) {
        nativeNewFromMediaDiscoverer(md);
        init();
    }

    /**
     *
     * @param m Should not be released
     */
    protected MediaList(Media m) {
        nativeNewFromMedia(m);
        init();
    }

    private synchronized Media insertMediaFromEvent(int index) {
        mCount++;

        for (int i = mCount - 1; i >= index; --i)
            mMediaArray.put(i + 1, mMediaArray.valueAt(i));
        final Media media = new Media(this, index);
        mMediaArray.put(index, media);
        return media;
    }

    private synchronized Media removeMediaFromEvent(int index) {
        mCount--;
        final Media media = mMediaArray.get(index);
        if (media != null)
            media.release();
        for (int i = index; i < mCount; ++i) {
            mMediaArray.put(i, mMediaArray.valueAt(i + 1));
        }
        return media;
    }

    public void setEventListener(EventListener listener) {
        super.setEventListener(listener);
    }

    @Override
    protected synchronized Event onEventNative(int eventType, long arg1, float arg2) {
        if (mLocked)
            throw new IllegalStateException("already locked from event callback");
        mLocked = true;
        Event event = null;
        int index;

        switch (eventType) {
        case Event.ItemAdded:
            index = (int) arg1;
            if (index != -1) {
                final Media media = insertMediaFromEvent(index);
                event = new Event(eventType, media, index);
            }
            break;
        case Event.ItemDeleted:
            index = (int) arg1;
            if (index != -1) {
                final Media media = removeMediaFromEvent(index);
                event = new Event(eventType, media, index);
            }
            break;
        case Event.EndReached:
            event = new Event(eventType, null, -1);
            break;
        }
        mLocked = false;
        return event;
    }

    /**
     * Get the number of Media.
     */
    public synchronized int getCount() {
        return mCount;
    }

    /**
     * Get a Media at specified index.
     *
     * @param index index of the media
     * @return Media hold by MediaList. This Media should be released with {@link #release()}.
     */
    public synchronized Media getMediaAt(int index) {
        if (index < 0 || index >= getCount())
            throw new IndexOutOfBoundsException();
        final Media media = mMediaArray.get(index);
        media.retain();
        return media;
    }

    @Override
    public void onReleaseNative() {
        for (int i = 0; i < mMediaArray.size(); ++i) {
            final Media media = mMediaArray.get(i);
            if (media != null)
                media.release();
        }

        nativeRelease();
    }

    private synchronized void lock() {
        if (mLocked)
            throw new IllegalStateException("already locked");
        mLocked = true;
        nativeLock();
    }

    private synchronized void unlock() {
        if (!mLocked)
            throw new IllegalStateException("not locked");
        mLocked = false;
        nativeUnlock();
    }

    protected synchronized boolean isLocked() {
        return mLocked;
    }

    /* JNI */
    private native void nativeNewFromLibPlay(LibPlay libplay);
    private native void nativeNewFromMediaDiscoverer(MediaDiscoverer md);
    private native void nativeNewFromMedia(Media m);
    private native void nativeRelease();
    private native int nativeGetCount();
    private native void nativeLock();
    private native void nativeUnlock();
}

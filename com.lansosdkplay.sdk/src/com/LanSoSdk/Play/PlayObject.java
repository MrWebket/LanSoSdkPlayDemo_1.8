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

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

abstract class PlayObject<T extends PlayEvent> {
    private PlayEvent.Listener<T> mEventListener = null;
    private Handler mHandler = null;
    private int mNativeRefCount = 1;

    /**
     * Returns true if native object is released
     */
    public synchronized boolean isReleased() {
        return mNativeRefCount == 0;
    }

    /**
     * Increment internal ref count of the native object.
     * @return true if media is retained
     */
    public synchronized final boolean retain() {
        if (mNativeRefCount > 0) {
            mNativeRefCount++;
            return true;
        } else
            return false;
    }

    /**
     * Release the native object if ref count is 1.
     *
     * After this call, native calls are not possible anymore.
     * You can still call others methods to retrieve cached values.
     * For example: if you parse, then release a media, you'll still be able to retrieve all Metas or Tracks infos.
     */
    public final void release() {
        int refCount = -1;
        synchronized (this) {
            if (mNativeRefCount == 0)
                return;
            if (mNativeRefCount > 0) {
                refCount = --mNativeRefCount;
            }
            // clear event list
            if (refCount == 0)
                setEventListener(null);
        }
        if (refCount == 0) {
            // detach events when not synchronized since onEvent is executed synchronized
            nativeDetachEvents();
            synchronized (this) {
                onReleaseNative();
            }
        }
    }

    /**
     * Set an event listener.
     * Events are sent via the android main thread.
     *
     * @param listener see {@link PlayEvent.Listener}
     */
    protected synchronized void setEventListener(PlayEvent.Listener<T> listener) {
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        mEventListener = listener;
        if (mEventListener != null && mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Called when libplay send events.
     *
     * @param eventType event type
     * @param arg1 first argument
     * @param arg2 second argument
     * @return Event that will be dispatched to listeners
     */
    protected abstract T onEventNative(int eventType, long arg1, float arg2);

    /**
     * Called when native object is released (refcount is 0).
     *
     * This is where you must release native resources.
     */
    protected abstract void onReleaseNative();

    /* JNI */
    @SuppressWarnings("unused") /* Used from JNI */
    private long mInstance = 0;
    private synchronized void dispatchEventFromNative(int eventType, long arg1, float arg2) {
        if (isReleased())
            return;
        final T event = onEventNative(eventType, arg1, arg2);

        class EventRunnable implements Runnable {
            private final PlayEvent.Listener<T> listener;
            private final T event;

            private EventRunnable(PlayEvent.Listener<T> listener, T event) {
                this.listener = listener;
                this.event = event;
            }
            @Override
            public void run() {
                listener.onEvent(event);
            }
        }

        if (event != null && mEventListener != null && mHandler != null)
            mHandler.post(new EventRunnable(mEventListener, event));
    }
    private native void nativeDetachEvents();

    /* used only before API 7: substitute for NewWeakGlobalRef */
    @SuppressWarnings("unused") /* Used from JNI */
    private Object getWeakReference() {
        return new WeakReference<PlayObject>(this);
    }
    @SuppressWarnings("unchecked,unused") /* Used from JNI */
    private static void dispatchEventFromWeakNative(Object weak, int eventType, long arg1, float arg2) {
        PlayObject obj = ((WeakReference<PlayObject>)weak).get();
        if (obj != null)
            obj.dispatchEventFromNative(eventType, arg1, arg2);
    }
}

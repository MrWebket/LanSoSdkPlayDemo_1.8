/*****************************************************************************
 * class AWindow.java
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
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.LanSoSdk.Play.Util.AndroidVersion;


import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AWindow implements IAWindowNativeHandler, IPlayVout {
    private static final String TAG = "AWindow";

    private static final int ID_VIDEO = 0;
    private static final int ID_SUBTITLES = 1;
    private static final int ID_VIDEO2 = 2;
    private static final int ID_MAX = 3;

    public interface SurfaceCallback {
        @MainThread
        void onSurfacesCreated(AWindow vout);
        @MainThread
        void onSurfacesDestroyed(AWindow vout);
    }

    private class SurfaceHelper {
        private final int mId;
        private final SurfaceView mSurfaceView;
        private final TextureView mTextureView;
        private final SurfaceHolder mSurfaceHolder;
        private Surface mSurface;

        private SurfaceHelper(int id, SurfaceView surfaceView) {
            mId = id;
            mTextureView = null;
            mSurfaceView = surfaceView;
            mSurfaceHolder = mSurfaceView.getHolder();
        }

        private SurfaceHelper(int id, TextureView textureView) {
            mId = id;
            mSurfaceView = null;
            mSurfaceHolder = null;
            mTextureView = textureView;
        }

        private SurfaceHelper(int id, Surface surface, SurfaceHolder surfaceHolder) {
            mId = id;
            mSurfaceView = null;
            mTextureView = null;
            mSurfaceHolder = surfaceHolder;
            mSurface = surface;
        }

        private void setSurface(Surface surface) {
            if (surface.isValid() && getNativeSurface(mId) == null) {
                mSurface = surface;
                setNativeSurface(mId, mSurface);
                onSurfaceCreated();
            }
        }

        private void attachSurfaceView() {
            mSurfaceHolder.addCallback(mSurfaceHolderCallback);
            setSurface(mSurfaceHolder.getSurface());
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        private void attachTextureView() {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            setSurface(new Surface(mTextureView.getSurfaceTexture()));
        }

        private void attachSurface() {
            if (mSurfaceHolder != null)
                mSurfaceHolder.addCallback(mSurfaceHolderCallback);
            setSurface(mSurface);
        }

        public void attach() {
            if (mSurfaceView != null) {
                attachSurfaceView();
            } else if (mTextureView != null) {
                attachTextureView();
            } else if (mSurface != null) {
                attachSurface();
            } else
                throw new IllegalStateException();
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        private void releaseSurfaceTexture() {
            if (mTextureView != null)
                mTextureView.setSurfaceTextureListener(null);
        }

        public void release() {
            mSurface = null;
            setNativeSurface(mId, null);
            if (mSurfaceHolder != null)
                mSurfaceHolder.removeCallback(mSurfaceHolderCallback);
            releaseSurfaceTexture();
        }

        public boolean isReady() {
            return mSurfaceView == null || mSurface != null;
        }

        public Surface getSurface() {
            return mSurface;
        }

        public SurfaceHolder getSurfaceHolder() {
            return mSurfaceHolder;
        }

        private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder != mSurfaceHolder)
                    throw new IllegalStateException("holders are different");
                setSurface(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                onSurfaceDestroyed();
            }
        };

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        private TextureView.SurfaceTextureListener createSurfaceTextureListener() {
            return new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    setSurface(new Surface(surfaceTexture));
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    onSurfaceDestroyed();
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            };
        }

        private final TextureView.SurfaceTextureListener mSurfaceTextureListener =
                AndroidVersion.isICSOrLater() ? createSurfaceTextureListener() : null;
    }

    private final static int SURFACE_STATE_INIT = 0;
    private final static int SURFACE_STATE_ATTACHED = 1;
    private final static int SURFACE_STATE_READY = 2;

    private final SurfaceHelper[] mSurfaceHelpers;
    private final SurfaceCallback mSurfaceCallback;
    private final AtomicInteger mSurfacesState = new AtomicInteger(SURFACE_STATE_INIT);
    private ArrayList<IPlayVout.Callback> mIPlayVoutCallbacks = new ArrayList<IPlayVout.Callback>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Object mNativeLock = new Object();
    /* synchronized Surfaces accessed by an other thread from JNI */
    private final Surface[] mSurfaces;
    private long mCallbackNativeHandle = 0;
    private int mMouseAction = -1, mMouseButton = -1, mMouseX = -1, mMouseY = -1;
    private int mWindowWidth = -1, mWindowHeight = -1;

    private boolean isSurface2Seted=false; 
    
    protected AWindow(SurfaceCallback surfaceCallback) {
        mSurfaceCallback = surfaceCallback;
        mSurfaceHelpers = new SurfaceHelper[ID_MAX];
        mSurfaceHelpers[ID_VIDEO] = null;
        mSurfaceHelpers[ID_SUBTITLES] = null;
        mSurfaceHelpers[ID_VIDEO2] = null;
        
        mSurfaces = new Surface[ID_MAX];        
        mSurfaces[ID_VIDEO] = null;
        mSurfaces[ID_SUBTITLES] = null;
        mSurfaces[ID_VIDEO2] = null;
        
        isSurface2Seted=false;
    }

    private void ensureInitState() throws IllegalStateException {
        if (mSurfacesState.get() != SURFACE_STATE_INIT)
            throw new IllegalStateException("Can't set view when already attached. " +
                    "Current state: " + mSurfacesState.get() + ", " +
                    "mSurfaces[ID_VIDEO]: " + mSurfaceHelpers[ID_VIDEO] + " / " + mSurfaces[ID_VIDEO] + ", " +
                    "mSurfaces[ID_SUBTITLES]: " + mSurfaceHelpers[ID_SUBTITLES] + " / " + mSurfaces[ID_SUBTITLES]);
    }

    private void setView(int id, SurfaceView view) {
        ensureInitState();
        if (view == null)
            throw new NullPointerException("view is null");
        final SurfaceHelper surfaceHelper = mSurfaceHelpers[id];
        if (surfaceHelper != null)
            surfaceHelper.release();

        mSurfaceHelpers[id] = new SurfaceHelper(id, view);
    }

    private void setView(int id, TextureView view) {
        if (!AndroidVersion.isICSOrLater())
            throw new IllegalArgumentException("TextureView not implemented in this android version");
        ensureInitState();
        if (view == null)
            throw new NullPointerException("view is null");
        final SurfaceHelper surfaceHelper = mSurfaceHelpers[id];
        if (surfaceHelper != null)
            surfaceHelper.release();

        mSurfaceHelpers[id] = new SurfaceHelper(id, view);
    }

    private void setSurface(int id, Surface surface, SurfaceHolder surfaceHolder) {
        ensureInitState();
        if (!surface.isValid() && surfaceHolder == null)
            throw new IllegalStateException("surface is not attached and holder is null");
        final SurfaceHelper surfaceHelper = mSurfaceHelpers[id];
        if (surfaceHelper != null)
            surfaceHelper.release();

        mSurfaceHelpers[id] = new SurfaceHelper(id, surface, surfaceHolder);
    }

    @Override
    @MainThread
    public void setVideoView(SurfaceView videoSurfaceView) {
        setView(ID_VIDEO, videoSurfaceView);
    }

    @Override
    @MainThread
    public void setVideoView(TextureView videoTextureView) {
        setView(ID_VIDEO, videoTextureView);
    }

    @Override
    public void setVideoSurface(Surface videoSurface, SurfaceHolder surfaceHolder) {
        setSurface(ID_VIDEO, videoSurface, surfaceHolder);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setVideoSurface(SurfaceTexture videoSurfaceTexture) {
        setSurface(ID_VIDEO, new Surface(videoSurfaceTexture), null);
    }

    
    @Override
    @MainThread
    public void setVideoView2(SurfaceView videoSurfaceView) {
        setView(ID_VIDEO2, videoSurfaceView);
        isSurface2Seted=true;
    }

    @Override
    @MainThread
    public void setVideoView2(TextureView videoTextureView) {
        setView(ID_VIDEO2, videoTextureView);
        isSurface2Seted=true;
    }
    @Override
    public void setVideoSurface2(Surface videoSurface, SurfaceHolder surfaceHolder) {
        setSurface(ID_VIDEO2, videoSurface, surfaceHolder);
        isSurface2Seted=true;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setVideoSurface2(SurfaceTexture videoSurfaceTexture) {
        setSurface(ID_VIDEO2, new Surface(videoSurfaceTexture), null);
        isSurface2Seted=true;
    }
   
    @Override
    public void setVideoSurface2Showing(boolean isShow) {
    	// TODO Auto-generated method stub
    	isSurface2Seted=isShow;
    }
    
    
    
    @Override
    @MainThread
    public void setSubtitlesView(SurfaceView subtitlesSurfaceView) {
        setView(ID_SUBTITLES, subtitlesSurfaceView);
    }

    @Override
    @MainThread
    public void setSubtitlesView(TextureView subtitlesTextureView) {
        setView(ID_SUBTITLES, subtitlesTextureView);
    }

    @Override
    public void setSubtitlesSurface(Surface subtitlesSurface, SurfaceHolder surfaceHolder) {
        setSurface(ID_SUBTITLES, subtitlesSurface, surfaceHolder);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setSubtitlesSurface(SurfaceTexture subtitlesSurfaceTexture) {
        setSurface(ID_SUBTITLES, new Surface(subtitlesSurfaceTexture), null);
    }

    @Override
    @MainThread
    public void attachViews() {
        if (mSurfacesState.get() != SURFACE_STATE_INIT || mSurfaceHelpers[ID_VIDEO] == null)
            throw new IllegalStateException("already attached or video view not configured");
        mSurfacesState.set(SURFACE_STATE_ATTACHED);
        synchronized (mBuffersGeometryCond) {
            mBuffersGeometryCond.configured = false;
            mBuffersGeometryCond.abort = false;
        }
        for (int id = 0; id < ID_MAX; ++id) {
            final SurfaceHelper surfaceHelper = mSurfaceHelpers[id];
            if (surfaceHelper != null)
                surfaceHelper.attach();
        }
    }

    @Override
    @MainThread
    public void detachViews() {
        if (mSurfacesState.get() == SURFACE_STATE_INIT)
            return;
        mSurfacesState.set(SURFACE_STATE_INIT);
        mHandler.removeCallbacksAndMessages(null);
        synchronized (mBuffersGeometryCond) {
            mBuffersGeometryCond.abort = true;
            mBuffersGeometryCond.notifyAll();
        }
        for (int id = 0; id < ID_MAX; ++id) {
            final SurfaceHelper surfaceHelper = mSurfaceHelpers[id];
            if (surfaceHelper != null)
                surfaceHelper.release();
            mSurfaceHelpers[id] = null;
        }
        if (mSurfaceCallback != null)
            mSurfaceCallback.onSurfacesDestroyed(this);
        for (IPlayVout.Callback cb : mIPlayVoutCallbacks)
            cb.onSurfacesDestroyed(this);
    }

    @Override
    @MainThread
    public boolean areViewsAttached() {
        return mSurfacesState.get() != SURFACE_STATE_INIT;
    }

    @MainThread
    private void onSurfaceCreated() {
        if (mSurfacesState.get() != SURFACE_STATE_ATTACHED)
            throw new IllegalArgumentException("invalid state");

        final SurfaceHelper videoHelper = mSurfaceHelpers[ID_VIDEO];
        final SurfaceHelper videoHelper2 = mSurfaceHelpers[ID_VIDEO2];
        final SurfaceHelper subtitlesHelper = mSurfaceHelpers[ID_SUBTITLES];
        if (videoHelper == null)
            throw new NullPointerException("videoHelper shouldn't be null here");

        if (videoHelper!=null && videoHelper.isReady() && (subtitlesHelper == null || subtitlesHelper.isReady())) 
        {
        	if(isSurface2Seted )
        	{
        		if(videoHelper2!=null && videoHelper2.isReady())
        		{
        			  mSurfacesState.set(SURFACE_STATE_READY);
                      if (mSurfaceCallback != null)
                          mSurfaceCallback.onSurfacesCreated(this);
                      for (IPlayVout.Callback cb : mIPlayVoutCallbacks)
                          cb.onSurfacesCreated(this);
        		}
        	}else{
        		 mSurfacesState.set(SURFACE_STATE_READY);
                 if (mSurfaceCallback != null)
                     mSurfaceCallback.onSurfacesCreated(this);
                 for (IPlayVout.Callback cb : mIPlayVoutCallbacks)
                     cb.onSurfacesCreated(this);
        	}
        }
        
    }

    @MainThread
    private void onSurfaceDestroyed() {
        detachViews();
    }

    protected boolean areSurfacesWaiting() {
        return mSurfacesState.get() == SURFACE_STATE_ATTACHED;
    }

    @Override
    public void sendMouseEvent(int action, int button, int x, int y) {
        synchronized (mNativeLock) {
            if (mCallbackNativeHandle != 0)
                nativeOnMouseEvent(mCallbackNativeHandle, action, button, x, y);
            else {
                mMouseAction = action;
                mMouseButton = button;
                mMouseX = x;
                mMouseY = y;
            }
        }
    }

    @Override
    public void setWindowSize(int width, int height) {
        synchronized (mNativeLock) {
            if (mCallbackNativeHandle != 0)
                nativeOnWindowSize(mCallbackNativeHandle, width, height);
            else {
                mWindowWidth = width;
                mWindowHeight = height;
            }
        }
    }

    @Override
    public boolean setCallback(long nativeHandle) {
        synchronized (mNativeLock) {
            if (mCallbackNativeHandle != 0 && nativeHandle != 0)
                return false;
            mCallbackNativeHandle = nativeHandle;
            if (mCallbackNativeHandle != 0) {
                if (mMouseAction != -1)
                    nativeOnMouseEvent(mCallbackNativeHandle, mMouseAction, mMouseButton, mMouseX, mMouseY);
                if (mWindowWidth != -1 && mWindowHeight != -1)
                    nativeOnWindowSize(mCallbackNativeHandle, mWindowWidth, mWindowHeight);
            }
            mMouseAction = mMouseButton = mMouseX = mMouseY = -1;
            mWindowWidth = mWindowHeight = -1;
        }
        return true;
    }

    private void setNativeSurface(int id, Surface surface) {
        synchronized (mNativeLock) {
            mSurfaces[id] = surface;
        }
    }

    private Surface getNativeSurface(int id) {
        synchronized (mNativeLock) {
            return mSurfaces[id];
        }
    }

    @Override
    public Surface getVideoSurface() {
        return getNativeSurface(ID_VIDEO);
    }
    
    @Override
    public Surface getVideoSurface2() {
        return getNativeSurface(ID_VIDEO2);
    }
    @Override
    public Surface getSubtitlesSurface() {
        return getNativeSurface(ID_SUBTITLES);
    }

    private static class BuffersGeometryCond {
        private boolean configured = false;
        private boolean abort = false;
    }
    private final BuffersGeometryCond mBuffersGeometryCond = new BuffersGeometryCond();

    @Override
    public boolean setBuffersGeometry(final Surface surface, final int width, final int height, final int format) {
        if (AndroidVersion.isICSOrLater())
            return false;
        if (width * height == 0)
            return false;
        Log.d(TAG, "configureSurface: " + width + "x" + height);

        synchronized (mBuffersGeometryCond) {
            if (mBuffersGeometryCond.configured || mBuffersGeometryCond.abort)
                return false;
        }

        mHandler.post(new Runnable() {
            private SurfaceHelper getSurfaceHelper(Surface surface) {
                for (int id = 0; id < ID_MAX; ++id) {
                    final SurfaceHelper surfaceHelper = mSurfaceHelpers[id];
                    if (surfaceHelper != null && surfaceHelper.getSurface() == surface)
                        return surfaceHelper;
                }
                return null;
            }

            @Override
            public void run() {
                final SurfaceHelper surfaceHelper = getSurfaceHelper(surface);
                final SurfaceHolder surfaceHolder = surfaceHelper != null ? surfaceHelper.getSurfaceHolder() : null;

                if (surfaceHolder != null) {
                    if (surfaceHolder.getSurface().isValid()) {
                        if (format != 0)
                            surfaceHolder.setFormat(format);
                        surfaceHolder.setFixedSize(width, height);
                    }
                }

                synchronized (mBuffersGeometryCond) {
                    mBuffersGeometryCond.configured = true;
                    mBuffersGeometryCond.notifyAll();
                }
            }
        });

        try {
            synchronized (mBuffersGeometryCond) {
                while (!mBuffersGeometryCond.configured && !mBuffersGeometryCond.abort)
                    mBuffersGeometryCond.wait();
                mBuffersGeometryCond.configured = false;
            }
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @Override
    public void addCallback(IPlayVout.Callback callback) {
        if (!mIPlayVoutCallbacks.contains(callback))
            mIPlayVoutCallbacks.add(callback);
    }

    @Override
    public void removeCallback(IPlayVout.Callback callback) {
        mIPlayVoutCallbacks.remove(callback);
    }

    @Override
    public void setWindowLayout(final int width, final int height, final int visibleWidth, final int visibleHeight, final int sarNum, final int sarDen) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (IPlayVout.Callback cb : mIPlayVoutCallbacks)
                    cb.onNewLayout(AWindow.this, width, height, visibleWidth, visibleHeight, sarNum, sarDen);
            }
        });
    }
    public native void nativeOnMouseEvent(long nativeHandle, int action, int button, int x, int y);
    public native void nativeOnWindowSize(long nativeHandle, int width, int height);
}
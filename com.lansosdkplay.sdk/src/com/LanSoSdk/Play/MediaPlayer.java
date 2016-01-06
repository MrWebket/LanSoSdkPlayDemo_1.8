/*****************************************************************************
 * MediaPlayer.java
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



import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;



/**
 * 
 *modify by LanSoSdk team.(由LanSoSdk团队优化)
 */
@SuppressWarnings("unused")  
public class MediaPlayer extends PlayObject<MediaPlayer.Event> implements LibPlay.HardwareAccelerationError,IPlayVout.Callback{

    public static class Event extends PlayEvent {
        //public static final int MediaChanged        = 0x100;
        //public static final int NothingSpecial      = 0x101;
        public static final int Opening             = 0x102;
        public static final int Buffering           = 0x103;    ///LanSoSdk add.
        
        
        public static final int Playing             = 0x104;
        public static final int Paused              = 0x105;
        public static final int Stopped             = 0x106;
        //public static final int Forward             = 0x107;
        //public static final int Backward            = 0x108;
        public static final int EndReached          = 0x109;
        public static final int EncounteredError   = 0x10a;
        public static final int TimeChanged         = 0x10b;
        public static final int PositionChanged     = 0x10c;
        public static final int SeekableChanged     = 0x10d;
        public static final int PausableChanged     = 0x10e;
        //public static final int TitleChanged        = 0x10f;
        //public static final int SnapshotTaken       = 0x110;
        //public static final int LengthChanged       = 0x111;
        public static final int Vout                = 0x112;
        //public static final int ScrambledChanged    = 0x113;
        public static final int ESAdded             = 0x114;
        public static final int ESDeleted           = 0x115;
        //public static final int ESSelected          = 0x116;
        
        
        protected Event(int type) {
            super(type);
        }
        protected Event(int type, long arg1) {
            super(type, arg1);
        }
        protected Event(int type, float arg2) {
            super(type, arg2);
        }
        public float getBuffering() {  //0---100
            return arg2;
        }
        public long getTimeChanged() {
            return arg1;
        }
        public float getPositionChanged() {
            return arg2;
        }
        public int getVoutCount() {
            return (int) arg1;
        }
        public int getEsChangedType() {
            return (int) arg1;
        }
        public boolean getPausable() {
            return arg1 != 0;
        }
        public boolean getSeekable() {
            return arg1 != 0;
        }
    }

    public interface EventListener extends PlayEvent.Listener<MediaPlayer.Event> {}

    public static class Position {
        public static final int Disable = -1;
        public static final int Center = 0;
        public static final int Left = 1;
        public static final int Right = 2;
        public static final int Top = 3;
        public static final int TopLeft = 4;
        public static final int TopRight = 5;
        public static final int Bottom = 6;
        public static final int BottomLeft = 7;
        public static final int BottomRight = 8;
    }

    public static class Navigate {
        public static final int Activate = 0;
        public static final int Up = 1;
        public static final int Down = 2;
        public static final int Left = 3;
        public static final int Right = 4;
    }

    public static class Title {
        /**
         * duration in milliseconds
         */
        public final long duration;

        /**
         * title name
         */
        public final String name;

        /**
         * true if the title is a menu
         */
        public final boolean menu;

        public Title(long duration, String name, boolean menu) {
            this.duration = duration;
            this.name = name;
            this.menu = menu;
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static Title createTitleFromNative(long duration, String name, boolean menu) {
        return new Title(duration, name, menu);
    }

    public static class Chapter {
        /**
         * time-offset of the chapter in milliseconds
         */
        public final long timeOffset;

        /**
         * duration of the chapter in milliseconds
         */
        public final long duration;

        /**
         * chapter name
         */
        public final String name;

        private Chapter(long timeOffset, long duration, String name) {
            this.timeOffset = timeOffset;
            this.duration = duration;
            this.name = name;
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static Chapter createChapterFromNative(long timeOffset, long duration, String name) {
        return new Chapter(timeOffset, duration, name);
    }

    public static class TrackDescription {
        public final int id;
        public final String name;

        private TrackDescription(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static TrackDescription createTrackDescriptionFromNative(int id, String name) {
        return new TrackDescription(id, name);
    }

    public static class Equalizer {
        @SuppressWarnings("unused") /* Used from JNI */
        private long mInstance;

        private Equalizer() {
            nativeNew();
        }

        private Equalizer(int index) {
            nativeNewFromPreset(index);
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                nativeRelease();
            } finally {
                super.finalize();
            }
        }

        /**
         * Create a new default equalizer, with all frequency values zeroed.
         * The new equalizer can subsequently be applied to a media player by invoking
         * {@link MediaPlayer#setEqualizer}.
         */
        public static Equalizer create() {
            return new Equalizer();
        }

        /**
         * Create a new equalizer, with initial frequency values copied from an existing
         * preset.
         * The new equalizer can subsequently be applied to a media player by invoking
         * {@link MediaPlayer#setEqualizer}.
         */
        public static Equalizer createFromPreset(int index) {
            return new Equalizer(index);
        }

        /**
         * Get the number of equalizer presets.
         */
        public static int getPresetCount() {
            return nativeGetPresetCount();
        }

        /**
         * Get the name of a particular equalizer preset.
         * This name can be used, for example, to prepare a preset label or menu in a user
         * interface.
         *
         * @param  index index of the preset, counting from zero.
         * @return preset name, or NULL if there is no such preset
         */

        public static String getPresetName(int index) {
            return nativeGetPresetName(index);
        }

        /**
         * Get the number of distinct frequency bands for an equalizer.
         */
        public static int getBandCount() {
            return nativeGetBandCount();
        }

        /**
         * Get a particular equalizer band frequency.
         * This value can be used, for example, to create a label for an equalizer band control
         * in a user interface.
         *
         * @param index index of the band, counting from zero.
         * @return equalizer band frequency (Hz), or -1 if there is no such band
         */
        public static float getBandFrequency(int index) {
            return nativeGetBandFrequency(index);
        }

        /**
         * Get the current pre-amplification value from an equalizer.
         *
         * @return preamp value (Hz)
         */
        public float getPreAmp() {
            return nativeGetPreAmp();
        }

        /**
         * Set a new pre-amplification value for an equalizer.
         * The new equalizer settings are subsequently applied to a media player by invoking
         * {@link MediaPlayer#setEqualizer}.
         * The supplied amplification value will be clamped to the -20.0 to +20.0 range.
         *
         * @param preamp value (-20.0 to 20.0 Hz)
         * @return true on success.
         */
        public boolean setPreAmp(float preamp) {
            return nativeSetPreAmp(preamp);
        }

        /**
         * Get the amplification value for a particular equalizer frequency band.
         *
         * @param index counting from zero, of the frequency band to get.
         * @return amplification value (Hz); NaN if there is no such frequency band.
         */
        public float getAmp(int index) {
            return nativeGetAmp(index);
        }

        /**
         * Set a new amplification value for a particular equalizer frequency band.
         * The new equalizer settings are subsequently applied to a media player by invoking
         * {@link MediaPlayer#setEqualizer}.
         * The supplied amplification value will be clamped to the -20.0 to +20.0 range.
         *
         * @param index counting from zero, of the frequency band to set.
         * @param amp amplification value (-20.0 to 20.0 Hz).
         * \return true on success.
         */
        public boolean setAmp(int index, float amp) {
            return nativeSetAmp(index, amp);
        }

        private static native int nativeGetPresetCount();
        private static native String nativeGetPresetName(int index);
        private static native int nativeGetBandCount();
        private static native float nativeGetBandFrequency(int index);
        private native void nativeNew();
        private native void nativeNewFromPreset(int index);
        private native void nativeRelease();
        private native float nativeGetPreAmp();
        private native boolean nativeSetPreAmp(float preamp);
        private native float nativeGetAmp(int index);
        private native boolean nativeSetAmp(int index, float amp);
    }

    private Media mMedia = null;
    private boolean mPlaying = false;
    private boolean mPlayRequested = false;
    private int mVoutCount = 0;
    private boolean mAudioReset = false;
    private String mAudioOutput = null;
    private String mAudioOutputDevice = null;

    private final AWindow mWindow = new AWindow(new AWindow.SurfaceCallback() {
        @Override
        public void onSurfacesCreated(AWindow vout) {
            boolean play = false;
            boolean enableVideo = false;
            synchronized (MediaPlayer.this) {
                if (!mPlaying && mPlayRequested)
                    play = true;
                else if (mVoutCount == 0)
                    enableVideo = true;
            }
            if (play)
                play();
            else if (enableVideo)
                setVideoTrackEnabled(true);
        }

        @Override
        public void onSurfacesDestroyed(AWindow vout) {
            boolean disableVideo = false;
            synchronized (MediaPlayer.this) {
                if (mVoutCount > 0)
                    disableVideo = true;
            }
            if (disableVideo)
                setVideoTrackEnabled(false);
            synchronized (MediaPlayer.this) {
                /* Wait for Vout destruction (mVoutCount = 0) in order to be sure that the surface is not
                 * used after leaving this callback. This shouldn't be needed when using MediaCodec or
                 * AndroidWindow (i.e. after Android 2.3) since the surface is ref-counted */
                while (mVoutCount > 0) {
                    try {
                        MediaPlayer.this.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    });

    /**
     * ************************************************************************************************************************************************
     * new a MediaPlayer Object. 
     * after using . call {@link #release()} to release this object.
     * 
     */
    public  int getVersionCode(Context ctx) {
		int version = 0;
		try {
			version = ctx.getPackageManager().getPackageInfo(ctx.getApplicationInfo().packageName, 0).versionCode;
		} catch (Exception e) {
			Log.e("sno","getVersionInt"+ e);
		}
		return version;
	}
    
    LibPlay mLibPlay;
    public MediaPlayer(Context context)  
    {
    	
    	mLibPlay =new LibPlay();
       	Log.i("MediaPlayer","current LanSoSdkPlay version is:"+mLibPlay.version());
       	

       	Log.i("MediaPlayer","SDK XML getVersionCode:"+getVersionCode(context));
       	
       	
    	mLibPlay.setOnHardwareAccelerationError(this);
    	setNativeCrashListener();
    	nativeNewFromLibPlay(context,mLibPlay, mWindow);	
    	setAudioOutput("android_audiotrack");
    }
    /**
     * set surfaceView to display video picture.
     * @param view
     */
    public void setVideoView(SurfaceView view)
    {
    	mWindow.setVideoView(view);
    }
    public void setWindowSize(int width, int height)
    {
    	mWindow.setWindowSize(width, height);
    }
    public void setSubtitlesView(SurfaceView subtitlesSurfaceView)
    {
    	mWindow.setSubtitlesView(subtitlesSurfaceView);
    }
    public void setVideoSurface2Showing(boolean isShow)
    {
    	mWindow.setVideoSurface2Showing(isShow);
    }
    public void setVideoView2(SurfaceView view)
    {
    	mWindow.setVideoView2(view);
    }
    
    
    
    
    
    /**
     * 
     *Sets the data source (Uri  object) to use.
     *
     * @param path	 the path of the uri. 
     * 				if media is an absolute path, can use:  Uri path=Uri.fromFile(new File("/storage/sdcard1/xxxx.mp4"));
     * 				if media is a http/rtsp/rtmp/ URL. can use: Uri path=Uri.parse("rtsp://192.168.1.33:554/xxxx");  
     * @param isOnlySW  true if only use software codec.
     */
    public void setDataSource(Uri path,boolean isOnlySW) 
    {
    	if(mLibPlay!=null)
    	{
    		final Media media = new Media(mLibPlay,path);
    		if(isOnlySW)
    			media.setHWDecoderEnabled(false, false); 
    		else
    			media.setHWDecoderEnabled(true, false);  
    		
            setMedia(media);
            media.release(); 
            setRate(1.0f);
    	}
    }
    
    /**
     * Create an empty MediaPlayer
     *
     * @param LibPlay a valid LibPlay
     */
    public MediaPlayer(Context context,LibPlay libplay) {
        nativeNewFromLibPlay(context,libplay, mWindow);
    }

    /**
     * Create a MediaPlayer from a Media
     *
     * @param media a valid Media object
     */
    public MediaPlayer(Media media) {
        if (media == null || media.isReleased())
            throw new IllegalArgumentException("Media is null or released");
        mMedia = media;
        mMedia.retain();
        nativeNewFromMedia(mMedia, mWindow);
    }

    /**
     * Get the IPlayVout helper.
     */
    public IPlayVout getPlayVout() {
        return mWindow;
    }

    /**
     * Set a Media
     *
     * @param media a valid Media object
     */
    public void setMedia(Media media) {
        if (media != null) {
            if (media.isReleased())
                throw new IllegalArgumentException("Media is released");
            media.setDefaultMediaPlayerOptions();
        }
        nativeSetMedia(media);
        synchronized (this) {
            if (mMedia != null) {
                mMedia.release();
            }
            if (media != null)
                media.retain();
            mMedia = media;
        }
    }

    /**
     * Get the Media used by this MediaPlayer. This Media should be released with {@link #release()}.
     */
    public synchronized Media getMedia() {
        if (mMedia != null)
            mMedia.retain();
        return mMedia;
    }

    /**
     * Play the media
     *
     */
    public void play() {
        synchronized (this) {
            if (!mPlaying) {
                /* HACK: stop() reset the audio output, so set it again before first play. */
                if (mAudioReset) {
                    if (mAudioOutput != null)
                        nativeSetAudioOutput(mAudioOutput);
                    if (mAudioOutputDevice != null)
                        nativeSetAudioOutputDevice(mAudioOutputDevice);
                    mAudioReset = false;
                }
                mPlayRequested = true;
                if (mWindow.areSurfacesWaiting())
                    return;
            }
            mPlaying = true;
        }
        nativePlay();
    }

    /**
     * Stops the playing media
     *
     */
    public void stop() {
        synchronized (this) {
            mPlayRequested = false;
            mPlaying = false;
            mAudioReset = true;
        }
        nativeStop();
    }
/**
 * *************************************************************start*****************************************************************
 */
    /**
     * modify by  lanSoSdk.
     * @author LanSoSdk
     *
     */
	public interface onNativeCrashListener {	    
    	void onNativeCrash();
    }
	private onNativeCrashListener mOnNativeCrashListener=null;
	
	
	/**
	 * * Register a callback to be invoked when the native creashed
	 * @param listener the callback that will be run
	 */
	public void setOnNativeCrashListener(onNativeCrashListener listener)
	{
		mOnNativeCrashListener=listener;
	}
	
	private void setNativeCrashListener()
	{
		 LibPlay.setOnNativeCrashListener(new  LibPlay.OnNativeCrashListener() {
             @Override
             public void onNativeCrash() {
            	 if(mOnNativeCrashListener!=null)
            	 {
            		 mOnNativeCrashListener.onNativeCrash();
            	 }
             }
         });
	}

    private boolean mHardwareAccelerationError=false;
    @Override
    public void eventHardwareAccelerationError() {
    	mHardwareAccelerationError = true;
    	if(mOnHardwareAccelerationErrorListener!=null)
    	{
    		mOnHardwareAccelerationErrorListener.eventHardwareAccelerationError();
    	}
    }
    
    public interface onHardwareAccelerationErrorListener {	    
    	void eventHardwareAccelerationError();
    }
	private onHardwareAccelerationErrorListener mOnHardwareAccelerationErrorListener=null;
	
	
	/**
	 * * Register a callback to be invoked when the hardware  acceleration Error.
	 * @param listener
	 */
	public void setOnHardwareAccelerationErrorListener(onHardwareAccelerationErrorListener listener)
	{
		mOnHardwareAccelerationErrorListener=listener;
	}
	
	/****************************************************************************/
	/**
	 * 截屏
	 *
	 */
	    public interface onSnapShotCompletedListener {
	        void snapShotCompleted( byte[] bytes,int width,int height,int bytesPerPixel); 
	    }
	    public void setOnSnapShotCompletedListener( onSnapShotCompletedListener listener) {
	    	nativeSetOnSnapShotCompletedListener(listener);
	    }
	    private native void nativeSetOnSnapShotCompletedListener( onSnapShotCompletedListener listener);

	    private native void nativeTriggerSnapShot();  //截屏, 截屏后,会调用snapshot的回调.
	    
		public void triggerSnapShot()
	    {
	    	nativeTriggerSnapShot();
	    }
	 
 	/**
 	 * 
 	 */
	/**
	 * sarNum 是视频采样宽高比的分母.
	 * sarDen 是视频采用宽高比的分子. 一般的情况是1:1,但有的时候, 采用的分辨率是4:3,但在编码的时候,可能会指定宽高比,这样在播放的时候, 就需要按照指定的来播放.
	 * 
	 * @author LanSoSdk
	 *
	 */
	public interface onVideoSizeChangedListener {	    
    	void onVideoSizeChanged(MediaPlayer mediaplayer,int width, int height,int visibleWidth,
    			int visibleHeight, int sarNum, int sarDen);
    }
	private onVideoSizeChangedListener mOnVideoSizeChangedListener=null;
	
	
	/**
	 * Register a callback to be invoked when the video size is known or updated.
	 * @param listener the callback that will be run
	 */
	public void setOnVideoSizeChangedListener(onVideoSizeChangedListener listener)
	{
		mOnVideoSizeChangedListener=listener;
		mWindow.addCallback(this);
		mWindow.attachViews();
	}
	
	/**
	 * remove video size changed callback.
	 */
	public void removeOnVideoSizeChangedListener()
	{
		mOnVideoSizeChangedListener=null;
		mWindow.removeCallback(this);
		mWindow.detachViews();
	}
	
	@Override
	public void onNewLayout(IPlayVout playVout, int width, int height, int visibleWidth,
			int visibleHeight, int sarNum, int sarDen) {
		// TODO Auto-generated method stub
		 if(mOnVideoSizeChangedListener!=null)
	     {
	        	mOnVideoSizeChangedListener.onVideoSizeChanged(this,width, height,visibleWidth,visibleHeight,sarNum,sarDen);
	     }
	}
	@Override
	public void onSurfacesCreated(IPlayVout playVout) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSurfacesDestroyed(IPlayVout playVout) {
		// TODO Auto-generated method stub
		
	}
	
    
    
	/**
	 * ********************************************************end****************************************************************
	 */
    /**
     * Set if, and how, the video title will be shown when media is played
     *
     * @param position see {@link Position}
     * @param timeout
     */
    public void setVideoTitleDisplay(int position, int timeout) {
        nativeSetVideoTitleDisplay(position, timeout);
    }

    /**
     * Selects an audio output module.
     * Any change will take effect only after playback is stopped and
     * restarted. Audio output cannot be changed while playing.
     *
     * @return true on success.
     */
    public boolean setAudioOutput(String aout) {
        final boolean ret = nativeSetAudioOutput(aout);
        if (ret) {
            synchronized (this) {
                mAudioOutput = aout;
            }
        }
        return ret;
    }

    /**
     * Configures an explicit audio output device.
     * Audio output will be moved to the device specified by the device identifier string.
     *
     * @return true on success.
     */
    public boolean setAudioOutputDevice(String id) {
        final boolean ret = nativeSetAudioOutputDevice(id);
        if (ret) {
            synchronized (this) {
                mAudioOutputDevice = id;
            }
        }
        return ret;
    }

    /**
     * Get the full description of available titles.
     *
     * @return the list of titles
     */
    public Title[] getTitles() {
        return nativeGetTitles();
    }

    /**
     * Get the full description of available chapters.
     *
     * @param title index of the title (if -1, use the current title)
     * @return the list of Chapters for the title
     */
    public Chapter[] getChapters(int title) {
        return nativeGetChapters(title);
    }

    /**
     * Get the number of available video tracks.
     */
    public int getVideoTracksCount() {
        return nativeGetVideoTracksCount();
    }

    /**
     * Get the list of available video tracks.
     */
    public TrackDescription[] getVideoTracks() {
        return nativeGetVideoTracks();
    }

    /**
     * Get the current video track.
     *
     * @return the video track ID or -1 if no active input
     */
    public int getVideoTrack() {
        return nativeGetVideoTrack();
    }

    /**
     * Set the video track.
     *
     * @return true on success.
     */
    public boolean setVideoTrack(int index) {
        return nativeSetVideoTrack(index);
    }

    /**
     * Set the enabled state of the video track
     *
     * @param enabled
     */
    public void setVideoTrackEnabled(boolean enabled) {
        if (!enabled) {
            setVideoTrack(-1);
        } else if (getVideoTrack() == -1) {
            final MediaPlayer.TrackDescription tracks[] = getVideoTracks();

            if (tracks != null) {
                for (MediaPlayer.TrackDescription track : tracks) {
                    if (track.id != -1) {
                        setVideoTrack(track.id);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Get the number of available audio tracks.
     */
    public int getAudioTracksCount() {
        return nativeGetAudioTracksCount();
    }

    /**
     * Get the list of available audio tracks.
     */
    public TrackDescription[] getAudioTracks() {
        return nativeGetAudioTracks();
    }

    /**
     * Get the current audio track.
     *
     * @return the audio track ID or -1 if no active input
     */
    public int getAudioTrack() {
        return nativeGetAudioTrack();
    }

    /**
     * Set the audio track.
     *
     * @return true on success.
     */
    public boolean setAudioTrack(int index) {
        return nativeSetAudioTrack(index);
    }

    /**
     * Get the current audio delay.
     *
     * @return delay in microseconds.
     */
    public long getAudioDelay() {
        return nativeGetAudioDelay();
    }

    /**
     * Set current audio delay. The audio delay will be reset to zero each time the media changes.
     *
     * @param delay in microseconds.
     * @return true on success.
     */
    public boolean setAudioDelay(long delay) {
        return nativeSetAudioDelay(delay);
    }

    /**
     * Get the number of available spu (subtitle) tracks.
     */
    public int getSpuTracksCount() {
        return nativeGetSpuTracksCount();
    }

    /**
     * Get the list of available spu (subtitle) tracks.
     */
    public TrackDescription[] getSpuTracks() {
        return nativeGetSpuTracks();
    }

    /**
     * Get the current spu (subtitle) track.
     *
     * @return the spu (subtitle) track ID or -1 if no active input
     */
    public int getSpuTrack() {
        return nativeGetSpuTrack();
    }

    /**
     * Set the spu (subtitle) track.
     *
     * @return true on success.
     */
    public boolean setSpuTrack(int index) {
        return nativeSetSpuTrack(index);
    }

    /**
     * Get the current spu (subtitle) delay.
     *
     * @return delay in microseconds.
     */
    public long getSpuDelay() {
        return nativeGetSpuDelay();
    }

    /**
     * Set current spu (subtitle) delay. The spu delay will be reset to zero each time the media changes.
     *
     * @param delay in microseconds.
     * @return true on success.
     */
    public boolean setSpuDelay(long delay) {
        return nativeSetSpuDelay(delay);
    }

    /**
     * Apply new equalizer settings to a media player.
     *
     * The equalizer is first created by invoking {@link Equalizer#create()} or
     * {@link Equalizer#createFromPreset(int)}}.
     *
     * It is possible to apply new equalizer settings to a media player whether the media
     * player is currently playing media or not.
     *
     * Invoking this method will immediately apply the new equalizer settings to the audio
     * output of the currently playing media if there is any.
     *
     * If there is no currently playing media, the new equalizer settings will be applied
     * later if and when new media is played.
     *
     * Equalizer settings will automatically be applied to subsequently played media.
     *
     * To disable the equalizer for a media player invoke this method passing null.
     *
     * @return true on success.
     */
    public boolean setEqualizer(Equalizer equalizer) {
        return nativeSetEqualizer(equalizer);
    }

    /**
     * Set a new video subtitle file.
     *
     * @param path local path.
     * @return true on success.
     */
    public boolean setSubtitleFile(String path) {
        return nativeSetSubtitleFile(path);
    }

    /**
     * Sets the speed of playback (1 being normal speed, 2 being twice as fast)
     *
     * @param rate
     */
    public native void setRate(float rate);

    /**
     * Get the current playback speed
     */
    public native float getRate();

    /**
     * Returns true if any media is playing
     */
    public native boolean isPlaying();

    /**
     * Returns true if any media is seekable
     */
    public native boolean isSeekable();

    /**
     * Pauses any playing media
     */
    public native void pause();

    /**
     * Get player state.
     */
    public native int getPlayerState();

    /**
     * Gets volume as integer
     */
    public native int getVolume();

    /**
     * Sets volume as integer
     * @param volume: Volume level passed as integer
     */
    public native int setVolume(int volume);

    /**
     * Gets the current movie time (in ms).
     * @return the movie time (in ms), or -1 if there is no media.
     */
    public native long getTime();

    /**
     * Sets the movie time (in ms), if any media is being played.
     * @param time: Time in ms.
     * @return the movie time (in ms), or -1 if there is no media.
     */
    public native long setTime(long time);

    /**
     * Gets the movie position.
     * @return the movie position, or -1 for any error.
     */
    public native float getPosition();

    /**
     * Sets the movie position.
     * @param pos: movie position.
     */
    public native void setPosition(float pos);

    /**
     * Gets current movie's length in ms.
     * @return the movie length (in ms), or -1 if there is no media.
     */
    public native long getLength();

    public native int getTitle();
    public native void setTitle(int title);
    public native int getChapter();
    public native int previousChapter();
    public native int nextChapter();
    public native void setChapter(int chapter);
    public native void navigate(int navigate);

    public synchronized void setEventListener(EventListener listener) {
        super.setEventListener(listener);
    }

    @Override
    protected synchronized Event onEventNative(int eventType, long arg1, float arg2) {
        switch (eventType) {
        	case Event.Buffering:
        		return new Event(eventType,arg2);  //float type. percentage.
        	
            case Event.Stopped:
            case Event.EndReached:
            case Event.EncounteredError:
                mVoutCount = 0;
                notify();
            case Event.Opening:
            case Event.Playing:
            case Event.Paused:
                return new Event(eventType);
            case Event.TimeChanged:
                return new Event(eventType, arg1);
            case Event.PositionChanged:
                return new Event(eventType, arg2);
            case Event.Vout:
                mVoutCount = (int) arg1;
                notify();
                return new Event(eventType, arg1);
            case Event.ESAdded:
            case Event.ESDeleted:
            case Event.SeekableChanged:
            case Event.PausableChanged:
                return new Event(eventType, arg1);
        }
        return null;
    }

    @Override
    protected void onReleaseNative() {
        if (mMedia != null)
            mMedia.release();
        nativeRelease();
    }

    /* JNI */
    private native void nativeNewFromLibPlay(Context context,LibPlay libplay, IAWindowNativeHandler window);
    private native void nativeNewFromMedia(Media media, IAWindowNativeHandler window);
    private native void nativeRelease();
    private native void nativeSetMedia(Media media);
    private native void nativePlay();
    private native void nativeStop();
    private native void nativeSetVideoTitleDisplay(int position, int timeout);
    private native boolean nativeSetAudioOutput(String aout);
    private native boolean nativeSetAudioOutputDevice(String id);
    private native Title[] nativeGetTitles();
    private native Chapter[] nativeGetChapters(int title);
    private native int nativeGetVideoTracksCount();
    private native TrackDescription[] nativeGetVideoTracks();
    private native int nativeGetVideoTrack();
    private native boolean nativeSetVideoTrack(int index);
    private native int nativeGetAudioTracksCount();
    private native TrackDescription[] nativeGetAudioTracks();
    private native int nativeGetAudioTrack();
    private native boolean nativeSetAudioTrack(int index);
    private native long nativeGetAudioDelay();
    private native boolean nativeSetAudioDelay(long delay);
    private native int nativeGetSpuTracksCount();
    private native TrackDescription[] nativeGetSpuTracks();
    private native int nativeGetSpuTrack();
    private native boolean nativeSetSpuTrack(int index);
    private native long nativeGetSpuDelay();
    private native boolean nativeSetSpuDelay(long delay);
    private native boolean nativeSetSubtitleFile(String path);
    private native boolean nativeSetEqualizer(Equalizer equalizer);
    
    
    
    /******************************************************************************** */
     public native void playNextFrame();  ///beta function.
    
    //video picture effects....
    public native void setDisableAllEffect();  
    public native String getCurrentEffects();
    public native void setEnableAnaglyph(boolean isEnable);  
    public native void setEnableMirror(boolean isEnable);
    public native void setEnablePsychedelic(boolean isEnable);
    public native void setEnableWave(boolean isEnable);
    public native void setEnableRipple(boolean isEnable);
    public native void setEnableMotiondetect(boolean isEnable);
    
    
    
    
    public native void setEnableInvert(boolean isEnable);
    public native void setEnablePosterize(boolean isEnable);    

    public native void setEnableExtract(boolean isEnable); 
    public native void setExtractValue(int value);
    
    public native void setEnableSepia(boolean isEnable);
    public native void setSepiaValue(int value); 

    public native void setEnableMotionblur(boolean isEnable);
    public native void setMotionblurValue(int value);    
    
    //video adjuct effectttt... gamma..and so on...
    public native void setEnableAdjuct(boolean isEnable);  
    public native boolean setContrastValue(float value);
    public native boolean setBrightnessValue(float value);
    public native boolean setHueValue(float value);
    public native boolean setSaturationValue(float value);
    public native boolean setGammaValue(float value);
}

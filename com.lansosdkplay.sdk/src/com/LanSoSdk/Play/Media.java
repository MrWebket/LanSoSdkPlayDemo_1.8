/*****************************************************************************
 * Media.java
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

import android.net.Uri;
import android.util.Log;

import com.LanSoSdk.Play.Util.AndroidVersion;
import com.LanSoSdk.Play.Util.HWDecoderUtil;

import java.io.FileDescriptor;

@SuppressWarnings("unused")
public class Media extends PlayObject<Media.Event> {
    private final static String TAG = "LibPlay/Media";

    public static class Event extends PlayEvent {
        public static final int MetaChanged = 0;
        public static final int SubItemAdded = 1;
        public static final int DurationChanged = 2;
        public static final int ParsedChanged = 3;
        //public static final int Freed                      = 4;
        public static final int StateChanged = 5;
        public static final int SubItemTreeAdded = 6;

        protected Event(int type) {
            super(type);
        }
        protected Event(int type, long arg1) {
            super(type, arg1);
        }

        public int getMetaId() {
            return (int) arg1;
        }
    }

    public interface EventListener extends PlayEvent.Listener<Media.Event> {}

    public static class Type {
        public static final int Unknown = 0;
        public static final int File = 1;
        public static final int Directory = 2;
        public static final int Disc = 3;
        public static final int Stream = 4;
        public static final int Playlist = 5;
    }
    public static class Meta {
        public static final int Title = 0;
        public static final int Artist = 1;
        public static final int Genre = 2;
        public static final int Copyright = 3;
        public static final int Album = 4;
        public static final int TrackNumber = 5;
        public static final int Description = 6;
        public static final int Rating = 7;
        public static final int Date = 8;
        public static final int Setting = 9;
        public static final int URL = 10;
        public static final int Language = 11;
        public static final int NowPlaying = 12;
        public static final int Publisher = 13;
        public static final int EncodedBy = 14;
        public static final int ArtworkURL = 15;
        public static final int TrackID = 16;
        public static final int TrackTotal = 17;
        public static final int Director = 18;
        public static final int Season = 19;
        public static final int Episode = 20;
        public static final int ShowName = 21;
        public static final int Actors = 22;
        public static final int AlbumArtist = 23;
        public static final int DiscNumber = 24;
        public static final int MAX = 25;
    }

    public static class State {
        public static final int NothingSpecial = 0;
        public static final int Opening = 1;
        public static final int Buffering = 2;
        public static final int Playing = 3;
        public static final int Paused = 4;
        public static final int Stopped = 5;
        public static final int Ended = 6;
        public static final int Error = 7;
        public static final int MAX = 8;
    }

    public static class Parse {
        public static final int ParseLocal   = 0;
        public static final int ParseNetwork = 0x01;
        public static final int FetchLocal   = 0x02;
        public static final int FetchNetwork = 0x04;
    }

    public static abstract class Track {
        public static class Type {
            public static final int Unknown = -1;
            public static final int Audio = 0;
            public static final int Video = 1;
            public static final int Text = 2;
        }

        public final int type;
        public final String codec;
        public final String originalCodec;
        public final int id;
        public final int profile;
        public final int level;
        public final int bitrate;
        public final String language;
        public final String description;

        private Track(int type, String codec, String originalCodec, int id, int profile,
                int level, int bitrate, String language, String description) {
            this.type = type;
            this.codec = codec;
            this.originalCodec = originalCodec;
            this.id = id;
            this.profile = profile;
            this.level = level;
            this.bitrate = bitrate;
            this.language = language;
            this.description = description;
        }
    }

    public static class AudioTrack extends Track {
        public final int channels;
        public final int rate;

        private AudioTrack(String codec, String originalCodec, int id, int profile,
                int level, int bitrate, String language, String description,
                int channels, int rate) {
            super(Type.Audio, codec, originalCodec, id, profile, level, bitrate, language, description);
            this.channels = channels;
            this.rate = rate;
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static Track createAudioTrackFromNative(String codec, String originalCodec, int id, int profile,
            int level, int bitrate, String language, String description,
            int channels, int rate) {
        return new AudioTrack(codec, originalCodec, id, profile,
                level, bitrate, language, description,
                channels, rate);
    }
    public static class VideoTrack extends Track {
        public final int height;
        public final int width;
        public final int sarNum;
        public final int sarDen;
        public final int frameRateNum;
        public final int frameRateDen;

        private VideoTrack(String codec, String originalCodec, int id, int profile,
                int level, int bitrate, String language, String description,
                int height, int width, int sarNum, int sarDen, int frameRateNum, int frameRateDen) {
            super(Type.Video, codec, originalCodec, id, profile, level, bitrate, language, description);
            this.height = height;
            this.width = width;
            this.sarNum = sarNum;
            this.sarDen = sarDen;
            this.frameRateNum = frameRateNum;
            this.frameRateDen = frameRateDen;
        }
    }

    /* Used from JNI */
    private static Track createVideoTrackFromNative(String codec, String originalCodec, int id, int profile,
            int level, int bitrate, String language, String description,
            int height, int width, int sarNum, int sarDen, int frameRateNum, int frameRateDen) {
        return new VideoTrack(codec, originalCodec, id, profile,
                level, bitrate, language, description,
                height, width, sarNum, sarDen, frameRateNum, frameRateDen);
    }

    public static class SubtitleTrack extends Track {
        public final String encoding;

        private SubtitleTrack(String codec, String originalCodec, int id, int profile,
                int level, int bitrate, String language, String description,
                String encoding) {
            super(Type.Text, codec, originalCodec, id, profile, level, bitrate, language, description);
            this.encoding = encoding;
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static Track createSubtitleTrackFromNative(String codec, String originalCodec, int id, int profile,
            int level, int bitrate, String language, String description,
            String encoding) {
        return new SubtitleTrack(codec, originalCodec, id, profile,
                level, bitrate, language, description,
                encoding);
    }

    private static final int PARSE_STATUS_INIT = 0x00;
    private static final int PARSE_STATUS_PARSING = 0x01;
    private static final int PARSE_STATUS_PARSED = 0x02;

    private Uri mUri = null;
    private MediaList mSubItems = null;
    private int mParseStatus = PARSE_STATUS_INIT;
    private final String mNativeMetas[] = new String[Meta.MAX];
    private Track mNativeTracks[] = null;
    private long mDuration = -1;
    private int mState = -1;
    private int mType = -1;
    private boolean mCodecOptionSet = false;

    /**
     * Create a Media from  and a local path starting with '/'.
     *
     */
    public Media(LibPlay libplay, String path) {
        nativeNewFromPath(libplay, path);
        mUri = UriFromMrl(nativeGetMrl());
    }

    /**
     * Create a Media from LibPlay and a Uri
     *
     */
    public Media(LibPlay libplay, Uri uri) {
        nativeNewFromLocation(libplay, locationFromUri(uri));
        mUri = uri;
    }

    /**
     * Create a Media from LibPlay and a FileDescriptor
     *
     */
    public Media(LibPlay libplay, FileDescriptor fd) {
        nativeNewFromFd(libplay, fd);
        mUri = UriFromMrl(nativeGetMrl());
    }

    /**
     *
     * @param ml Should not be released and locked
     * @param index index of the Media from the MediaList
     */
    protected Media(MediaList ml, int index) {
        if (ml == null || ml.isReleased())
            throw new IllegalArgumentException("MediaList is null or released");
        if (!ml.isLocked())
            throw new IllegalStateException("MediaList should be locked");
        nativeNewFromMediaList(ml, index);
        mUri = UriFromMrl(nativeGetMrl());
    }

    private static final String URI_AUTHORIZED_CHARS = "!'()*";

    private static Uri UriFromMrl(String mrl) {
        final char array[] = mrl.toCharArray();
        final StringBuilder sb = new StringBuilder(array.length);

        for (int i = 0; i < array.length; ++i) {
            final char c = array[i];
            if (c == '%') {
                if (array.length - i >= 3) {
                    try {
                        final int hex = Integer.parseInt(new String(array, i + 1, 2), 16);
                        if (URI_AUTHORIZED_CHARS.indexOf(hex) != -1) {
                            sb.append((char) hex);
                            i += 2;
                            continue;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }

            }
            sb.append(c);
        }

        return Uri.parse(sb.toString());
    }

    private static String locationFromUri(Uri uri) {
        final char array[] = uri.toString().toCharArray();
        final StringBuilder sb = new StringBuilder(array.length * 2);

        for (final char c : array) {
            if (URI_AUTHORIZED_CHARS.indexOf(c) != -1)
                sb.append("%").append(Integer.toHexString(c));
            else
                sb.append(c);
        }

        return sb.toString();
    }

    public void setEventListener(EventListener listener) {
        super.setEventListener(listener);
    }

    @Override
    protected synchronized Event onEventNative(int eventType, long arg1, float arg2) {
        switch (eventType) {
        case Event.MetaChanged:
            // either we update all metas (if first call) or we update a specific meta
            int id = (int) arg1;
            if (id >= 0 && id < Meta.MAX)
                mNativeMetas[id] = null;
            return new Event(eventType, arg1);
        case Event.DurationChanged:
            mDuration = -1;
            break;
        case Event.ParsedChanged:
            postParse();
            break;
        case Event.StateChanged:
            mState = -1;
            break;
        }
        return new Event(eventType);
    }

    /**
     * Get the MRL associated with the Media.
     */
    public synchronized Uri getUri() {
        return mUri;
    }

    /**
     * Get the duration of the media.
     */
    public long getDuration() {
        synchronized (this) {
            if (mDuration != -1)
                return mDuration;
            if (isReleased())
                return 0;
        }
        final long duration = nativeGetDuration();
        synchronized (this) {
            mDuration = duration;
            return mDuration;
        }
    }

    /**
     * Get the state of the media.
     *
     * @see State
     */
    public int getState() {
        synchronized (this) {
            if (mState != -1)
                return mState;
            if (isReleased())
                return State.Error;
        }
        final int state = nativeGetState();
        synchronized (this) {
            mState = state;
            return mState;
        }
    }

    /**
     * Get the subItems MediaList associated with the Media. This Media should be alive (not released).
     *
     * @return subItems as a MediaList. This MediaList should be released with {@link #release()}.
     */
    public MediaList subItems() {
        synchronized (this) {
            if (mSubItems != null) {
                mSubItems.retain();
                return mSubItems;
            }
        }
        final MediaList subItems = new MediaList(this);
        synchronized (this) {
            mSubItems = subItems;
            mSubItems.retain();
            return mSubItems;
        }
    }

    private synchronized void postParse() {
        // fetch if parsed and not fetched
        mParseStatus &= ~PARSE_STATUS_PARSING;
        mParseStatus |= PARSE_STATUS_PARSED;
        mNativeTracks = null;
        mDuration = -1;
        mState = -1;
        mType = -1;
    }

    /**
     * Parse the media synchronously with a flag. This Media should be alive (not released).
     *
     * @param flags see {@link Parse}
     * @return true in case of success, false otherwise.
     */
    public boolean parse(int flags) {
        boolean parse = false;
        synchronized (this) {
            if ((mParseStatus & (PARSE_STATUS_PARSED | PARSE_STATUS_PARSING)) == 0) {
                mParseStatus |= PARSE_STATUS_PARSING;
                parse = true;
            }
        }
        if (parse && nativeParse(flags)) {
            postParse();
            return true;
        } else
            return false;
    }

    /**
     * Parse the media and local art synchronously. This Media should be alive (not released).
     *
     * @return true in case of success, false otherwise.
     */
    public boolean parse() {
        return parse(Parse.FetchLocal);
    }

    /**
     * Parse the media asynchronously with a flag. This Media should be alive (not released).
     *
     * To track when this is over you can listen to {@link Event#ParsedChanged}
     * event (only if this methods returned true).
     *
     * @param flags see {@link Parse}
     * @return true in case of success, false otherwise.
     */
    public boolean parseAsync(int flags) {
        boolean parse = false;
        synchronized (this) {
            if ((mParseStatus & (PARSE_STATUS_PARSED | PARSE_STATUS_PARSING)) == 0) {
                mParseStatus |= PARSE_STATUS_PARSING;
                parse = true;
            }
        }
        return parse && nativeParseAsync(flags);
    }

    /**
     * Parse the media and local art asynchronously. This Media should be alive (not released).
     *
     * @see #parseAsync(int)
     */
    public boolean parseAsync() {
        return parseAsync(Parse.FetchLocal);
    }

    /**
     * Returns true if the media is parsed This Media should be alive (not released).
     */
    public synchronized boolean isParsed() {
        return (mParseStatus & PARSE_STATUS_PARSED) != 0;
    }

    /**
     * Get the type of the media
     *
     * @see {@link Type}
     */
    public int getType() {
        synchronized (this) {
            if (mType != -1)
                return mType;
            if (isReleased())
                return Type.Unknown;
        }
        final int type = nativeGetType();
        synchronized (this) {
            mType = type;
            return mType;
        }
    }

    private Track[] getTracks() {
        synchronized (this) {
            if (mNativeTracks != null)
                return mNativeTracks;
            if (isReleased())
                return null;
        }
        final Track[] tracks = nativeGetTracks();
        synchronized (this) {
            mNativeTracks = tracks;
            return mNativeTracks;
        }
    }

    /**
     * Get the Track count.
     */
    public int getTrackCount() {
        final Track[] tracks = getTracks();
        return tracks != null ? tracks.length : 0;
    }

    /**
     * Get a Track
     * The Track can be casted to {@link AudioTrack}, {@link VideoTrack} or {@link SubtitleTrack} in function of the {@link Track.Type}.
     *
     * @param idx index of the track
     * @return Track or null if not idx is not valid
     * @see #getTrackCount()
     */
    public Track getTrack(int idx) {
        final Track[] tracks = getTracks();
        if (tracks == null || idx < 0 || idx >= tracks.length)
            return null;
        return tracks[idx];
    }

    /**
     * Get a Meta.
     *
     * @param id see {@link Meta}
     * @return meta or null if not found
     */
    public String getMeta(int id) {
        if (id < 0 || id >= Meta.MAX)
            return null;

        synchronized (this) {
            if (mNativeMetas[id] != null)
                return mNativeMetas[id];
            if (isReleased())
                return null;
        }

        final String meta = nativeGetMeta(id);
        synchronized (this) {
            mNativeMetas[id] = meta;
            return meta;
        }
    }


    private static String getMediaCodecModule() {
        return AndroidVersion.isLolliPopOrLater() ? "mediacodec_ndk" : "mediacodec_jni";
    }

    /**
     * Add or remove hw acceleration media options
     *
     * @param enabled if true, hw decoder will be used
     * @param force force hw acceleration even for unknown devices
     */
    public void setHWDecoderEnabled(boolean enabled, boolean force) {
    	
             addOption(":file-caching=1500");
             addOption(":network-caching=1500");
             
        final HWDecoderUtil.Decoder decoder = enabled ?
                HWDecoderUtil.getDecoderFromDevice() :
                HWDecoderUtil.Decoder.NONE;

        if (decoder == HWDecoderUtil.Decoder.NONE ||
                (decoder == HWDecoderUtil.Decoder.UNKNOWN && !force)) {
            addOption(":codec=all");
            return;
        }
        final StringBuilder sb = new StringBuilder(":codec=");
        if (decoder == HWDecoderUtil.Decoder.MEDIACODEC)
            sb.append(getMediaCodecModule()).append(",");
        else if (decoder == HWDecoderUtil.Decoder.OMX)
            sb.append("iomx,");
        else
            sb.append(getMediaCodecModule()).append(",iomx,");
        sb.append("all");

        addOption(sb.toString());
    }

    /**
     * Enable HWDecoder options if not already set
     */
    protected void setDefaultMediaPlayerOptions() {
        boolean codecOptionSet;
        synchronized (this) {
            codecOptionSet = mCodecOptionSet;
            mCodecOptionSet = true;
        }
        if (!codecOptionSet)
            setHWDecoderEnabled(true, false);
    }

    /**
     * Add an option to this Media. This Media should be alive (not released).
     *
     * @param option ":option" or ":option=value"
     */
    public void addOption(String option) {
        synchronized (this) {
            if (!mCodecOptionSet && option.startsWith(":codec="))
                mCodecOptionSet = true;
        }
        nativeAddOption(option);
    }

    @Override
    protected void onReleaseNative() {
        if (mSubItems != null)
            mSubItems.release();
        nativeRelease();
    }

    /* JNI */
    private native void nativeNewFromPath(LibPlay libplay, String path);
    private native void nativeNewFromLocation(LibPlay libplay, String location);
    private native void nativeNewFromFd(LibPlay libplay, FileDescriptor fd);
    private native void nativeNewFromMediaList(MediaList ml, int index);
    private native void nativeRelease();
    private native boolean nativeParseAsync(int flags);
    private native boolean nativeParse(int flags);
    private native String nativeGetMrl();
    private native int nativeGetState();
    private native String nativeGetMeta(int id);
    private native Track[] nativeGetTracks();
    private native long nativeGetDuration();
    private native int nativeGetType();
    private native void nativeAddOption(String option);
}
/*****************************************************************************
 * MediaBrowser.java
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

package com.LanSoSdk.Play.Util;

import android.net.Uri;

import java.util.ArrayList;

import com.LanSoSdk.Play.LibPlay;
import com.LanSoSdk.Play.Media;
import com.LanSoSdk.Play.MediaDiscoverer;
import com.LanSoSdk.Play.MediaList;

public class MediaBrowser {
    private static final String TAG = "MediaBrowser";

    public static enum Discover {
        UPNP("upnp"),
        SMB("dsm")
        ;

        private final String str;
        Discover(String str) {
            this.str = str;
        }
    }

    private final LibPlay mLibPlay;
    private final ArrayList<MediaDiscoverer> mMediaDiscoverers = new ArrayList<MediaDiscoverer>();
    private final ArrayList<Media> mDiscovererMediaArray = new ArrayList<Media>();
    private MediaList mBrowserMediaList;
    private Media mMedia;
    private EventListener mEventListener;
    private boolean mAlive;

    private static final String IGNORE_LIST_OPTION =  ":ignore-filetypes=";
    private String mIgnoreList = "db,nfo,ini,jpg,jpeg,ljpg,gif,png,pgm,pgmyuv,pbm,pam,tga,bmp,pnm,xpm,xcf,pcx,tif,tiff,lbm,sfv,txt,sub,idx,srt,cue,ssa";

    /**
     * Listener called when medias are added or removed.
     */
    public interface EventListener {
        /**
         * Received when a new media is added.
         * @param index
         * @param media
         */
        public void onMediaAdded(int index, Media media);
        /**
         * Received when a media is removed (Happens only when you discover networks)
         * @param index
         * @param media Released media, but cached attributes are still
         * available (like media.getMrl())
         */
        public void onMediaRemoved(int index, Media media);
        /**
         * Called when browse ended.
         * It won't be called when you discover networks
         */
        public void onBrowseEnd();
    }

    public MediaBrowser(LibPlay libplay, EventListener listener) {
        mLibPlay = libplay;
        mLibPlay.retain();
        mEventListener = listener;
        mAlive = true;
    }

    private synchronized void reset() {
        for (MediaDiscoverer md : mMediaDiscoverers)
            md.release();
        mMediaDiscoverers.clear();
        mDiscovererMediaArray.clear();
        if (mMedia != null) {
            mMedia.release();
            mMedia = null;
        }

        if (mBrowserMediaList != null) {
            mBrowserMediaList.release();
            mBrowserMediaList = null;
        }
    }

    /**
     * Release the MediaBrowser.
     */
    public synchronized void release() {
        reset();
        if (!mAlive)
            throw new IllegalStateException("MediaBrowser released more than one time");
        mLibPlay.release();
        mAlive = false;
    }

    /**
     * Reset this media browser and register a new EventListener
     * @param eventListener new EventListener for this browser
     */
    public synchronized void changeEventListener(EventListener eventListener){
        reset();
        mEventListener = eventListener;
    }

    private void startMediaDiscoverer(String discovererName) {
        MediaDiscoverer md = new MediaDiscoverer(mLibPlay, discovererName);
        mMediaDiscoverers.add(md);
        final MediaList ml = md.getMediaList();
        ml.setEventListener(mDiscovererMediaListEventListener);
        ml.release();
        md.start();
    }

    /**
     * Discover networks shares using a list of Discoverers
     */
    public synchronized void discoverNetworkShares(Discover discovers[]) {
        reset();
        for (Discover discover : discovers)
            startMediaDiscoverer(discover.str);
    }

    /**
     * Discover networks shares using a specified Discoverer
     * @param discovererName
     */
    public synchronized void discoverNetworkShares(Discover discover) {
        Discover discovers[] = new Discover[1];
        discovers[0] = discover;
        discoverNetworkShares(discovers);
    }

    /**
     * Browse to the specified local path starting with '/'.
     *
     * @param path
     */
    public synchronized void browse(String path) {
        final Media media = new Media(mLibPlay, path);
        browse(media);
        media.release();
    }

    /**
     * Browse to the specified uri.
     *
     * @param uri
     */
    public synchronized void browse(Uri uri) {
        final Media media = new Media(mLibPlay, uri);
        browse(media);
        media.release();
    }

    /**
     * Browse to the specified media.
     *
     * @param media Can be a media returned by MediaBrowser.
     */
    public synchronized void browse(Media media) {
        /* media can be associated with a medialist,
         * so increment ref count in order to don't clean it with the medialist
         */
        media.retain();
        media.addOption(IGNORE_LIST_OPTION+mIgnoreList);
        reset();
        mBrowserMediaList = media.subItems();
        mBrowserMediaList.setEventListener(mBrowserMediaListEventListener);
        media.parseAsync(Media.Parse.ParseNetwork);
        mMedia = media;
    }

    /**
     * Get the number or media.
     */
    public synchronized int getMediaCount() {
        return mBrowserMediaList != null ? mBrowserMediaList.getCount() : mDiscovererMediaArray.size();
    }

    /**
     * Get a media at a specified index. Should be released with {@link #release()}.
     */
    public synchronized Media getMediaAt(int index) {
        if (index < 0 || index >= getMediaCount())
            throw new IndexOutOfBoundsException();
        final Media media = mBrowserMediaList != null ? mBrowserMediaList.getMediaAt(index) :
                mDiscovererMediaArray.get(index);
        media.retain();
        return media;
    }

    /**
     * Override the extensions list to be ignored in browsing
     * default is "db,nfo,ini,jpg,jpeg,ljpg,gif,png,pgm,pgmyuv,pbm,pam,tga,bmp,pnm,xpm,xcf,pcx,tif,tiff,lbm,sfv,txt,sub,idx,srt,cue,ssa"
     *
     * @param list files extensions to be ignored by browser
     */
    public synchronized void setIgnoreFileTypes(String list) {
        mIgnoreList = list;
    }

    private final MediaList.EventListener mBrowserMediaListEventListener = new MediaList.EventListener() {
        @Override
        public void onEvent(MediaList.Event event) {
            if (mEventListener == null)
                return;
            final MediaList.Event mlEvent = (MediaList.Event) event;

            /*
             * We use an intermediate array here since more than one MediaDiscoverer can be used
             */
            switch (mlEvent.type) {
            case MediaList.Event.ItemAdded:
                mEventListener.onMediaAdded(mlEvent.index, mlEvent.media);
                break;
            case MediaList.Event.ItemDeleted:
                mEventListener.onMediaRemoved(mlEvent.index, mlEvent.media);
                break;
            case MediaList.Event.EndReached:
                mEventListener.onBrowseEnd();
            }
        }
    };

    private final MediaList.EventListener mDiscovererMediaListEventListener = new MediaList.EventListener() {
        @Override
        public void onEvent(MediaList.Event event) {
            if (mEventListener == null)
                return;
            final MediaList.Event mlEvent = (MediaList.Event) event;
            int index = -1;

            /*
             * We use an intermediate array here since more than one MediaDiscoverer can be used
             */
            switch (mlEvent.type) {
            case MediaList.Event.ItemAdded:
                synchronized (MediaBrowser.this) {
                    /* one item can be found by severals discoverers */
                    boolean found = false;
                    for (Media media : mDiscovererMediaArray) {
                        if (media.getUri().equals(mlEvent.media.getUri())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        mDiscovererMediaArray.add(mlEvent.media);
                        index = mDiscovererMediaArray.size() - 1;
                    }
                }
                if (index != -1)
                    mEventListener.onMediaAdded(index, mlEvent.media);
                break;
            case MediaList.Event.ItemDeleted:
                synchronized (MediaBrowser.this) {
                    index = mDiscovererMediaArray.indexOf(mlEvent.media);
                    if (index != -1)
                        mDiscovererMediaArray.remove(index);
                }
                if (index != -1)
                    mEventListener.onMediaRemoved(index, mlEvent.media);
                break;
            case MediaList.Event.EndReached:
                mEventListener.onBrowseEnd();
            }
        }
    };
}

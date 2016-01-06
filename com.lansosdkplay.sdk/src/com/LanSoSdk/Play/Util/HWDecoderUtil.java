/*****************************************************************************
 * HWDecUtil.java
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

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Utility class that return the preferred hardware decoder from a list of known devices.
 */
public class HWDecoderUtil {

    public static final boolean HAS_SUBTITLES_SURFACE = AndroidVersion.isGingerbreadOrLater();

    public enum Decoder {
        UNKNOWN, NONE, OMX, MEDIACODEC, ALL
    }

    public enum AudioOutput {
        OPENSLES, AUDIOTRACK, ALL
    }

    private static class DecoderBySOC {
        public final String key;
        public final String value;
        public final Decoder dec;
        public DecoderBySOC(String key, String value, Decoder dec) {
            this.key = key;
            this.value = value;
            this.dec = dec;
        }
    }

    private static class AudioOutputBySOC {
        public final String key;
        public final String value;
        public final AudioOutput aout;

        public AudioOutputBySOC(String key, String value, AudioOutput aout) {
            this.key = key;
            this.value = value;
            this.aout = aout;
        }
    }

    private static final DecoderBySOC[] sBlacklistedDecoderBySOCList = new DecoderBySOC[] {
        /*
         * FIXME: Theses cpu crash in MediaCodec. We need to get hands on these devices in order to debug it.
         */
        new DecoderBySOC("ro.product.board", "msm8916", Decoder.NONE), //Samsung Galaxy Core Prime
        new DecoderBySOC("ro.product.board", "MSM8225", Decoder.NONE), //Samsung Galaxy Core
        new DecoderBySOC("ro.product.board", "hawaii", Decoder.NONE), // Samsung Galaxy Ace 4
    };

    private static final DecoderBySOC[] sDecoderBySOCList = new DecoderBySOC[] {
        /*
         *  Put first devices you want to blacklist
         *  because theses devices can match the next rules.
         */
        new DecoderBySOC("ro.product.brand", "SEMC", Decoder.NONE), // Xperia S
        new DecoderBySOC("ro.board.platform", "msm7627", Decoder.NONE), // QCOM S1

        /*
         * Devices working on OMX
         */
        new DecoderBySOC("ro.board.platform", "omap3", Decoder.OMX), // Omap 3
        new DecoderBySOC("ro.board.platform", "rockchip", Decoder.OMX), // Rockchip RK29
        new DecoderBySOC("ro.board.platform", "rk29", Decoder.OMX), // Rockchip RK29
        new DecoderBySOC("ro.board.platform", "msm7630", Decoder.OMX), // QCOM S2
        new DecoderBySOC("ro.board.platform", "s5pc", Decoder.OMX), // Exynos 3
        new DecoderBySOC("ro.board.platform",  "montblanc", Decoder.OMX), // Montblanc
        new DecoderBySOC("ro.board.platform", "exdroid", Decoder.OMX), // Allwinner A31
        new DecoderBySOC("ro.board.platform", "sun6i", Decoder.OMX), // Allwinner A31

        /*
         * Devices working only on Mediacodec
         */
        new DecoderBySOC("ro.board.platform", "exynos4", Decoder.MEDIACODEC), // Exynos 4 (Samsung Galaxy S2/S3)

        /*
         * Devices working on Mediacodec and OMX
         */
        new DecoderBySOC("ro.board.platform", "omap4", Decoder.ALL), // Omap 4
        new DecoderBySOC("ro.board.platform", "tegra", Decoder.ALL), // Tegra 2 & 3
        new DecoderBySOC("ro.board.platform", "tegra3", Decoder.ALL), // Tegra 3
        new DecoderBySOC("ro.board.platform", "msm8660", Decoder.ALL), // QCOM S3
        new DecoderBySOC("ro.board.platform", "exynos5", Decoder.ALL), // Exynos 5 (Samsung Galaxy S4)
        new DecoderBySOC("ro.board.platform", "rk30", Decoder.ALL), // Rockchip RK30
        new DecoderBySOC("ro.board.platform", "rk31", Decoder.ALL), // Rockchip RK31
        new DecoderBySOC("ro.board.platform", "mv88de3100", Decoder.ALL), // Marvell ARMADA 1500

        new DecoderBySOC("ro.hardware", "mt83", Decoder.ALL), //MTK
    };

    private static final AudioOutputBySOC[] sAudioOutputBySOCList = new AudioOutputBySOC[] {
        /* getPlaybackHeadPosition returns an invalid position on Fire OS,
         * thus Audiotrack is not usable */
        new AudioOutputBySOC("ro.product.brand", "Amazon", AudioOutput.OPENSLES),
    };

    private static final HashMap<String, String> sSystemPropertyMap = new HashMap<String, String>();

    /**
     * @return the hardware decoder known to work for the running device
     * (Always return Dec.ALL after Android 4.3)
     */
    public static Decoder getDecoderFromDevice() {
        /*
         * Try first blacklisted decoders (for all android versions)
         */
        for (DecoderBySOC decBySOC : sBlacklistedDecoderBySOCList) {
            final String prop = getSystemPropertyCached(decBySOC.key);
            if (prop != null) {
                if (prop.contains(decBySOC.value))
                    return decBySOC.dec;
            }
        }
        /*
         * Always try MediaCodec after JellyBean MR2,
         * Try OMX or MediaCodec after HoneyComb depending on device properties.
         * Otherwise, use software decoder by default.
         */
        if (AndroidVersion.isJellyBeanMR2OrLater())
            return Decoder.ALL;
        else if (AndroidVersion.isHoneycombOrLater()) {
            for (DecoderBySOC decBySOC : sDecoderBySOCList) {
                final String prop = getSystemPropertyCached(decBySOC.key);
                if (prop != null) {
                    if (prop.contains(decBySOC.value))
                        return decBySOC.dec;
                }
            }
        }
        return Decoder.UNKNOWN;
    }

    /**
     * @return the audio output known to work for the running device
     * (By default, returns ALL, i.e AudioTrack + OpenSles)
     */
    public static AudioOutput getAudioOutputFromDevice() {
        if (!AndroidVersion.isGingerbreadOrLater()) {
            return AudioOutput.AUDIOTRACK;
        } else {
            for (AudioOutputBySOC aoutBySOC : sAudioOutputBySOCList) {
                final String prop = getSystemPropertyCached(aoutBySOC.key);
                if (prop != null) {
                    if (prop.contains(aoutBySOC.value))
                        return aoutBySOC.aout;
                }
            }
            return AudioOutput.ALL;
        }
    }

    private static String getSystemPropertyCached(String key) {
        String prop = sSystemPropertyMap.get(key);
        if (prop == null) {
            prop = getSystemProperty(key, "none");
            sSystemPropertyMap.put(key, prop);
        }
        return prop;
    }

    private static String getSystemProperty(String key, String def) {
        try {
            final ClassLoader cl = ClassLoader.getSystemClassLoader();
            final Class<?> SystemProperties = cl.loadClass("android.os.SystemProperties");
            final Class<?>[] paramTypes = new Class[] { String.class, String.class };
            final Method get = SystemProperties.getMethod("get", paramTypes);
            final Object[] params = new Object[] { key, def };
            return (String) get.invoke(SystemProperties, params);
        } catch (Exception e){
            return def;
        }
    }
}

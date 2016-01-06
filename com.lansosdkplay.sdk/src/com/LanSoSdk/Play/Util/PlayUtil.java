/*****************************************************************************
 * PlayUtil.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.LanSoSdk.Play.LibPlay;
import com.LanSoSdk.Play.Media;

public class PlayUtil {
    public final static String TAG = "Util";

    private static String errorMsg = null;
    private static boolean isCompatible = false;

    public static String getErrorMsg() {
        return errorMsg;
    }

    public static boolean hasCompatibleCPU(Context context) {
        // If already checked return cached result
        if (errorMsg != null || isCompatible) return isCompatible;

        final File lib = searchLibrary(context.getApplicationInfo());
        if (lib == null)
            return true;

        ElfData elf = readLib(lib);
        if (elf == null) {
            Log.e(TAG, "WARNING: Unable to read liblansosdkplay.so; cannot check device ABI!");
            Log.e(TAG, "WARNING: Cannot guarantee correct ABI for this build (may crash)!");
            return true;
        }

        String CPU_ABI = android.os.Build.CPU_ABI;
        String CPU_ABI2 = "none";
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) { // CPU_ABI2 since 2.2
            try {
                CPU_ABI2 = (String) android.os.Build.class.getDeclaredField("CPU_ABI2").get(null);
            } catch (Exception e) {
            }
        }

        final boolean elfHasX86 = elf.e_machine == EM_386 || elf.e_machine == EM_X86_64;
        final boolean elfHasArm = elf.e_machine == EM_ARM || elf.e_machine == EM_AARCH64;
        final boolean elfHasMips = elf.e_machine == EM_MIPS;
        final boolean elfIs64bits = elf.is64bits;

        Log.i(TAG, "ELF ABI = " + (elfHasArm ? "arm" : elfHasX86 ? "x86" : "mips") + ", " +
                (elfIs64bits ? "64bits" : "32bits"));
        Log.i(TAG, "ELF arch = " + elf.att_arch);
        Log.i(TAG, "ELF fpu = " + elf.att_fpu);
        
        
        boolean hasNeon = false, hasFpu = false, hasArmV6 = false,
                hasArmV7 = false, hasMips = false, hasX86 = false, is64bits = false;
        float bogoMIPS = -1;
        int processors = 0;

        if (CPU_ABI.equals("x86") ||
                CPU_ABI2.equals("x86")) {
            hasX86 = true;
        } else if (CPU_ABI.equals("x86_64") ||
                CPU_ABI2.equals("x86_64")) {
            hasX86 = true;
            is64bits = true;
        } else if (CPU_ABI.equals("armeabi-v7a") ||
                CPU_ABI2.equals("armeabi-v7a")) {
            hasArmV7 = true;
            hasArmV6 = true; /* Armv7 is backwards compatible to < v6 */
        } else if (CPU_ABI.equals("armeabi") ||
                CPU_ABI2.equals("armeabi")) {
            hasArmV6 = true;
        } else if (CPU_ABI.equals("arm64-v8a") ||
                CPU_ABI2.equals("arm64-v8a")) {
            hasNeon = true;
            hasArmV6 = true;
            hasArmV7 = true;
            is64bits = true;
        }
        
        
        FileReader fileReader = null;
        BufferedReader br = null;
        try {
            fileReader = new FileReader("/proc/cpuinfo");
            br = new BufferedReader(fileReader);
            String line;
            while ((line = br.readLine()) != null) {
                if (!hasArmV7 && line.contains("AArch64")) {
                    hasArmV7 = true;
                    hasArmV6 = true; /* Armv8 is backwards compatible to < v7 */
                }
                if (!hasArmV7 && line.contains("ARMv7")) {
                    hasArmV7 = true;
                    hasArmV6 = true; /* Armv7 is backwards compatible to < v6 */
                }
                if (!hasArmV7 && !hasArmV6 && line.contains("ARMv6"))
                    hasArmV6 = true;
                // "clflush size" is a x86-specific cpuinfo tag.
                // (see kernel sources arch/x86/kernel/cpu/proc.c)
                if (line.contains("clflush size"))
                    hasX86 = true;
                if (line.contains("GenuineIntel"))
                    hasX86 = true;
                // "microsecond timers" is specific to MIPS.
                // see arch/mips/kernel/proc.c
                if (line.contains("microsecond timers"))
                    hasMips = true;
                if (!hasNeon && (line.contains("neon") || line.contains("asimd")))
                    hasNeon = true;
                if (!hasFpu && (line.contains("vfp") || (line.contains("Features") && line.contains("fp"))))
                    hasFpu = true;
                if (line.startsWith("processor"))
                    processors++;
                if (bogoMIPS < 0 && line.toLowerCase(Locale.ENGLISH).contains("bogomips")) {
                    String[] bogo_parts = line.split(":");
                    try {
                        bogoMIPS = Float.parseFloat(bogo_parts[1].trim());
                    } catch (NumberFormatException e) {
                        bogoMIPS = -1; // invalid bogomips
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            errorMsg = "IOException whilst reading cpuinfo flags";
            isCompatible = false;
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                }
            }
        }
        if (processors == 0)
            processors = 1; // possibly borked cpuinfo?

        // Enforce proper architecture to prevent problems
        if (elfHasX86 && !hasX86) {
            errorMsg = "x86 build on non-x86 device";
            isCompatible = false;
            return false;
        } else if (elfHasArm && hasX86) {
            errorMsg = "ARM build on x86 device";
            isCompatible = false;
            return false;
        }

        if (elfHasMips && !hasMips) {
            errorMsg = "MIPS build on non-MIPS device";
            isCompatible = false;
            return false;
        } else if (elfHasArm && hasMips) {
            errorMsg = "ARM build on MIPS device";
            isCompatible = false;
            return false;
        }

        if (elf.e_machine == EM_ARM && elf.att_arch.startsWith("v7") && !hasArmV7) {
            errorMsg = "ARMv7 build on non-ARMv7 device";
            isCompatible = false;
            return false;
        }
        if (elf.e_machine == EM_ARM) {
            if (elf.att_arch.startsWith("v6") && !hasArmV6) {
                errorMsg = "ARMv6 build on non-ARMv6 device";
                isCompatible = false;
                return false;
            } else if (elf.att_fpu && !hasFpu) {
                errorMsg = "FPU-enabled build on non-FPU device";
                isCompatible = false;
                return false;
            }
        }
        if (elfIs64bits && !is64bits) {
            errorMsg = "64bits build on 32bits device";
            isCompatible = false;
        }

        float frequency = -1;
        fileReader = null;
        br = null;
        String line = "";
        try {
            fileReader = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
            br = new BufferedReader(fileReader);
            line = br.readLine();
            if (line != null)
                frequency = Float.parseFloat(line) / 1000.f; /* Convert to MHz */
        } catch (IOException ex) {
            Log.w(TAG, "Could not find maximum CPU frequency!");
        } catch (NumberFormatException e) {
            Log.w(TAG, "Could not parse maximum CPU frequency!");
            Log.w(TAG, "Failed to parse: " + line);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                }
            }
        }

        errorMsg = null;
        isCompatible = true;
        // Store into MachineSpecs
        machineSpecs = new MachineSpecs();
        machineSpecs.hasArmV6 = hasArmV6;
        machineSpecs.hasArmV7 = hasArmV7;
        machineSpecs.hasFpu = hasFpu;
        machineSpecs.hasMips = hasMips;
        machineSpecs.hasNeon = hasNeon;
        machineSpecs.hasX86 = hasX86;
        machineSpecs.is64bits = is64bits;
        machineSpecs.bogoMIPS = bogoMIPS;
        machineSpecs.processors = processors;
        machineSpecs.frequency = frequency;
        return true;
    }

    public static MachineSpecs getMachineSpecs() {
        return machineSpecs;
    }

    private static MachineSpecs machineSpecs = null;

    public static class MachineSpecs {
        public boolean hasNeon;
        public boolean hasFpu;
        public boolean hasArmV6;
        public boolean hasArmV7;
        public boolean hasMips;
        public boolean hasX86;
        public boolean is64bits;
        public float bogoMIPS;
        public int processors;
        public float frequency; /* in MHz */
    }

    private static final int EM_386 = 3;
    private static final int EM_MIPS = 8;
    private static final int EM_ARM = 40;
    private static final int EM_X86_64 = 62;
    private static final int EM_AARCH64 = 183;
    private static final int ELF_HEADER_SIZE = 52;
    private static final int SECTION_HEADER_SIZE = 40;
    private static final int SHT_ARM_ATTRIBUTES = 0x70000003;

    private static class ElfData {
        ByteOrder order;
        boolean is64bits;
        int e_machine;
        int e_shoff;
        int e_shnum;
        int sh_offset;
        int sh_size;
        String att_arch;
        boolean att_fpu;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static File searchLibrary(ApplicationInfo applicationInfo) {
        // Search for library path
        String[] libraryPaths;
        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            final String property = System.getProperty("java.library.path");
            libraryPaths = property.split(":");
        } else {
            libraryPaths = new String[1];
            if (AndroidVersion.isGingerbreadOrLater())
                libraryPaths[0] = applicationInfo.nativeLibraryDir;
            else
                libraryPaths[0] = applicationInfo.dataDir + "/lib";
        }
        if (libraryPaths[0] == null) {
            Log.e(TAG, "can't find library path");
            return null;
        }

        File lib;
        for (String libraryPath : libraryPaths) {
            lib = new File(libraryPath, "lansosdkplay.so");
            if (lib.exists() && lib.canRead())
                return lib;
        }
        Log.e(TAG, "WARNING: Can't find shared library");
        return null;
    }

    /** '*' prefix means it's unsupported */
    private final static String[] CPU_archs = {"*Pre-v4", "*v4", "*v4T",
            "v5T", "v5TE", "v5TEJ",
            "v6", "v6KZ", "v6T2", "v6K", "v7",
            "*v6-M", "*v6S-M", "*v7E-M", "*v8"};

    private static ElfData readLib(File file) {
        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile(file, "r");

            ElfData elf = new ElfData();
            if (!readHeader(in, elf))
                return null;

            switch (elf.e_machine) {
                case EM_386:
                case EM_MIPS:
                case EM_X86_64:
                case EM_AARCH64:
                    return elf;
                case EM_ARM:
                    in.close();
                    in = new RandomAccessFile(file, "r");
                    if (!readSection(in, elf))
                        return null;
                    in.close();
                    in = new RandomAccessFile(file, "r");
                    if (!readArmAttributes(in, elf))
                        return null;
                    break;
                default:
                    return null;
            }
            return elf;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    private static boolean readHeader(RandomAccessFile in, ElfData elf) throws IOException {
        // http://www.sco.com/developers/gabi/1998-04-29/ch4.eheader.html
        byte[] bytes = new byte[ELF_HEADER_SIZE];
        in.readFully(bytes);
        if (bytes[0] != 127 ||
                bytes[1] != 'E' ||
                bytes[2] != 'L' ||
                bytes[3] != 'F' ||
                (bytes[4] != 1 && bytes[4] != 2)) {
            Log.e(TAG, "ELF header invalid");
            return false;
        }

        elf.is64bits = bytes[4] == 2;
        elf.order = bytes[5] == 1
                ? ByteOrder.LITTLE_ENDIAN // ELFDATA2LSB
                : ByteOrder.BIG_ENDIAN;   // ELFDATA2MSB

        // wrap bytes in a ByteBuffer to force endianess
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(elf.order);

        elf.e_machine = buffer.getShort(18);    /* Architecture */
        elf.e_shoff = buffer.getInt(32);        /* Section header table file offset */
        elf.e_shnum = buffer.getShort(48);      /* Section header table entry count */
        return true;
    }

    private static boolean readSection(RandomAccessFile in, ElfData elf) throws IOException {
        byte[] bytes = new byte[SECTION_HEADER_SIZE];
        in.seek(elf.e_shoff);

        for (int i = 0; i < elf.e_shnum; ++i) {
            in.readFully(bytes);

            // wrap bytes in a ByteBuffer to force endianess
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(elf.order);

            int sh_type = buffer.getInt(4); /* Section type */
            if (sh_type != SHT_ARM_ATTRIBUTES)
                continue;

            elf.sh_offset = buffer.getInt(16);  /* Section file offset */
            elf.sh_size = buffer.getInt(20);    /* Section size in bytes */
            return true;
        }

        return false;
    }

    private static boolean readArmAttributes(RandomAccessFile in, ElfData elf) throws IOException {
        byte[] bytes = new byte[elf.sh_size];
        in.seek(elf.sh_offset);
        in.readFully(bytes);

        // wrap bytes in a ByteBuffer to force endianess
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(elf.order);

        //http://infocenter.arm.com/help/topic/com.arm.doc.ihi0044e/IHI0044E_aaelf.pdf
        //http://infocenter.arm.com/help/topic/com.arm.doc.ihi0045d/IHI0045D_ABI_addenda.pdf
        if (buffer.get() != 'A') // format-version
            return false;

        // sub-sections loop
        while (buffer.remaining() > 0) {
            int start_section = buffer.position();
            int length = buffer.getInt();
            String vendor = getString(buffer);
            if (vendor.equals("aeabi")) {
                // tags loop
                while (buffer.position() < start_section + length) {
                    int start = buffer.position();
                    int tag = buffer.get();
                    int size = buffer.getInt();
                    // skip if not Tag_File, we don't care about others
                    if (tag != 1) {
                        buffer.position(start + size);
                        continue;
                    }

                    // attributes loop
                    while (buffer.position() < start + size) {
                        tag = getUleb128(buffer);
                        if (tag == 6) { // CPU_arch
                            int arch = getUleb128(buffer);
                            elf.att_arch = CPU_archs[arch];
                        } else if (tag == 27) { // ABI_HardFP_use
                            getUleb128(buffer);
                            elf.att_fpu = true;
                        } else {
                            // string for 4=CPU_raw_name / 5=CPU_name / 32=compatibility
                            // string for >32 && odd tags
                            // uleb128 for other
                            tag %= 128;
                            if (tag == 4 || tag == 5 || tag == 32 || (tag > 32 && (tag & 1) != 0))
                                getString(buffer);
                            else
                                getUleb128(buffer);
                        }
                    }
                }
                break;
            }
        }
        return true;
    }

    private static String getString(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder(buffer.limit());
        while (buffer.remaining() > 0) {
            char c = (char) buffer.get();
            if (c == 0)
                break;
            sb.append(c);
        }
        return sb.toString();
    }

    private static int getUleb128(ByteBuffer buffer) {
        int ret = 0;
        int c;
        do {
            ret <<= 7;
            c = buffer.get();
            ret |= c & 0x7f;
        } while ((c & 0x80) > 0);

        return ret;
    }

    /**
     * Get a media thumbnail.
     * @return a bytearray with the RGBA thumbnail data inside.
     */
    public static byte[] getThumbnail(LibPlay libplay, Uri uri, int i_width, int i_height) {
        /* dvd thumbnails can work only with dvdsimple demux */
        if (uri.getLastPathSegment().endsWith(".iso"))
            uri = Uri.parse("dvdsimple://" + uri.getEncodedPath());
        final Media media = new Media(libplay, uri);
        byte[] bytes = getThumbnail(media, i_width, i_height);
        media.release();
        return bytes;
    }

    public static byte[] getThumbnail(Media media, int i_width, int i_height) {
        media.addOption(":no-audio");
        media.addOption(":no-spu");
        media.addOption(":no-osd");
        return nativeGetThumbnail(media, i_width, i_height);
    }

    private static native byte[] nativeGetThumbnail(Media media, int i_width, int i_height);
}

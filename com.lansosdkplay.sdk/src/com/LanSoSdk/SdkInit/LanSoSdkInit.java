package com.LanSoSdk.SdkInit;

import java.io.File;

import android.content.Context;
import android.util.Log;

public class LanSoSdkInit {
	
	/**
	 * 压缩包和 解压后的文件都
	 *  都放到 /data/data/packagename/files下面.
	 */
	 private static final String ARM_V7A_ARCHIVE="liblansosdkplay-v7a.so";
	 private static final String ARM64_ARCHIVE="liblansosdkplay-a64.so";
	 private static final String X86_64_ARCHIVE="liblansosdkplay-x64.so";
	 
	  private static String  ARCHIVE_NAME;
	  
	  
	  
	  private static final String[] LIBS_V7A = {"libanw.10.so","libanw.13.so","libanw.14.so","libanw.18.so","libanw.21.so","libiomx.10.so","libiomx.13.so","libiomx.14.so","liblansosdkplay.so"};
	  private static final String[] LIBS_A64 = {"libffmpeg.so", "libOMX.9.so", "libOMX.14.so", "libOMX.18.so"};
	  private static final String[] LIBS_X64 = {"libffmpeg.so", "libOMX.14.so"};
	  
	  private static String[]  LIBS_ARRAY;
	  
	  public static String LIBS_DIR;
	 
	 public static boolean LanSoSdkInit(Context ctx)
	 {
		 	LIBS_DIR=ExtractLib.getCopyedDir(ctx);
			String cpuAbi=CPU.getCpuABI();
			if(cpuAbi.equals("armeabi-v7a")){
				LIBS_ARRAY=LIBS_V7A;
				ARCHIVE_NAME=ARM_V7A_ARCHIVE;
				
			}else if(cpuAbi.equals("arm64-v8a")){
				LIBS_ARRAY=LIBS_A64;
				ARCHIVE_NAME=ARM64_ARCHIVE;
				
			}else if(cpuAbi.equals("x86_64")){
				LIBS_ARRAY=LIBS_X64;
				ARCHIVE_NAME=X86_64_ARCHIVE;
				
			}else{
				LIBS_ARRAY=LIBS_V7A;
				ARCHIVE_NAME=ARM_V7A_ARCHIVE;
				Log.w("LanSoSdk","this machine cannot know. we use armeabi-v7a libs...");
			}
			
			Log.i("LanSoSdk","LIBS_ARRAY."+LIBS_ARRAY+ARCHIVE_NAME);
			return libsHasExist(ctx) || doExtractLibs(ctx);
	 }
	 private static boolean libsHasExist(Context ctx)
	 {	
			if(LIBS_DIR!=null)
			{
				 for (String lib : LIBS_ARRAY) {
			           File fileLib = new File(LIBS_DIR, lib);
			             if(!fileLib.exists()){
			            	 return false;
			             }
			     }
				 return true;
			}else{
				
			}
			return false;
	 }
	 private static boolean doExtractLibs(Context ctx)
	 {
		 	String srcPath,dstPath;
		 	boolean isFileCopied = ExtractLib.copyLibToData(ctx, ARCHIVE_NAME,ARCHIVE_NAME);
			  if(isFileCopied)
			  {
				  srcPath= ExtractLib.getCopyedDir(ctx)+File.separator+ ARCHIVE_NAME;
				  dstPath=ExtractLib.getCopyedDir(ctx);

				  ExtractLib.extract7z(srcPath, dstPath);
				  return libsHasExist(ctx);
			  }else{
				  Log.i("LanSoSdk","isFileCopied----false");
			  }
			  return false;
	 }
}

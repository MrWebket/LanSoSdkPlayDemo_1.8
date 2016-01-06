package com.LanSoSdk.SdkInit;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class ExtractLib {
		private static final String TAG = "ExtractLib";
	
	
	 private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	 private static final int EOF = -1;
	 public static  String LIB_DIR;
	 
	 
	public static boolean extract7z(String filePath, String outPath)
	{
		File outDir = new File(outPath);
		if(!outDir.exists() || !outDir.isDirectory())
		{
			outDir.mkdirs();
		}
		return (ExtractLib.extractFile(filePath, outPath) == 1);
	}
	
	//JNI interface
	private static native int extractFile(String filePath, String outPath);
	
	static {
		System.loadLibrary("lansoinit");
	}
	
	public static boolean copyLibToData(Context context, String assetName, String outFileName) {
		
		File filesDirectory =context.getFilesDir();
		
		InputStream is;
		try {
			is = context.getAssets().open(assetName);
			final FileOutputStream os = new FileOutputStream(new File(filesDirectory, outFileName));
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			
			int n;
			while(EOF != (n = is.read(buffer))) {
				os.write(buffer, 0, n);
			}
			IOUtils.closeSilently(os);
			IOUtils.closeSilently(is);
			
			return true;
		} catch (IOException e) {
			Log.e("sno","issue in coping binary from assets to data. ", e);
		}
        return false;
	}
	public static  String getCopyedDir(Context context)
	{
		return context.getFilesDir().getAbsolutePath();
	}
}

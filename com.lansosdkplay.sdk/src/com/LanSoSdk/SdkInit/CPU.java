package com.LanSoSdk.SdkInit;

import android.os.Build;

public class CPU {

	
	public static String getCpuABI()
	{	
			String retStr=null;
			String CPU_ABI = android.os.Build.CPU_ABI;
		    String CPU_ABI2 = "none";
		    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) { // CPU_ABI2 since 2.2
		        try {
		            CPU_ABI2 = (String) android.os.Build.class.getDeclaredField("CPU_ABI2").get(null);
		        } catch (Exception e) {
		        }
		    }
	       if (CPU_ABI.equals("x86") || CPU_ABI2.equals("x86")) {
	    	   retStr="x86";
	    	   
	        } else if (CPU_ABI.equals("x86_64") || CPU_ABI2.equals("x86_64")) {
	        	retStr="x86_64";
	        	
	        } else if (CPU_ABI.equals("armeabi-v7a") ||CPU_ABI2.equals("armeabi-v7a")) {
	        	retStr="armeabi-v7a";
	        	
	        } else if (CPU_ABI.equals("armeabi") || CPU_ABI2.equals("armeabi")) {
	        	retStr="armeabi";
	        } else if (CPU_ABI.equals("arm64-v8a") || CPU_ABI2.equals("arm64-v8a")) {
	        	retStr="arm64-v8a";
	        }
		    return retStr;   
	}
}

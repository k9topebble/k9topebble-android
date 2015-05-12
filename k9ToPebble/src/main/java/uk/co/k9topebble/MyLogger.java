package uk.co.k9topebble;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class MyLogger {
	public static void d(String tag, String msg)
	{
		if (K9Defines.DEBUG_ENABLED)
		{
			Log.e(tag, msg);
		}
		
		if (K9Defines.DEBUG_FILE_ENABLED)
		{
			appendLog("DEBUG", tag, msg);
		}
	}	
	public static void e(String tag, String msg)
	{
		if (K9Defines.DEBUG_ENABLED)
		{
			Log.e(tag, msg);
		}
		if (K9Defines.DEBUG_FILE_ENABLED)
		{
			appendLog("ERROR", tag, msg);
		}
		}	
	public static void i(String tag, String msg)
	{
		if (K9Defines.DEBUG_ENABLED)
		{
			Log.i(tag, msg);
		}
		if (K9Defines.DEBUG_FILE_ENABLED)
		{
			appendLog("INFO", tag, msg);
		}
	}
	public static void w(String tag, String msg)
	{
		if (K9Defines.DEBUG_ENABLED)
		{
			Log.w(tag, msg);
		}
		if (K9Defines.DEBUG_FILE_ENABLED)
		{
			appendLog("WARN", tag, msg);
		}
	}
	public static void v(String tag, String msg)
	{
		if (K9Defines.DEBUG_ENABLED)
		{
			Log.v(tag, msg);
		}
		if (K9Defines.DEBUG_FILE_ENABLED)
		{
			appendLog("VERBOSE", tag, msg);
		}
	}
	
	public static File getFile()
	{
		String root = Environment.getExternalStorageDirectory().toString();
		File file = new File (root + "/k9topebble/log.txt");
		if (!file.exists())
		{
			try
			{
				File myDir = new File(root + "/k9topebble/");    
				myDir.mkdirs();
				file.createNewFile();
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return file;
	}

	public static void appendLog(String level, String tag, String text)
	{       
	   File logFile = getFile();

	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(level + ":" + tag + ":" + text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
}

package com.wangling.anypcadmin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ProgramSettings {

	private static final String PREFS_NAME = "ProgramSettings";
	
	public static final String STRING_REGKEY_NAME_CURRENTPEERLANG = "CurrentPeerLang";
	
	public static final String STRING_REGKEY_NAME_FILEOPENDIR = "FileOpenDir";
	
	public static final String STRING_REGKEY_NAME_FILESAVEASDIR = "FileSaveAsDir";
	
	public static final String STRING_REGKEY_NAME_FIRSTRUN = "FirstRun";
	
	public static final String STRING_REGKEY_NAME_HTTPSERVER = "HttpServer";
	
	public static final String STRING_REGKEY_NAME_NODEID  = "NodeId";
	
	public static final String STRING_REGKEY_NAME_NODEPASS  = "NodePass";
	
	public static final String STRING_REGKEY_NAME_REMEMBERPASS = "RememberPass";


	public static String GetSoftwareKeyValue(Context context, String keyName, String defValue)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getString(keyName, defValue);
	}
	
	public static int GetSoftwareKeyDwordValue(Context context, String keyName, int defValue)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getInt(keyName, defValue);
	}
	
	public static void SaveSoftwareKeyValue(Context context, String keyName, String value)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(keyName, value);
		editor.commit();
	}
	
	public static void SaveSoftwareKeyDwordValue(Context context, String keyName, int value)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt(keyName, value);
		editor.commit();
	}
}

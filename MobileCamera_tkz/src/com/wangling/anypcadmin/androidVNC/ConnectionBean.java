/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package com.wangling.anypcadmin.androidVNC;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ImageView.ScaleType;

import java.lang.Comparable;

/**
 * @author Michael A. MacDonald
 *
 */
public class ConnectionBean extends AbstractConnectionBean {
	public static final ConnectionBean newInstance=new ConnectionBean();
	private ConnectionBean()
	{
		set_Id(0);
		setAddress("");
		setPassword("");
		setKeepPassword(true);
		setNickname("");
		setUserName("");
		setPort(5900);
		setColorModel(COLORMODEL.C64.nameString());
		setScaleMode(ScaleType.MATRIX);
		setInputMode(VncCanvasActivity.TOUCH_ZOOM_MODE);
		setRepeaterId("");
		setMetaListId(1);
	}
	
	boolean isNew()
	{
		return get_Id()== 0;
	}
	
	ScaleType getScaleMode()
	{
		return ScaleType.valueOf(getScaleModeAsString());
	}
	
	void setScaleMode(ScaleType value)
	{
		setScaleModeAsString(value.toString());
	}
	
	@Override
	public String toString() {
		if ( isNew())
		{
			return "New";
		}
		return getNickname()+":"+getAddress()+":"+getPort();
	}
}

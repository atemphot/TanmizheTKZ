package com.unirst.cn;
/*
 * @(#)OriginatingApp.java	1.1
 *
 * Copyright (c) Nokia Corporation 2002
 *
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


public class OriginatingApp {

	private String m_strPhoneNumTo;
	private String m_strSubject;
	private String m_textContent;
	private String m_jpgFilePath;
	private String m_jpgFilePath2;
	
  public OriginatingApp(Context context, String strPhoneNumTo, String strSubject, String textContent, String jpgFilePath, String jpgFilePath2) {
	  
	  m_strPhoneNumTo = strPhoneNumTo;
	  m_strSubject = strSubject;
	  m_textContent = textContent;
	  m_jpgFilePath = jpgFilePath;
	  m_jpgFilePath2 = jpgFilePath2;
	  
    MMMessage mm = new MMMessage();
    SetMessage(mm);
    AddContents(mm);

    MMEncoder encoder=new MMEncoder();
    encoder.setMessage(mm);

    try {
      encoder.encodeMessage();
      byte[] out = encoder.getMessage();

      MMSender sender = new MMSender();
      
      sender.setMMSCURL(findApnMMSC(context));      
      sender.setProxyIp(findApnMMSPROXY(context));
      sender.setProxyPort(findApnMMSPORT(context));
      
		Log.e("Ms","Message sent to " + sender.getMMSCURL());
		sender.send(out);

    } catch (Exception e) {
     Log.e("Ms","e"+e.getMessage());
    }
  }

  private void SetMessage(MMMessage mm) {
    mm.setVersion(IMMConstants.MMS_VERSION_10);
    mm.setMessageType(IMMConstants.MESSAGE_TYPE_M_SEND_REQ);
    mm.setTransactionId("0000000066");
    mm.setDate(new Date(System.currentTimeMillis()));
    
    //mm.setFrom("+8618612345678/TYPE=PLMN");
    //mm.addToAddress("+8618675838452/TYPE=PLMN");
    
    m_strPhoneNumTo = m_strPhoneNumTo.replace(" ", ";");
    m_strPhoneNumTo = m_strPhoneNumTo.replace(",", ";");
    String[] numArray = m_strPhoneNumTo.split(";");
    for (int i = 0; i < numArray.length; i++)
    {
    	numArray[i] = numArray[i].trim();
    	if (false == numArray[i].equals("")) {
    		mm.addToAddress("" + numArray[i] + "/TYPE=PLMN");
    	}
    }
    
    mm.setDeliveryReport(true);
    mm.setReadReply(true);
    mm.setSenderVisibility(IMMConstants.SENDER_VISIBILITY_SHOW);
    mm.setSubject(m_strSubject);
    mm.setMessageClass(IMMConstants.MESSAGE_CLASS_PERSONAL);
    mm.setPriority(IMMConstants.PRIORITY_HIGH);
    mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_MIXED);
//    In case of multipart related message and a smil presentation available
//    mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_RELATED);
//    mm.setMultipartRelatedType(IMMConstants.CT_APPLICATION_SMIL);
    mm.setPresentationId("<A0>"); // where <A0> is the id of the content containing the SMIL presentation
  }

  private void AddContents(MMMessage mm) {
   
    // Adds text content
    MMContent part1 = new MMContent();
     
    
    byte[] buf1;
	try {
		buf1 = m_textContent.getBytes("UTF-8");
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	}
    part1.setContent(buf1, 0, buf1.length);
    part1.setContentId("<0>");
    part1.setType(IMMConstants.CT_TEXT_PLAIN);
    mm.addContent(part1);

    // Adds image content
    MMContent part2 = new MMContent();
    
    
    byte[] buf2 = readFile(m_jpgFilePath);
    if (null == buf2) {
    	return;
    }
   
    part2.setContent(buf2, 0, buf2.length);
    part2.setContentId("<1>");
    part2.setType(IMMConstants.CT_IMAGE_JPEG);
    mm.addContent(part2);

    
    if (null != m_jpgFilePath2)
    {
	    // Adds image2 content
	    MMContent part3 = new MMContent();
	    
	    
	    byte[] buf3 = readFile(m_jpgFilePath2);
	    if (null == buf3) {
	    	return;
	    }
	   
	    part3.setContent(buf3, 0, buf3.length);
	    part3.setContentId("<2>");
	    part3.setType(IMMConstants.CT_IMAGE_JPEG);
	    mm.addContent(part3);
    }
    
  }

  private byte[] readFile(String filename) {
    int fileSize=0;
    RandomAccessFile fileH=null;

    // Opens the file for reading.
    try {
      fileH = new RandomAccessFile(filename, "r");
      fileSize = (int) fileH.length();
    } catch (IOException ioErr) {
      System.err.println("Cannot find " + filename);
      System.err.println(ioErr);
      //System.exit(200);
      return null;
    }

    // allocates the buffer large enough to hold entire file
    byte[] buf = new byte[fileSize];

    // reads all bytes of file
    int i=0;
    try {
       while (true) {
         try {
           buf[i++] = fileH.readByte();
         } catch (EOFException e) {
          break;
         }
       }
    } catch (Exception e) {
    Log.e("Ms","ERROR in reading of file"+filename);
    }

    return buf;
  }

  private static String findApnMMSC(Context context){
			String ret_mmsc = "";
			Uri uri = Uri.parse("content://telephony/carriers");
			String[] projection = new String[]{"_id","apn","mmsc","type"};
			String selection = "current = 1";

			Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, null);
			if (cursor != null && cursor.moveToFirst())
			{
		        do{
		        	int id = cursor.getInt(cursor.getColumnIndex("_id"));
		        	String apn = cursor.getString(cursor.getColumnIndex("apn"));
		        	String mmsc = cursor.getString(cursor.getColumnIndex("mmsc"));
		        	String type = cursor.getString(cursor.getColumnIndex("type"));
		        	
		        	if (ret_mmsc.equals("") == true && mmsc.equals("") == false && mmsc.equalsIgnoreCase("null") == false){
		        		ret_mmsc = mmsc;
		        	}
		        	if(type.equalsIgnoreCase("mms") == true && mmsc.equals("") == false && mmsc.equalsIgnoreCase("null") == false){
		        		ret_mmsc = mmsc;
		        		break;
		        	}
		        }while(cursor.moveToNext());
			}
	        
	        return ret_mmsc;
	}
  
	private static String findApnMMSPROXY(Context context){
		String ret_mmsproxy = "";
		Uri uri = Uri.parse("content://telephony/carriers");
		String[] projection = new String[]{"_id","apn","mmsproxy","type"};
		String selection = "current = 1";

		Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, null);
		if (cursor != null && cursor.moveToFirst())
		{
	        do{
	        	int id = cursor.getInt(cursor.getColumnIndex("_id"));
	        	String apn = cursor.getString(cursor.getColumnIndex("apn"));
	        	String mmsproxy = cursor.getString(cursor.getColumnIndex("mmsproxy"));
	        	String type = cursor.getString(cursor.getColumnIndex("type"));
	        	
	        	if (ret_mmsproxy.equals("") == true && mmsproxy.equals("") == false && mmsproxy.equalsIgnoreCase("null") == false){
	        		ret_mmsproxy = mmsproxy;
	        	}
	        	if(type.equalsIgnoreCase("mms") == true && mmsproxy.equals("") == false && mmsproxy.equalsIgnoreCase("null") == false){
	        		ret_mmsproxy = mmsproxy;
	        		break;
	        	}
	        }while(cursor.moveToNext());
		}
      
      return ret_mmsproxy;
	}
  
	private static int findApnMMSPORT(Context context){
		int ret = 80;
		String ret_mmsport = "";
		Uri uri = Uri.parse("content://telephony/carriers");
		String[] projection = new String[]{"_id","apn","mmsport","type"};
		String selection = "current = 1";

		Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, null);
		if (cursor != null && cursor.moveToFirst())
		{
	        do{
	        	int id = cursor.getInt(cursor.getColumnIndex("_id"));
	        	String apn = cursor.getString(cursor.getColumnIndex("apn"));
	        	String mmsport = cursor.getString(cursor.getColumnIndex("mmsport"));
	        	String type = cursor.getString(cursor.getColumnIndex("type"));
	        	
	        	if (ret_mmsport.equals("") == true && mmsport.equals("") == false && mmsport.equalsIgnoreCase("null") == false){
	        		ret_mmsport = mmsport;
	        	}
	        	if(type.equalsIgnoreCase("mms") == true && mmsport.equals("") == false && mmsport.equalsIgnoreCase("null") == false){
	        		ret_mmsport = mmsport;
	        		break;
	        	}
	        }while(cursor.moveToNext());
		}
      
		try {
			ret = Integer.parseInt(ret_mmsport);
		} catch (NumberFormatException e) {	
			return 80;
		}
		
      return ret;
	}
	
	public static int findMmsApn(Context context)
	{
		String mms_apn = "";
		Uri uri = Uri.parse("content://telephony/carriers");
		String[] projection = new String[]{"_id","apn","mmsc","type"};
		String selection = "current = 1";

		Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, null);
		if (cursor != null && cursor.moveToFirst())
		{
	        do{
	        	int id = cursor.getInt(cursor.getColumnIndex("_id"));
	        	String apn = cursor.getString(cursor.getColumnIndex("apn"));
	        	String mmsc = cursor.getString(cursor.getColumnIndex("mmsc"));
	        	String type = cursor.getString(cursor.getColumnIndex("type"));
	        	
	        	if(type.equalsIgnoreCase("mms") == true){
	        		mms_apn = apn;
	        		break;
	        	}
	        }while(cursor.moveToNext());
		}
        
		int ret_id = -1;
		if (cursor != null && cursor.moveToFirst())
		{
	        do{
	        	int id = cursor.getInt(cursor.getColumnIndex("_id"));
	        	String apn = cursor.getString(cursor.getColumnIndex("apn"));
	        	String mmsc = cursor.getString(cursor.getColumnIndex("mmsc"));
	        	String type = cursor.getString(cursor.getColumnIndex("type"));
	        	
	        	if (ret_id == -1 
	        			&& mmsc.equals("") == false && mmsc.equalsIgnoreCase("null") == false
	        			&& apn.equals("") == false && apn.equalsIgnoreCase("null") == false 
	        			&& type.equalsIgnoreCase("mms") == false )
	        	{
	        		ret_id = id;
	        	}
	        	if(apn.equalsIgnoreCase(mms_apn) == true 
	        			&& apn.equals("") == false && apn.equalsIgnoreCase("null") == false
	        			&& type.equalsIgnoreCase("mms") == false )
	        	{
	        		ret_id = id;
	        		break;
	        	}
	        }while(cursor.moveToNext());
		}
		
        return ret_id;
	}
	
	public static int getPreferApn(Context context)
	{
		int apn_id = -1;
		
		try {
			Uri uri = Uri.parse("content://telephony/carriers/preferapn");
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor != null && cursor.moveToFirst())
			{
		       	apn_id = cursor.getInt(cursor.getColumnIndex("_id"));
			}
		} catch (Exception e) { e.printStackTrace(); }
		
		return apn_id;
	}
	
	public static void setPreferApn(Context context, int id)
	{
		try {
			Uri uri = Uri.parse("content://telephony/carriers/preferapn");
			ContentValues values = new ContentValues();
			values.put("apn_id", id);
	
			context.getContentResolver().update(uri, values, null, null);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static int deleteApn(Context context, String apn)
	{
		int ret = 0;
		try {
			Uri uri = Uri.parse("content://telephony/carriers");
			String selection = "apn=\'" + apn + "\'";
	
			ret = context.getContentResolver().delete(uri, selection, null);
		} catch (Exception e) { e.printStackTrace(); }
		return ret;
	}
}

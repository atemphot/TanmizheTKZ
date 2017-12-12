/*
 * @(#)MMSender.java	1.1
 *
 * Copyright (c) Nokia Corporation 2002 *
 */

package com.unirst.cn;

import java.net.*;
import java.io.*;
import java.util.*;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.net.http.AndroidHttpClient;
import android.util.Log;

/**
 * The MMSender class provides methods to send Multimedia Messages to a predefined
 * Multimedia Service Center (MMSC).
 *
 */

public class MMSender {

  private String m_sUrl; // the URL of the MMSC
  private String m_sProxyIp;
  private int m_nProxyPort;
  private Hashtable hHeader;

  public MMSender() {
    hHeader = new Hashtable();
  }

  /**
   * Add a new Header and Value in the HTTP Header
   *
   * @param header header name
   * @param value  header value
   */
  public void addHeader(String header,String value) {

    String str = (String)hHeader.get(header);
    if (str!=null)
      str+=","+value;
    else
      str = value;
    hHeader.put(header,str);
  }

  /**
   * Clear the HTTP Header
   *
   */
  public void clearHeader() {
    hHeader.clear();
  }

  /**
   * Sets the Multimedia Messaging Service Center (MMSC) address
   *
   * @param value the URL 
   */
  public void setMMSCURL(String value) {
    m_sUrl=value;
  }

  /**
   * Gets the Multimedia Messaging Service Center address.
   *
   * @return the address
   */
  public String getMMSCURL() {
    return m_sUrl;
  }
  
  
  public void setProxyIp(String value) {
	    m_sProxyIp=value;
  }
  
  public String getProxyIp() {
	    return m_sProxyIp;
  }
  
  
  public void setProxyPort(int value) {
	    m_nProxyPort=value;
  }

  public int getProxyPort() {
	    return m_nProxyPort;
  }
  
  
  /**
   * Sends a Multimedia Message having a MMMessage object
   *
   * @param mmMsg the Multimedia Message object
   */
  public void send(MMMessage mmMsg) throws MMSenderException{

    MMEncoder encoder=new MMEncoder();
    encoder.setMessage(mmMsg);
    try {
      encoder.encodeMessage();
    } catch (MMEncoderException e) {
      throw new MMSenderException("An error occurred encoding the Multimedia Message for sending.");
    }

    byte [] buf=encoder.getMessage();

    send(buf);
  }

  /**
   * Sends a Multimedia Message having an array of bytes representing the message.
   *
   * @param buf the array of bytes representing the Multimedia Message.
   */
  public boolean send(byte[] pdu) throws MMSenderException{

      final String HDR_KEY_ACCEPT = "Accept";
      final String HDR_VALUE_ACCEPT = "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";
      final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";
      final String HDR_VALUE_ACCEPT_LANGUAGE = "";

      String mmsUrl = getMMSCURL();
      String mmsProxy = getProxyIp();
      int mmsProxyPort = getProxyPort();

      AndroidHttpClient client = null;
      try { 
          URI hostUrl = new URI(mmsUrl);
          HttpHost target = new HttpHost(hostUrl.getHost(), 
                  hostUrl.getPort(), HttpHost.DEFAULT_SCHEME_NAME);
          client = AndroidHttpClient.newInstance("Android-Mms/2.0");
          HttpPost post = new HttpPost(mmsUrl);
          ByteArrayEntity entity = new ByteArrayEntity(pdu);
          entity.setContentType("application/vnd.wap.mms-message");
          post.setEntity(entity);
          post.addHeader(HDR_KEY_ACCEPT, HDR_VALUE_ACCEPT);
          post.addHeader(HDR_KEY_ACCEPT_LANGUAGE, HDR_VALUE_ACCEPT_LANGUAGE);

          HttpParams params = client.getParams();
          HttpProtocolParams.setContentCharset(params, "UTF-8");

          ConnRouteParams.setDefaultProxy(params, new HttpHost(mmsProxy, mmsProxyPort));
          HttpResponse response = client.execute(target, post);
          StatusLine status = response.getStatusLine();
          System.out.println("status : " + status.getStatusCode());
          if (status.getStatusCode() != 200) {
              throw new IOException("HTTP error: " + status.getReasonPhrase());
          }

          client.close();
          return true;
          
      } catch (Exception e) { 
          e.printStackTrace(); 
          Log.d("Ms", "Send MMS failed£∫"+e.getMessage()); 
          //∑¢ÀÕ ß∞‹¥¶¿Ì  
      }
      
	  if (null != client) {
		  client.close();
	  }
      return false; 
  }

  
}
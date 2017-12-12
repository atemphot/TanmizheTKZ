package com.wangling.remotephone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.unirst.cn.OriginatingApp;
import com.wangling.remotephone.MainListActivity.MainHandler;
import com.wangling.tkz.R;


public class PhoneNumActivity extends Activity {
	
	static final int UI_MSG_MESSAGEBOX = 1;
	static final int UI_MSG_MESSAGETIP = 2;
	
	class MainHandler extends Handler {
		
		public MainHandler() {
			
		}
		
		public MainHandler(Looper l) {
			super(l);
		}
		
		@Override
		public void handleMessage(Message msg) {
			
			int what = msg.what;
			
			switch(what)
			{
			case UI_MSG_MESSAGEBOX://obj
				SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), (String)(msg.obj));
				break;
				
			case UI_MSG_MESSAGETIP://obj
				SharedFuncLib.MyMessageTip(_instance, (String)(msg.obj));
				break;
				
			default:
				break;				
			}
			
			super.handleMessage(msg);
		}
	}

	private void sendMMS(String strPhoneNumTo, String strSubject, String textContent, String jpgFilePath, String jpgFilePath2)
    {
    	final String _strPhoneNumTo = strPhoneNumTo;
    	final String _strSubject = strSubject;
    	final String _textContent = textContent;
    	final String _jpgFilePath = jpgFilePath;
    	final String _jpgFilePath2 = jpgFilePath2;
    	
    	new Thread(){
            @Override
            public void run(){
    				Log.i("Ms", "Start MMS...");
    				
    				try {
    					int prefer_apn_id = OriginatingApp.getPreferApn(_instance);
    					int mms_apn_id = OriginatingApp.findMmsApn(_instance);
    					Log.d("Ms", "prefer_apn_id=" + prefer_apn_id + ", mms_apn_id=" + mms_apn_id);
    				} catch (Exception e) {  }
    				
    			    //if (prefer_apn_id != mms_apn_id && mms_apn_id != -1)
    			    //{
    			    //	OriginatingApp.setPreferApn(_instance, mms_apn_id);
    			    //}
    			    
    			    //ConnectivityManager connMgr = (ConnectivityManager)_instance.getSystemService(Context.CONNECTIVITY_SERVICE);
    			    //int result = connMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_MMS);
    			    
    				
    				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE); 
    			    boolean bWifiEnabled = wifiManager.isWifiEnabled();
    			    if (bWifiEnabled)
    			    {
    			    	wifiManager.setWifiEnabled(false);
    			    	while (WifiManager.WIFI_STATE_DISABLED != wifiManager.getWifiState())
    			    	{		    			    		
    			    		try {
								Thread.sleep(100);
							} catch (InterruptedException e) {  }
    			    	}
    			    }

    				try {
						Thread.sleep(500);
					} catch (InterruptedException e) {  }
    			    
    			    Log.d("Ms", "send--->");
    				OriginatingApp oa = new OriginatingApp(_instance, 
    						_strPhoneNumTo, _strSubject, _textContent, _jpgFilePath, _jpgFilePath2);
    				Log.d("Ms", "send done!!!");
    				
    				try {
						Thread.sleep(500);
					} catch (InterruptedException e) {  }
    				
					 
	   				if (bWifiEnabled)
	   			    {
	   					wifiManager.setWifiEnabled(true);
	   			    }
	   				else {//不使用Wifi数据连接的情况，就可能需要蜂窝数据连接，需要切换回原接入点
						 //if (prefer_apn_id != mms_apn_id && prefer_apn_id != -1 && mms_apn_id != -1)
		    			 //{
		    			 //   OriginatingApp.setPreferApn(_instance, prefer_apn_id);
		    			 //   try {
						//		Thread.sleep(8000);
						//	} catch (InterruptedException e) {  }
		    			 //}
	   				}
	   				
	   				String str = _instance.getResources().getString(R.string.msg_sendmms_done);
	   				Message send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, str);
		        	_instance.mMainHandler.sendMessage(send_msg);
            }
        }.start();
    }
    
	private static PhoneNumActivity _instance = null;//////////////////
	private MainHandler mMainHandler = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonenum);
        
        findViewById(R.id.help_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnHelp();
        	}
        });
        
        findViewById(R.id.sendmms_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnSendMMS();
        	}
        });
        
        findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnCancel();
        	}
        });
        
        
        _instance = this;
        mMainHandler = new MainHandler();
    }
    
    private void onBtnHelp()
    {
		Uri mUri = Uri.parse("http://ykz.e2eye.com/LocHome.php");
		Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
		startActivity(mIntent);
    }
    
    private void onBtnSendMMS()
    {
    	if (false == MainListActivity.GetUserActivated())
    	{
    		SharedFuncLib.MyMessageBox(this, getResources().getString(R.string.app_name), getResources().getString(R.string.msg_level_too_low_for_this_function));
    		return;
    	}
    	
    	EditText editPhoneNum = (EditText)findViewById(R.id.id_edit_target_phonenum);
    	String strPhoneNum = editPhoneNum.getText().toString();
    	EditText editEmailAddr = (EditText)findViewById(R.id.id_edit_noti_emailaddr);
    	String strEmailAddr = editEmailAddr.getText().toString();
    	
    	if (strPhoneNum == null || strPhoneNum.equals(""))
    	{
    		SharedFuncLib.MyMessageBox(this, 
    				getResources().getString(R.string.app_name), 
    				getResources().getString(R.string.msg_targe_phonenum_empty));
    		return;
    	}
    	
    	if (strEmailAddr == null || strEmailAddr.equals(""))
    	{
    		SharedFuncLib.MyMessageBox(this, 
    				getResources().getString(R.string.app_name), 
    				getResources().getString(R.string.msg_noti_emailaddr_empty));
    		return;
    	}
    	
    	try {
    		sendMMS(strPhoneNum, "MMS", "Two pictures for you!", 
    				"/data/data/" + getPackageName() + "/remotephone/timg.jpg", 
    				"/data/data/" + getPackageName() + "/remotephone/timg2.jpg");
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
    private void onBtnCancel()
    {
    	this.setResult(RESULT_CANCELED);
		this.finish();
    }
    
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {   
           
        if(keyCode == KeyEvent.KEYCODE_BACK){   
        	onBtnCancel();
            return true;   
        }else{         
            return super.onKeyDown(keyCode, event);   
        }   
    }
    
}

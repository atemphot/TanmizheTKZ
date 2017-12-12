package com.wangling.remotephone;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.wangling.anypcadmin.androidVNC.BitmapImplHint;
import com.wangling.anypcadmin.androidVNC.COLORMODEL;
import com.wangling.anypcadmin.androidVNC.ConnectionBean;
import com.wangling.anypcadmin.androidVNC.VncCanvasActivity;
import com.wangling.tkz.R;


public class MainListActivity extends ListActivity {
	
	
	static final int WORK_MSG_REFRESH = 1;
	static final int WORK_MSG_ON_CONNECTED = 2;
	
	class WorkerHandler extends Handler {
		
		public WorkerHandler() {
			
		}
		
		public WorkerHandler(Looper l) {
			super(l);
		}
		
		@Override
		public void handleMessage(Message msg) {
			
			int ret;
			Message send_msg = null;
			int what = msg.what;
			
			switch(what)
			{
			case WORK_MSG_REFRESH://obj:null
				
				send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_PROGRESS_SHOW, _instance.getResources().getString(R.string.msg_please_wait));
		    	_instance.mMainHandler.sendMessage(send_msg);
		    	
		    	_instance.m_nCurrentSelected = -1;
		    	_instance.m_nodesArray.clear();
		    	DoSearchServers(0, 100);//NODES_PER_PAGE
	    		send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_REFRESH_RESULT);
		    	_instance.mMainHandler.sendMessage(send_msg);
		    	
		    	send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_PROGRESS_CANCEL);
		    	_instance.mMainHandler.sendMessage(send_msg);
		    	
				break;
				
			case WORK_MSG_ON_CONNECTED://arg1:type, arg2:fhandle
				_instance.conn_type = msg.arg1;
				_instance.conn_fhandle = msg.arg2;
				
				//////////////////////////PROGRESS_SHOW
				String strMsgText = null;
				if (_instance.conn_type == SharedFuncLib.SOCKET_TYPE_TCP)
				{
					strMsgText = _instance.getResources().getString(R.string.msg_connect_checking_password_tcp);
				}
				else {
					strMsgText = _instance.getResources().getString(R.string.msg_connect_checking_password);
				}
		    	send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_PROGRESS_SHOW, strMsgText);
		    	_instance.mMainHandler.sendMessage(send_msg);
		    	
		    	String strPass = AppSettings.GetSoftwareKeyValue(_instance, 
		    			"" + _instance.m_nodesArray.get(_instance.m_nCurrentSelected).comments_id + AppSettings.STRING_REGKEY_NAME_CAM_PASSWORD, 
		    			"");
		    	if (false == strPass.equals("")) {
		    		strPass = SharedFuncLib.phpMd5(strPass);
		    	}
		    	
		    	int[] arrResults = new int[3];
		    	ret = SharedFuncLib.CtrlCmdHELLO(_instance.conn_type, _instance.conn_fhandle, strPass, arrResults);
		    	_instance.m_nodesArray.get(_instance.m_nCurrentSelected).func_flags = (byte)(arrResults[2]);
		    	if (0 == ret && SharedFuncLib.CTRLCMD_RESULT_OK == arrResults[0]) {
			    	//////////////////////////PROGRESS_CANCEL
			    	send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_PROGRESS_CANCEL);
			    	_instance.mMainHandler.sendMessage(send_msg);
		    		
		    		send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_CONNECT_RESULT);
		        	_instance.mMainHandler.sendMessage(send_msg);
		        	break;
		    	}
		    	
		    	strPass = SharedFuncLib.phpMd5("rouji");
		    	ret = SharedFuncLib.CtrlCmdHELLO(_instance.conn_type, _instance.conn_fhandle, strPass, arrResults);
		    	_instance.m_nodesArray.get(_instance.m_nCurrentSelected).func_flags = (byte)(arrResults[2]);
		    	
		    	//////////////////////////PROGRESS_CANCEL
		    	send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_PROGRESS_CANCEL);
		    	_instance.mMainHandler.sendMessage(send_msg);
		    	
		    	if (0 != ret) {
		    		send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGEBOX, _instance.getResources().getString(R.string.msg_connect_checking_password_failed1));
		        	_instance.mMainHandler.sendMessage(send_msg);
		        	DoDisconnect();
		    	}
		    	else if (SharedFuncLib.CTRLCMD_RESULT_NG == arrResults[0]) {
		    		send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, _instance.getResources().getString(R.string.msg_connect_checking_password_retry));
		        	_instance.mMainHandler.sendMessage(send_msg);
		        	
		        	//Show password dialog...
		        	send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_CONNECT_PASSDLG);
		        	_instance.mMainHandler.sendMessage(send_msg);
		    	}
		    	else {// OK, go to AvParam or AvPlay
    		       	send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_CONNECT_RESULT);
		        	_instance.mMainHandler.sendMessage(send_msg);
		    	}
				
				break;
				
			default:
				break;	
			}
			
			super.handleMessage(msg);
		}
	}
	
	
	static final int UI_MSG_AUTO_START = 0;
	static final int UI_MSG_MESSAGEBOX = 1;
	static final int UI_MSG_MESSAGETIP = 2;
	static final int UI_MSG_PROGRESS_SHOW = 3;
	static final int UI_MSG_PROGRESS_CANCEL = 4;
	static final int UI_MSG_REFRESH_RESULT = 5;
	static final int UI_MSG_CONNECT_PASSDLG = 6;
	static final int UI_MSG_CONNECT_RESULT = 7;
	
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
			case UI_MSG_AUTO_START:
				onBtnRefresh();
				break;
				
			case UI_MSG_MESSAGEBOX://obj
				SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), (String)(msg.obj));
				break;
				
			case UI_MSG_MESSAGETIP://obj
				SharedFuncLib.MyMessageTip(_instance, (String)(msg.obj));
				break;
				
			case UI_MSG_PROGRESS_SHOW://obj
				if (null == mProgressDialog) {
					mProgressDialog = new ProgressDialog(_instance);
					mProgressDialog.setCancelable(false);
					mProgressDialog.setMessage((String)(msg.obj));
					mProgressDialog.show();
				}
				else {
					mProgressDialog.setMessage((String)(msg.obj));
				}
				break;
			
			case UI_MSG_PROGRESS_CANCEL:
				if (null != mProgressDialog) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				break;
			
			case UI_MSG_REFRESH_RESULT:
				NodeListAdapter adapter = new NodeListAdapter(_instance, m_nodesArray);
				mListView.setAdapter(adapter);
				break;
			
			case UI_MSG_CONNECT_PASSDLG:
				final View textEntryView = LayoutInflater.from(_instance).inflate(  
		                R.layout.dlgpass, null);
		        final EditText editPass=(EditText)textEntryView.findViewById(R.id.edit_pass);
		        AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
		        builder.setCancelable(false);
		        builder.setTitle(_instance.getResources().getString(R.string.msg_connect_dlgpass_title));
		        builder.setView(textEntryView);
		        builder.setPositiveButton(_instance.getResources().getString(R.string.ui_ok_btn),
		                new DialogInterface.OnClickListener() {  
		                    public void onClick(DialogInterface dialog, int whichButton) {
		                    	
		                    	String strPass = editPass.getText().toString();
		        		    	if (false == strPass.equals("")) {
		        		    		strPass = SharedFuncLib.phpMd5(strPass);
		        		    	}
		        		    	
		        		    	int[] arrResults = new int[3];
		        		    	int ret = SharedFuncLib.CtrlCmdHELLO(_instance.conn_type, _instance.conn_fhandle, strPass, arrResults);
		        		    	_instance.m_nodesArray.get(_instance.m_nCurrentSelected).func_flags = (byte)(arrResults[2]);
		        		    	
		        		    	if (0 != ret) {
		        		    		Message send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, _instance.getResources().getString(R.string.msg_connect_checking_password_failed1));
		        		        	_instance.mMainHandler.sendMessage(send_msg);
		        		        	DoDisconnect();
		        		        	onBtnRefresh();
		        		    	}
		        		    	else if (SharedFuncLib.CTRLCMD_RESULT_NG == arrResults[0]) {
		        		    		Message send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, _instance.getResources().getString(R.string.msg_connect_checking_password_retry));
		        		        	_instance.mMainHandler.sendMessage(send_msg);
		        		        	
		        		        	//Show password dialog...
		        		        	send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_CONNECT_PASSDLG);
		        		        	_instance.mMainHandler.sendMessage(send_msg);
		        		    	}
		        		    	else {// OK, save password and go to AvParam or AvPlay
		        		    		
		        		    		AppSettings.SaveSoftwareKeyValue(_instance, 
		        			    			"" + _instance.m_nodesArray.get(_instance.m_nCurrentSelected).comments_id + AppSettings.STRING_REGKEY_NAME_CAM_PASSWORD, 
		        			    			editPass.getText().toString());
		        		    		
		        		        	Message send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_CONNECT_RESULT);
		        		        	_instance.mMainHandler.sendMessage(send_msg);
		        		    	}
		        		    	
		        		    	dialog.dismiss();
		                    }
		                });
		        builder.setNegativeButton(_instance.getResources().getString(R.string.ui_cancel_btn),
		                new DialogInterface.OnClickListener() {  
		                    public void onClick(DialogInterface dialog, int whichButton) {  
		    		    		Message send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, _instance.getResources().getString(R.string.msg_connect_checking_password_failed2));
		    		        	_instance.mMainHandler.sendMessage(send_msg);
		    		        	DoDisconnect();
		    		        	onBtnRefresh();
		    		        	dialog.dismiss();
		                    }
		                });
		        builder.show();
				break;
			
			case UI_MSG_CONNECT_RESULT:
			  
			  String strUsername = AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_USERNAME, "");
			  	int ret = MainListActivity.MayiUseStart(MainListActivity.GetUserId(), strUsername, m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
			  	if (ret < 0) {
			  		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
	        		DoDisconnect();
			  		SharedFuncLib.MyMessageBox(_instance, 
		    				getResources().getString(R.string.app_name), 
		    				getResources().getString(R.string.msg_communication_error));
			  		onBtnRefresh();
	    			break;
			  	}
			  	else if (ret == 0) {
			  		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
	        		DoDisconnect();
			  		SharedFuncLib.MyMessageBox(_instance, 
		    				getResources().getString(R.string.app_name), 
		    				MainListActivity.MayiUseStartResult());
			  		onBtnRefresh();
	    			break;
			  	}
			  	else {
			  		_instance.use_id = ret;
			  	}
			  	
			  if (_instance.do_func == DO_FUNC_FULL)
			  {
				    if (false == m_nodesArray.get(m_nCurrentSelected).isAnypcNode())
				    {
			    		try {
							Thread.sleep(2000);// Wait remote VNC server...
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				    
				    Intent intent = new Intent(_instance, ConnectActivity.class);
	    	    	Bundle bundle = new Bundle();
	    	    	bundle.putInt("comments_id", m_nodesArray.get(m_nCurrentSelected).comments_id);
	    	    	bundle.putInt("conn_type", _instance.conn_type);
	    	    	bundle.putInt("conn_fhandle", _instance.conn_fhandle);
	    	    	bundle.putInt("audio_channels", m_nodesArray.get(m_nCurrentSelected).audio_channels);
	    	    	bundle.putInt("video_channels", m_nodesArray.get(m_nCurrentSelected).video_channels);
	    	    	bundle.putString("device_uuid", m_nodesArray.get(m_nCurrentSelected).device_uuid);
	    	    	bundle.putString("node_name", m_nodesArray.get(m_nCurrentSelected).node_name);
	    	    	bundle.putString("os_info", m_nodesArray.get(m_nCurrentSelected).os_info);
	    	    	bundle.putBoolean("bLanNode", m_nodesArray.get(m_nCurrentSelected).bLanNode);
	    	    	bundle.putString("ip_str", m_nodesArray.get(m_nCurrentSelected).ip_str);
	    	    	bundle.putString("pub_ip_str", m_nodesArray.get(m_nCurrentSelected).pub_ip_str);
	    	    	bundle.putByte("func_flags", m_nodesArray.get(m_nCurrentSelected).func_flags);
	    	    	intent.putExtras(bundle);
		        	startActivityForResult(intent, REQUEST_CODE_CONNECT);
			  }
			  else if (_instance.do_func == DO_FUNC_VNC)
			  {
			    	int wTcpPort = 5901;
			    	String szSysTempPass = "ykz123";
			    	if (m_nodesArray.get(m_nCurrentSelected).os_info.contains("anypc01#rouji.com")) {
			    		wTcpPort = 5900;
			    		szSysTempPass = "admin123!@#";
			    	}
				  
		    		if ((m_nodesArray.get(m_nCurrentSelected).func_flags & SharedFuncLib.FUNC_FLAGS_HASROOT) == 0)
		    		{
		    			MainListActivity.MayiUseEnd(_instance.use_id, MainListActivity.GetUserId(), strUsername, m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
		    			SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
		        		DoDisconnect();
		    			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_root_for_this_function));
		        		onBtnRefresh();
		    			break;
		    		}
		    		else if (((m_nodesArray.get(m_nCurrentSelected).func_flags & SharedFuncLib.FUNC_FLAGS_ACTIVATED) == 0 || false == MainListActivity.GetUserActivated())
		    				&& SharedFuncLib.getLowestLevelForVnc() > 0)
		    		{
		    			MainListActivity.MayiUseEnd(_instance.use_id, MainListActivity.GetUserId(), strUsername, m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
		    			SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
		        		DoDisconnect();
		    			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_level_too_low_for_this_function));
		        		onBtnRefresh();
		    			break;
		    		}
		    		else if ((m_nodesArray.get(m_nCurrentSelected).func_flags & SharedFuncLib.FUNC_FLAGS_VNC) == 0)
		    		{
		    			MainListActivity.MayiUseEnd(_instance.use_id, MainListActivity.GetUserId(), strUsername, m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
		    			SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
		        		DoDisconnect();
		    			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_support_this_function));
		        		onBtnRefresh();
		    			break;
		    		}
				    
				    if (false == m_nodesArray.get(m_nCurrentSelected).isAnypcNode())
				    {
			    		try {
							Thread.sleep(3000);// Wait remote VNC server...
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
		    		
		    		if (SharedFuncLib.CtrlCmdPROXY(_instance.conn_type, _instance.conn_fhandle, wTcpPort) < 0)
		    		{
		    			MainListActivity.MayiUseEnd(_instance.use_id, MainListActivity.GetUserId(), strUsername, m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
		    			SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
		        		DoDisconnect();
		    			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_vnc_connection_aborted));
		        		onBtnRefresh();
		    			break;
		    		}
		    		
					_instance.proxy_tcp_port += 1;
					SharedFuncLib.ProxyClientClearQuitFlag();
					if (_instance.conn_type == SharedFuncLib.SOCKET_TYPE_TCP)
					{
						SharedFuncLib.ProxyClientStartProxy(_instance.conn_type, _instance.conn_fhandle, false, _instance.proxy_tcp_port);
					}
					else if (_instance.conn_type == SharedFuncLib.SOCKET_TYPE_UDT)
					{
						SharedFuncLib.ProxyClientStartSlave(_instance.proxy_tcp_port);
					}
					
					ConnectionBean selected = ConnectionBean.newInstance;
					selected.setNickname("VNC-Server");
		    		selected.setAddress("127.0.0.1");
		    		selected.setPort(_instance.proxy_tcp_port);
		    		selected.setPassword(szSysTempPass);
		    		selected.setInputMode("TOUCH_ZOOM_MODE");
		    		selected.setColorModel((COLORMODEL.C256).nameString());
		    		selected.setFollowMouse(true);
		    		selected.setForceFull(BitmapImplHint.AUTO);
		    		
		    		Intent intent = new Intent(_instance, VncCanvasActivity.class);
	    	    	Bundle bundle = new Bundle();
	    	    	bundle.putInt("comments_id", m_nodesArray.get(m_nCurrentSelected).comments_id);
	    	    	bundle.putInt("conn_type", _instance.conn_type);
	    	    	bundle.putInt("conn_fhandle", _instance.conn_fhandle);
	    	    	bundle.putString("device_uuid", m_nodesArray.get(m_nCurrentSelected).device_uuid);
	    	    	intent.putExtras(bundle);
		        	startActivityForResult(intent, REQUEST_CODE_VNCVIEWER);
			  }
			  else if (_instance.do_func == DO_FUNC_AV)
			  {////////
		    		//if (((m_nodesArray.get(m_nCurrentSelected).func_flags & SharedFuncLib.FUNC_FLAGS_ACTIVATED) == 0 || false == MainListActivity.GetUserActivated())
		    		//		&& SharedFuncLib.getLowestLevelForAv() > 0)
				    if (AvPlayActivity.m_bAvPlayRestricted)
		    		{
				    	MainListActivity.MayiUseEnd(_instance.use_id, MainListActivity.GetUserId(), strUsername, m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
		        		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
		        		DoDisconnect();
		    			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_level_too_low_for_this_function));
		        		onBtnRefresh();
		    			break;
		    		}
		    		else if ((m_nodesArray.get(m_nCurrentSelected).func_flags & SharedFuncLib.FUNC_FLAGS_AV) == 0)
		    		{
		    			MainListActivity.MayiUseEnd(_instance.use_id, MainListActivity.GetUserId(), strUsername, m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
		        		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
		        		DoDisconnect();
		    			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_support_this_function));
		        		onBtnRefresh();
		    			break;
		    		}
				  
	    		//if (0 == AppSettings.GetSoftwareKeyDwordValue(_instance, "" + m_nodesArray.get(m_nCurrentSelected).comments_id + AppSettings.STRING_REGKEY_NAME_CAM_AVPARAM_FRAMERATE, 0))
	    		if (true)
		    	{
	    	    	Intent intent = new Intent(_instance, AvParamActivity.class);
	    	    	Bundle bundle = new Bundle();
	    	    	bundle.putInt("comments_id", m_nodesArray.get(m_nCurrentSelected).comments_id);
	    	    	bundle.putInt("conn_type", _instance.conn_type);
	    	    	bundle.putInt("audio_channels", m_nodesArray.get(m_nCurrentSelected).audio_channels);
	    	    	bundle.putInt("video_channels", m_nodesArray.get(m_nCurrentSelected).video_channels);
	    	    	intent.putExtras(bundle);
	    	    	startActivityForResult(intent, REQUEST_CODE_AVPARAM);
	    		}
			  }////////
			  
			  mMainHandler.removeCallbacks(auto_send_ctrlnull_runnable);
			  mMainHandler.postDelayed(auto_send_ctrlnull_runnable, 25000);
			  
			  mMainHandler.removeCallbacks(auto_use_refresh_runnable);
			  mMainHandler.postDelayed(auto_use_refresh_runnable, MainListActivity.GetUseRegisterPeriod() * 1000);
			  
				break;
				
			default:
				break;				
			}
			
			super.handleMessage(msg);
		}
	}
	
	
	final Runnable auto_send_ctrlnull_runnable = new Runnable() {
		public void run() {
			try {
				//此处本来是要发送CMD_CODE_NULL，由于受控端低版本不能处理CMD_CODE_NULL，暂时屏蔽
				//SharedFuncLib.CtrlCmdSendNULL(conn_type, conn_fhandle);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (_instance != null) {
				mMainHandler.postDelayed(auto_send_ctrlnull_runnable, 25000);
			}
		}
	};
	
	final Runnable auto_use_refresh_runnable = new Runnable() {
		public void run() {
			try {
				if (_instance.use_id > 0) {
					MainListActivity.MayiUseRefresh(_instance.use_id);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (_instance != null) {
				mMainHandler.postDelayed(auto_use_refresh_runnable, MainListActivity.GetUseRegisterPeriod() * 1000);
			}
		}
	};
	
	private static MainListActivity _instance = null;//////////////////
	
	private static final String TAG = "MainListActivity";
	private PowerManager.WakeLock m_wl = null;
	private WorkerHandler mWorkerHandler = null;
	private MainHandler mMainHandler = null;
	
	private ProgressDialog mProgressDialog = null;
	private int conn_type;
	private int conn_fhandle;
	private int proxy_tcp_port = 10000;
	
	private int use_id;
	private boolean isGuajiNode;
	private int do_func;
	
	public static final int DO_FUNC_FULL = 0;
	public static final int DO_FUNC_AV = 1;
	public static final int DO_FUNC_VNC = 2;
	public static final int DO_FUNC_FT = 3;
	public static final int DO_FUNC_DP = 4;//DroidPlanner
	
	private ListView mListView = null;
	private List<ANYPC_NODE> m_nodesArray = null;
	private int m_nCurrentSelected;
	
	
	public static int GetNodesCount()
	{
		if (_instance == null) {
    		return 0;
    	}
    	
    	return _instance.m_nodesArray.size();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlist);
        
        
        Log.d(TAG, "Acquiring wake lock");
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        m_wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MainListActivity PARTIAL_WAKE_LOCK");
        m_wl.acquire();
        
        
        Worker worker = new Worker("MainListActivity Worker");
        mWorkerHandler = new WorkerHandler(worker.getLooper());
        mMainHandler = new MainHandler();
        
        
        mListView = this.getListView();
        m_nodesArray = new ArrayList<ANYPC_NODE>();
    	m_nCurrentSelected = -1;
        
     
    	findViewById(R.id.intrusion_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnIntrusion();
        	}
        });
    	findViewById(R.id.phonenum_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnPhoneNum();
        	}
        });
        findViewById(R.id.addcam_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnAddCam();
        	}
        });
        findViewById(R.id.refresh_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnRefresh();
        	}
        });
        findViewById(R.id.exitapp_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnExitApp();
        	}
        });
        
        
        
        _instance = this;//////////////////
        
        String strUsername = AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_USERNAME, "");
        String strNickname = MainListActivity.GetNickName();
        if (strUsername.equals(strNickname)) {
        	((TextView)findViewById(R.id.id_text_username_val)).setText(strUsername);
        }
        else {
        	((TextView)findViewById(R.id.id_text_username_val)).setText(strUsername + "(" + strNickname + ")");
        }
        
        ((TextView)findViewById(R.id.id_text_change_nick)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        findViewById(R.id.id_text_change_nick).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Uri mUri = Uri.parse("http://" + MainListActivity.GetMayiServer() + "/toukuizhe/001PreChangeNick.php?username="+ AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_USERNAME, "") + "&random=" + System.currentTimeMillis()/1000);
        		Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
        		startActivity(mIntent);
        	}
        });
        
        ((TextView)findViewById(R.id.id_text_change_pass)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        findViewById(R.id.id_text_change_pass).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Uri mUri = Uri.parse("http://" + MainListActivity.GetMayiServer() + "/toukuizhe/001PreChangePass.php?username="+ AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_USERNAME, "") + "&random=" + System.currentTimeMillis()/1000);
        		Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
        		startActivity(mIntent);
        	}
        });
        
        if (MainListActivity.GetUserActivated()) {
        	((TextView)findViewById(R.id.id_text_validate_time_val)).setText(MainListActivity.GetExpireTime());
        }
        else {
        	((TextView)findViewById(R.id.id_text_validate_time_val)).setText(getResources().getString(R.string.ui_text_not_vip));
        }
        
        ((TextView)findViewById(R.id.id_text_userscore_val)).setText("" + MainListActivity.GetUserScore());
        
        ((TextView)findViewById(R.id.id_text_score_detail)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        findViewById(R.id.id_text_score_detail).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Uri mUri = Uri.parse("http://" + MainListActivity.GetMayiServer() + "/toukuizhe/001ScoreDetail.php?username="+ AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_USERNAME, "") + "&random=" + System.currentTimeMillis()/1000);
        		Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
        		startActivity(mIntent);
        	}
        });
        
        ((TextView)findViewById(R.id.id_text_score_cash)).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        findViewById(R.id.id_text_score_cash).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Uri mUri = Uri.parse("http://" + MainListActivity.GetMayiServer() + "/toukuizhe/001ScoreCash.php?username="+ AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_USERNAME, "") + "&random=" + System.currentTimeMillis()/1000);
        		Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
        		startActivity(mIntent);
        	}
        });
        
        
        
        mMainHandler.sendEmptyMessageDelayed(UI_MSG_AUTO_START, 500);
        
        /* Start native... */
        SetThisObject();
        StartNative("utf8", getResources().getString(R.string.app_lang));
        
        int ver = SharedFuncLib.getAppVersion();
        String ver_str = String.format("%s Ver %d.%d.%d", 
        		_instance.getResources().getString(R.string.app_name),
        		(ver & 0xff000000)>>24,
        		(ver & 0x00ff0000)>>16,
        		(ver & 0x0000ff00)>>8);
        _instance.setTitle(ver_str);
    }
    
    @Override
    protected void onDestroy() {
    	
    	mMainHandler.removeCallbacks(auto_send_ctrlnull_runnable);
    	mMainHandler.removeCallbacks(auto_use_refresh_runnable);
    	
    	_instance = null;//////////////////
    	
    	if (null != mProgressDialog) {
    		mProgressDialog.dismiss();
    		mProgressDialog = null;
    	}
    	
        Log.d(TAG, "Release wake lock");
    	m_wl.release();
    	
    	mWorkerHandler.getLooper().quit();
    	
    	m_nCurrentSelected = -1;
    	m_nodesArray.clear();    	
    	
    	
    	StopNative();
    	
    	super.onDestroy();
    }
    
    private void onBtnIntrusion()
    {
    	Intent intent = new Intent(this, IntrusionActivity.class);
    	startActivityForResult(intent, REQUEST_CODE_INTRUSION);
    }
    
    private void onBtnPhoneNum()
    {
    	Intent intent = new Intent(this, PhoneNumActivity.class);
    	startActivity(intent);
    }
    
    private void onBtnAddCam()
    {
    	Intent intent = new Intent(this, AddCamActivity.class);
    	startActivityForResult(intent, REQUEST_CODE_ADDCAM);
    }
    
    private void onBtnRefresh()
    {
    	Message send_msg = mWorkerHandler.obtainMessage(WORK_MSG_REFRESH);
    	mWorkerHandler.sendMessage(send_msg);
    }
    
    private void onBtnExitApp()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
        builder.setTitle(_instance.getResources().getString(R.string.app_name));
        builder.setMessage(_instance.getResources().getString(R.string.msg_exit_app_or_not));
        builder.setPositiveButton(_instance.getResources().getString(R.string.ui_yes_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button1");
                    	dialog.dismiss();
                    	
                    	MainListActivity.MayiLogoutUser(MainListActivity.GetUserId());
                    	try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {}
                    	
                    	_instance.finish();
                    	android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
        builder.setNegativeButton(_instance.getResources().getString(R.string.ui_no_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button3");
                    	dialog.dismiss();
                    }
                });
        builder.show();
    }
    
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {   
           
        if(keyCode == KeyEvent.KEYCODE_BACK){   
        	onBtnExitApp();
            return true;   
        }else{         
            return super.onKeyDown(keyCode, event);   
        }   
    }   
    
    
    static final int REQUEST_CODE_INTRUSION = 0;
    static final int REQUEST_CODE_ADDCAM = 1;
    static final int REQUEST_CODE_AVPARAM = 2;
    static final int REQUEST_CODE_AVPLAY = 3;
    static final int REQUEST_CODE_VNCVIEWER = 4;
    static final int REQUEST_CODE_DROIDPLANNER = 5;
    static final int REQUEST_CODE_CONNECT = 6;
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	switch (requestCode)
    	{
    	case REQUEST_CODE_INTRUSION:
    		if (RESULT_OK == resultCode)
    		{
    			onBtnRefresh();
    		}
    		break;
    		
    	case REQUEST_CODE_ADDCAM:
    		if (RESULT_OK == resultCode)
    		{
    			onBtnRefresh();
    		}
    		break;
    		
    	case REQUEST_CODE_AVPARAM:
    		if (RESULT_OK == resultCode)
    		{
    			Intent intent = null;
    			if (m_nodesArray.get(m_nCurrentSelected).isRobNode()
    				|| m_nodesArray.get(m_nCurrentSelected).isUavNode()) {
    				intent = new Intent(_instance, AvCtrlActivity.class);
    			}
    			else {
    				intent = new Intent(_instance, AvPlayActivity.class);
    			}
    	    	Bundle bundle = new Bundle();
    	    	bundle.putInt("comments_id", m_nodesArray.get(m_nCurrentSelected).comments_id);
    	    	bundle.putInt("conn_type", _instance.conn_type);
    	    	bundle.putInt("conn_fhandle", _instance.conn_fhandle);
    	    	bundle.putInt("audio_channels", m_nodesArray.get(m_nCurrentSelected).audio_channels);
    	    	bundle.putInt("video_channels", m_nodesArray.get(m_nCurrentSelected).video_channels);
    	    	bundle.putString("device_uuid", m_nodesArray.get(m_nCurrentSelected).device_uuid);
    	    	bundle.putByte("func_flags", m_nodesArray.get(m_nCurrentSelected).func_flags);
    	    	intent.putExtras(bundle);
    	    	startActivityForResult(intent, REQUEST_CODE_AVPLAY);
    		}
    		else {
    			mMainHandler.removeCallbacks(auto_send_ctrlnull_runnable);
    			mMainHandler.removeCallbacks(auto_use_refresh_runnable);
    			MainListActivity.MayiUseEnd(_instance.use_id, MainListActivity.GetUserId(), AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_USERNAME, ""), m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
    			SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
        		DoDisconnect();
        		onBtnRefresh();
    		}
    		break;
    		
    	case REQUEST_CODE_AVPLAY:
    		mMainHandler.removeCallbacks(auto_send_ctrlnull_runnable);
    		mMainHandler.removeCallbacks(auto_use_refresh_runnable);
    		MainListActivity.MayiUseEnd(_instance.use_id, MainListActivity.GetUserId(), AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_USERNAME, ""), m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
    		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
    		DoDisconnect();
    		onBtnRefresh();
    		break;
    		
    	case REQUEST_CODE_CONNECT:
    	case REQUEST_CODE_VNCVIEWER:
    	case REQUEST_CODE_DROIDPLANNER:
    		SharedFuncLib.ProxyClientAllQuit();
    		
    		mMainHandler.removeCallbacks(auto_send_ctrlnull_runnable);
    		mMainHandler.removeCallbacks(auto_use_refresh_runnable);
    		MainListActivity.MayiUseEnd(_instance.use_id, MainListActivity.GetUserId(), AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_USERNAME, ""), m_nodesArray.get(m_nCurrentSelected).comments_id, 0, "");
    		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
    		DoDisconnect();
    		onBtnRefresh();
    		break;
    	}
    }

    @Override 
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
    	m_nCurrentSelected = position;
    	if (m_nCurrentSelected >= 0) {
    		if (m_nodesArray.get(m_nCurrentSelected).isOnline())
    		{
    			if (m_nodesArray.get(m_nCurrentSelected).isRobNode() 
    				|| m_nodesArray.get(m_nCurrentSelected).isUavNode())
    			{
    				//this.dismissDialog(id);
    				this.showDialog(DIALOG_ONLINE_2_OPERATIONS);
    			}
    			else {
    				//this.dismissDialog(id);
    				this.showDialog(DIALOG_ONLINE_OPERATIONS);
    			}
    		}
    		else
    		{
    			//this.dismissDialog(id);
    			this.showDialog(DIALOG_OFFLINE_OPERATIONS);
    		}
    	}
    }
    
    
    static final int DIALOG_OFFLINE_OPERATIONS = 0;
    static final int DIALOG_ONLINE_OPERATIONS = 1;
    static final int DIALOG_ONLINE_2_OPERATIONS = 2;
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
		Builder builder = new AlertDialog.Builder(_instance); 
    	Dialog dialog = null;
    	
    	switch (id)
    	{
    	case DIALOG_ONLINE_OPERATIONS:  
    		builder.setItems(R.array.array_select_operation_items, new DialogInterface.OnClickListener(){
	    			public void onClick(DialogInterface dialog, int which)
	    			{
	    				if (0 == which)
	    				{
	    					do_func = DO_FUNC_FULL;
	    					onMenuItemConnect();
	    				}
	    				else if (1 == which)
	    				{
	    					do_func = DO_FUNC_FULL;
	    					onMenuItemConnectTcp();
	    				}
	    				else if (2 == which)
	    				{
	    					do_func = DO_FUNC_AV;
	    					onMenuItemConnectTcp();
	    				}
	    				else if (3 == which)
	    				{
	    					do_func = DO_FUNC_VNC;
	    					onMenuItemConnectTcp();
	    				}
	    				else if (4 == which)
	    				{
	    					onMenuItemLocation();
	    				}
	    				else if (5 == which)
	    				{
	    					onMenuItemComments();
	    				}
	    				else if (6 == which)
	    				{
	    					onMenuItemRemove();
	    				}
	    			}
    			});
    		dialog = builder.create();
    		break;
    		
    	case DIALOG_ONLINE_2_OPERATIONS:  
    		builder.setItems(R.array.array_select_operation_2_items, new DialogInterface.OnClickListener(){
	    			public void onClick(DialogInterface dialog, int which)
	    			{
	    				if (0 == which)
	    				{
	    					do_func = DO_FUNC_AV;
	    					onMenuItemConnect();
	    				}
	    				else if (1 == which)
	    				{
	    					do_func = DO_FUNC_AV;
	    					onMenuItemConnectTcp();
	    				}
	    				else if (2 == which)
	    				{
	    					do_func = DO_FUNC_DP;
	    					onMenuItemConnect();
	    				}
	    				else if (3 == which)
	    				{
	    					do_func = DO_FUNC_DP;
	    					onMenuItemConnectTcp();
	    				}
	    				else if (4 == which)
	    				{
	    					onMenuItemLocation();
	    				}
	    				else if (5 == which)
	    				{
	    					onMenuItemSetParams();
	    				}
	    				else if (6 == which)
	    				{
	    					onMenuItemRemove();
	    				}
	    			}
    			});
    		dialog = builder.create();
    		break;
    		
    	case DIALOG_OFFLINE_OPERATIONS:
    		builder.setItems(R.array.array_select_operation_offline_items, new DialogInterface.OnClickListener(){
	    			public void onClick(DialogInterface dialog, int which)
	    			{
	    				if (0 == which)
	    				{
	    					onMenuItemLocation();
	    				}
	    				else if (1 == which)
	    				{
	    					onMenuItemComments();
	    				}
	    				else if (2 == which)
	    				{
	    					onMenuItemRemove();
	    				}
	    			}
    			});
    		dialog = builder.create();
    		break;
    		
    	default:
    		break;
    	}
    	
    	return dialog;
    }
    
    private void onMenuItemConnect()
    {
    	//autoAddCam(m_nodesArray.get(m_nCurrentSelected).comments_id);
    	
    	if (false == m_nodesArray.get(m_nCurrentSelected).isOnline()) {
    		return;
    	}
    	
    	if (m_nodesArray.get(m_nCurrentSelected).os_info.contains("repeater#rouji.com") 
    			&& false == MainListActivity.GetUserActivated())
    	{
    		if (0 != AppSettings.GetSoftwareKeyDwordValue(_instance, "" + m_nodesArray.get(m_nCurrentSelected).comments_id + AppSettings.STRING_REGKEY_NAME_CAM_AVPARAM_FRAMERATE, 0))
    		{
    			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_level_too_low_for_this_function));
        		return;
    		}
    	}
    	
    	DoConnect(m_nodesArray.get(m_nCurrentSelected).node_id_str,
    			m_nodesArray.get(m_nCurrentSelected).pub_ip_str,
    			m_nodesArray.get(m_nCurrentSelected).pub_port_str,
    			m_nodesArray.get(m_nCurrentSelected).bLanNode,
    			m_nodesArray.get(m_nCurrentSelected).no_nat,
    			m_nodesArray.get(m_nCurrentSelected).nat_type);
    }
    
    private void onMenuItemConnectTcp()
    {
    	//autoAddCam(m_nodesArray.get(m_nCurrentSelected).comments_id);
    	
    	if (false == m_nodesArray.get(m_nCurrentSelected).isOnline()) {
    		return;
    	}
    	
    	if (true == m_nodesArray.get(m_nCurrentSelected).bLanNode) {
    		SharedFuncLib.MyMessageTip(_instance, _instance.getResources().getString(R.string.msg_lan_cant_tcp_connect));
    		return;
    	}
    	
    	if (m_nodesArray.get(m_nCurrentSelected).os_info.contains("repeater#rouji.com") 
    			&& false == MainListActivity.GetUserActivated())
    	{
    		if (0 != AppSettings.GetSoftwareKeyDwordValue(_instance, "" + m_nodesArray.get(m_nCurrentSelected).comments_id + AppSettings.STRING_REGKEY_NAME_CAM_AVPARAM_FRAMERATE, 0))
    		{
    			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_level_too_low_for_this_function));
        		return;
    		}
    	}
    	
    	DoConnectTcp(m_nodesArray.get(m_nCurrentSelected).node_id_str,
    			m_nodesArray.get(m_nCurrentSelected).pub_ip_str,
    			m_nodesArray.get(m_nCurrentSelected).pub_port_str,
    			m_nodesArray.get(m_nCurrentSelected).bLanNode,
    			m_nodesArray.get(m_nCurrentSelected).no_nat,
    			m_nodesArray.get(m_nCurrentSelected).nat_type);
    }
    
    private void onMenuItemSetParams()
    {
    	//autoAddCam(m_nodesArray.get(m_nCurrentSelected).comments_id);
    	
    	Intent intent = new Intent(_instance, AvParamActivity.class);
    	Bundle bundle = new Bundle();
    	bundle.putInt("comments_id", m_nodesArray.get(m_nCurrentSelected).comments_id);
    	bundle.putInt("conn_type", SharedFuncLib.SOCKET_TYPE_UNKNOWN);
    	if (m_nodesArray.get(m_nCurrentSelected).isOnline())
    	{
	    	bundle.putInt("audio_channels", m_nodesArray.get(m_nCurrentSelected).audio_channels);
	    	bundle.putInt("video_channels", m_nodesArray.get(m_nCurrentSelected).video_channels);
	    } else {
	    	bundle.putInt("audio_channels", 2);
	    	bundle.putInt("video_channels", 2);
		}
    	intent.putExtras(bundle);
    	startActivity(intent);
    }
    
    private void onMenuItemComments()
    {
    	final View textEntryView = LayoutInflater.from(_instance).inflate(  
                R.layout.dlgtext, null);
        final EditText editText=(EditText)textEntryView.findViewById(R.id.id_edit_inputtext);
        AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
        builder.setCancelable(false);
        builder.setTitle(_instance.getResources().getString(R.string.msg_modify_comments_title));
        builder.setView(textEntryView);
        builder.setPositiveButton(_instance.getResources().getString(R.string.ui_ok_btn),
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    	String strComments = editText.getText().toString();
                    	AppSettings.SaveSoftwareKeyValue(_instance, "" + m_nodesArray.get(m_nCurrentSelected).comments_id + AppSettings.STRING_REGKEY_NAME_CAM_COMMENTS, strComments);
            			
                    	NodeListAdapter adapter = new NodeListAdapter(_instance, m_nodesArray);
        				mListView.setAdapter(adapter);
        		    	
        		    	dialog.dismiss();
                    }
                });
        builder.setNegativeButton(_instance.getResources().getString(R.string.ui_cancel_btn),
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {
    		        	dialog.dismiss();
                    }
                });
        builder.show();
    }
    
    private void onMenuItemLocation()
    {
    	//autoAddCam(m_nodesArray.get(m_nCurrentSelected).comments_id);
    	
    	if (m_nodesArray.get(m_nCurrentSelected).device_uuid.contains("@ANYPC@")) {
    		SharedFuncLib.MyMessageTip(_instance, _instance.getResources().getString(R.string.msg_not_support_location));
    		return;
    	}
    
    	String strIdPassParam = null;
    	strIdPassParam = "cam_id=" + m_nodesArray.get(m_nCurrentSelected).comments_id;
    	
    	String strPass = AppSettings.GetSoftwareKeyValue(_instance, 
    			"" + _instance.m_nodesArray.get(_instance.m_nCurrentSelected).comments_id + AppSettings.STRING_REGKEY_NAME_CAM_PASSWORD, 
    			"");
    	if (false == strPass.equals("")) {
    		String strPasswd = "O0" + Base64.encodeToString(strPass.getBytes(), Base64.NO_WRAP);
    		strIdPassParam += "&cam_pass=" + URLEncoder.encode(strPasswd);
    	}
    	
    	if (false == m_nodesArray.get(m_nCurrentSelected).isOnline()) {
    		Uri mUri = Uri.parse("http://ykz.e2eye.com/cloudctrl/LocationMap.php?" + strIdPassParam);
			Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
			startActivity(mIntent);
			return;
    	}
    	
    	String arr[] = m_nodesArray.get(m_nCurrentSelected).os_info.split("@");
    	if (arr.length >= 3 && arr[0].equals("Windows") == false && arr[1].equals("NONE") == false && arr[2].equals("NONE") == false) {
			Uri mUri = Uri.parse("http://ykz.e2eye.com/cloudctrl/LocationMap.php?lati=" + arr[2] + "&longi=" + arr[1] + "&" + strIdPassParam);
			Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
			startActivity(mIntent);
			return;
    	}

    	Uri mUri = Uri.parse("http://ykz.e2eye.com/cloudctrl/LocationMap.php?" + strIdPassParam);
		Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
		startActivity(mIntent);
    }
    
    private void onMenuItemRemove()
    {
    	if (m_nodesArray.get(m_nCurrentSelected).isLanOnly()) {
    		return;
    	}
    	
    	int nCamId = m_nodesArray.get(m_nCurrentSelected).comments_id;
    	
    	int ret = MainListActivity.MayiDelNode(MainListActivity.GetUserId(), nCamId);
		if (ret < 0) {
			SharedFuncLib.MyMessageBox(this, 
    				getResources().getString(R.string.app_name), 
    				getResources().getString(R.string.msg_communication_error));
			return;
		}
		else if (ret == 0) {
			SharedFuncLib.MyMessageBox(this, 
    				getResources().getString(R.string.app_name), 
    				MainListActivity.MayiDelNodeResult());
			return;
		}
    	
    	onBtnRefresh();
    }
    
    
    public void FillAnyPCNode(int index, boolean bLanNode, String node_id_str, String node_name,
    		int version, String ip_str, String port_str, String pub_ip_str, String pub_port_str,
    		boolean no_nat, int nat_type, boolean is_admin, boolean is_busy,
    		int audio_channels, int video_channels, 
    		String os_info, String device_uuid, int comments_id, String location 		)
    {
    	if (null == m_nodesArray) {
    		return;
    	}
    	
    	if (0 == index) {
    		m_nodesArray.clear();
    	}
    	
    	ANYPC_NODE node = new ANYPC_NODE();
    	
    	node.bLanNode = bLanNode;  /* LAN_NODE_SUPPORT */
    	node.node_id_str = node_id_str;
    	node.node_name = node_name;
    	node.version = version;
    	node.ip_str = ip_str;
    	node.port_str = port_str;
    	node.pub_ip_str = pub_ip_str;
    	node.pub_port_str = pub_port_str;
    	node.no_nat = no_nat;
    	node.nat_type = nat_type;
    	node.is_admin = is_admin;
    	node.is_busy = is_busy;
    	node.audio_channels = audio_channels;
    	node.video_channels = video_channels;
    	node.os_info = os_info;
    	node.device_uuid = device_uuid;
    	node.comments_id = comments_id;
    	node.location = location;
    	
    	m_nodesArray.add(node);
    }
    
    public static String j_get_viewer_nodeid()
    {
    	if (_instance == null) {
    		return null;
    	}
    
    	String nodeid = AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_VIEWERNODEID, "");
    	if (nodeid.equals(""))
    	{
    		String newid = null;
    		do {
	    		int temp = (int)(System.currentTimeMillis()/1000);
	    		newid = String.format("%02X-%02X-%02X-%02X-%02X-%02X", 
	    				(temp & 0x000000ff) >> 0,
	    				(temp & 0x0000ff00) >> 8,
	    				(temp & 0x00ff0000) >> 16,
	    				(temp & 0xff000000) >> 24,
	    				(byte)(Math.random() * 255),
	    				(byte)(Math.random() * 255) 	);
    		} while (newid.equals("00-00-00-00-00-00") || newid.equals("FF-FF-FF-FF-FF-FF"));
    		
    		AppSettings.SaveSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_VIEWERNODEID, newid);
    		return newid;
    	}
    	else {
    		return nodeid;
    	}
    }
    
    public static void j_show_text(String text)
    {
    	if (_instance == null) {
    		return;
    	}
    	
    	Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, text);
    	_instance.mMainHandler.sendMessage(msg);
    	Message msg2 = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, text);
    	_instance.mMainHandler.sendMessageDelayed(msg2, 3000);
    	Message msg3 = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, text);
    	_instance.mMainHandler.sendMessageDelayed(msg3, 6000);
    }
    
    public static void j_messagebox(int msg_rid)
    {
    	if (_instance == null) {
    		return;
    	}
    	
    	Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGEBOX, _instance.getResources().getString(msg_rid));
    	_instance.mMainHandler.sendMessage(msg);
    }
    
    public static void j_messagetip(int msg_rid)
    {
    	if (_instance == null) {
    		return;
    	}
    	
    	Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, _instance.getResources().getString(msg_rid));
    	_instance.mMainHandler.sendMessage(msg);
    }
    
    public static void j_progress_show(int msg_rid)
    {
    	if (_instance == null) {
    		return;
    	}
    	
    	Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_PROGRESS_SHOW, _instance.getResources().getString(msg_rid));
    	_instance.mMainHandler.sendMessage(msg);
    }    
    
    public static void j_progress_show_format1(int msg_rid, int arg1)
    {
    	if (_instance == null) {
    		return;
    	}
    	
    	String obj = String.format(_instance.getResources().getString(msg_rid), arg1);
    	Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_PROGRESS_SHOW, obj);
    	_instance.mMainHandler.sendMessage(msg);
    }
    
    public static void j_progress_show_format2(int msg_rid, int arg1, int arg2)
    {
    	if (_instance == null) {
    		return;
    	}
    	
    	String obj = String.format(_instance.getResources().getString(msg_rid), arg1, arg2);
    	Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_PROGRESS_SHOW, obj);
    	_instance.mMainHandler.sendMessage(msg);
    }
    
    public static void j_progress_cancel()
    {
    	if (_instance == null) {
    		return;
    	}
    	
    	Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_PROGRESS_CANCEL);
    	_instance.mMainHandler.sendMessage(msg);
    }
    
    public static void j_on_connected(int type, int fhandle)
    {
    	if (_instance == null) {
    		return;
    	}
    	
    	Message msg = _instance.mWorkerHandler.obtainMessage(WORK_MSG_ON_CONNECTED, type, fhandle);
    	_instance.mWorkerHandler.sendMessage(msg);
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////
    public native void SetThisObject();
    public native int StartNative(String str_client_charset, String str_client_lang);
    public native void StopNative();
    public native int DoSearchServers(int page_offset, int page_rows);
    public native void DoConnect(String node_id_str, String pub_ip_str, String pub_port_str, boolean bLanNode, boolean no_nat, int nat_type);
    public native void DoConnectTcp(String node_id_str, String pub_ip_str, String pub_port_str, boolean bLanNode, boolean no_nat, int nat_type);
    public native void DoDisconnect();
    
    
    public static native int FakeConnectNetwork();
    public static native int DoFetchNode(String strType, String strRegion);
    public static native String GetPasswdBuff();
    public static native String GetDescBuff();
    public static native void MayiStartup(String str_client_charset, String str_client_lang);
    public static native int MayiLoginUser(String strUsername, String strPassword);
    public static native int MayiLogoutUser(int user_id);
    public static native int MayiAddNode(int user_id, int node_id);
    public static native String MayiAddNodeResult();
    public static native int MayiDelNode(int user_id, int node_id);
    public static native String MayiDelNodeResult();
    public static native int MayiUseStart(int user_id, String strUserUsername, int node_id, int guaji_id, String strGuajiName);
    public static native String MayiUseStartResult();
    public static native int MayiUseRefresh(int use_id);
    public static native int MayiUseEnd(int use_id, int user_id, String strUserUsername, int node_id, int guaji_id, String strReviewContent);
    public static native String MayiQueryIds(int user_id, int page_offset, int page_rows);
    
    public static native String GetMayiServer();
    public static native String GetLoginResult();
    public static native int GetUserId();
    public static native int GetUserScore();
    public static native boolean GetUserActivated();
    public static native String GetExpireTime();
    public static native String GetNickName();
    public static native boolean GetVipFilterNodes();
    public static native int GetGuajiRegisterPeriod();
    public static native int GetUseRegisterPeriod();
    public static native String GetDefSecurityHole();
    public static native String GetDefDashangContent();
    public static native String GetDefRegion();
}

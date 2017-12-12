package com.wangling.remotephone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.wangling.anypcadmin.ProgramSettings;
import com.wangling.anypcadmin.androidFT.FtActivity;
import com.wangling.anypcadmin.androidVNC.BitmapImplHint;
import com.wangling.anypcadmin.androidVNC.COLORMODEL;
import com.wangling.anypcadmin.androidVNC.ConnectionBean;
import com.wangling.anypcadmin.androidVNC.VncCanvasActivity;
import com.wangling.tkz.R;


public class ConnectActivity extends Activity {
	
	private static ConnectActivity _instance = null;//////////////////
	
	private static final String TAG = "ConnectActivity";
	private PowerManager.WakeLock m_wl = null;
	
	private int comments_id;
	private int conn_type;
	private int conn_fhandle;
	private int audio_channels = 0;
	private int video_channels = 1;
	private String device_uuid = null;
	private String node_name;
	private String os_info;
	private boolean bLanNode;
	private String ip_str;
	private String pub_ip_str;
	private byte func_flags;

	private int proxy_tcp_port = 20000;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);
        
        Bundle extras = getIntent().getExtras();
        comments_id = extras.getInt("comments_id");
        conn_type = extras.getInt("conn_type");
        conn_fhandle = extras.getInt("conn_fhandle");
        audio_channels = extras.getInt("audio_channels");
        video_channels = extras.getInt("video_channels");
        device_uuid = extras.getString("device_uuid");
        node_name = extras.getString("node_name");
        os_info = extras.getString("os_info");
        bLanNode = extras.getBoolean("bLanNode");
        ip_str = extras.getString("ip_str");
        pub_ip_str = extras.getString("pub_ip_str");
        func_flags = extras.getByte("func_flags");
        
        
        Log.d(TAG, "Acquiring wake lock");
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        m_wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "ConnectActivity SCREEN_DIM_WAKE_LOCK");
        m_wl.acquire();
        
        
        ((TextView)findViewById(R.id.id_text_pri_ip_val)).setText(bLanNode ? getResources().getString(R.string.msg_local_lan) : ip_str);
        ((TextView)findViewById(R.id.id_text_pub_ip_val)).setText(bLanNode ? getResources().getString(R.string.msg_local_lan) : pub_ip_str);
        
        if (comments_id > 0) {
        	((TextView)findViewById(R.id.id_text_name_val)).setText("[ID:" + comments_id + "] " + node_name);
        }
        else {
        	((TextView)findViewById(R.id.id_text_name_val)).setText("[ID:" + "NONE" + "] " + node_name);
        }
        
        if (os_info.startsWith("Windows"))
        {
        	((TextView)findViewById(R.id.id_text_os_info_val)).setText("Microsoft Windows");
        }
        else if (os_info.startsWith("Android"))
        {
        	if ((func_flags & SharedFuncLib.FUNC_FLAGS_HASROOT) != 0) {
        		((TextView)findViewById(R.id.id_text_os_info_val)).setText(os_info.substring(0, os_info.indexOf("@")) + " [Rooted]");
        	}
        	else {
        		((TextView)findViewById(R.id.id_text_os_info_val)).setText(os_info.substring(0, os_info.indexOf("@")) + " [Not Root]");
        	}
        }
        else {
        	((TextView)findViewById(R.id.id_text_os_info_val)).setText("Unknown");
        }
        
        
        findViewById(R.id.av_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnAv();
        	}
        });
        
        findViewById(R.id.vnc_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnVnc();
        	}
        });
        
        findViewById(R.id.ft_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnFt();
        	}
        });
        
        findViewById(R.id.lock_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnLock();
        	}
        });
        
        findViewById(R.id.unlock_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnUnlock();
        	}
        });
        
        findViewById(R.id.shutdown_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnShutdown();
        	}
        });
        
        findViewById(R.id.reboot_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnReboot();
        	}
        });
        
        findViewById(R.id.uninstall_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnUninstall();
        	}
        });
        
        if (device_uuid.contains("@ANYPC@")) {
        	findViewById(R.id.lock_btn).setEnabled(false);
        	findViewById(R.id.unlock_btn).setEnabled(false);
        }
        else {
        	findViewById(R.id.lock_btn).setEnabled(true);
        	findViewById(R.id.unlock_btn).setEnabled(true);
        }
        
        findViewById(R.id.shutdown_btn).setEnabled(false);
        findViewById(R.id.reboot_btn).setEnabled(false);
        findViewById(R.id.uninstall_btn).setEnabled(false);
        
        _instance = this;//////////////////
    }
    
    @Override
    protected void onDestroy() {
    	
    	_instance = null;//////////////////
    	
        Log.d(TAG, "Release wake lock");
    	m_wl.release();
    	
    	super.onDestroy();
    }
    
    private void onBtnAv()
    {
    	//if (((func_flags & SharedFuncLib.FUNC_FLAGS_ACTIVATED) == 0 || false == MainListActivity.GetUserActivated())
		//		&& SharedFuncLib.getLowestLevelForAv() > 0)
    	if (AvPlayActivity.m_bAvPlayRestricted)
		{
			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_level_too_low_for_this_function));
    		return;
		}
		else if ((func_flags & SharedFuncLib.FUNC_FLAGS_AV) == 0)
		{
			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_support_this_function));
    		return;
		}
    	
    	//if (0 == AppSettings.GetSoftwareKeyDwordValue(_instance, "" + _instance.comments_id + AppSettings.STRING_REGKEY_NAME_CAM_AVPARAM_FRAMERATE, 0))
		if (true)
    	{
	    	Intent intent = new Intent(_instance, AvParamActivity.class);
	    	Bundle bundle = new Bundle();
	    	bundle.putInt("comments_id", _instance.comments_id);
	    	bundle.putInt("conn_type", _instance.conn_type);
	    	bundle.putInt("audio_channels", _instance.audio_channels);
	    	bundle.putInt("video_channels", _instance.video_channels);
	    	intent.putExtras(bundle);
	    	startActivityForResult(intent, REQUEST_CODE_AVPARAM);
		}
		else {
	    	Intent intent = new Intent(_instance, AvPlayActivity.class);
	    	Bundle bundle = new Bundle();
	    	bundle.putInt("comments_id", _instance.comments_id);
	    	bundle.putInt("conn_type", _instance.conn_type);
	    	bundle.putInt("conn_fhandle", _instance.conn_fhandle);
	    	bundle.putInt("audio_channels", _instance.audio_channels);
	    	bundle.putInt("video_channels", _instance.video_channels);
	    	bundle.putString("device_uuid", _instance.device_uuid);
	    	bundle.putByte("func_flags", _instance.func_flags);
	    	intent.putExtras(bundle);
	    	startActivityForResult(intent, REQUEST_CODE_AVPLAY);
		}
    }
	
    private void onBtnVnc()
    {
    	int wTcpPort = 5901;
    	String szSysTempPass = "ykz123";
    	if (os_info.contains("anypc01#rouji.com")) {
    		wTcpPort = 5900;
    		szSysTempPass = "admin123!@#";
    	}
    	
    	if ((func_flags & SharedFuncLib.FUNC_FLAGS_HASROOT) == 0)
		{
			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_root_for_this_function));
    		return;
		}
		else if (((func_flags & SharedFuncLib.FUNC_FLAGS_ACTIVATED) == 0 || false == MainListActivity.GetUserActivated())
				&& SharedFuncLib.getLowestLevelForVnc() > 0)
		{
			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_level_too_low_for_this_function));
    		return;
		}
		else if ((func_flags & SharedFuncLib.FUNC_FLAGS_VNC) == 0)
		{
			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_support_this_function));
    		return;
		}
    	
    	if (SharedFuncLib.CtrlCmdPROXY(_instance.conn_type, _instance.conn_fhandle, wTcpPort) < 0)
		{
			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_vnc_connection_aborted));
    		return;
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
    	bundle.putInt("comments_id", _instance.comments_id);
    	bundle.putInt("conn_type", _instance.conn_type);
    	bundle.putInt("conn_fhandle", _instance.conn_fhandle);
    	bundle.putString("device_uuid", _instance.device_uuid);
    	intent.putExtras(bundle);
    	startActivityForResult(intent, REQUEST_CODE_VNCVIEWER);
    }
    
    private void onBtnFt()
    {
    	int wTcpPort = 5901;
    	String szSysTempPass = "ykz123";
    	if (os_info.contains("anypc01#rouji.com")) {
    		wTcpPort = 5900;
    		szSysTempPass = "admin123!@#";
    	}
    	
    	if (((func_flags & SharedFuncLib.FUNC_FLAGS_ACTIVATED) == 0 || false == MainListActivity.GetUserActivated())
				&& SharedFuncLib.getLowestLevelForFt() > 0)
		{
			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_level_too_low_for_this_function));
    		return;
		}
		else if ((func_flags & SharedFuncLib.FUNC_FLAGS_FT) == 0)
		{
			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_support_this_function));
    		return;
		}
    	
    	if (SharedFuncLib.CtrlCmdPROXY(_instance.conn_type, _instance.conn_fhandle, wTcpPort) < 0)
		{
			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_vnc_connection_aborted));
    		return;
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
		
		
		String strLang = "CHS";
		//就像电脑版的Ft一样，发送GBK的请求过去！！！
		//Android VNC Server会自动将GBK请求字符串转换为UTF8
		//Android VNC Server发回的数据带<!UTF-8!>前缀，会在rfbbytes_to_string_ex函数中处理。
		ProgramSettings.SaveSoftwareKeyValue(_instance, ProgramSettings.STRING_REGKEY_NAME_CURRENTPEERLANG, strLang);
		
		//selected.setAddress("127.0.0.1");
		//selected.setPort(_instance.proxy_tcp_port);
		//selected.setPassword(szSysTempPass);
		
		Intent intent = new Intent(_instance, FtActivity.class);
    	Bundle bundle = new Bundle();
    	bundle.putString("ADDRESS", "127.0.0.1");
    	bundle.putInt("PORT", _instance.proxy_tcp_port);
    	bundle.putString("PASSWORD", szSysTempPass);
    	intent.putExtras(bundle);
    	startActivityForResult(intent, REQUEST_CODE_FT);
    }
    
    private void onBtnLock()
    {
    	SharedFuncLib.CtrlCmdARM(conn_type, conn_fhandle);
    }
    
    private void onBtnUnlock()
    {
    	SharedFuncLib.CtrlCmdDISARM(conn_type, conn_fhandle);
    }
    
    private void onBtnShutdown()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
        builder.setTitle(_instance.getResources().getString(R.string.app_name));
        builder.setMessage(_instance.getResources().getString(R.string.msg_shutdown_or_not));
        builder.setPositiveButton(_instance.getResources().getString(R.string.ui_ok_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button1");
                    	
                    	if ((func_flags & SharedFuncLib.FUNC_FLAGS_HASROOT) == 0)
                		{
                			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_root_for_this_function));
                    		return;
                		}
                    	SharedFuncLib.CtrlCmdAVCONTRL(conn_type, conn_fhandle, SharedFuncLib.AV_CONTRL_SYSTEM_SHUTDOWN, 0);
                    	try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	
                    	SharedFuncLib.ProxyClientAllQuit();
                		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
                    	
                    	dialog.dismiss();
                    	
                    	_instance.finish();
                    }
                });
        builder.setNegativeButton(_instance.getResources().getString(R.string.ui_cancel_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button3");
                    	dialog.dismiss();
                    }
                });
        builder.show();
    }
    
    private void onBtnReboot()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
        builder.setTitle(_instance.getResources().getString(R.string.app_name));
        builder.setMessage(_instance.getResources().getString(R.string.msg_reboot_or_not));
        builder.setPositiveButton(_instance.getResources().getString(R.string.ui_ok_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button1");
                    	
                    	if ((func_flags & SharedFuncLib.FUNC_FLAGS_HASROOT) == 0)
                		{
                			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_root_for_this_function));
                    		return;
                		}
                    	SharedFuncLib.CtrlCmdAVCONTRL(conn_type, conn_fhandle, SharedFuncLib.AV_CONTRL_SYSTEM_REBOOT, 0);
                    	try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	
                    	SharedFuncLib.ProxyClientAllQuit();
                		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
                    	
                    	dialog.dismiss();
                    	
                    	_instance.finish();
                    }
                });
        builder.setNegativeButton(_instance.getResources().getString(R.string.ui_cancel_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button3");
                    	dialog.dismiss();
                    }
                });
        builder.show();
    }
    
    private void onBtnUninstall()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
        builder.setTitle(_instance.getResources().getString(R.string.app_name));
        builder.setMessage(_instance.getResources().getString(R.string.msg_uninstall_or_not));
        builder.setPositiveButton(_instance.getResources().getString(R.string.ui_ok_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button1");
                    	
                    	if ((func_flags & SharedFuncLib.FUNC_FLAGS_HASROOT) == 0)
                		{
                			SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_server_not_root_for_this_function));
                    		return;
                		}
                    	if (device_uuid.contains("@ANYPC@"))
                    	{
                    		SharedFuncLib.CtrlCmdRUN(conn_type, conn_fhandle, "uninstall.exe");
                    	}
                    	else {
                    		SharedFuncLib.CtrlCmdRUN(conn_type, conn_fhandle, "su -c \"mount -o remount,rw /system\" ; su -c \"pm uninstall com.wangling.remotephone\" ; su -c \"rm /system/app/MobileCamera.apk\" ; su -c \"rm /system/app/MobileCamera/MobileCamera.apk\" ; su -c \"reboot\"");
                    	}
                    	try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	
                    	SharedFuncLib.ProxyClientAllQuit();
                		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
                    	
                    	dialog.dismiss();
                    	                    	
                    	_instance.finish();
                    }
                });
        builder.setNegativeButton(_instance.getResources().getString(R.string.ui_cancel_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button3");
                    	dialog.dismiss();
                    }
                });
        builder.show();
    }
    
    private void onBtnDisconnect()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
        builder.setTitle(_instance.getResources().getString(R.string.app_name));
        builder.setMessage(_instance.getResources().getString(R.string.msg_disconnect_or_not));
        builder.setPositiveButton(_instance.getResources().getString(R.string.ui_ok_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button1");
                    	
                    	SharedFuncLib.ProxyClientAllQuit();
                		SharedFuncLib.CtrlCmdBYE(conn_type, conn_fhandle);
                    	
                    	dialog.dismiss();
                    	                    	
                    	_instance.finish();
                    }
                });
        builder.setNegativeButton(_instance.getResources().getString(R.string.ui_cancel_btn),
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
        	onBtnDisconnect();
            return true;   
        }else{         
            return super.onKeyDown(keyCode, event);   
        }   
    }
    
    
    static final int REQUEST_CODE_AVPARAM = 2;
    static final int REQUEST_CODE_AVPLAY = 3;
    static final int REQUEST_CODE_VNCVIEWER = 4;
    static final int REQUEST_CODE_FT = 5;
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	switch (requestCode)
    	{
    	case REQUEST_CODE_AVPARAM:
    		if (RESULT_OK == resultCode)
    		{
    	    	Intent intent = new Intent(_instance, AvPlayActivity.class);
    	    	Bundle bundle = new Bundle();
    	    	bundle.putInt("comments_id", _instance.comments_id);
    	    	bundle.putInt("conn_type", _instance.conn_type);
    	    	bundle.putInt("conn_fhandle", _instance.conn_fhandle);
    	    	bundle.putInt("audio_channels", _instance.audio_channels);
    	    	bundle.putInt("video_channels", _instance.video_channels);
    	    	bundle.putString("device_uuid", _instance.device_uuid);
    	    	bundle.putByte("func_flags", _instance.func_flags);
    	    	intent.putExtras(bundle);
    	    	startActivityForResult(intent, REQUEST_CODE_AVPLAY);
    		}
    		else {
    			;
    		}
    		break;
    		
    	case REQUEST_CODE_AVPLAY:
    		break;
    		
    	case REQUEST_CODE_VNCVIEWER:
    	case REQUEST_CODE_FT:
    		SharedFuncLib.ProxyClientAllQuit();
    		break;
    	}
    }
    
}

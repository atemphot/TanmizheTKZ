package com.wangling.remotephone;

import com.ant.liao.GifView;
import com.ant.liao.GifView.GifImageType;
import com.wangling.remotephone.MainListActivity.MainHandler;
import com.wangling.tkz.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;


public class IntrusionActivity extends Activity {
	
	static final int UI_MSG_SHOWSTATUS = 0;
	static final int UI_MSG_MESSAGEBOX = 1;
	static final int UI_MSG_MESSAGETIP = 2;
	static final int UI_MSG_RELEASE_MP = 3;
	static final int UI_MSG_SCAN_DONE = 4;
	
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
			case UI_MSG_SHOWSTATUS://obj
				((TextView)findViewById(R.id.id_text_status)).setText((String)(msg.obj));
				break;
				
			case UI_MSG_MESSAGEBOX://obj
				SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), (String)(msg.obj));
				break;
				
			case UI_MSG_MESSAGETIP://obj
				SharedFuncLib.MyMessageTip(_instance, (String)(msg.obj));
				break;
				
			case UI_MSG_RELEASE_MP://obj
				MediaPlayer mp = (MediaPlayer)(msg.obj);
				mp.stop();
				mp.release();
				Log.d(TAG, "MediaPlayer released!");
				break;
				
			case UI_MSG_SCAN_DONE:
				_instance.setResult(RESULT_OK);
		    	_instance.finish();
				break;
				
			default:
				break;				
			}
			
			super.handleMessage(msg);
		}
	}
	
	private void SetDlgItemText(String str)
	{
    	Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_SHOWSTATUS, str);
    	_instance.mMainHandler.sendMessage(msg);
	}
	
	private int abs_rand()
	{
		int x = (int)(Math.random()*10000.0f);
		return x;
	}
	
	private int ScanningThreadFunc()
	{
		String str = _instance.getResources().getString(R.string.msg_downloading_hacker);
		SetDlgItemText(str);
    	
		MainListActivity.FakeConnectNetwork();
		
		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {}
		
		
		if (m_selectRegion == -1)
		{
			if (MainListActivity.GetUserActivated())
			{
				str = _instance.getResources().getString(R.string.msg_vip_multi_thread_scan);
				SetDlgItemText(str);
				try { Thread.sleep(2000); } catch (InterruptedException e) {}
			}
			
			int r = abs_rand();
			int nSec = ((r) % 150) + 50;
			
			for (int i = 0; i < nSec; i++) {

				int ip1 = 192;
				int ip2 = 168;
				int ip3 = abs_rand() % 255;
				int ip4 = abs_rand() % 255;
				str = String.format(_instance.getResources().getString(R.string.msg_scanning_lan_ip_format), ip1, ip2, ip4);
				SetDlgItemText(str);
				try { Thread.sleep(400); } catch (InterruptedException e) {}////
				if (m_quitThread) {
					return 0;
				}

				int index = abs_rand() % aa_count;
				str = _instance.getResources().getString(R.string.msg_trying_security_hole_1);
				str +=  aa[index];
				SetDlgItemText(str);
				try { Thread.sleep(300); } catch (InterruptedException e) {}////
				if (m_quitThread) {
					return 0;
				}

				index = abs_rand() % aa_count;
				str = _instance.getResources().getString(R.string.msg_trying_security_hole_1);
				str +=  aa[index];
				SetDlgItemText(str);
				try { Thread.sleep(300); } catch (InterruptedException e) {}////
				if (m_quitThread) {
					return 0;
				}
			}
			
			str = _instance.getResources().getString(R.string.msg_scan_no_device_with_hole);
			SetDlgItemText(str);
			try { Thread.sleep(2000); } catch (InterruptedException e) {}
			
			Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGEBOX, str);
	    	_instance.mMainHandler.sendMessage(msg);
	    	
	    	return 0;
		}
		
		
		int r = abs_rand();
		int nSec = ((r) % (60*15)) + 60*1;
		if (MainListActivity.GetUserActivated())
		{
			nSec = nSec/2;
			if (nSec < 15) {
				nSec = 15;
			}
			str = _instance.getResources().getString(R.string.msg_vip_multi_thread_scan);
			SetDlgItemText(str);
			try { Thread.sleep(2000); } catch (InterruptedException e) {}
		}
		else if (MainListActivity.GetNodesCount() <= 10) {
			nSec = ((r) % 20) + 15;
		}
		else if (MainListActivity.GetNodesCount() <= 20) {
			nSec = ((r) % 150) + 30;
		}
		
		for (int i = 0; i < nSec; i++) {

			int ip1 = nSec % 56;
			int ip2 = (r) % 56;
			int ip3 = abs_rand() % 255;
			int ip4 = abs_rand() % 255;
			str = String.format(_instance.getResources().getString(R.string.msg_scanning_ip_format), ip1, ip2, ip3, ip4);
			SetDlgItemText(str);
			try { Thread.sleep(400); } catch (InterruptedException e) {}////
			if (m_quitThread) {
				return 0;
			}

			int index = abs_rand() % aa_count;
			str = _instance.getResources().getString(R.string.msg_trying_security_hole_1);
			str +=  aa[index];
			SetDlgItemText(str);
			try { Thread.sleep(300); } catch (InterruptedException e) {}////
			if (m_quitThread) {
				return 0;
			}

			index = abs_rand() % aa_count;
			str = _instance.getResources().getString(R.string.msg_trying_security_hole_1);
			str +=  aa[index];
			SetDlgItemText(str);
			try { Thread.sleep(300); } catch (InterruptedException e) {}////
			if (m_quitThread) {
				return 0;
			}
		}
		
		SetDlgItemText(_instance.getResources().getString(R.string.msg_use_hole_installing_skd));
		try { Thread.sleep(5000); } catch (InterruptedException e) {}////
		
		String typeStr = null;
		if (m_selectType == 1) {
			typeStr = "@ANYPC@";
		}
		else if (m_selectType == 2) {
			typeStr = "@YKZ@";
		}
		else {
			typeStr = "";
		}
		
		while (false == m_quitThread) {
			int ret;
			
			ret = MainListActivity.DoFetchNode(typeStr, "");
			if (ret < 0) {
				SetDlgItemText(_instance.getResources().getString(R.string.msg_communication_error_retry_later));
				try { Thread.sleep(5000); } catch (InterruptedException e) {}////
				continue;
			}
			else if (ret == 0) {
				SetDlgItemText(_instance.getResources().getString(R.string.msg_installing_skd));
				try { Thread.sleep(5000); } catch (InterruptedException e) {}////
				continue;
			}
			int fetch_id = ret;
			String passwd_buff = MainListActivity.GetPasswdBuff();
			String desc_buff = MainListActivity.GetDescBuff();
			
			ret = MainListActivity.MayiAddNode(MainListActivity.GetUserId(), fetch_id);
			if (ret < 0) {
				SetDlgItemText(_instance.getResources().getString(R.string.msg_communication_error_retry_later));
				try { Thread.sleep(5000); } catch (InterruptedException e) {}////
				continue;
			}
			else if (ret == 0) {
				str = MainListActivity.MayiAddNodeResult();
				if (str.contains(_instance.getResources().getString(R.string.keywords_duplicate_add)) == false)
				{
					Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGETIP, str);
			    	_instance.mMainHandler.sendMessage(msg);
				}
				else {
					SetDlgItemText(_instance.getResources().getString(R.string.msg_another_method_installing_skd));
				}
				try { Thread.sleep(15000); } catch (InterruptedException e) {}////
				continue;
			}
			
			//pDlg->m_addOK = TRUE;
			
			AppSettings.SaveSoftwareKeyValue(_instance, "" + fetch_id + AppSettings.STRING_REGKEY_NAME_CAM_PASSWORD, passwd_buff);
			
			
			SetDlgItemText(_instance.getResources().getString(R.string.msg_huhuhu_install_skd_ok));
			
			str = _instance.getResources().getString(R.string.msg_congratulations_install_skd_ok_1);
			str += desc_buff;
			Message msg = _instance.mMainHandler.obtainMessage(UI_MSG_MESSAGEBOX, str);
	    	_instance.mMainHandler.sendMessage(msg);
			
	    	MediaPlayer mp = MediaPlayer.create(this, R.raw.scan_done);
	    	if (null != mp) {
	    		Log.d(TAG, "MediaPlayer start...");
	    		mp.start();
	    		Message send_msg = _instance.mMainHandler.obtainMessage(UI_MSG_RELEASE_MP, mp);
	        	_instance.mMainHandler.sendMessageDelayed(send_msg, 5000);
	    	}
	    	try { Thread.sleep(5000); } catch (InterruptedException e) {}////
	    	
	    	
			SetDlgItemText(_instance.getResources().getString(R.string.msg_deleting_hacker));
			try { Thread.sleep(3000); } catch (InterruptedException e) {}////
			
			msg = _instance.mMainHandler.obtainMessage(UI_MSG_SCAN_DONE);
	    	_instance.mMainHandler.sendMessage(msg);
	    	
	    	break;
		}
		
		return 0;
	}
	
	
	private static final String TAG = "IntrusionActivity";
	private static IntrusionActivity _instance = null;
	private PowerManager.WakeLock m_wl = null;
	private MainHandler mMainHandler = null;
	private GifView gf1 = null;
	private int m_selectRegion = 0;
	private int m_selectType = 0;
	private boolean m_quitThread = false;
	private String[] aa = null;
	private int aa_count = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       	setContentView(R.layout.intrusion);
    	
        Log.d(TAG, "Acquiring wake lock");
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        m_wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "IntrusionActivity SCREEN_DIM_WAKE_LOCK");
        m_wl.acquire();
        
        _instance = this;
        
        mMainHandler = new MainHandler();
        
        
        findViewById(R.id.id_btn_start).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnStart();
        	}
        });
        
        findViewById(R.id.id_btn_stop).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnStop();
        	}
        });
        
        
        //初始化扫描地区 下拉框。。。
        String strRegions = MainListActivity.GetDefRegion();
        String[] targetRegionArray = strRegions.split(",");
        
        ArrayAdapter<String> adapterRegion = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, targetRegionArray);
        adapterRegion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        Spinner spinnerRegion = (Spinner)findViewById(R.id.id_spinner_target_region);
        spinnerRegion.setAdapter(adapterRegion);
        spinnerRegion.setSelection(0);
        spinnerRegion.setEnabled(true);
        
        
        //初始化扫描目标 下拉框。。。
        String[] targetTypeArray = new String[3];
        targetTypeArray[0] = getResources().getString(R.string.ui_select_type_all);
        targetTypeArray[1] = getResources().getString(R.string.ui_select_type_pc);
        targetTypeArray[2] = getResources().getString(R.string.ui_select_type_mobile);
        
        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, targetTypeArray);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        Spinner spinnerType = (Spinner)findViewById(R.id.id_spinner_target_type);
        spinnerType.setAdapter(adapterType);
        spinnerType.setSelection(0);
        spinnerType.setEnabled(true);
        
        
        ((CheckBox)findViewById(R.id.id_checkbox_cn_first)).setChecked(true);
        ((CheckBox)findViewById(R.id.id_checkbox_cn_first)).setEnabled(false);
        
        
        //Gif动画。。。
        gf1 = (GifView)findViewById(R.id.id_gif_scan);
        gf1.setGifImage(R.drawable.gif_scan);
        gf1.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		;
        	}
        });
        gf1.setShowDimension(176*3, 220*3);
        gf1.setGifImageType(GifImageType.COVER);
        gf1.showCover();
        
        
        this.setTitle(getResources().getString(R.string.ui_intrusion_title));
    }
    
    private void onBtnStart()
    {
    	findViewById(R.id.id_btn_start).setEnabled(false);
    	
        Spinner spinnerRegion = (Spinner)findViewById(R.id.id_spinner_target_region);
        m_selectRegion = spinnerRegion.getSelectedItemPosition();
        spinnerRegion.setEnabled(false);
        if (m_selectRegion == spinnerRegion.getCount() - 1)
        {
        	if (MainListActivity.GetUserActivated() == false)
        	{
        		SharedFuncLib.MyMessageBox(_instance, _instance.getResources().getString(R.string.app_name), _instance.getResources().getString(R.string.msg_cant_scan_local_lan));
        		spinnerRegion.setEnabled(true);
        		findViewById(R.id.id_btn_start).setEnabled(true);
        		return;
        	}
        	else {
        		m_selectRegion = -1;
        	}
        }
        else {
        	spinnerRegion.setSelection(0);
        	m_selectRegion = 0;
        }
    	
        Spinner spinnerType = (Spinner)findViewById(R.id.id_spinner_target_type);
        m_selectType = spinnerType.getSelectedItemPosition();
        spinnerType.setEnabled(false);
        
        //显示动画。。。
    	gf1.showAnimation();
    	
    	
    	String strHoles = MainListActivity.GetDefSecurityHole();
        String[] holesArray1 = strHoles.split(",");
        aa = new String[holesArray1.length];
        aa_count = 0;
        for (int i = 0; i < holesArray1.length; i++)
        {
        	if (m_selectType == 1 && false == holesArray1[i].contains("Windows")) {
        		;
        	}
        	else if (m_selectType == 2 && true == holesArray1[i].contains("Windows")) {
        		;
        	}
        	else {
        		aa[aa_count] = holesArray1[i];
        		aa_count += 1;
        	}
        }
        if (0 == aa_count) {
        	String str = _instance.getResources().getString(R.string.msg_hacker_info_not_available);
        	((TextView)findViewById(R.id.id_text_status)).setText(str);
        	SharedFuncLib.MyMessageTip(_instance, str);
        	return;
        }
        
        String strMsg = _instance.getResources().getString(R.string.msg_download_hacker_or_not_1);
        for (int i = 0; i < aa_count; i++)
        {
        	String str = String.format("\t(%d). [", i+1);
        	strMsg += str;
        	strMsg += aa[i];
        	strMsg += _instance.getResources().getString(R.string.msg_download_hacker_or_not_mid);
        }
        strMsg += _instance.getResources().getString(R.string.msg_download_hacker_or_not_2);
    	
    	
        AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
        builder.setTitle(_instance.getResources().getString(R.string.app_name));
        builder.setMessage(strMsg);
        builder.setPositiveButton(_instance.getResources().getString(R.string.ui_ok_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button1");
                    	
                    	dialog.dismiss();
                    	
                    	m_quitThread = false;
                    	
                    	new Thread(new Runnable() {
                			public void run()
                			{
                				ScanningThreadFunc();
                			}
                		}).start();
                    	
                    	if (false == MainListActivity.GetUserActivated())
                    	{
                    		SharedFuncLib.MyMessageTip(_instance, _instance.getResources().getString(R.string.msg_vip_can_scan_fast));
                    	}
                    }
                });
        builder.setNegativeButton(_instance.getResources().getString(R.string.ui_cancel_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //setTitle("点击了对话框上的Button3");
                    	dialog.dismiss();
                    	
                    	onBtnStop();
                    }
                });
        builder.show();
    	
    }
    
    private void onBtnStop()
    {
    	m_quitThread = true;
    	
    	gf1.showCover();
    	
    	_instance.setResult(RESULT_CANCELED);
    	_instance.finish();
    }
    
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {   
           
        if(keyCode == KeyEvent.KEYCODE_BACK){
        	onBtnStop();
            return true;   
        }else{         
            return super.onKeyDown(keyCode, event);   
        }
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "Release wake lock");
    	m_wl.release();
    	
    	_instance = null;
    	
    	super.onDestroy();
    }
}

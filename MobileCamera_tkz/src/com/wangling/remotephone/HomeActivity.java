package com.wangling.remotephone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.unirst.cn.OriginatingApp;
import com.wangling.tkz.R;


public class HomeActivity extends Activity {
	
	private void copy_file_to_datadir(String filename)
	{
		byte[] buff = new byte[1024];
		int ret;
		
		try {
			AssetManager am = getAssets();
			InputStream is = am.open(filename);
			File f = new File("/data/data/" + getPackageName() + "/remotephone/" + filename);
			if (false == f.exists()) {
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			while ((ret = is.read(buff, 0, buff.length)) > 0)
			{
				fos.write(buff, 0, ret);
			}
			fos.close();
			is.close();
		} catch (Exception e) {}
	}	
	
	private void copy_file_to_datadir_ex(String subname, String filename)
	{
		byte[] buff = new byte[1024];
		int ret;
		
		File dir = new File("/data/data/" + getPackageName() + "/remotephone/" + subname);
		if (false == dir.exists()) {
			dir.mkdir();
		}
		
		try {
			AssetManager am = getAssets();
			InputStream is = am.open(subname + "/" + filename);
			File f = new File("/data/data/" + getPackageName() + "/remotephone/" + subname + "/"  + filename);
			if (false == f.exists()) {
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			while ((ret = is.read(buff, 0, buff.length)) > 0)
			{
				fos.write(buff, 0, ret);
			}
			fos.close();
			is.close();
		} catch (Exception e) {}
	}
	
	
	private static final String TAG = "HomeActivity";
	private static HomeActivity __this = null;
	private PowerManager.WakeLock m_wl = null;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       	setContentView(R.layout.home);
    	
        Log.d(TAG, "Acquiring wake lock");
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        m_wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "HomeActivity SCREEN_DIM_WAKE_LOCK");
        m_wl.acquire();
        
        __this = this;
        
        findViewById(R.id.id_btn_login).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnLogin();
        	}
        });
        
        findViewById(R.id.id_btn_register).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnRegister();
        	}
        });
        
        
        String strUserName = AppSettings.GetSoftwareKeyValue(this, AppSettings.STRING_REGKEY_NAME_USERNAME, "");
        if (false == strUserName.equals("")) {
        	((EditText)findViewById(R.id.id_edit_username)).setText(strUserName);
        	
        	String strUserPass = AppSettings.GetSoftwareKeyValue(this, AppSettings.STRING_REGKEY_NAME_USERPASS, "");
        	if (false == strUserPass.equals("")) {
        		((EditText)findViewById(R.id.id_edit_password)).setText(strUserPass);
        	}
        	else {
        		((EditText)findViewById(R.id.id_edit_password)).setText("");
        	}
        }
        
        int tmpVal = AppSettings.GetSoftwareKeyDwordValue(this, AppSettings.STRING_REGKEY_NAME_REMEMBERPASS, 0);
        if (tmpVal == 1) {
        	((CheckBox)findViewById(R.id.id_check_rememberpass)).setChecked(true);
        }
        else {
        	((CheckBox)findViewById(R.id.id_check_rememberpass)).setChecked(false);
        	((EditText)findViewById(R.id.id_edit_password)).setText("");
        }
        
    	{
    		File dir = new File("/data/data/" + getPackageName() + "/remotephone");
    		if (false == dir.exists()) {
    			dir.mkdir();
    		}
    		
    		copy_file_to_datadir("timg.jpg");
    		copy_file_to_datadir("timg2.jpg");
    	}
        
    	//For K-Touch W655...
    	OriginatingApp.deleteApn(this, "3gnet");
    	OriginatingApp.deleteApn(this, "cmnet");
    	
    	
    	MainListActivity.MayiStartup("utf8", getResources().getString(R.string.app_lang));
    	
        int ver = SharedFuncLib.getAppVersion();
        String ver_str = String.format("%s V%d.%d.%d", 
        		getResources().getString(R.string.app_name),
        		(ver & 0xff000000)>>24,
        		(ver & 0x00ff0000)>>16,
        		(ver & 0x0000ff00)>>8);
        setTitle(ver_str);
        
    }
    
    private void onBtnLogin()
    {
    	final String strUsername = ((EditText)findViewById(R.id.id_edit_username)).getText().toString();
    	final String strUserpass = ((EditText)findViewById(R.id.id_edit_password)).getText().toString();
    	
    	if (strUsername.equals("")) {
    		SharedFuncLib.MyMessageTip(this, this.getResources().getString(R.string.msg_username_empty));
    		return;
    	}
    	if (strUserpass.equals("")) {
    		SharedFuncLib.MyMessageTip(this, this.getResources().getString(R.string.msg_password_empty));
    		return;
    	}
    	
    	int tmpVal = ((CheckBox)findViewById(R.id.id_check_rememberpass)).isChecked() ? 1 : 0;
    	AppSettings.SaveSoftwareKeyDwordValue(this, AppSettings.STRING_REGKEY_NAME_REMEMBERPASS, tmpVal);
    	
    	
    	int ret = MainListActivity.MayiLoginUser(strUsername, strUserpass);
    	if (ret < 0) {
    		SharedFuncLib.MyMessageTip(__this, __this.getResources().getString(R.string.msg_communication_error));
    	}
    	else if (ret == 0) /* NG */ {
    		SharedFuncLib.MyMessageTip(__this, MainListActivity.GetLoginResult());
    	}
    	else {
    		__this.finish();
    		
    		AppSettings.SaveSoftwareKeyValue(__this, AppSettings.STRING_REGKEY_NAME_USERNAME, strUsername);
    		AppSettings.SaveSoftwareKeyValue(__this, AppSettings.STRING_REGKEY_NAME_USERPASS, strUserpass);
    		
    		Intent intent = new Intent(__this, MainListActivity.class);
    		startActivity(intent);
    	}
    }
    
    private void onBtnRegister()
    {
		Uri mUri = Uri.parse("http://" + MainListActivity.GetMayiServer() + "/toukuizhe/001Register.html");
		Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
		startActivity(mIntent);
    }
    
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {   
           
        if(keyCode == KeyEvent.KEYCODE_BACK){
        	finish();
        	android.os.Process.killProcess(android.os.Process.myPid());
            return true;   
        }else{         
            return super.onKeyDown(keyCode, event);   
        }
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "Release wake lock");
    	m_wl.release();
    	
    	super.onDestroy();
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////
    static {
        System.loadLibrary("up2p"); //The first
        System.loadLibrary("shdir");//The second
        System.loadLibrary("avrtp");//The third
    }
}

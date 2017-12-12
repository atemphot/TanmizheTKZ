package com.wangling.anypcadmin;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class SharedFuncLib {
	
	private static SharedFuncLib _instance = null;
	
	public static synchronized  SharedFuncLib getInstance() {   
        if (_instance == null)   
        	_instance = new SharedFuncLib();   
        return _instance;   
    }
	
	
	public void ShowMessageBox(Context context, String msg)
	{
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
	}
	
	
	public static byte[] toHH(int n) {  
		  byte[] b = new byte[4];  
		  b[3] = (byte) (n & 0xff);  
		  b[2] = (byte) (n >> 8 & 0xff);  
		  b[1] = (byte) (n >> 16 & 0xff);  
		  b[0] = (byte) (n >> 24 & 0xff);  
		  return b;  
	}
	
	public static int lBytesToInt(byte[] b) {  
		  int s = 0;  
		  for (int i = 0; i < 3; i++) {  
		    if (b[3-i] >= 0) {  
		    s = s + b[3-i];  
		    } else {  
		    s = s + 256 + b[3-i];  
		    }  
		    s = s * 256;  
		  }  
		  if (b[0] >= 0) {  
		    s = s + b[0];  
		  } else {  
		    s = s + 256 + b[0];  
		  }  
		  return s;  
	}
	
	public static int reverseInt(int i) {
		  int result = lBytesToInt(toHH(i));
		  return result;
	}
	
	public int ntohl(int i)
	{
		return reverseInt(i);
	}
	
	public int htonl(int i)
	{
		return reverseInt(i);
	}
}

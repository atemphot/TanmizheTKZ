package com.wangling.anypcadmin.androidFT;

import java.io.DataInputStream;
import java.io.IOException;

import com.wangling.tkz.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;


public class FtActivity extends Activity {
	
	private String serverCharset;
	
	class RfbProtoFt {
		DataInputStream is;
		
		String rfbbytes_to_string_ex(DataInputStream tmp_is, int[] count) throws IOException
		{
			byte[] rfbbytes = new byte[2048];
			
			count[0] = 0;
			for (int i = 0; i < rfbbytes.length; i++)
			{
				rfbbytes[i] = (byte)tmp_is.readByte();
				count[0] += 1;
				if (rfbbytes[i] == 0) {
					break;
				}
			}

			if (count[0] - 1 > 0)
			{
				byte[] rfbbytes2 = new byte[count[0] - 1];
				for (int j = 0; j < count[0] - 1; j++)
				{
					rfbbytes2[j] = rfbbytes[j];
				}
				String str = new String(rfbbytes2, serverCharset);
				if (str.startsWith("<!UTF-8!>"))
				{
					str = new String(rfbbytes2, "UTF-8");
					str = str.substring(9);
				}
				//System.out.println("rfbbytes_to_string_ex: \"" + str + "\"");
				return str;
			}
			else {
				//System.out.println("rfbbytes_to_string_ex: \"<empty>\"");
				return "";
			}
		}
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
    }
    
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {   
           
        if(keyCode == KeyEvent.KEYCODE_BACK){   
        	finish();
            return true;   
        }else{         
            return super.onKeyDown(keyCode, event);   
        }   
    }
    
}

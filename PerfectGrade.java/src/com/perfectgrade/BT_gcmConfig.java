/*  File Version: 3.0
 *	Copyright, David Book, buzztouch.com
 *
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without modification, are 
 *	permitted provided that the following conditions are met:
 *
 *	Redistributions of source code must retain the above copyright notice which includes the
 *	name(s) of the copyright holders. It must also retain this list of conditions and the 
 *	following disclaimer. 
 *
 *	Redistributions in binary form must reproduce the above copyright notice, this list 
 *	of conditions and the following disclaimer in the documentation and/or other materials 
 *	provided with the distribution. 
 *
 *	Neither the name of David Book, or buzztouch.com nor the names of its contributors 
 *	may be used to endorse or promote products derived from this software without specific 
 *	prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 *	IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 *	INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 *	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 *	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 *	WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 *	ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 *	OF SUCH DAMAGE. 
 */

package com.perfectgrade;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class BT_gcmConfig{

	//name of this class for the debugger...
	static final String CLASS_NAME = "BT_gcmConfig";
	
    //Google GCM API Project Number (provided by google), setup in AndroidManifest.xml...
    static final String SENDER_ID = getGCMProjectNumber();

    //tags for log messages...
    static final String TAG = "GCM";
    static final String DISPLAY_MESSAGE_ACTION = "com.perfectgrade.DISPLAY_MESSAGE";
    static final String EXTRA_MESSAGE = "message";
 
    //tells UI to show a message...
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }	
    
	//get GCM Project Number from AndroidManifest.xml...
	public static String getGCMProjectNumber(){
		
		//get the googleAPIProjectNumber from the AndroidManifest.xml file...
		ApplicationInfo ai = null;
		String tmpGcmNumber = "";
		try{
			ai = perfectgrade_appDelegate.getApplication().getPackageManager().getApplicationInfo(perfectgrade_appDelegate.getApplication().getPackageName(), PackageManager.GET_META_DATA);
			tmpGcmNumber = (String)ai.metaData.get("googleCloudMessagingProjectNumber");	
			BT_debugger.showIt(CLASS_NAME + ":getGCMProjectNumber. Using Google GCM Project Number in AndroidManifest: \"" + tmpGcmNumber + "\"");
		}catch(NameNotFoundException e) {
			BT_debugger.showIt(CLASS_NAME + ":getGCMProjectNumber. EXCEPTION. Google GCM Project Number not found in AndroidManifest?");
		}

		//retuan...
		return tmpGcmNumber;
		
	}
	

	
}






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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


public class BT_activity_start extends Activity {
	public String activityName = "BT_activity_start";

	//onCreate...
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		BT_debugger.showIt(activityName + ":onCreate");	
		BT_debugger.showIt(activityName + ":onCreate This device is running Android Build Vers:" + Build.VERSION.SDK_INT);
		
		//make sure device is running Ice Cream Sandwich or higher...
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				adb.setTitle(getString(R.string.deviceNotSupportedTitle));
				adb.setMessage(getString(R.string.deviceNotSupportedDescription));
				adb.setIcon(R.drawable.icon);
				adb.setCancelable(false);
				adb.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						
						//kill app...
						finish();
						
					}
				});		
				adb.show();
		
		}else{
		
			//no title, full screen...
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 	    
			//inflate the view for this activity...
			setContentView(R.layout.bt_activity_start);
        
			//load app data...
			loadAppConfigData();
			
		}
        
  	}
	
	 //loadAppConfigData...
	 public void loadAppConfigData(){
		BT_debugger.showIt(activityName + ":loadAppConfigData");			

   		//initialize the loading fragment...
   		Fragment theFragment = new BT_fragment_load_config_data();
	    		
	 	//fragment transaction object...
	 	FragmentManager fm = getFragmentManager();
	 	FragmentTransaction ft = fm.beginTransaction();
	    ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);

	 	//show the fragment...
	 	ft.replace(R.id.fragmentBox, theFragment, "currentFragment");
	 	ft.commit();
		
	 }	 
	 
   
    //configureEnvironment...shows app's first screen after parsing config data...
    public void configureEnvironment(){
 		BT_debugger.showIt(activityName + ":configureEnvironment");
    
 		/*
 			This method is fired by the BT_fragment_load_config.java class after
 			loading all the app's data...Activities that use the BT_fragment_load_config
 			fragment should implement their own "configureEnvironment" method because the
 			loader fragment calls a method with that name in the host activity after it
 			completes. This is useful when "reloading" the app's data.
 		*/
 		
		//get the theme data to find a possible splash screen...
		BT_item theThemeData = perfectgrade_appDelegate.rootApp.getRootTheme();
		String splashScreenItemId = BT_strings.getJsonPropertyValue(theThemeData.getJsonObject(), "splashScreenItemId", "");
		
		//load a splash screen fragment if used...
		if(splashScreenItemId.length() > 1){
	 		
			BT_debugger.showIt(activityName + ":configureEnvironment This app uses a splash screen with JSON itemId: " + splashScreenItemId);
	 		BT_debugger.showIt(activityName + ":configureEnvironment Starting BT_activity_splash");
		
			BT_item splashScreenJSON = perfectgrade_appDelegate.rootApp.getScreenDataByItemId(splashScreenItemId);
			Fragment splashFrag = perfectgrade_appDelegate.rootApp.initPluginWithScreenData(splashScreenJSON);
			this.showFragmentForSplashScreen(splashFrag);
				
		
		}else{
	 		
			//no splash screen is used, kill this Activity and show the BT_host_activity...
			BT_debugger.showIt(activityName + ":configureEnvironment This app does not use a splash screen");
	 		BT_debugger.showIt(activityName + ":configureEnvironment Starting BT_activity_host");
		
			Intent i = new Intent(BT_activity_start.this, BT_activity_host.class);
			startActivity(i);
			
			//finish this activity (remove it from the back stack)...
			this.finish();
			
			//fade in next screen...
            overridePendingTransition(R.anim.bt_fadein,R.anim.bt_fadeout);

			
		}//splashScreenItemId...
 		
    }	
    
	 //showFragmentForSplashScreen...
	 public void showFragmentForSplashScreen(Fragment theFragment){
		BT_debugger.showIt(activityName + ":showFragmentForSplashScreen");			

		if(theFragment != null){
		
		 	//fragment transaction object...
		 	FragmentManager fm = this.getFragmentManager();
		 	FragmentTransaction ft = fm.beginTransaction();
		    ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
	
		 	//show the fragment...
		 	ft.replace(R.id.fragmentBox, theFragment, "currentFragment");
		 	ft.commit();
		
		}else{
			BT_debugger.showIt(activityName + ":showFragmentForSplashScreen ERROR: Splash Screen fragment is null?");			
		}
		 	
	 }	
    
    
    /*

	//handles rotation events...
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		switch(newConfig.orientation){
	    	case  Configuration.ORIENTATION_LANDSCAPE:
	    		BT_debugger.showIt(activityName + ":onConfigurationChanged to landscape");
	    		break;
	      	case Configuration.ORIENTATION_PORTRAIT:
	    		BT_debugger.showIt(activityName + ":onConfigurationChanged to portrait");
	    		break;
	      	case Configuration.ORIENTATION_UNDEFINED:
	      		BT_debugger.showIt(activityName + ":onConfigurationChanged is unidentified");
	      		break;
	      	default:
	    }	  
	}  		
	
	*/

}

































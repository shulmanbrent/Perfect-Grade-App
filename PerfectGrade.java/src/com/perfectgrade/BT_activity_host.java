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

import org.json.JSONObject;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gcm.GCMRegistrar;
import static com.perfectgrade.BT_gcmConfig.EXTRA_MESSAGE;
import com.perfectgrade.BT_fragment.BTFragmentResumedListener;


public class BT_activity_host extends Activity implements LocationListener, SensorEventListener, 
												TabListener, BTFragmentResumedListener{
	public String activityName = "BT_activity_host";
	public ProgressDialog loadingMessage = null;
	public String appLastModifiedOnServer = "";

	//action bar...
	public ActionBar actionBar;
	public Fragment currentFragment;
	
	//location listener...
	public LocationManager locationManager;
	public int locationUpdateCount = 0;
	public String locationListenerType = "";
	
	//accelerometer...decrease threshold to increase sensitivity...
	public final float SHAKE_THRESHOLD = (float) 7.0;
	public boolean sensorInitialized; 
	public SensorManager sensorManager; 
	public Sensor accelerometer; 
	public float shakeSpeedX, shakeSpeedY;

	//task to register with Google Cloud Messaging (GCM)...
	AsyncTask<Void, Void, Void> gcmRegisterTask;
	String gcmRegId = "";
	public boolean promptForPush = false;

	//////////////////////////////////////////////////////////////////////////
	//Activity life cycle events.
	
    //onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		BT_debugger.showIt(activityName + ":onCreate");	

		//must explicitly ask for an action bar on some devices...
	    getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	    
	    //setup the android action bar with a custom view for the title text...
	    actionBar = getActionBar();
   		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_USE_LOGO);
   		actionBar.setDisplayShowTitleEnabled(true);
   		actionBar.setDisplayUseLogoEnabled(true);	    
	    actionBar.setDisplayHomeAsUpEnabled(false);
   		actionBar.setTitle(getString(R.string.loading));
   		
 	    //inflate the bt_activity_host.xml for the content view...
        setContentView(R.layout.bt_activity_host);
    
        //configure the app's environment...
        configureEnvironment();
 	
	}
	
	//onStart
	@Override 
	public void onStart() {
		BT_debugger.showIt(activityName + ":onStart");	
		super.onStart();
		
		//start location manager?
		
		/*	Location Manager Logic (turn on GPS?)
		 	-------------------------------------
			Should this device report it's location? The device will turn on it's GPS and begin
		 	tracking it's location if three things are true:
		 	1) The application's configuration data is set to "startLocationUpdates" 
		 	2) The user has not turned on "allow location tracking" in a BT_screen_settingsLocation
		 	3) The device is capable of tracking it's location (has GPS)
		 */
		//Save battery! We remember the last reported location in the app's delegate so we don't have to turn on the GSP for every screen...
		if(!perfectgrade_appDelegate.foundUpdatedLocation && perfectgrade_appDelegate.rootApp.getRootDevice().canUseGPS()){
			if(perfectgrade_appDelegate.rootApp.getStartLocationUpdates().equalsIgnoreCase("1")){
				BT_debugger.showIt(activityName + ": start GPS is set to YES in the applications configuration data, trying to start GPS");
				if(!BT_strings.getPrefString("userAllowLocation").equalsIgnoreCase("prevent")){
					BT_debugger.showIt(activityName + ": user has not prevented the GPS from starting using a BT_screen_settingsLocation screen");
					
					//trigger method...
					this.getLastGPSLocation();
					
				}else{
					BT_debugger.showIt(activityName + ": user has prevented the GPS from starting using a BT_screen_settingsLocation screen");
				}
			}else{
				BT_debugger.showIt(activityName + ": start GPS is set to NO in the applications configuration data, not starting GPS");
			}
		}//already found location and saved it in the app's delegate.
		
		
		
	}
	
    //onResume
    @Override
    public void onResume() {
    	BT_debugger.showIt(activityName + ":onResume");	
    	
    	//handle accelerometer...
    	if(sensorManager != null){
    		if(perfectgrade_appDelegate.rootApp.getRootDevice().canDetectShaking()){
    			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    		}
    	}
    	
		//report to cloud...must have dataURL's
		if(perfectgrade_appDelegate.rootApp.getDataURL().length() > 1 && perfectgrade_appDelegate.rootApp.getReportToCloudURL().length() > 1){
			this.reportToCloud();
		}else{
	        BT_debugger.showIt(activityName + ":reportToCloud no dataURL and / or reportToCloudURL, both required for remote updates, not reporting.");	
		}
    	
    	//call super...
    	super.onResume();
   }
    
    //onPause
    @Override
    public void onPause() {
		BT_debugger.showIt(activityName + ":onPause");	
        super.onPause();
        
		//handle loading message...
		if(this.loadingMessage != null){
			if(this.loadingMessage.isShowing()){
				this.loadingMessage.hide();
			}
		}     
		
		//handle accelerometer...
    	if(sensorManager != null){
    		if(perfectgrade_appDelegate.rootApp.getRootDevice().canDetectShaking()){
    			sensorManager.unregisterListener(this);
    		}
    	}

    }
    
    //onStop
	@Override 
	public void onStop(){
		BT_debugger.showIt(activityName + ":onStop");	
		super.onStop();
		
		//hide loading message if it's showing...
		if(this.loadingMessage != null){
			if(this.loadingMessage.isShowing()){
				this.loadingMessage.hide();
			}
		} 		
		
	}	
	
	//onDestroy
    @Override
    public void onDestroy() {
		BT_debugger.showIt(activityName + ":onDestroy");	
		super.onDestroy();
		
		//kill possible GCM (push notifications) registration task...
		if (gcmRegisterTask != null) {
			gcmRegisterTask.cancel(true);
        }
        try{
            unregisterReceiver(baseHandlePushReceiver);
            GCMRegistrar.onDestroy(this);
        }catch(Exception e){
        	//ignore...
        }		

		//hide loading message if it's showing...
		if(this.loadingMessage != null){
			if(this.loadingMessage.isShowing()){
				this.loadingMessage.hide();
			}
		}     
    
    }
    
    //onSaveInstanceState...
    @Override
    public void onSaveInstanceState(Bundle saveState) {
		BT_debugger.showIt(activityName + ":onSaveInstanceState");	
    	super.onSaveInstanceState(saveState);
    }   
    
    //onActivityResult...
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
   		BT_debugger.showIt(activityName + ":onActivityResult");	
       	super.onActivityResult(requestCode, resultCode, data);
    }     

    //end Activity life-cycle events
	//////////////////////////////////////////////////////////////////////////
		
	//////////////////////////////////////////////////////////////////////////
	//configureEnvironment (can't do this until after app's data is loaded...
	public void configureEnvironment(){
		BT_debugger.showIt(activityName + ":configureEnvironment");
	
		//register for push logic...
		if(perfectgrade_appDelegate.rootApp.getPromptForPushNotifications().equals("1")){
			if(perfectgrade_appDelegate.rootApp.getRegisterForPushURL().length() > 3){
				BT_debugger.showIt(activityName + ":configureEnvironment Prompt For Push Notifications is ON (set to 1).");
				promptForPush = true;
			}else{
				BT_debugger.showIt(activityName + ":configureEnvironment Prompt For Push Notifications is ON (set to 1) but no registerForPushURL was found?");
			}
		}else{
			BT_debugger.showIt(activityName + ":configureEnvironment Prompt For Push Notifications is OFF (set to 0) in app's configuration data");
		}

		//if setup for push...
		if(promptForPush){
			try{
		        	
				GCMRegistrar.checkDevice(this);
		        GCMRegistrar.checkManifest(this);
		        	
		        //setup intent to "listen" for messages from GCM...
		        registerReceiver(baseHandlePushReceiver, new IntentFilter("com.perfectgrade.DISPLAY_MESSAGE"));        	
		        	
		        //get a possible existing gcmRegId...
		        gcmRegId = GCMRegistrar.getRegistrationId(this);

			}catch(java.lang.NoClassDefFoundError e){
				BT_debugger.showIt(activityName + ":configureEnvironment Error configuring Push Notification setup. EXCEPTION " + e.toString());
			}
		}
		
		//see if this device is already registered...
		if(gcmRegId.length() < 1 && promptForPush){
			
			//prompt user for "allow" push notifications if they have NOT previously said "no thanks"...
            if(BT_fileManager.doesCachedFileExist("rejectedpush.txt")){
				BT_debugger.showIt(activityName + ":configureEnvironment Device owner has rejected Push Notifications");
            }else{
            	confirmRegisterForPush();
            }
			
		}
		
		//log message if device is already registered...
		if(gcmRegId.length() > 1){
			BT_debugger.showIt(activityName + ":configureEnvironment Device is registered for Google Cloud Messaging (Push) with token: " + gcmRegId);
		}
		
	    //setup accelerometer to listen for shaking events...
        sensorInitialized = false;
        if(perfectgrade_appDelegate.rootApp.getRootDevice().canDetectShaking()){
        	sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        	accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        	sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        
		//location Manager. Remember the last reported location in the app's delegate so we don't have to turn on the GPS for every screen...
		if(!perfectgrade_appDelegate.foundUpdatedLocation && perfectgrade_appDelegate.rootApp.getRootDevice().canUseGPS()){
			if(perfectgrade_appDelegate.rootApp.getStartLocationUpdates().equalsIgnoreCase("1")){
				BT_debugger.showIt(activityName + ": start GPS is set to YES (set to 1) in the applications configuration data, trying to start GPS");
				if(!BT_strings.getPrefString("userAllowLocation").equalsIgnoreCase("prevent")){
					BT_debugger.showIt(activityName + ":configureEnvironment user has not prevented the GPS from starting using a BT_screen_settingsLocation screen");
					
					//grab last GPS location...
					getLastGPSLocation();
					
				}else{
					BT_debugger.showIt(activityName + ":configureEnvironment user has prevented the GPS from starting using a BT_screen_settingsLocation screen");
				}
			}else{
				BT_debugger.showIt(activityName + ":configureEnvironment start GPS is set to NO (set to 0) in the applications configuration data, not starting GPS");
			}
		}//already found location and saved it in the app's delegate.
			
		//setup tabs (zero means "show first tab as selected")...
		setupTabs(0);

	}
	
		
	//////////////////////////////////////////////////////////////////////////
	//tab setup and interface methods...
	public void setupTabs(int selectedTab){
		BT_debugger.showIt(activityName + ":setupTabs  (" + perfectgrade_appDelegate.rootApp.getTabs().size() + " tabs)");

		//configure action bar...
        actionBar = getActionBar();
       	if(actionBar != null){
       		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_USE_LOGO);
       		actionBar.setDisplayShowTitleEnabled(true);
       		actionBar.setDisplayUseLogoEnabled(true);
       		
       		//pre 4.0 does not allow this...
       		try{
       			actionBar.setHomeButtonEnabled(true);
       			actionBar.setDisplayHomeAsUpEnabled(false);
       		}catch(java.lang.NoSuchMethodError e){
       			BT_debugger.showIt(activityName + ":setupTabs Cannot set homeButtonEnabled on this version of Android");
       		}
       		
       		//remove tabs if we're using them...
       		if(actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS){
       			actionBar.removeAllTabs();
       		}
       		
       	}else{
    		BT_debugger.showIt(activityName + ":setupTabs ActionBar is null?");
       	}
       	
       	//get home screen JSON data (for screen connected to first tab or home screen if not tabbed)...
		BT_item tmpHomeScreenData = perfectgrade_appDelegate.rootApp.getHomeScreenData();
		
		//use tabs if we have them....
		if(perfectgrade_appDelegate.rootApp.getTabs().size() > 0){
			
			//change navigation mode...
	    	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    	
	    	//setup tabs with title and icon. NOTE: Android Action bars generally use text only for tabs and not icons...
	    	for(int t = 0; t < perfectgrade_appDelegate.rootApp.getTabs().size(); t++){
	    		BT_item thisTab = perfectgrade_appDelegate.rootApp.getTabs().get(t);
	       		
	    		//get title and icon for this tab...
	    		String title = BT_strings.getJsonPropertyValue(thisTab.getJsonObject(), "textLabel", "");
	    		Drawable d = null;
	    		String iconName = BT_strings.getJsonPropertyValue(thisTab.getJsonObject(), "iconName", "");
	    		if(iconName.length() > 0){
	    			d = BT_fileManager.getDrawableByName(iconName);
	    		}
	    		
	        	ActionBar.Tab tmpTab = actionBar.newTab();
	    		tmpTab.setText(title);
	    		tmpTab.setTabListener(this);
	    		tmpTab.setTag(t);
	    		if(d != null) tmpTab.setIcon(d);
				actionBar.addTab(tmpTab); 

	    	}
	    	
	    	//select default tab if not the default...
	    	if(selectedTab > 0){
	    		actionBar.setSelectedNavigationItem(selectedTab);
	    		
	    		//remember the selected tab index in the BT_application object...
	    		perfectgrade_appDelegate.rootApp.setSelectedTab(selectedTab);
	    		
	    	}

		}else{ //not using tabs...
		
			BT_debugger.showIt(activityName + ":setupTabs (no tabs, finding home screen)");
			tmpHomeScreenData.setIsHomeScreen(true);
			
			//show the home screen fragment...
			this.showAppHomeScreen();
			
		}	
		
		//setup home screen nav bar and background properties...
	 	if(tmpHomeScreenData != null){
   		
	 		//setup nav bar and background for this fragment...
	 		this.configureNavBarAndBackgroundForScreen(tmpHomeScreenData);
		
	 		//remember this fragment's JSON data as the "current screen" so we can refer to it later...
	 		perfectgrade_appDelegate.rootApp.setCurrentScreenData(tmpHomeScreenData);
	 	
	 	}
	 	
	}
	
	//onTabSelected...
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		BT_debugger.showIt(activityName + ":onTabSelected Tab Index: " + tab.getPosition());
		
		//get the JSON data for the selected tab...
		BT_item thisTab = perfectgrade_appDelegate.rootApp.getTabs().get(tab.getPosition());
		String homeScreenItemId = BT_strings.getJsonPropertyValue(thisTab.getJsonObject(), "homeScreenItemId", "");

		//get the JSON data for this screen...
		BT_item screenData = perfectgrade_appDelegate.rootApp.getScreenDataByItemId(homeScreenItemId);
		
		//set as "homeScreen" if this is tab zero...
   		screenData.setIsHomeScreen(tab.getPosition() == 0 ? true : false);
     	
		//create the fragment for the selected tab...
		Fragment theFrag = this.initPluginWithScreenData(screenData);
   		
  		//show the fragment...
   		this.showFragmentForPlugin(theFrag, screenData, false, "");
   		
		//remember the selected tab index in the BT_application object...
		perfectgrade_appDelegate.rootApp.setSelectedTab(tab.getPosition());
		
	}


	//onTabUnselected...
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		//BT_debugger.showIt(activityName + ":onTabUnselected Tab Index: " + tab.getPosition());
	}
	
	//onTabReselected...
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		BT_debugger.showIt(activityName + ":onTabReselected Tab Index: " + tab.getPosition());
        
		//get the JSON data for the selected tab...
		BT_item thisTab = perfectgrade_appDelegate.rootApp.getTabs().get(tab.getPosition());
		String homeScreenItemId = BT_strings.getJsonPropertyValue(thisTab.getJsonObject(), "homeScreenItemId", "");

		//get the JSON data for this screen...
		BT_item screenData = perfectgrade_appDelegate.rootApp.getScreenDataByItemId(homeScreenItemId);
		
		//set as "homeScreen" if this is tab zero...
   		screenData.setIsHomeScreen(tab.getPosition() == 0 ? true : false);
     	
		//create the fragment for the selected tab...
		Fragment theFrag = this.initPluginWithScreenData(screenData);
   		
  		//show the fragment...
   		this.showFragmentForPlugin(theFrag, screenData, false, "");
   		
		//remember the selected tab index in the BT_application object...
		perfectgrade_appDelegate.rootApp.setSelectedTab(tab.getPosition());
		
	}	
	/////////////////////////////////////////////////////////////////////////////////////////

	
	/////////////////////////////////////////////////////////////////////////////////////////
 	//fragment changed listener. See BT_fragment.java for interface declaration...
	public void BTFragmentResumed(BT_item theScreenData) {
	 	BT_debugger.showIt(activityName + ":BTFragmentResumed");
	 	
	 	//setup the fragment's nav bar...
	 	if(theScreenData != null){
   		
	 		//setup nav bar for this fragment...
	 		this.configureNavBarAndBackgroundForScreen(theScreenData);
	 		
	 		//remember this fragment's JSON data as the "current screen" so we can refer to it later...
	 		perfectgrade_appDelegate.rootApp.setCurrentScreenData(theScreenData);
	 	
	 	}

	}
	/////////////////////////////////////////////////////////////////////////////////////////
 	
	
	
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
		BT_debugger.showIt(activityName + ":onBackPressed");

		//currentFragment must exist...
		if(currentFragment != null){
		
			
			/*
	        	use reflection to trigger a handleBackButton method in the current plugin...not all
	        	plugins implement this method...but can if it's needed.
	        	
				In most cases the handleBackButton method should "go back" by popping the current
				fragment off the stack with:
				
				getActivity().getFragmentManager().popBackStackImmediate();
			*/
			
			java.lang.reflect.Method backMethod = null;
			try{
				backMethod = currentFragment.getClass().getMethod("handleBackButton");
			}catch(SecurityException e){
				BT_debugger.showIt(activityName + ":onBackPressed. EXCEPTION (0): " + e.toString());
			}catch(NoSuchMethodException e){
				BT_debugger.showIt(activityName + ":onBackPressed. Current fragment does not have an \"onBackPressed\" method implemented");
			}
			
			//fire the handleBackButton method if we found it...
			if(backMethod != null){
		    	try{
		    		backMethod.invoke(currentFragment);
				}catch(Exception e){
					BT_debugger.showIt(activityName + ":onBackPressed. EXCEPTION (1): " + e.toString());
				}
			}
		
		}
		
    }
	
	//handles rotation events...
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		switch(newConfig.orientation){
	    	case  Configuration.ORIENTATION_LANDSCAPE:
	    		BT_debugger.showIt(activityName + ":onConfigurationChanged to landscape");
	    		perfectgrade_appDelegate.rootApp.getRootDevice().updateDeviceOrientation("landscape");
	    		break;
	      	case Configuration.ORIENTATION_PORTRAIT:
	    		BT_debugger.showIt(activityName + ":onConfigurationChanged to portrait");
	      		perfectgrade_appDelegate.rootApp.getRootDevice().updateDeviceOrientation("portrait");
	    		break;
	      	case Configuration.ORIENTATION_UNDEFINED:
	      		BT_debugger.showIt(activityName + ":onConfigurationChanged is unidentified");
	      		perfectgrade_appDelegate.rootApp.getRootDevice().updateDeviceOrientation("portrait");
	      		break;
	      	default:
	    }	  
		  
		//update device size so we can keep track...
		perfectgrade_appDelegate.rootApp.getRootDevice().updateDeviceSize();
	} 
	
	
	//show alert message
	public void showAlert(String theTitle, String theMessage) {
		if(theTitle.equals("")) theTitle = "No Alert Title?";
		if(theMessage.equals("")) theMessage = "No alert message?";

		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(theTitle);
		adb.setMessage(theMessage);
		adb.setCancelable(false);
		adb.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});			
		
		//create dialogue
		AlertDialog ad = adb.create();
		ad.show();

	}	
	
	
	//showToast...
	public void showToast(String theMessage, String shortOrLong){
		Toast toast = null;
		if(shortOrLong.equalsIgnoreCase("short")){
			toast = Toast.makeText(getBaseContext(), theMessage, Toast.LENGTH_SHORT);
		}else{
			toast = Toast.makeText(this, theMessage, Toast.LENGTH_LONG);
		}
		toast.show();
	}
	
	
	//showProgress....
	public void showProgress(String theTitle, String theMessage){
		
		//validate title and message passed into this method...
		if(theTitle == null) theTitle =  "";
		if(theMessage == null) theMessage = "";

		if(theTitle.length() < 1) theTitle =  getString(R.string.loading);
		if(theMessage.length() < 1) theMessage =  "";
		
		//show an Android Progress Dialog...
		loadingMessage = ProgressDialog.show(this, theTitle, theMessage, true);
		loadingMessage.setCanceledOnTouchOutside(false);
		loadingMessage.setCancelable(false);	
		
	}
 
	
	//hideProgress...
	public void hideProgress(){
		if(loadingMessage != null && loadingMessage.isShowing()){
			loadingMessage.dismiss();
		}
	}
	
	//confirmRefreshAppData...(asked after reportingToCloud)
	public void confirmRefreshAppData(){

		final AlertDialog confirmRefreshAlert = new AlertDialog.Builder(this).create();
		confirmRefreshAlert.setTitle(getString(R.string.confirmRefreshTitle));
		confirmRefreshAlert.setMessage(getString(R.string.confirmRefreshDescription));
		confirmRefreshAlert.setIcon(R.drawable.icon);
		confirmRefreshAlert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  
	    	  //refresh data...
	    	  confirmRefreshAlert.dismiss();
	    	  refreshAppData();
	    	  
	    } }); 
		confirmRefreshAlert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  confirmRefreshAlert.dismiss();
		    } }); 
		confirmRefreshAlert.show();
	}		
	
	//refreshAppData...
	public void refreshAppData(){
		BT_debugger.showIt(activityName + ":refreshAppData");
		
		//setTitle, remove tabs...
		if(actionBar != null){
			actionBar.setTitle(getString(R.string.loading));
			if(actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS){
				actionBar.removeAllTabs();
			}
		}
		
		//create an instance of our config data loader fragment...
		BT_fragment_load_config_data loaderFrag = new BT_fragment_load_config_data();
		loaderFrag.needsRefreshed = true;
		
		//show this frag...
		this.showFragmentForPlugin(loaderFrag, null, false, "");
		
	}
	
	/////////////////////////////////////////////////////
	//Google Cloud Messaging methods (Push Notifications)
	
	//confirmRegisterForPush...
	public void confirmRegisterForPush(){

		final AlertDialog confirmPushAlert = new AlertDialog.Builder(this).create();
		confirmPushAlert.setTitle("Accept Push Notifications?");
		confirmPushAlert.setMessage("This app would like to send you notifications and simple messages. Is this OK?");
		confirmPushAlert.setIcon(R.drawable.icon);
		confirmPushAlert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  
	    	  //dismiss dialgue...
	    	  confirmPushAlert.dismiss();
	    	  
	    	  //register for push...
	    	  registerForPush();
	    	  
	    } }); 
		confirmPushAlert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  
		    	  //save a text file to remember that user "rejected push"...
		    	  BT_fileManager.saveTextFileToCache("rejected", "rejectedpush.txt");

		    	  //dismiss dialgue..		    	  
		    	  confirmPushAlert.dismiss();
		    	  
		    } }); 
		confirmPushAlert.show();
	}
	
	//registerForPush...
	public void registerForPush(){
		BT_debugger.showIt(activityName + ":registerForPush");
		
		//CGM Push classes must be available on the device (must have Google Play app installed)...
		try{  

		    //remove "rejected push" file...
	  	  	BT_fileManager.deleteFile("rejectedpush.txt");
			
			boolean doSetupForPush = false;
			if(perfectgrade_appDelegate.rootApp.getPromptForPushNotifications().equals("1")  && perfectgrade_appDelegate.rootApp.getRegisterForPushURL().length() > 3){
				doSetupForPush = true;
			}
			
			//see if this device is already registered...
			if(gcmRegId.equals("") && doSetupForPush){
	        	BT_debugger.showIt(activityName + ":device is NOT registered with GCM (Google Cloud Messaging)");
	
	        	//GCMRegistration id not available, register now...
	        	perfectgrade_appDelegate.rootApp.setPushRegistrationId("");
				GCMRegistrar.register(this, BT_gcmConfig.SENDER_ID);
				
			}
			
			
			//if we already have a registration id, make sure it's registered on server...
			if(!gcmRegId.equals("") && doSetupForPush) {
				
				//device is registered on GCM already...
	            if(GCMRegistrar.isRegisteredOnServer(this)) {
	    		
	            	BT_debugger.showIt(activityName + ":device is registered with GCM (Google Cloud Messaging)");
	            	perfectgrade_appDelegate.rootApp.setPushRegistrationId(gcmRegId);
	
	            }else{
	                
	            	//try to register again, off the UI thread...
	                final Context context = this;
	                gcmRegisterTask = new AsyncTask<Void, Void, Void>(){
	 
	                    @Override
	                    protected Void doInBackground(Void... params) {
	
	                    	//register on backend server...
	                        BT_gcmServerUtils.gcmRegisterOnServer(context, gcmRegId);
	                        return null;
	                    }
	 
	                    @Override
	                    protected void onPostExecute(Void result) {
	                    	gcmRegisterTask = null;
	                    }
	 
	                };
	                gcmRegisterTask.execute(null, null, null);
	            }			
			} //gcmRegId == ""
			
			
		}catch (Exception exception) {
	        BT_debugger.showIt(activityName + ": EXCEPTION. Google GCM (Push Notifications) not supported on this device");
		}

		
		
	}
	
	//unregisterForPush...
	public void unregisterForPush(){
		BT_debugger.showIt(activityName + ":unregisterForPush");
		
  	  	//save a text file to remember that user "rejected push"...
  	  	BT_fileManager.saveTextFileToCache("rejected", "rejectedpush.txt");
		
       	//try to register again, off the UI thread...
        final Context context = this;
        gcmRegisterTask = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {

            	//register on backend server...
                BT_gcmServerUtils.gcmUnregisterOnServer(context, gcmRegId);
                
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
            	gcmRegisterTask = null;
            }

        };
        gcmRegisterTask.execute(null, null, null);
  	  	
	}	
	
 	//Broadcast Receiver to handle Push Notifications...
    private final BroadcastReceiver baseHandlePushReceiver = new BroadcastReceiver() {
    	@Override
        public void onReceive(Context context, Intent intent) {
 	 		BT_debugger.showIt(activityName + ":BroadcastReceiver baseHandlePushReceiver");
 	 		String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
 	 		
            //wake device if sleeping...
			BT_gcmWakeLocker.acquire(getApplicationContext());
	 
			//show quick toast so user knows a new message arrived...
			showToast(newMessage, "long");
	            
	        //releasing wake lock
			BT_gcmWakeLocker.release();
				
        }//getIntent
    }; 	
	
	
	//END Google Cloud Messaging Methods (Push)
	/////////////////////////////////////////////////////
	
	
	

	/////////////////////////////////////////////////////
	//sensor events (device shaking)...
	
	//onAccuracyChanged...
	public void onAccuracyChanged(Sensor arg0, int arg1){
    	//BT_debugger.showIt(activityName + ":onAccuracyChanged (shake detection by accelerometer)");
    	
    	/*
    	 	This method is unused, it's included because this Activity implements
    	 	the SensorEventListener class and this method is required by that class
    	*/ 	
    	
	}

	//onSensorChanged...
	public void onSensorChanged(SensorEvent event){
    	//BT_debugger.showIt(activityName + ":onSensorChanged (shake detection by accelerometer)");
    	
    	//acceleromoter values passed to this onSensorChanged method (event is an array of values passed to this method)...
		//the values passed in are normally float data types. We cast them to integers for simplicity...
		float x = (float)event.values[0];
		float y = (float)event.values[1];
    	
    	if(!sensorInitialized){
       		
    		//flag as initialized...
    		sensorInitialized = true;
     		
    		//remember values...
    		shakeSpeedX = x;
    		shakeSpeedY = y;
    		
    	}else{
    		
    		
    		float currentX = Math.abs(shakeSpeedX - x);
    		float currentY = Math.abs(shakeSpeedY - y);
    		if (currentX < SHAKE_THRESHOLD) currentX = 0;
    		if (currentY < SHAKE_THRESHOLD) currentY = 0;
    		shakeSpeedX = x;
    		shakeSpeedY = y;
    		
        	//is the device shaking...
    		boolean isShaking = false;
     		if(currentX > currentY){
    			//shaking horizontal...
            	//BT_debugger.showIt(activityName + ":onSensorChanged (shake detection). Device is shaking horizontally. X: " + Float.toString(shakeSpeedX) + " Y:" + Float.toString(shakeSpeedY));
            	perfectgrade_appDelegate.rootApp.getRootDevice().setIsShaking(true);
            	isShaking = true;
     		}else if(currentY > currentX){
    			//shaking veritcal...
            	//BT_debugger.showIt(activityName + ":onSensorChanged (shake detection). Device is shaking vertically. X: " + Float.toString(shakeSpeedX) + " Y:" + Float.toString(shakeSpeedY));
            	perfectgrade_appDelegate.rootApp.getRootDevice().setIsShaking(true);
            	isShaking = true;
     		}else{
    			//not shaking...
            	perfectgrade_appDelegate.rootApp.getRootDevice().setIsShaking(false);
            	isShaking = false;
    		}
     		
            //use reflection to trigger the deviceIsShaking method in the current plugin...
     		if(isShaking){
     			
	    		java.lang.reflect.Method shakeMethod = null;
	    		try{
	    			shakeMethod = currentFragment.getClass().getMethod("deviceIsShaking", float.class, float.class);
	    		}catch(SecurityException e){
	    			BT_debugger.showIt(activityName + ":onSensorChanged. EXCEPTION (0): " + e.toString());
	    		}catch(NoSuchMethodException e) {
	    			BT_debugger.showIt(activityName + ":onSensorChanged. Current fragment does not have an \"deviceIsShaking\" method implemented");
	    		}
    		
	    		//fire the deviceIsShaking method if we found it...
	    		if(shakeMethod != null){
	    	    	try{
	    	    		shakeMethod.invoke(currentFragment, shakeSpeedX, shakeSpeedY);
	    			}catch(Exception e){
	    				BT_debugger.showIt(activityName + ":onSensorChanged. EXCEPTION (1): " + e.toString());
	    			}
	    		}
    		
     		}//isShaking...
     		
    	}    	
	}
	
	//end sensor events (device shaking)...
	/////////////////////////////////////////////////////
	
	
  
     
	/////////////////////////////////////////////////////
    //Location Manager Methods
	
	//getLastGPSLocation...
     public void getLastGPSLocation(){
         try{
         	//only ask for location info "once" when app launches (saves battery)
 			if(!perfectgrade_appDelegate.foundUpdatedLocation){
 				locationUpdateCount = 0;
 	        	if(this.locationManager == null){
 	        		this.locationUpdateCount = 0;
 	        		this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);   
 	        	}
 	        	if(this.locationManager != null){
 	            	Location lastLocation = this.locationManager.getLastKnownLocation("gps");
 	            	if(lastLocation != null){
 	            	    
 	            		
 	            		//remember in delegate
 	            		perfectgrade_appDelegate.rootApp.getRootDevice().setDeviceLatitude(String.valueOf(lastLocation.getLatitude()));
 	            		perfectgrade_appDelegate.rootApp.getRootDevice().setDeviceLongitude(String.valueOf(lastLocation.getLongitude()));

 	            		String s = "";
 	            		//s += " Updated: " + lastLocation.getTime();
 	            		s += " Lat: " + lastLocation.getLatitude();
 	            		s += " Lon: " + lastLocation.getLongitude();
 	            		s += " Accuracy: " + lastLocation.getAccuracy();
 	            		BT_debugger.showIt(activityName + ":getLastGPSLocation " + s);
 	            		
 	            	}
 	            	//start listening for location updates if we have GPS enabled...
 	            	if(this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 	            		locationListenerType = "GPS";
 	            		startListeningForLocationUpdate();
 	            	}else{
 	            		if(this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 	            			locationListenerType = "NETWORK/CELL";
 	            			startListeningForLocationUpdate();
 	            		}else{
 	 	            		BT_debugger.showIt(activityName + ":getLastGPSLocation Can't start GPS or Network Services to get location info.");
 	            		}
 	            	}
 	            	
 	        	}else{
 	        		BT_debugger.showIt(activityName + ":getLastGPSLocation locationManager == null?");
 	        	}
 			}
         }catch (Exception je){
      		BT_debugger.showIt(activityName + ":getLastGPSLocation EXCEPTION " + je.toString());
 	    }
     }
 	
 	//LocationListener must implement these methods
 	
    //onProviderDisabled (location manager provider)...
    public void onProviderDisabled(String theProvider){
  		BT_debugger.showIt(activityName + ":onProviderDisabled The GPS is disabled on this device.");
 	};  
 	
 	//onProviderEnabled (location manager provider)...
 	public void onProviderEnabled(String theProvider){
  		BT_debugger.showIt(activityName + ":onProviderDisabled The GPS is enabled on this device.");
 	};
 	
 	//onLocationChanged...
 	public void onLocationChanged(Location location){
 		this.locationUpdateCount++;
  		BT_debugger.showIt(activityName + ":onLocationChanged The device's location changed.");
   		
  		try{

  			/*
  			 	Example of how to get the device's current location in any code you write anywhere in your app....

  				String myLatitude = perfectgrade_appDelegate.rootApp.getRootDevice().getDeviceLatitude();
  				String myLongitude = perfectgrade_appDelegate.rootApp.getRootDevice().getDeviceLongitude();
  				
  				Then convert myLatitude and myLongitude strings to doubles, integers, floats as needed.
  			 
  			*/
  			
	 	    perfectgrade_appDelegate.rootApp.getRootDevice().setDeviceLatitude(String.valueOf(location.getLatitude()));
	 	    perfectgrade_appDelegate.rootApp.getRootDevice().setDeviceLongitude(String.valueOf(location.getLongitude()));
	 	    
	 		String s = "";
     		s += "From: " + locationListenerType;
	 		//s += " Updated: " + location.getTime();
	 		s += " Lat:: " + location.getLatitude();
	 		s += " Lon:: " + location.getLongitude();
	 		s += " Accuracy:: " + location.getAccuracy();
	 		BT_debugger.showIt(activityName + ":onLocationChanged " + s);
	 		
	 		//stop listening after 10 reports (about 10 seconds) or if we have good accuracy faster....
	 		if(locationUpdateCount > 10 || location.getAccuracy() < 25){
	 	  		BT_debugger.showIt(activityName + ":onLocationChanged turning off GPS to save battery, saved last location.");
	 			
	 	  		//flag foundUpdatedLocation in the delgate so other screens don't turn on the GPS	 	  		
	 	  		perfectgrade_appDelegate.foundUpdatedLocation = true;
	 	  		
	 	  		//stop listening (kill the locationManager)...
	 			stopListeningForLocationUpdate();
	 			
	 		}
  		}catch(Exception e){
  			
  		}
  	};
  	
  	//onStatusChanged...
 	public void onStatusChanged(String theProvider, int status, Bundle extras){
 		try{
 			BT_debugger.showIt(activityName + ":onStatusChanged (for the location manager)");
 		}catch(Exception e){
 			
 		}
 	};
 	
 	//startListeningForLocationUpdate..
 	public void startListeningForLocationUpdate(){
 		BT_debugger.showIt(activityName + ":startListeningForLocationUpdate (started listening for location changes)");
 		try{
 			if(this.locationManager != null){
 				
 				//we we started this in getLastGPSLocation() we set a flag to tell us what type of service to setup..
 				if(locationListenerType == "GPS"){
 					//request updates from GPS...
     				this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
     				BT_debugger.showIt(activityName + ":startListeningForLocationUpdate asking for GPS locations updates...");
 				}
 				if(locationListenerType == "NETWORK/CELL"){
 					//request updates from Network (Cell Towwers, Wi-Fi)...
     				this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
     				BT_debugger.showIt(activityName + ":startListeningForLocationUpdate asking for Network (Cell Tower or Wi-Fi) location updates...");
 				}
 				
  			}
 		}catch(Exception e){
 		}
     }
 	
 	//stopListeningForLocationUpdate...
 	public void stopListeningForLocationUpdate(){
 		BT_debugger.showIt(activityName + ":stopListeningForLocationUpdate (stopped listening to location changes)");
        try{ 
        	if(this.locationManager != null){
        		this.locationManager.removeUpdates(this);
        		this.locationManager = null;
        	}
        }catch(Exception e){
        	
        }
     }
 	//END location methods
	/////////////////////////////////////////////////////

    
    //initPluginWithScreenData. Returns a Fragment for a plugins JSON...
    public Fragment initPluginWithScreenData(BT_item theScreenData){
    	
		if(theScreenData != null){
			BT_debugger.showIt(activityName + ":initPluginWithScreenData. Calling helper method in BT_application");
		}
	 	
	 	
	 	//this is a helper method that uses the BT_application class to get the fragment...
	 	Fragment theFrag = perfectgrade_appDelegate.rootApp.initPluginWithScreenData(theScreenData);
	 	
		//return...
		return theFrag;
	 	
    }
    
    //showFragmentForPlugin...
    public void showFragmentForPlugin(Fragment theFragment, BT_item theScreenData, boolean addToBackStack, String transitionType){
	 	
    	if(theScreenData == null){
	 		BT_debugger.showIt(activityName + ":showFragmentForPlugin \"NULL SCREEN DATA\"");
	 	}else{
			BT_debugger.showIt(activityName + ":showFragmentForPlugin. Showing plugin with JSON itemId: \"" + theScreenData.getItemId() + "\" itemType: \"" + theScreenData.getItemType() + "\" itemNickname: \"" + theScreenData.getItemNickname() + "\"");
	 	}
	 	//remember the "current" screen data as the "previous" screen data...
	 	perfectgrade_appDelegate.rootApp.setPreviousScreenData(perfectgrade_appDelegate.rootApp.getCurrentScreenData());

	 	//fragment transaction object...
	 	FragmentManager fm = getFragmentManager();
	 	FragmentTransaction ft = fm.beginTransaction();
	    ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);

	 	//find the transition resources if we're using one...
	 	if(transitionType.length() > 1){
	 		
				//ANIMATIONS BETWEEN FRAGMENTS NOT IMPLEMENTED
				//--------------------------------------------
	 			int enterTrans = BT_viewUtilities.getEnterTransitionFromType(transitionType);
	 			int exitTrans = BT_viewUtilities.getExitTransitionFromType(transitionType);
	 			if(enterTrans > 0 && exitTrans > 0){
			 		ft.setCustomAnimations(enterTrans, exitTrans);
	 			}
	 		
	 		
	 	}
	 	
 	 	//assume this fragment should be added to the back-stack...Tab Taps should NOT be added to back-stack...
	 	if(addToBackStack != false){
	 		ft.addToBackStack("itemTap");
	 	}else{
	 		//do nothing...
	 	}
	 	
	 	//show the fragment...
	 	ft.replace(R.id.fragmentBox, theFragment, "currentFragment");
	 	ft.commit();
	 	
 		//remember this fragment's JSON data as the "current screen data" so we can refer to it later...
 		perfectgrade_appDelegate.rootApp.setCurrentScreenData(theScreenData);

 		//remember this fragment so we can refer to it later...
 		this.currentFragment = theFragment;
 		
		//setup nav bar and background...
		this.configureNavBarAndBackgroundForScreen(theScreenData);
	 	
    }    
    
    //showAppHomeScreen...
    public void showAppHomeScreen(){
	 	BT_debugger.showIt(activityName + ":showAppHomeScreen");
		  
	 	//get screen data for the app's home screen...
		  BT_item homeScreenData = perfectgrade_appDelegate.rootApp.getHomeScreenData();
		  Fragment homeScreenFrag = this.initPluginWithScreenData(homeScreenData);
	  
		  //show the home screen fragment...
		  this.showFragmentForPlugin(homeScreenFrag, homeScreenData, false, "");
	  
		  //set selected tab (if we're using tabs)...
		  if(actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS){
			  actionBar.setSelectedNavigationItem(0);
		  }
		  
		  //setup nav bar and background for this fragment...
		  this.configureNavBarAndBackgroundForScreen(homeScreenData);
		
		  //remember this fragment's JSON data as the "current screen" so we can refer to it later...
		  perfectgrade_appDelegate.rootApp.setCurrentScreenData(homeScreenData);
	 	
    }
    
    
    //configureNavBarAndBackgroundForScreen. Returns a Fragment for a plugins JSON...
    public void configureNavBarAndBackgroundForScreen(BT_item theScreenData){
        
    	if(theScreenData == null){
        	BT_debugger.showIt(activityName + ":configureNavBarAndBackgroundForScreen. NULL SCREEN DATA?");
        }else{
        	BT_debugger.showIt(activityName + ":configureNavBarAndBackgroundForScreen Calling helper methods in BT_viewUtilities...");

        	
        	//get the JSON data for this screen...
			String navBarTitleText = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "navBarTitleText", "");
	    	String navBarStyle = BT_strings.getStyleValueForScreen(theScreenData, "navBarStyle", "default");
	    	String navBarBackgroundColor = BT_strings.getStyleValueForScreen(theScreenData, "navBarBackgroundColor", "");
	    	String hideTabBarWhenScreenLoads = BT_strings.getStyleValueForScreen(theScreenData, "hideTabBarWhenScreenLoads", "");
	    	
			//DEPRECATED: older configurations may use "hideBottomTabBarWhenScreenLoads" instead....
			if(hideTabBarWhenScreenLoads.length() < 1){
	    		hideTabBarWhenScreenLoads = BT_strings.getStyleValueForScreen(theScreenData, "hideBottomTabBarWhenScreenLoads", "");
			}
			if(hideTabBarWhenScreenLoads.length() < 1){
				hideTabBarWhenScreenLoads = "0";
			}
			
			
	    	
			//set action bar title truncating...
	  	    final int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
	   	    final TextView title = (TextView)getWindow().findViewById(actionBarTitle);
	   	    if (title != null){
	   	    	title.setEllipsize(TruncateAt.MARQUEE);
	   	    }
			
	   	    //set title...
	   	    if(actionBar != null){
	   	    	
	   	    	//set title...
	   	    	actionBar.setTitle(navBarTitleText);
	   	    	
	   	    	//show or hide the "up/back" arrow on the actionBar if this is or is not the home screen (hide on home screen)..
	   	    	actionBar.setDisplayHomeAsUpEnabled(!theScreenData.isHomeScreen());
	   	    	
	   	    	//action bar color...
	   	    	if(navBarBackgroundColor.length() > 0){
	   	    		ColorDrawable cd = new ColorDrawable(BT_color.getColorFromHexString(navBarBackgroundColor));
	   	    		actionBar.setBackgroundDrawable(cd);
	   	    	}
	   	    
	   	    	//hide the action bar as needed...
	   	    	if(navBarStyle.equalsIgnoreCase("hidden")){
	   	    		actionBar.hide();
	   	    	}else{
	   	    		actionBar.show();
	   	    	}
			
	   	    	//hide the tabs if needed..
	   	    	if(hideTabBarWhenScreenLoads.equalsIgnoreCase("1")){
	   	    		actionBar.removeAllTabs();
	   	    	}else{
	   	    		
	   	    		//setup tabs again if they were removed previously...
	   	    		if(actionBar.getTabCount() < 1){
	   	    			if(perfectgrade_appDelegate.rootApp.getSelectedTab() > -1){
	   	    				setupTabs(perfectgrade_appDelegate.rootApp.getSelectedTab());
	   	    			}
	   	    		}
	   	    		
	   	    	}
	   	    	
	   	    	
	   	    }	
	   	    	
		 	//ask BT_viewUtilities to setup this screens background color....
		 	BT_viewUtilities.updateBackgroundColorsForScreen(this, theScreenData);
	
		 	//ask BT_viewUtilities to setup this screens background image...
			ImageView backgroundImageView = (ImageView)findViewById(R.id.backgroundImageView);
		 	BT_viewUtilities.updateBackgroundImageForScreen(backgroundImageView, theScreenData);
		 	
	 	}
	 	
    }
    
    
    
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //handles messages after report to cloud thread completes...
	private Handler reportToCloudHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
 			
			//hide progress...
 			hideProgress();
 			
 			//compare, save, continue..
			String cachedConfigModifiedFileName = perfectgrade_appDelegate.cachedConfigModifiedFileName;
			String previousModified = "";
			
			//parse returned data...
			try{
			
	            JSONObject obj = new JSONObject(appLastModifiedOnServer);
	            if(obj.has("lastModifiedUTC")){
	            	appLastModifiedOnServer = obj.getString("lastModifiedUTC");
	    			BT_debugger.showIt(activityName + ":handleReportToCloudResults appLastModifiedOnServer (value on server): " + appLastModifiedOnServer);
	            }
				
	            //ignore if we don't have a "server date"
	            if(appLastModifiedOnServer.length() > 1){

		            //see if we have a cached lastModified file...
		            if(BT_fileManager.doesCachedFileExist(cachedConfigModifiedFileName)){
		            	previousModified = BT_fileManager.readTextFileFromCache(cachedConfigModifiedFileName);
		    			BT_debugger.showIt(activityName + ":handleReportToCloudResults previousModified (value on device): " + previousModified);
		            }
				
		            //save downloaded lastModifiedDate
	            	BT_fileManager.saveTextFileToCache(appLastModifiedOnServer, cachedConfigModifiedFileName);
	            
		            //do we prompt for refresh?
		    		if(appLastModifiedOnServer.length() > 3 && previousModified.length() > 3){
		    			if(!appLastModifiedOnServer.equalsIgnoreCase(previousModified)){
		    				BT_debugger.showIt(activityName + ":handleReportToCloudResults server data changed, app needs refreshed");
		    				confirmRefreshAppData();
		    			}else{
		    				BT_debugger.showIt(activityName + ":handleReportToCloudResults server data not changed, no refresh needed");
		    			}
		
		    		}
			    		
	            }//appLastModifiedOnServer
	            
			}catch(Exception e){
				BT_debugger.showIt(activityName + ":handleReportToCloudResults EXCEPTION processing results: " + e.toString());
			} 			
 			
 		}
	};
	
	//reportToCloud
	public void reportToCloud(){
		BT_debugger.showIt(activityName + ":reportToCloud");			

		new Thread(){
			
			@Override
			public void run(){
				
		   		//prepare looper...
	    		Looper.prepare();

				//dataURL and reportToCloudURL may be empty or not used at all...
				String dataURL = BT_strings.mergeBTVariablesInString(perfectgrade_appDelegate.rootApp.getDataURL());
				String reportToCloudURL = BT_strings.mergeBTVariablesInString(perfectgrade_appDelegate.rootApp.getReportToCloudURL());
				
	            //do we have a data URL for remote updates?
        		if(dataURL.length() < 1){
        			BT_debugger.showIt(activityName + ":reportToCloudWorkerThread does not use a dataURL, automatic updates disabled.");			
        		}
        		if(reportToCloudURL.length() < 1){
        			BT_debugger.showIt(activityName + ":reportToCloudWorkerThread does not use a reportToCloudURL, automatic updates disabled.");			
        		}						
        		
				//if we have a dataURL AND a reportToCloudURL...report to cloud...
				if(dataURL.length() > 5 && reportToCloudURL.length() > 5){
	    			
					//if we have a currentMode, append it to the end of the URL...
					if(perfectgrade_appDelegate.rootApp.getCurrentMode().length() > 1){
						reportToCloudURL += "&currentMode=" + perfectgrade_appDelegate.rootApp.getCurrentMode();
					}					
					
					BT_debugger.showIt(activityName + ":reportToCloudWorkerThread getting lastModified from reportToCloudURL " + reportToCloudURL);
				
	   	    		BT_downloader objDownloader = new BT_downloader(reportToCloudURL);
	   	    		objDownloader.setSaveAsFileName("");
	   	    		appLastModifiedOnServer = objDownloader.downloadTextData();

				}//dataURL.length() 
	 				
				//send message...
				sendMessageToMainThread(0);
				
			}
			
			//send message....
			private void sendMessageToMainThread(int what){
				Message msg = Message.obtain();
				msg.what = what;
				reportToCloudHandler.sendMessage(msg);
			}
			
		}.start();
		
	}
	//END report to cloud methods...
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
    

         
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 	/////////////////////////////////////////////////////
	//options menu (action bar menu or hardware button)...
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
	 	//BT_debugger.showIt(activityName + ":onCreateOptionsMenu");

	 	//menu items are added in individual fragments (plugins) BEFORE this method runs...
		
		//return...
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	 	//BT_debugger.showIt(activityName + ":onOptionsItemSelected");
	 	
		//item selections are handled in individual fragments (plugins) but this must return FALSE to prevent interceptions...
	   switch (item.getItemId()){
	      default:
	  		return false;
	   }	 	
	 	
	}



	
}

































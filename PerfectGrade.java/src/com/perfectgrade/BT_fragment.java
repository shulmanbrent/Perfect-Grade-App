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
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class BT_fragment extends Fragment{

	public String fragmentName = "BT_fragment";
	public String screenItemId = "na";
	public BT_item screenData = null;
	public ProgressDialog loadingMessage = null;
	ArrayList<BT_item> actionBarMenuItems = null;
	
	//sends fragment change events to host activity (BT_activity_host)...
	BTFragmentResumedListener fragChangedListener;

	//default constructor
	public BT_fragment(){
		
		/*
			we use an empty constructor when creating plugins so we can set the screen data for the fragment
			before Android calls the onCreate() method. See BT_activity_hostinitPluginWithScreenData
		*/
		
		//set the fragmentName to the Child Class that extends BT_fragment...
		fragmentName = getClass().getSimpleName();
		
	
	}
	
    //this fragments parent activity should implement this method...
    public interface BTFragmentResumedListener{
        public void BTFragmentResumed(BT_item theScreenData);
    }	
	
	
	//////////////////////////////////////////////////////////////////////////
	//fragment life cycle events. Fragments that extend this class may 
	//override these events...
	
	
	//onCreate...
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		//allow modification of host activities options menu...
		setHasOptionsMenu(true);		
		
		//show life-cycle event in LogCat console...
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onCreate JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}
		
	}	
	
	//onCreateView...	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState){
		
		/*
			no view is returned by this class. This is because this class serves as the parent class for individual 
			fragments and each of those child classes return their own views (plugins have their own view)...
		 */
		
		//return...
		return null;
		
	}		
		
	//onAttach...
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		
		//show life-cycle event in LogCat console...
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onAttach JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}
		
		//create the "fragment changed listener"...
		if(!screenItemId.equals("na") && screenItemId.equals("")){
			try{
        		fragChangedListener = (BTFragmentResumedListener)activity;
        	}catch(ClassCastException e){
    			BT_debugger.showIt(fragmentName + ":onAttach EXCEPTION: " + e.toString());	
        	}
		}

		
	}	
	
	
	
	//onActivityCreated...
	@Override 
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		//show life-cycle event in LogCat console...
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onActivityCreated JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}
		
		/*
		 	Update the nav bar and background color if this fragment is running in the BT_activity_host
		 	activity. It's possible that it's running in BT_activity_start (because it's a splash screen fragment),
		 	ignore	this in these cases.	 
		*/
		
		//ask host activity to update it's background (only do this if host activity is BT_activity_host...
		String className = this.getActivity().getClass().getSimpleName();
		if(this.screenData != null && className.equalsIgnoreCase("BT_ACTIVITY_HOST")){
			try{
				((BT_activity_host)getActivity()).configureNavBarAndBackgroundForScreen(this.screenData);
			}catch(Exception e){
				//host activity is not an instance of BT_activity_host...
			}
		}
		
	}
	 
	//onStart...
	@Override 
	public void onStart(){
		super.onStart();

		//show life-cycle event in LogCat console...
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onStart JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}

	}
	
	//onResume...
	@Override
	public void onResume() {
		super.onResume();
		
		//show life-cycle event in LogCat console...
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onResume JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}
	}
    
	//onPause...
	@Override
	public void onPause() {
        super.onPause();
        
		//show life-cycle event in LogCat console...
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onPause JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}
	}

	//onStop...
	@Override 
	public void onStop(){
		super.onStop();
		
		//show life-cycle event in LogCat console...
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onStop JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}
	}	
	
	//onDestroy...
	@Override
	public void onDestroy() {
		super.onDestroy();

		//show life-cycle event in LogCat console...
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onDestroy JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}
	}	
    
	//onSaveInstanceState...
	@Override
	public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);

		//show life-cycle event in LogCat console...
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onSavedInstanceState JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}
	}    
	
	//End Fragment life cycle events...
	//////////////////////////////////////////////////////////////////////////
    
    //set screen data...
	public void setScreenData(BT_item theScreenData){
		this.screenData = theScreenData;
		
		//set the screenId...
		this.screenItemId = theScreenData.getItemId();
		
		if(theScreenData != null){
			BT_debugger.showIt(fragmentName + ":setScreenData JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + theScreenData.getItemType() + "\" itemNickname: \"" + theScreenData.getItemNickname() + "\"");
		}
		
		
		
	}
	
 	//show alert message
	public void showAlert(String theTitle, String theMessage) {
		if(theTitle.equals("")) theTitle = "No Alert Title?";
		if(theMessage.equals("")) theMessage = "No alert message?";

		AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
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
			toast = Toast.makeText(getActivity().getBaseContext(), theMessage, Toast.LENGTH_SHORT);
		}else{
			toast = Toast.makeText(getActivity(), theMessage, Toast.LENGTH_LONG);
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
		loadingMessage = ProgressDialog.show(getActivity(), theTitle, theMessage, true);
		loadingMessage.setCanceledOnTouchOutside(false);
		loadingMessage.setCancelable(true);		
		
	}
	
	//hideProgress...
	public void hideProgress(){
		if(loadingMessage != null){
			loadingMessage.dismiss();
		}
	}
    
    
	//handleRightNavButtonTap...
	public void handleRightNavButtonTap(BT_item theScreenData){
		if(theScreenData != null){
			BT_debugger.showIt(fragmentName + ":handleRightNavButtonTap JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + theScreenData.getItemType() + "\" itemNickname: \"" + theScreenData.getItemNickname() + "\"");
		}
		
    	//itemId, nickname or object...
        String loadScreenItemId = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "navBarRightButtonTapLoadScreenItemId", "");
        String loadScreenNickname = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "navBarRightButtonTapLoadScreenNickname", "");
        BT_item tapScreenLoadObject = null;
        BT_item tapMenuItemObject = null;
    	if(loadScreenItemId.length() > 1 && !loadScreenItemId.equalsIgnoreCase("none")){
			BT_debugger.showIt(fragmentName + ":handleRightNavButtonTap right button loads screen with itemId: \"" + loadScreenItemId + "\"");
    		tapScreenLoadObject = perfectgrade_appDelegate.rootApp.getScreenDataByItemId(loadScreenItemId);
    	}else{
    		if(loadScreenNickname.length() > 1){
				BT_debugger.showIt(fragmentName + ":handleRightNavButtonTap right button loads screen with nickname: \"" + loadScreenNickname + "\"");
    			tapScreenLoadObject = perfectgrade_appDelegate.rootApp.getScreenDataByItemNickname(loadScreenNickname);
    		}else{
    			try{
	    			JSONObject obj = theScreenData.getJsonObject();
		            if(obj.has("navBarRightButtonTapLoadScreenObject")){
						BT_debugger.showIt(fragmentName + ":handleRightNavButtonTap right button loads screen object configured with JSON object.");
		            	JSONObject tmpRightScreen = obj.getJSONObject("navBarRightButtonTapLoadScreenObject");
		            	tapScreenLoadObject = new BT_item();
    		            if(tmpRightScreen.has("itemId")) tapScreenLoadObject.setItemId(tmpRightScreen.getString("itemId"));
    		            if(tmpRightScreen.has("itemNickname")) tapScreenLoadObject.setItemNickname(tmpRightScreen.getString("itemNickname"));
    		            if(tmpRightScreen.has("itemType")) tapScreenLoadObject.setItemType(tmpRightScreen.getString("itemType"));
    		            tapScreenLoadObject.setJsonObject(tmpRightScreen);
		            }
    			}catch(Exception e){
					BT_debugger.showIt(fragmentName + ":handleRightNavButtonTap EXCEPTION reading screen-object configured for right-side nav button: " + e.toString());
    			}
    		}
    	}

    	//if we have a screen object to load from the right-button tap, build a BT_item object...
    	if(tapScreenLoadObject != null){
    		
    		tapMenuItemObject = new BT_item();
    		tapMenuItemObject.setItemId("unused");
    		tapMenuItemObject.setItemNickname("unused");
    		tapMenuItemObject.setItemType("BT_menuItem");
    		
    		//create json object for the BT_item...
    		try{
	    		JSONObject tmpMenuJson = new JSONObject();
	    		tmpMenuJson.put("itemId", "unused");
	    		tmpMenuJson.put("itemNickname", "unused");
	    		tmpMenuJson.put("itemType", "BT_menuItem");
	    		
	    		//set JSON
	    		tapMenuItemObject.setJsonObject(tmpMenuJson);
	    		
    		}catch(Exception e){
				BT_debugger.showIt(fragmentName + ":handleRightNavButtonTap EXCEPTION creating right-button BT_menuItem: " + e.toString());
    		}
    		
        	//call loadScreenObject (static method in this class)...
   			loadScreenObject(tapMenuItemObject, tapScreenLoadObject);
   		
    	}else{
			BT_debugger.showIt(fragmentName + ":handleRightNavButtonTap ERROR. No screen is connected to this button?");	
			showAlert(getString(R.string.errorTitle), getString(R.string.errorNoScreenConnected));
    	}
		
	} //handleRightNavButton
	
	
	
	
	//loads a screen object...
	public void loadScreenObject(BT_item theMenuItemData, BT_item theScreenData){
		if(theScreenData != null){
			BT_debugger.showIt(fragmentName + ":loadScreenObject. Loading screen with JSON itemId: \"" + theScreenData.getItemId() + "\" itemType: \"" + theScreenData.getItemType() + "\" itemNickname: \"" + theScreenData.getItemNickname() + "\"");
		}
		
		
		//if the loadScreenItemId == "none".....
		if(theMenuItemData != null){
			if(BT_strings.getJsonPropertyValue(theMenuItemData.getJsonObject(), "loadScreenWithItemId", "").equalsIgnoreCase("none")){
				return;
			}
		}
		
		//if this screen data has loadScreenItemId == "none"...
		if(theScreenData != null){
			if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "loadScreenWithItemId", "").equalsIgnoreCase("none")){
				return;
			}
		}
		
        //next screen may require a login..
        boolean allowNextScreen = true;
        if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "loginRequired", "").equalsIgnoreCase("1")){
        	if(!perfectgrade_appDelegate.rootApp.getRootUser().getIsLoggedIn()){
        		allowNextScreen = false;
        		BT_debugger.showIt(fragmentName + ":loadScreenObject login required - user IS NOT logged in");
        	}else{
        		BT_debugger.showIt(fragmentName + ":loadScreenObject login required - user IS logged in");
        	}
        }

        //alert if not allowed...
        if(!allowNextScreen){
        	showAlert(getString(R.string.logInRequiredTitle), getString(R.string.logInRequiredMessage));
        	
    		//bail
    		return;
        }

        //if continuing...
        if(allowNextScreen){
        	
         	//if we passed in a menu-item..
        	if(theMenuItemData != null){
	        	
        		//possible sound effect...
	            if(BT_strings.getJsonPropertyValue(theMenuItemData.getJsonObject(), "soundEffectFileName", "").length() > 1){
	            	perfectgrade_appDelegate.playSoundEffect(BT_strings.getJsonPropertyValue(theMenuItemData.getJsonObject(), "soundEffectFileName", ""));
	            }
	        	
	            //possible stop audio track...
	            if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "audioStopsOnScreenExit", "").equalsIgnoreCase("1")){
	            	BT_debugger.showIt(fragmentName + ":loadScreenObject stopping audio....");
	            }
	            
	            //possible vibrateDevice...
	            if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "vibrateDevice", "").equalsIgnoreCase("1")){
	            	BT_debugger.showIt(fragmentName + ":loadScreenObject vibrateDevice device....");
	            	this.vibrateDevice();
	            }
	            
	            //remember previous menu object before setting current menu object
	            perfectgrade_appDelegate.rootApp.setPreviousMenuItemData(perfectgrade_appDelegate.rootApp.getCurrentMenuItemData());
	    		            
	    		//remember current menu object
	            perfectgrade_appDelegate.rootApp.setCurrentMenuItemData(theMenuItemData);
	         
        	}//menuItemData == null...
        	
        	//type of screen to load next...
            String nextScreenType = theScreenData.getItemType();
            
    		//some screens aren't screens at all! Like "Call Us" and "Email Us" item. In these cases, we only
    		//trigger a method and do not load a custom activity...
            
            //call us...
            if(nextScreenType.equalsIgnoreCase("BT_screen_call") || 
            		nextScreenType.equalsIgnoreCase("BT_placeCall")){
            	BT_debugger.showIt(fragmentName + ":launching dialer...");
            	launchPhoneDialerWithScreenData(theScreenData);
            	return;
            }

            //email / share email...
            if(nextScreenType.equalsIgnoreCase("BT_screen_email") || 
            		nextScreenType.equalsIgnoreCase("BT_shareEmail") ||
            		nextScreenType.equalsIgnoreCase("BT_sendEmail")){
            	BT_debugger.showIt(fragmentName + ":launching email compose sheet...");
            	sendEmailWithScreenData(theScreenData);
            	return;
            }        
            
            //text us / share text (SMS)...
            if(nextScreenType.equalsIgnoreCase("BT_screen_sms") || 
            		nextScreenType.equalsIgnoreCase("BT_shareSms") ||
            		nextScreenType.equalsIgnoreCase("BT_sendSms") ||
            		nextScreenType.equalsIgnoreCase("BT_shareSms")){
            	BT_debugger.showIt(fragmentName + ":launching SMS / Text Message compose sheet...");
            	sendSMSWithScreenData(theScreenData);
            	return;
            }  
            
            //launch native app...
            if(nextScreenType.equalsIgnoreCase("BT_launchNativeApp")){
            	BT_debugger.showIt(fragmentName + ":launching native app....");
            
        		/*
                Launching native app requires an "appToLaunch" and a "dataURL"
                App Types:	browser, youTube, googleMaps, musicStore, appStore, mail, dialer, sms
                */
            	
               	//get the document file name or the URL...
            	String appToLaunch = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "appToLaunch", "");
            	String dataURL = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "dataURL", "");
            	if(appToLaunch.length() > 1 && dataURL.length() > 1){
            	
        			try{
        				
        				//browser...
        				if(appToLaunch.equalsIgnoreCase("browser")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dataURL)); 
         			    	 
        			    	  //ask user for the best app to use...
        			    	  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
         				}
        				
        				//youTube...
        				if(appToLaunch.equalsIgnoreCase("youTube")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dataURL)); 
         			    	 
        			    	  //ask user for the best app to use...
        			    	  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
         				}

        				//googleMaps...
        				if(appToLaunch.equalsIgnoreCase("googleMaps")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + dataURL)); 
        			    	  
         			    	  //ask user for the best app to use...
        			    	  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
        				}
        				
           				//nativeMaps...
        				if(appToLaunch.equalsIgnoreCase("nativeMaps")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + dataURL)); 
        			    	  
         			    	  //ask user for the best app to use...
        			    	  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
        				}

        				//musicStore...
        				if(appToLaunch.equalsIgnoreCase("musicStore")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dataURL)); 
         			    	 
        			    	  //ask user for the best app to use...
        			    	  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
         				}
        				
          				//appStore...
        				if(appToLaunch.equalsIgnoreCase("appStore")){
        			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dataURL)); 
 
        			    	  //ask user for the best app to use...
        			    	  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
        				}
        				
         				//mail...
        				if(appToLaunch.equalsIgnoreCase("mail")){

        			    	  Intent intent = new Intent(android.content.Intent.ACTION_SEND);  
        			    	  intent.setType("plain/text");  
        			    	  
         			    	  //email to address...
       			    		  String[] emailToAddressList = {dataURL};
       			    		  intent.putExtra(android.content.Intent.EXTRA_EMAIL, emailToAddressList);  

        			    	  //ask user for the best app to use...
        			    	  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
        				}
        				
         				//sms...
        				if(appToLaunch.equalsIgnoreCase("sms")){
        			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW);  
        			    	  intent.setType("vnd.android-dir/mms-sms");  
        			    	  
        			    	  //set the to-number....
       			    		  intent.putExtra("address", dataURL); 

        			    	  //ask user for the best app to use...
       			    		  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
        				}
        				
        				//dialer...
        				if(appToLaunch.equalsIgnoreCase("dialer")){
        					Intent intent = new Intent(Intent.ACTION_VIEW);
       			    		intent.setData(Uri.parse("tel:" + dataURL));
        			    	
        					//ask user for the best app to use...
        			    	startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
        				}
        				
        				//customURLScheme...
        				if(appToLaunch.equalsIgnoreCase("customURLScheme")){
        					Intent intent = new Intent(android.content.Intent.ACTION_VIEW);  
        					
        					//ask user for the best intent to use...
        					startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
        				}
        		    	  
        		    	  
        		    	  
       		    	}catch(Exception e){
                    	BT_debugger.showIt(fragmentName + ":EXCEPTION Launching native app: " + e.toString());
       		    	}			
            	
            	
            	}else{
                	BT_debugger.showIt(fragmentName + ":ERROR Launching native app. appToLaunch or dataURL empty?");
            	}
               	
            	//bail...
            	return;
            	
            }//launch native app...
            
 			//if the screen we are loading has an audio track, show "not supported on Android message"...
            if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "audioFileName", "").length() > 3 || BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "audioFileURL", "").length() > 3){
            	BT_debugger.showIt(fragmentName + ":screen uses audio file or URL. BACKGROUND AUDIO IS UNSUPPORTED ON ANDROID");
            }
            
       		//find a possible transition type associated with this menuItem...
            String transitionType = "";
            if(theMenuItemData != null){
            	transitionType = BT_strings.getJsonPropertyValue(theMenuItemData.getJsonObject(), "transitionType", "");
            }
            
			//ask the host activity to create the fragment for this item's JSON...
            if(theScreenData != null){
            	Fragment theFrag = ((BT_activity_host)getActivity()).initPluginWithScreenData(theScreenData);
            
            	//ask the host activity to show the fragment for this plugin...
            	((BT_activity_host)getActivity()).showFragmentForPlugin(theFrag, theScreenData, true, transitionType);
            }
            
        }//allowNextScreen...
        
        
	}//end loadScreenObject...
	
	//loadScreenWithId...
	public void loadScreenWithItemId(String screenId){
		BT_debugger.showIt(fragmentName + ":loadScreenWithItemId Screen Id: \"" + screenId + "\"");
	
		
	    //next screen may require a login..
        boolean allowNextScreen = true;
        
        //json data for this screen...
        BT_item theScreenData = perfectgrade_appDelegate.rootApp.getScreenDataByItemId(screenId);
        
        if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "loginRequired", "").equalsIgnoreCase("1")){
        	if(!perfectgrade_appDelegate.rootApp.getRootUser().getIsLoggedIn()){
        		allowNextScreen = false;
        		BT_debugger.showIt(fragmentName + ":loadScreenWithId login required - user IS NOT logged in");
        	}else{
        		BT_debugger.showIt(fragmentName + ":loadScreenWithId login required - user IS logged in");
        	}
        }
        
        
        //alert if not allowed...
        if(!allowNextScreen){
        	showAlert(getString(R.string.logInRequiredTitle), getString(R.string.logInRequiredMessage));
    		//bail
    		return;
        }else{
        	loadScreenObject(null, theScreenData);
        }
        
	
	}
	
	//loadScreenWithNickname...
	public void loadScreenWithNickname(String screenNickname){
		BT_debugger.showIt(fragmentName + ":loadScreenWithNickname Screen Id: \"" + screenNickname + "\"");
	
	    //next screen may require a login..
        boolean allowNextScreen = true;
        
        //json data for this screen...
        BT_item theScreenData = perfectgrade_appDelegate.rootApp.getScreenDataByItemNickname(screenNickname);
        
        if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "loginRequired", "").equalsIgnoreCase("1")){
        	if(!perfectgrade_appDelegate.rootApp.getRootUser().getIsLoggedIn()){
        		allowNextScreen = false;
        		BT_debugger.showIt(fragmentName + ":loadScreenWithNickname login required - user IS NOT logged in");
        	}else{
        		BT_debugger.showIt(fragmentName + ":loadScreenWithNickname login required - user IS logged in");
        	}
        }
        
        //alert if not allowed...
        if(!allowNextScreen){
        	showAlert(getString(R.string.logInRequiredTitle), getString(R.string.logInRequiredMessage));
    		//bail
    		return;
        }else{
        	loadScreenObject(null, theScreenData);
        }
	
	}
	
	
    //deviceIsShaking...
	public void deviceIsShaking(float shakeSpeedX, float shakeSpeedY){
    	BT_debugger.showIt(fragmentName + ":deviceIsShaking Speed X: " + Math.abs(shakeSpeedX) + " Speed Y: " + Math.abs(shakeSpeedY));	

    	/*
    	 	This method is fired by BT_activity_host when shake events occur. The shakeSpeedX and shakeSpeedY 
    	 	values can be used to determine what happens when the device is moving horizontally or vertically, or
    	 	both. In most cases a plugin will override this method with it's own implementation then fire
    	 	a method when the device is shaking or moving in one or both directions. 
    	  
    	  	Example...
    	  	if(shakeSpeedX > 5){
    	  		//device is shaking vertically...
    	  	}
    	  	if(shakeSpeedY > 7){
    	  		//device is shaking horizontally...
    	  		 
    	  	}
    	  	
    	  	The sensitivity of the shaking is set with BT_activity_host.SHAKE_THRESHOLD
    	  
    	*/
    	
    	
    	
	}
	

    //playSoundEffect...
	public void playSoundEffect(){
    	BT_debugger.showIt(fragmentName + ":playSoundEffect");	

	}
	
	//vibrateDevice...
	public void vibrateDevice(){
    	BT_debugger.showIt(fragmentName + ":vibrateDevice");	
		Vibrator shaker = (Vibrator)perfectgrade_appDelegate.getContext().getSystemService(Context.VIBRATOR_SERVICE);
    	if(perfectgrade_appDelegate.rootApp.getRootDevice().canVibrate()){
	    	BT_debugger.showIt(fragmentName + ":vibrateDevice (This device does support vibrating)");	
	        shaker.vibrate(250);
		}else{
	    	BT_debugger.showIt(fragmentName + ":vibrateDevice (This device does NOT support vibrating)");	
		}
	}
	
	//send email with screen data...
	public void sendEmailWithScreenData(BT_item theScreenData){
		BT_debugger.showIt(fragmentName + ":sendEmailWithScreenData");
		if(perfectgrade_appDelegate.rootApp.getRootDevice().canSendEmail()){
		
			try{
			
	    	  Intent intent = new Intent(android.content.Intent.ACTION_SEND);  
	    	  intent.setType("plain/text");  
	    	  
	    	  //do we have a subject....
	    	  String subject = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "emailSubject", "");
	    	  if(subject.length() > 1){
	    		  intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);  
	    	  }
	    	  
	    	  //do we have a message...
	    	  String emailMessage = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "emailMessage", "");
	    	  if(emailMessage.length() > 1){
	    		  intent.putExtra(android.content.Intent.EXTRA_TEXT, emailMessage);  
	    	  }	    	  
	    	  
	    	  //email to address...
	    	  String emailToAddress = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "emailToAddress", "");
	    	  BT_debugger.showIt("EMAIL TO: " + emailToAddress);
	    	  if(emailToAddress.length() > 1){
	    		  String[] emailToAddressList = {emailToAddress};
	    		  intent.putExtra(android.content.Intent.EXTRA_EMAIL, emailToAddressList);  
	    	  }	 	          

	    	  //ask user for the best app to use...
	    	  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
			
	    	}catch(Exception e){
	    		showAlert(getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
	    	}
	    	
		}else{
			showAlert(getString(R.string.errorTitle), getString(R.string.noNativeAppTitle));
		}
	}
	
	//send SMS with screen data...
	public void sendSMSWithScreenData(BT_item theScreenData){
		BT_debugger.showIt(fragmentName + ":sendSMSWithScreenData");
		if(perfectgrade_appDelegate.rootApp.getRootDevice().canSendSMS()){
		
			try{
				
	    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW);  
	    	  intent.setType("vnd.android-dir/mms-sms");  
	    	  
	    	  //do we have a text-to-number....
	    	  String textToNumber = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "textToNumber", "");
	    	  if(textToNumber.length() > 1){
	    		  intent.putExtra("address", textToNumber); 
	    	  }
	    	  
	    	  //do we have a textMessage...
	    	  String textMessage = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "textMessage", "");
	    	  if(textMessage.length() > 1){
	    		  intent.putExtra("sms_body", textMessage); 
	    	  }	    	  

	    	  //ask user for the best app to use...
	    	  startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
			
	    	}catch(Exception e){
	    		showAlert(getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
	    	}			
			
		}else{
			showAlert(getString(R.string.errorTitle), getString(R.string.noNativeAppTitle));
		}
	}	
	
	//start phone call with screen data...
	public void launchPhoneDialerWithScreenData(BT_item theScreenData){
		BT_debugger.showIt(fragmentName + ":launchDialerWithScreenData");
		if(perfectgrade_appDelegate.rootApp.getRootDevice().canMakeCalls()){
		
	    	try{
	    		
				Intent intent = new Intent(Intent.ACTION_VIEW);
				
				//number to call...
		    	String number = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "number", "");
		    	if(number.length() > 1){
		    		intent.setData(Uri.parse("tel:" + number));
		    	}
		    	
				//ask user for the best app to use...
		    	startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));  
	    		
	    	}catch(Exception e){
	    		showAlert(getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
	    	}
			
		}else{
			showAlert(getString(R.string.errorTitle), getString(R.string.noNativeAppTitle));
		}
	}	
	
	
	//backButtonPress...
	public void handleBackButton(){
		BT_debugger.showIt(fragmentName + ":handleBackButton");
		
		/*
			This method is fired by BT_activity_host when the built in Android back button is pressed. 
			If a plugin implements a handleBackButton() method (with no arguments) it will be triggered by 
			BT_activity_host when the user taps back on the device.
		*/
		
	}
	
	//not all documents load in the built-in webView....
	public boolean canLoadDocumentInWebView(String theFileNameOrURL){
		BT_debugger.showIt(fragmentName + ":canLoadDocumentInWebView \"" + theFileNameOrURL + "\"");
		boolean ret = true;
		if(theFileNameOrURL.length() > 1){
			
			//this is a list of file types that cannot load in a webView, add types as needed.
			
			ArrayList<String> doNotLoadList = new ArrayList<String>();
			doNotLoadList.add(".mp3");
			doNotLoadList.add(".zip");
			doNotLoadList.add(".doc");
			doNotLoadList.add(".pdf");
			doNotLoadList.add(".mpeg");
			doNotLoadList.add(".mp4");
			doNotLoadList.add(".xls");
			doNotLoadList.add(".mov");
			doNotLoadList.add("mailto");
			doNotLoadList.add("tel");

			//is our localFileName valid?
			for(int x = 0; x < doNotLoadList.size(); x++){
				if(theFileNameOrURL.contains(doNotLoadList.get(x))){
    				ret = false;
					break;
				}
			}
		}
		return ret;
	}		

    //onCreateOptionsMenu...
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onCreateOptionsMenu JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}

	 	/*
	 	 	Screens that extend BT_fragment should implement their own onCreateOptionsMenu() method to add
	 	 	action bar items specific to that screen. They should call super.onCreateOptionsMenu() AFTER
	 	 	adding customized items for that screen. 	  
	 	*/
	 	
	 	//for individual menu items...
	 	MenuItem menuItem = null;
	 	
	 	//add items from this screen "contextMenuItemId" if configured...Each item gets an id equal to it's index
	 	//in the loop so we can figure out what item was tapped in the onOptionsItemSelected() method...
	 	if(this.screenData != null){
	 		String contextMenuItemId = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "contextMenuItemId", "");
	 		if(contextMenuItemId.length() > 0){
				BT_debugger.showIt(fragmentName + ":onCreateOptionsMenu add items from contextMenuItemId: \"" + contextMenuItemId + "\"");
	 			actionBarMenuItems = new ArrayList<BT_item>();
	 			JSONArray tmpItems = perfectgrade_appDelegate.rootApp.getMenuItemsById(contextMenuItemId);
				if(tmpItems != null){
	                for(int m = 0; m < tmpItems.length(); m++){
						try{
							
							//save the object in the menuItems list...
							JSONObject tmpJson = tmpItems.getJSONObject(m);
		                	BT_item tmpItem = new BT_item();
		                	String titleText = "";
		                	if(tmpJson.has("titleText")) titleText = tmpJson.getString("titleText");
		                	if(tmpJson.has("itemId")) tmpItem.setItemId(tmpJson.getString("itemId"));
		                	if(tmpJson.has("itemType")) tmpItem.setItemType(tmpJson.getString("itemType"));
		                	tmpItem.setJsonObject(tmpJson);
		                	actionBarMenuItems.add(tmpItem);
		                	
		                	//add the item to the menu, use id of m + 1000 to avoid conflicts...
		            		menuItem = menu.add(Menu.NONE,(m + 1000), 0, titleText);
		            		menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
						
						}catch(JSONException e){

						}
	                }//for
				}//tmpItems...
	 		}//useMenuWithId...

		 	//if this is the app's home screen, show the refreshAppData option in the menu...
			if(this.screenData.isHomeScreen()){
				menuItem = menu.add(Menu.NONE, -1, 0, getString(R.string.refreshAppData));
				menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			}		
	 	
	 	
		 	//if this screen has a navBarRightButtonType setup...
	 		String navBarRightButtonType = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "navBarRightButtonType", "");
	 		if(navBarRightButtonType.length() > 0){
	 			
	 			//set the text for the button. This will not show if we find an icon...
				menuItem = menu.add(Menu.NONE, -2, 0, BT_viewUtilities.getRightNavBarTextForScreen(this.screenData));
	 			
	 			//get the icon (or null in the case of next, done, cancel, edit, save, etc)...
	 			Drawable d = BT_viewUtilities.getRightNavBarIconForScreen(this.screenData);
	 			if(d != null){
	 			
	 				//use the value from the localized strings file for "next, done, cancel, edit, save", etc...
					menuItem.setIcon(d);
	 			}
	 			
	 			//show this action item as always...
				menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

	 			
	 		}
	 	
	 	}//screenData is null...
	 	
	 	//add the standard close button (does nothing, closes menu)...
		menuItem = menu.add(Menu.NONE, -3, 0, getString(R.string.close));
		menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		
	 	//add the standard quit button (finishes "host" activity)...
		menuItem = menu.add(Menu.NONE, -4, 0, getString(R.string.quitApp));
		menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		//call super...
		super.onCreateOptionsMenu(menu, inflater);
	    
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(screenData != null){
			BT_debugger.showIt(fragmentName + ":onOptionsItemSelected JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		}
		
	 	/*
	 	 	Handling menu item taps: Each screen that extends BT_fragment should implement
	 	 	the onOptionsItemSelected() method to handle taps for items specific to that
	 	 	screen. Each menu item added for a screen should use an itemId greater than -1.
	 	*/
		
	 	//handle item selection. 
	 	switch (item.getItemId()){
	 	  case android.R.id.home:     
	 		  
	 		  //ask the host activity to show the home screen fragment...
	 		  ((BT_activity_host)getActivity()).showAppHomeScreen();
	 		 
	 		  //bail...
	 		  return true;	 	
	 		 	
	 	  	case -1:
	 	  		//ask the host activity to refresh app data...
	 			((BT_activity_host)getActivity()).refreshAppData();
	 			return true;

	 	  	case -2:
	 	  		//handle navBarRightButtonType tap.....
	 	  		handleRightNavButtonTap(this.screenData);
	 	  		return true;
	 			
	 	  	case -3:
	 			//close item, do nothing...
	 			return true;
	 			
	 	  	case -4:
	 			//finish activity...
	 	  		this.getActivity().finish();
	 			return true;
	 			
	 		//default, find the item in the menuItems array...
	 	  	default:
	 	  	
	 	  		//if this item has an index > 1000 then it's part of the "contextMenuItemId" property for this screen...
	 	  		if(item.getItemId() >= 1000){
	 	  			int useItemIndex = (item.getItemId() - 1000);
	 	  			BT_item tmpItem = actionBarMenuItems.get(useItemIndex);
	 		 		String loadScreenWithItemId = BT_strings.getJsonPropertyValue(tmpItem.getJsonObject(), "loadScreenWithItemId", "");
	 	  		
	 		 		//if we found a loadScreenWithItemId, ask the host activity to create and load the fragment for it...
	 		 		BT_item screenData = perfectgrade_appDelegate.rootApp.getScreenDataByItemId(loadScreenWithItemId);
	 		 	    Fragment theFrag = ((BT_activity_host)getActivity()).initPluginWithScreenData(screenData);
	 		 	    ((BT_activity_host)getActivity()).showFragmentForPlugin(theFrag, screenData, true, "");
	 	  		
	 	  		}
	 	  		
	   		
	 	}
	   
	 	//return...
	 	return super.onOptionsItemSelected(item);
	   	   
	   
	}

	
	  
}








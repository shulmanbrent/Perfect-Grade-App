/*
 *
 *	All rights reserved.
 *
 * 	Copyright 2011, David Book, buzztouch.com
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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class BT_screen_splash extends BT_fragment implements OnTouchListener{
	
	//properties...
	String transitionType = "";
	String effectName = "";
	int startTransitionAfterSeconds = 0;
	int transitionDurationSeconds = 0;
	
	
	//onCreateView...
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        
		/*
 			Note: fragmentName property is already setup in the parent class (BT_fragment). This allows us 
 			to add the 	name of this class file to the LogCat console using the BT_debugger.
		*/
		//show life-cycle event in LogCat console...
		BT_debugger.showIt(fragmentName + ":onCreateView JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");
		
		//inflate the layout file for this screen...
		View thisScreensView = inflater.inflate(R.layout.bt_screen_splash, container, false);
		
		//set touch listener to view...
		thisScreensView.setOnTouchListener(this);
		
		//must have screen data...
		if(this.screenData != null){
		
		 	//ask BT_viewUtilities to setup this screens background color...
		 	BT_viewUtilities.updateBackgroundColorsForScreen(this.getActivity(), screenData);
			
			//get animation values from JSON...
			this.transitionType = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "transitionType", "");
			this.effectName = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "effectName", "fallDown");
			this.startTransitionAfterSeconds = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "startTransitionAfterSeconds", "1"));
			this.transitionDurationSeconds = Integer.parseInt(BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "transitionDurationSeconds", "1"));
		 	
			//if we have an effect we do it first, then start the animation sequence...
			if(effectName != ""){
				new Handler().postDelayed(new Runnable() {
	            	public void run(){
	            	
	    				//pass effect type and reference to the background image to the startEffect method..
	    				ImageView imgView = (ImageView)getActivity().findViewById(R.id.backgroundImageView);
	    				startEffect(effectName, imgView);

	            	}
				}, 100);	
			}
			
		}//screenData != null...
		
		//show a message if this fragment is NOT running in the BT_activity_start Activity... (it should be)...
		if(this.getActivity().getClass().getSimpleName().toString().equalsIgnoreCase("BT_activity_host")){
			showToast("Note: The Action Bar will be hidden when splash screens are used during app launch.", "long");
		}
		
		//return...
		return thisScreensView;

        
	}
	
	//onResume...
	public void onResume(){
		super.onResume();
		
   		//will hold the name and/or URL of the randomized image...
		String tmpImage = "";
		String tmpURL = "";

		//large or small device...
		if(perfectgrade_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
			tmpImage = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "backgroundImageNameLargeDevice", "");
			tmpURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "backgroundImageURLLargeDevice", "");
		}else{
			tmpImage = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "backgroundImageNameSmallDevice", "");
			tmpURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "backgroundImageURLSmallDevice", "");
		}
	 	
		//use a default image (included in the BT core) if we didn't find one in the childItems...
		if(tmpImage.length() < 1 && tmpURL.length() < 1){
			tmpImage = "bt_member_275.png";
			tmpURL = "";
		}
		
		//create a BT_item to pass to the updateBackgroundImageForScreen() method in BT_viewUtilities...
		BT_item tmpItem = new BT_item();
		tmpItem.setItemId(this.screenData.getItemId());
		tmpItem.setItemType(this.screenData.getItemType());
		tmpItem.setItemNickname(this.screenData.getItemNickname());
		
		//use the backgroundImageScale property for the screen...
		String backgroundImageScale = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "backgroundImageScale", "center");

		//pass the same image for both small and large properties (we figured it out already)...
		String tmpJson = "{";
			tmpJson += "\"backgroundImageNameLargeDevice\":\"" + tmpImage + "\", ";
			tmpJson += "\"backgroundImageNameSmallDevice\":\"" + tmpImage + "\", ";
			tmpJson += "\"backgroundImageURLLargeDevice\":\"" + tmpURL + "\", ";
			tmpJson += "\"backgroundImageURLSmallDevice\":\"" + tmpURL + "\", ";
			tmpJson += "\"backgroundImageScale\":\"" + backgroundImageScale + "\" ";
		tmpJson += "}";
		
		//create JSONObject from string using BT_strings class...				
		tmpItem.setJsonObject(BT_strings.getJsonObjectFromString(tmpJson));
		
		//ask BT_viewUtilities to update our background image view using this randomized JSON data...
		ImageView backgroundImageView = (ImageView)this.getActivity().findViewById(R.id.backgroundImageView);
	 	BT_viewUtilities.updateBackgroundImageForScreen(backgroundImageView, tmpItem);
		
		//setup transition delay if not -1 (-1 means tap to start animation) 
		if(startTransitionAfterSeconds > -1){
			delayHandler.removeCallbacks(animateDelayTask);
			delayHandler.postDelayed(animateDelayTask, ((startTransitionAfterSeconds + 1) * 1000));
		}
		
	}
	
	//handle touch event..
	public boolean onTouch(View arg0, MotionEvent arg1) {
		if(startTransitionAfterSeconds < 1){
			animateSplashScreen();
		}
		return false;
	}		
	
	 //startEffect...
	  private void startEffect(final String effectType, final ImageView imageView){
		BT_debugger.showIt(fragmentName + ":startEffect. \"" + effectName + "\"");			
	    		
		//Effect: "movingFade" is an image overlay that animates left to right...
		if(effectType.equalsIgnoreCase("fallDown") && imageView != null){

			//animation comes from .xml resource...
			Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.bt_screen_splash_effect_falldown);
			imageView.startAnimation(anim);
		    
		}//movingFade...

			
		
	 }

	/////////////////////////////////////////////////////////////////////
	//handles animation delay 
	Handler delayHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			delayHandler.removeCallbacks(animateDelayTask);
		}
	};		
	
	private Runnable animateDelayTask = new Runnable() {
		public void run() {
		    animateSplashScreen();
		}
	};	
	//end delay stuff
	/////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////
	//handles animation finished delay 
	Handler animationFinishedHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			animationFinishedHandler.removeCallbacks(animationFinished);
		}
	};		
	
	private Runnable animationFinished = new Runnable() {
		public void run() {
			BT_debugger.showIt(fragmentName + ":animationFinished");

       		//start the BT_activity_host activity...
    		Intent i = new Intent(getActivity(), BT_activity_host.class);
    		startActivity(i);
     		
    		//finish this activity (remove it from the back stack)...
    		getActivity().finish();
    		
			//fade in next screen...
            getActivity().overridePendingTransition(R.anim.bt_fadein,R.anim.bt_fadeout);

			
		}
	};	
	//end delay stuff
	/////////////////////////////////////////////////////////////////////
	
	
	
	//handles animation...
	public void animateSplashScreen(){
		BT_debugger.showIt(fragmentName + ":animateSplashScreen");
			
		/*
			This fragment should be running in BT_activity_splash. The layout file for this
			Android Activity is bt_activity_splash.xml. Get a reference to the "containerView"
			(a FrameLayout) in the layout file so we can animate it...	
		*/
		
		try{
	 	
			//get a reference to the host activities "containerView" This is a FrameLayout ...
			final FrameLayout containerView = (FrameLayout)this.getActivity().findViewById(R.id.containerView);
		
			//animation comes from .xml resource...
			Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.bt_fadeout);
			anim.setDuration((transitionDurationSeconds * 1000));
			containerView.startAnimation(anim);
			containerView.setVisibility(View.GONE);

			//delay the transition...
			animationFinishedHandler.removeCallbacks(animationFinished);
			animationFinishedHandler.postDelayed(animationFinished, (transitionDurationSeconds * 1000));
		
		}catch(java.lang.NullPointerException e){
			BT_debugger.showIt(fragmentName + ":animateSplashScreen EXCEPTION: " + e.toString());
		}
		
	}
	
}












/*
 *	Copyright 2011, David Book, buzztouch.com
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class BT_screen_customURL extends BT_fragment{
	
	public WebView webView = null;
	public String dataURL = "";
	public String currentURL = "";
	public String originalURL = "";
	public AlertDialog confirmLaunchInNativeAppDialogue = null;
	public AlertDialog confirmEmailDocumentDialogue = null;

	public String showBrowserBarBack = "";
	public String showBrowserBarLaunchInNativeApp = "";
	public String showBrowserBarEmailDocument = "";
	public String showBrowserBarRefresh = "";
	
	//onCreateView...
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState){
        
		/*
 			Note: fragmentName property is already setup in the parent class (BT_fragment). This allows us 
 			to add the 	name of this class file to the LogCat console using the BT_debugger.
		*/
		//show life-cycle event in LogCat console...
		BT_debugger.showIt(fragmentName + ":onCreateView JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");

		
		
		//inflate the layout file for this screen...
		View thisScreensView = inflater.inflate(R.layout.bt_screen_customurl, container, false);
		
	     
		//reference to the webview in the layout file.
		webView = (WebView) thisScreensView.findViewById(R.id.webView);
		webView.setBackgroundColor(0);
		webView.setInitialScale(0);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setPluginsEnabled(true);
		webView.setWebViewClient(new WebViewClient(){
	    	
	    	@Override
	    	public boolean shouldOverrideUrlLoading(WebView view, String url){
				
	    		//remember the URL...
	    		currentURL = url;
	    		
	    		//load the URL in the app's built-in browser if it's in our list of types to load...
	    		if(canLoadDocumentInWebView(url)){
	    			
	    			//load url in built-in browser...
	    			showProgress(null, null);
	    			return false;	    			

	    		
	    		}else{
	    			
	    			//ask user what app to open this in if the method returned NO...
	    			try{
	    				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	    				startActivity(Intent.createChooser(i, getString(R.string.openWithWhatApp)));  
	    			}catch(Exception e){
	    				BT_debugger.showIt(fragmentName + ": Error launching native app for url: " + url);
	    				showAlert(getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
	    			}
	    			
	    			//do not try to load the URL..
	    			return true;	    			
	    			

	    		}
	    		
	        }
	        
	    	@Override
	        public void onPageFinished(WebView view, String url){
	    		hideProgress();
			    BT_debugger.showIt(fragmentName + ":onPageFinished finished Loading: " + url);
	        }
	    	
	    	@Override
	    	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
	    		hideProgress();
	    		showAlert(getString(R.string.errorTitle), getString(R.string.errorLoadingScreen));
	    		BT_debugger.showIt(fragmentName = ":onReceivedError ERROR loading url: " + failingUrl + " Description: " + description);
	    	}	    	
        
        });	
		
    	
		//fill JSON properties...
		dataURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dataURL", "");
		
		//must have a dataURL...
		if(dataURL.length() < 1){
			dataURL = "http://www.google.com";
		}
		
		currentURL = dataURL;
		originalURL = dataURL;
		
		//button options for hardware menu key...
		showBrowserBarBack = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarBack", "1");
		showBrowserBarLaunchInNativeApp = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarLaunchInNativeApp", "1");
		showBrowserBarEmailDocument = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarEmailDocument", "1");
		showBrowserBarRefresh = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarRefresh", "1");
		
		
		//prevent user interaction?
		String preventUserInteraction = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "preventUserInteraction", "0");
		if(preventUserInteraction.equalsIgnoreCase("1")){
			//can't seem to get Android to "prevent user interaction"...??? 
		}
		
		//hide scroll bars..
		String hideVerticalScrollBar = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "hideVerticalScrollBar", "0");
		if(hideVerticalScrollBar.equalsIgnoreCase("1")){
			webView.setVerticalScrollBarEnabled(false);
		}
		String hideHorizontalScrollBar = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "hideHorizontalScrollBar", "0");
		if(hideHorizontalScrollBar.equalsIgnoreCase("1")){
			webView.setHorizontalScrollBarEnabled(false);
		}
		String preventAllScrolling = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "preventAllScrolling", "0");
		if(preventAllScrolling.equalsIgnoreCase("1")){
			webView.setVerticalScrollBarEnabled(false);
			webView.setHorizontalScrollBarEnabled(false);
		}		

		
		//figure out what to load...
		if(dataURL.length() > 1){

			String useUrl = BT_strings.mergeBTVariablesInString(dataURL);
			BT_debugger.showIt(fragmentName + ": loading URL from: " + useUrl);
			this.loadUrl(useUrl);
			
		}else{
			BT_debugger.showIt(fragmentName + ": No URL found? Not loading web-view!");
			showAlert(getString(R.string.errorTitle), getString(R.string.errorNoURL));
		}
		
		
		//return...
		return thisScreensView;
		
 		
	}//onCreateView...

    
    //load URL in webView
	public void loadUrl(String theUrl){
		BT_debugger.showIt(fragmentName + ": loadUrl");
		try{
			webView.loadUrl(theUrl);
		}catch(Exception e){
        	BT_debugger.showIt(fragmentName + ":loadUrl Exception: " + e.toString());
		}
	}
	

    
     //back button...
    public void handleBackButton(){
    	BT_debugger.showIt(fragmentName + ":handleBackButton");
    	if(webView.canGoBack()){
            webView.goBack();
        }else{
        	showToast(getString(R.string.errorNoHistory), "short");
        	BT_debugger.showIt(fragmentName + ":handleBackButton cannot go back?");
        }
    }
    
    //refresh button...
    public void handleRefreshButton(){
    	BT_debugger.showIt(fragmentName + ":handleRefreshButton Current URL: " + currentURL);
    	if(currentURL.length() > 1){
    		showProgress(null, null);
    		webView.loadUrl(currentURL);
    	}else{
        	
    		BT_debugger.showIt(fragmentName + ":handleRefreshButton cannot refresh URL: " + currentURL);
    	}
    }
    
    //launch in native app button...
    public void handleLaunchInNativeAppButton(){
    	BT_debugger.showIt(fragmentName + ":handleLaunchInNativeAppButton");
    	if(currentURL.length() > 1 && originalURL.length() > 1){
    		confirmLaunchInNativeApp();
    	}else{
    		showAlert(getString(R.string.errorTitle), getString(R.string.cannotOpenDocumentInNativeApp));
        	BT_debugger.showIt(fragmentName + ":handleLaunchInNativeAppButton NO url?");
    	}
    }
    
    //handle email button
    public void handleEmailDocumentButton(){
    	BT_debugger.showIt(fragmentName + ":handleLaunchInNativeAppButton");
    	
		//must be able to send email...
		if(perfectgrade_appDelegate.rootApp.getRootDevice().canSendEmail()){
    	
	    	//send a link to a url ...
			if(currentURL.length() > 1){
					
				try{
					
					//tell Android launch the native email application...
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
					emailIntent.setType("plain/text");  
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.sharingWithYou));  
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, currentURL);  	    	  
	    	  
					//chooser will prompt user if they have more than one email client..
					startActivity(Intent.createChooser(emailIntent, getString(R.string.openWithWhatApp)));  
						
		    	}catch(Exception e){
		    		
		    		showAlert(getString(R.string.errorTitle), getString(R.string.cannotEmailDocument));
		           	BT_debugger.showIt(fragmentName + ":handleEmailDocumentButton EXCEPTION " + e.toString());
		    	  
		    	}
		    	
			}else{
		   		
				showAlert(getString(R.string.errorTitle), getString(R.string.cannotEmailDocument));
	           	BT_debugger.showIt(fragmentName + ":handleEmailDocumentButton Cannot email document, URL not available");
				
			}				

		}else{
	   		
			showAlert(getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
           	BT_debugger.showIt(fragmentName + ":handleEmailDocumentButton Cannot email document, device doesn't support email");
			
		}				
	}		

    
    //confirm launch in native app
	public void confirmLaunchInNativeApp(){
		confirmLaunchInNativeAppDialogue = new AlertDialog.Builder(this.getActivity()).create();
		confirmLaunchInNativeAppDialogue.setTitle(getString(R.string.confirm));
		confirmLaunchInNativeAppDialogue.setMessage(getString(R.string.confirmLaunchInNativeBrowser));
		confirmLaunchInNativeAppDialogue.setIcon(R.drawable.icon);
		
		//YES
		confirmLaunchInNativeAppDialogue.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  confirmLaunchInNativeAppDialogue.dismiss();
	    	  
	    	  //tell Android to load the URL in the best available Native App...
  				try{
  					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(currentURL));
  					startActivity(i);
  				}catch(Exception e){
  					BT_debugger.showIt(fragmentName + ": Error launching native app for url: " + currentURL);
  					showAlert(perfectgrade_appDelegate.getApplication().getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
  				}	    	  
	    } }); 
		
		//NO
		confirmLaunchInNativeAppDialogue.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				confirmLaunchInNativeAppDialogue.dismiss();
		    } }); 
		
		//show the confirmation box...
		confirmLaunchInNativeAppDialogue.show();
	}		

	//confirm email document
	public void confirmEmailDocumentDialogue(){
		confirmEmailDocumentDialogue = new AlertDialog.Builder(this.getActivity()).create();
		confirmEmailDocumentDialogue.setTitle(getString(R.string.confirm));
		confirmEmailDocumentDialogue.setMessage(getString(R.string.confirmEmailDocument));
		confirmEmailDocumentDialogue.setIcon(R.drawable.icon);
		
		//YES
		confirmEmailDocumentDialogue.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  confirmEmailDocumentDialogue.dismiss();
	    	  
	    	  //tell Android launch the native email application...
	    	  Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
	    	  emailIntent.setType("plain/text");  
	    	  emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.sharingWithYou));  
	    	  emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, currentURL);  	    	  
	    	  
	    	  //chooser will propmpt user if they have more than one email client..
	    	  startActivity(Intent.createChooser(emailIntent, getString(R.string.openWithWhatApp)));  
	    	  
	    } }); 
		
		//NO
		confirmEmailDocumentDialogue.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				confirmEmailDocumentDialogue.dismiss();
		    } }); 
		
		//show the confirmation box...
		confirmEmailDocumentDialogue.show();
	}		


	   //onCreateOptionsMenu...
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		BT_debugger.showIt(fragmentName + ":onCreateOptionsMenu JSON itemId: \"" + screenItemId + "\"");

	 	/*
	 	 	Screens that extend BT_fragment should implement their own onCreateOptionsMenu() method to add
	 	 	action bar items specific to that screen. They should call super.onCreateOptionsMenu() AFTER
	 	 	adding customized items for that screen. 	  
	 	*/
	 	
	 	//for individual menu items...
	 	MenuItem menuItem = null;
	 	
		//back...
		if(showBrowserBarBack.equalsIgnoreCase("1")){
			menuItem = menu.add(Menu.NONE, 1, 0, getString(R.string.back));
			menuItem.setIcon(BT_fileManager.getDrawableByName("bt_arrow_left.png"));
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}

		//launch in native app...
		if(showBrowserBarLaunchInNativeApp.equalsIgnoreCase("1")){
			menuItem = menu.add(Menu.NONE, 2, 0, getString(R.string.browserOpenInNativeApp));
			menuItem.setIcon(BT_fileManager.getDrawableByName("bt_web_site.png"));
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}	
		
		//email document...
		if(showBrowserBarEmailDocument.equalsIgnoreCase("1")){
			menuItem = menu.add(Menu.NONE, 3, 0, getString(R.string.browserEmailDocument));
			menuItem.setIcon(BT_fileManager.getDrawableByName("bt_email.png"));
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}

		//refresh page...
		if(showBrowserBarRefresh.equalsIgnoreCase("1")){
			menuItem = menu.add(Menu.NONE, 4, 0, getString(R.string.browserRefresh));
			menuItem.setIcon(BT_fileManager.getDrawableByName("bt_refresh.png"));
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}		
	 	
		//call super...
		super.onCreateOptionsMenu(menu, inflater);
	    
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		BT_debugger.showIt(fragmentName + ":onOptionsItemSelected JSON itemId: \"" + screenItemId + "\" Selected Item's Id: " + item.getItemId());
	 	
	 	/*
	 	 	Handling menu item taps: Each screen that extends BT_fragment should implement
	 	 	the onOptionsItemSelected() method to handle taps for items specific to that
	 	 	screen. Each menu item added for a screen should use an itemId greater than -1.
	 	*/
		
	 	//handle item selection. 
	 	switch (item.getItemId()){
	 		 	
	 	  	case 1:
	 	  		//back...
	 			handleBackButton();
	 			return true;

	 	  	case 2:
	 			//native app...
	 	  		handleLaunchInNativeAppButton();
	 			return true;
	 			
	 	  	case 3:
	 	  		//email document...
	 	  		handleEmailDocumentButton();
	 	  		return true;
	 	  	
	 	  	case 4:
	 	  		//refresh browser...
	 	  		handleRefreshButton();
	 	  		return true;

	 	}
	   
	 	//return...
	 	return super.onOptionsItemSelected(item);
	   	   
	   
	}
	
	
	
	
	
}


 



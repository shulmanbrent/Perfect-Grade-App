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

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class BT_screen_customHTML extends BT_fragment{
	
	public DownloadScreenDataWorker downloadScreenDataWorker;
	public WebView webView = null;
	public String localFileName = "";
	public String saveAsFileName = "";
	public String dataURL = "";
	public String currentURL = "";
	public String originalURL = "";

	public String showBrowserBarBack = "";
	public String showBrowserBarLaunchInNativeApp = "";
	public String showBrowserBarEmailDocument = "";
	public String showBrowserBarRefresh = "";
	public String forceRefresh = "";
	
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
		View thisScreensView = inflater.inflate(R.layout.bt_screen_customhtml, container, false);
		
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
		localFileName = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "localFileName", "");
		forceRefresh = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "forceRefresh", "0");
		currentURL = dataURL;
		originalURL = dataURL;
		
		//if we have NO localFileName and no dataURL, use the sample file for this plugin...
		if(localFileName.length() < 1 && dataURL.length() < 1){
			localFileName = "bt_screen_customhtml_sample.html";
		}
		
		
		//setup the saveAsFileName
		if(localFileName.length() > 1){
			
			//use the file name in the JSON data...
			saveAsFileName = localFileName;	
			
			//copy the file from the /assets/BT_Docs folder so we can use it later...
			BT_fileManager.copyAssetToCache("BT_Docs", saveAsFileName);
			
		}else{
		
			//create a file name...
			saveAsFileName = this.screenData.getItemId() + "_screenData.html";
		
		}
		
		//remove file if we are force-refreshing...
		if(forceRefresh.equalsIgnoreCase("1")){
			BT_fileManager.deleteFile(saveAsFileName);
		}		

		//button options for hardware menu key...
		showBrowserBarBack = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarBack", "0");
		showBrowserBarLaunchInNativeApp = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarLaunchInNativeApp", "0");
		showBrowserBarEmailDocument = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarEmailDocument", "0");
		showBrowserBarRefresh = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarRefresh", "0");
		
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

			//if we have a cached version, load that...
			if(BT_fileManager.doesCachedFileExist(saveAsFileName) && !forceRefresh.equalsIgnoreCase("1")){
			
				//load from cache
				BT_debugger.showIt(fragmentName + ": loading from cache: " +  saveAsFileName);
				String theData = BT_fileManager.readTextFileFromCache(saveAsFileName);
				this.showProgress(null, null);
				this.loadDataString(theData);
				
				
			}else{
				
				//load from URL
				BT_debugger.showIt(fragmentName + ": loading from URL: " +  dataURL);
				this.downloadAndSaveFile(dataURL, saveAsFileName);
			
			}
			
		}else{
			
			//HTML doc must be in /assets/BT_Docs folder...
			if(!BT_fileManager.doesProjectAssetExist("BT_Docs", saveAsFileName)){
			
				BT_debugger.showIt(fragmentName + ": ERROR. HTML file \"" + saveAsFileName + "\" does not exist in BT_Docs folder and not URL found? Not loading.");
				showAlert(getString(R.string.errorTitle), getString(R.string.errorLoadingScreen));
			
			}else{
				
				//load from BT_Docs folder...
				BT_debugger.showIt(fragmentName + ": loading from BT_Docs: " +  saveAsFileName);
				this.showProgress(null, null);
				webView.loadUrl("file:///android_asset/BT_Docs/" + saveAsFileName);
				
			}
			
		}
		
        //return...
        return thisScreensView;
        
 		
	}//onCreateView...

    
    //load URL in webView...
	public void loadUrl(String theUrl){
		BT_debugger.showIt(fragmentName + ": loadUrl");
		try{
			webView.loadUrl(theUrl);
		}catch(Exception e){
        	BT_debugger.showIt(fragmentName + ":loadUrl Exception: " + e.toString());
		}
	}
	
	//load html string...
	public void loadDataString(String theString){
		BT_debugger.showIt(fragmentName + ": loadDataString");
		webView.loadDataWithBaseURL(null, theString, "text/html", "utf-8", "about:blank");
		hideProgress();
	}	
	
    //back button...
    public void handleBackButton(){
    	BT_debugger.showIt(fragmentName + ":handleBackButton");
    	if(webView.canGoBack()){
            webView.goBack();
        }else{
        	BT_debugger.showIt(fragmentName + ":handleBackButton cannot go back?");
        }
    }
    
    //refresh button...
    public void handleRefreshButton(){
    	BT_debugger.showIt(fragmentName + ":handleRefreshButton");
    	if(currentURL.length() > 1){
    		
    		//remove cached version...
    		BT_fileManager.deleteFile(saveAsFileName);
    		
    		//re-load...
    		loadUrl(currentURL);
    		
    	}else{
        	BT_debugger.showIt(fragmentName + ":handleRefreshButton cannot refresh?");
    	}
    }
    
    //launch in native app button...
    public void handleLaunchInNativeAppButton(){
    	BT_debugger.showIt(fragmentName + ":handleLaunchInNativeAppButton");
    	if(currentURL.length() > 1 && originalURL.length() > 1){
    		launchInNativeApp();
    	}else{
			showAlert(perfectgrade_appDelegate.getApplication().getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
        	BT_debugger.showIt(fragmentName + ":handleLaunchInNativeAppButton NO url?");
    	}
    }
    
    
    //launch in native app
	public void launchInNativeApp(){
       	if(this.dataURL.length() > 1){
           	BT_debugger.showIt(fragmentName + ":handleLaunchInNativeAppButton URL: " + this.dataURL);
			
           	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.dataURL));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(intent);				

       	}else{
       		showAlert(getString(R.string.errorTitle), getString(R.string.cannotOpenDocumentInNativeApp));
           	BT_debugger.showIt(fragmentName + ":handleLaunchInNativeAppButton ERROR, no URL. Cannot load local files in native browser.");
       	}
	}		

	//handleEmailDocumentButton
	public void handleEmailDocumentButton(){
		
		//must have already downloaded the document
		if(perfectgrade_appDelegate.rootApp.getRootDevice().canSendEmail() && BT_fileManager.doesCachedFileExist(saveAsFileName)){
		
			try{
				
	    		//copy file from cache to SDCard so emailer can access it (can't email from internal files directory)...
	    		BT_fileManager.copyFileFromCacheToSDCard(saveAsFileName);
	    		  
	  		   	//copy from assets to internal cache...
	            File file = new File(perfectgrade_appDelegate.getApplication().getExternalCacheDir(), saveAsFileName);
	    		String savedToPath = file.getAbsolutePath();
				
	    		//make sure file exists...
	    		if(file.exists()){
	    		
	    			//send from path...THIS IS REQUIRED OR GMAIL CLIENT WILL NOT INCLUDE ATTACHMENT
	    			String sendFromPath = "file:///sdcard/Android/data/com.perfectgrade/cache/" + saveAsFileName;

	    			//send a link to a url OR a file, not both...
	    			if(currentURL.length() > 1){
						
	    				//tell Android launch the native email application...
						Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
						emailIntent.setType("plain/text");  
						emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.sharingWithYou));  
						emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, currentURL);  	    	  
		    	  
						//chooser will prompt user if they have more than one email client..
						startActivity(Intent.createChooser(emailIntent, getString(R.string.openWithWhatApp)));  
	    			
	    			}else{
	    				
		    			//tell Android launch the native email application...
		    			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
		    			emailIntent.setType("text/plain");
		    			emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.sharingWithYou));  
		    			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\n" + getString(R.string.attachedFile));  	    	  
		    			emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(sendFromPath));
				    	  
		    			//open users email app...
				    	 startActivity(emailIntent);
	    				
	    			}//dataURL...
			
	    		}else{
		           	BT_debugger.showIt(fragmentName + ":handleEmailDocumentButton Cannot email document, file does not exist: " + savedToPath);
	    		}	
					
	    	}catch(Exception e){
	    		
	    		showAlert(getString(R.string.errorTitle), getString(R.string.cannotEmailDocument));
	           	BT_debugger.showIt(fragmentName + ":handleEmailDocumentButton EXCEPTION " + e.toString());
	    	  
	    	}
	    	
		}else{
	   		
			showAlert(perfectgrade_appDelegate.getApplication().getString(R.string.noNativeAppTitle), getString(R.string.noNativeAppDescription));
           	BT_debugger.showIt(fragmentName + ":handleEmailDocumentButton Cannot email document, no URL provided");
			
		}				
	}		

	
   //download and save file....
    public void downloadAndSaveFile(String dataURL, String saveAsFileName){
    	
       	//show progress
       	showProgress(null, null);

       	//trigger the download...
      	downloadScreenDataWorker = new DownloadScreenDataWorker();
    	downloadScreenDataWorker.setDownloadURL(dataURL);
    	downloadScreenDataWorker.setSaveAsFileName(saveAsFileName);
    	downloadScreenDataWorker.setThreadRunning(true);
    	downloadScreenDataWorker.start();
       	
       	
    }
   	
	///////////////////////////////////////////////////////////////////
	//DownloadScreenDataThread and Handler
	Handler downloadScreenDataHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			hideProgress();
			
			//read text file, load in webView...
			//if we have a cached version, load that...
			if(BT_fileManager.doesCachedFileExist(saveAsFileName)){
			
				//load from cache
				BT_debugger.showIt(fragmentName + ": loading from cache: " +  saveAsFileName);
				String theData = BT_fileManager.readTextFileFromCache(saveAsFileName);
				loadDataString(theData);
				
			}else{
				
				//error. We should have downloaded then cached...
				showAlert(getString(R.string.errorTitle), getString(R.string.fileNotDownloadedYet));
				BT_debugger.showIt(fragmentName + ": ERROR loading from cache after download: " +  dataURL);
			
			}
			
	
				
		}
	};	   
    
	
 
	public class DownloadScreenDataWorker extends Thread{
		 boolean threadRunning = false;
		 String downloadURL = "";
		 String saveAsFileName = "";
		 void setThreadRunning(boolean bolRunning){
			 threadRunning = bolRunning;
		 }	
		 void setDownloadURL(String theURL){
			 downloadURL = theURL;
		 }
		 void setSaveAsFileName(String theFileName){
			 saveAsFileName = theFileName;
		 }
		 @Override 
    	 public void run(){
			
			 //downloader will fetch and save data..Set this screen data as "current" to be sure the screenId
			 //in the URL gets merged properly. Several screens could be loading at the same time...
			 String useURL = BT_strings.mergeBTVariablesInString(dataURL);
			 BT_debugger.showIt(fragmentName + ":downloading HTML (plain / text)  data from " + useURL + " Saving As: " + saveAsFileName);
			 BT_downloader objDownloader = new BT_downloader(useURL);
			 objDownloader.setSaveAsFileName(saveAsFileName);
			 @SuppressWarnings("unused")
			 String result = objDownloader.downloadTextData();
			
			 //send message to handler..
			 this.setThreadRunning(false);
			 downloadScreenDataHandler.sendMessage(downloadScreenDataHandler.obtainMessage());
   	 	
		 }
	}	
	//END DownloadScreenDataThread and Handler
	///////////////////////////////////////////////////////////////////	
	
		
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
			menuItem.setIcon(BT_fileManager.getDrawableByName("bt_prev.png"));
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}

		//launch in native app...
		if(showBrowserBarLaunchInNativeApp.equalsIgnoreCase("1")){
			menuItem = menu.add(Menu.NONE, 2, 0, getString(R.string.browserOpenInNativeApp));
			menuItem.setIcon(BT_fileManager.getDrawableByName("bt_action.png"));
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}	
		
		//email document...
		if(showBrowserBarEmailDocument.equalsIgnoreCase("1")){
			menuItem = menu.add(Menu.NONE, 3, 0, getString(R.string.browserEmailDocument));
			menuItem.setIcon(BT_fileManager.getDrawableByName("bt_compose.png"));
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}

		//refresh page...
		if(showBrowserBarRefresh.equalsIgnoreCase("1") && dataURL.length() > 1){
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


 



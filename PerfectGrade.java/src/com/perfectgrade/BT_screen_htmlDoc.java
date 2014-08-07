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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;


public class BT_screen_htmlDoc extends BT_fragment{
	
	public String dataURL = "";
	public String localFileName = "";
	public String saveAsFileName = "";
	public Button openWithButton;
	public Button downloadButton;
	public ImageView documentIconView;
	public AlertDialog confirmRefreshDialogue = null;
	public DownloadScreenDataWorker downloadScreenDataWorker;
	
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
		View thisScreensView = inflater.inflate(R.layout.bt_screen_htmldoc, container, false);
		
		//reference to the document type icon...
		documentIconView = (ImageView) thisScreensView.findViewById(R.id.documentTypeIcon);
		documentIconView.setImageDrawable(BT_fileManager.getDrawableByName("bt_screen_htmldoc.png"));
		
		//reference to "open" and "donwload" buttons...
		openWithButton = (Button) thisScreensView.findViewById(R.id.openWithButton);
		downloadButton = (Button) thisScreensView.findViewById(R.id.downloadButton);
		
		//fill JSON properties...
		localFileName = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "localFileName", "");
		dataURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dataURL", "");
		showBrowserBarEmailDocument = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarEmailDocument", "1");
		showBrowserBarRefresh = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "showBrowserBarRefresh", "0");
		forceRefresh = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "forceRefresh", "");
		
		//if we have NO localFileName and no dataURL, use the sample file for this plugin...
		if(localFileName.length() < 1 && dataURL.length() < 1){
			localFileName = "bt_screen_htmldoc_sample.html";
		}
		
		
		//figure out the saveAsFileName...
		if(localFileName.length() > 1){
			
			//use the file name in the JSON data...
			saveAsFileName = localFileName;			
			
			//copy the file from the /assets/BT_Docs folder to the cache if it doesn't already exist...
			if(BT_fileManager.doesProjectAssetExist("BT_Docs",saveAsFileName)){
				if(!BT_fileManager.doesCachedFileExist(saveAsFileName)){
					BT_fileManager.copyAssetToCache("BT_Docs", saveAsFileName);
				}
			}else{
				showAlert(perfectgrade_appDelegate.getApplication().getString(R.string.errorTitle), getString(R.string.errorDocumentNotInProject));
		        BT_debugger.showIt(fragmentName + ":onCreate. localFileName \"" + saveAsFileName + "\" does not exist in project! ");	
			}
			
		}else{
			saveAsFileName = this.screenData.getItemId() + "_screenData.xls";
		}
		
		//remove file if we are force-refreshing...
		if(forceRefresh.equalsIgnoreCase("1")){
			BT_fileManager.deleteFile(saveAsFileName);
		}

		//change click event for "open with" button...
		openWithButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	openDocInCache(saveAsFileName);
            }
        });       
       
		//click event for "document image"...
		documentIconView.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	openDocInCache(saveAsFileName);
            }
        });       
	
		//click event of the download button...
		downloadButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				downloadAndSaveFile(dataURL, saveAsFileName);
            }
            
		});
		
		//if file is not in the cache, disable the openWith button...
		if(!BT_fileManager.doesCachedFileExist(saveAsFileName)){
			openWithButton.setEnabled(false);
		}
			
		//hide download button if we don't have a URL...
		if(dataURL.length() < 5){
			downloadButton.setVisibility(View.GONE);
		}else{
			//if we have a dataURL, AND we have a cached "saveAsFileName", change label to "refresh"...
			if(BT_fileManager.doesCachedFileExist(saveAsFileName)){
				downloadButton.setText(getString(R.string.refreshFromURL));
				downloadButton.setOnClickListener(new OnClickListener(){
					public void onClick(View v){
						handleRefreshButton();
		            }
		            
				});
			}
		}
		
		//add face effect to image (it's not a button, it's an image)...
		AlphaAnimation alphaFade = new AlphaAnimation(0.3f, 1.0f);
		alphaFade.setDuration(500);
		alphaFade.setFillAfter(true);
		documentIconView.startAnimation(alphaFade);
		
       //return...
		return thisScreensView;
 		
	}//onCreateView...

	
	//openDocInCache...
    public void openDocInCache(String fileName){
        BT_debugger.showIt(fragmentName + ":openDocInCache. Calling helper method in BT_viewUtilities...");	
        
		//make sure doc is in cache already...
		if(BT_fileManager.doesCachedFileExist(saveAsFileName)){
			
			//use helper in BT_viewUtilities method to open the document...
			BT_viewUtilities.openDocInCacheWithMimeType(saveAsFileName, "text/html", this.getActivity());
			
		}else{
			
			//show document not cached alert..
			showAlert(perfectgrade_appDelegate.getApplication().getString(R.string.errorTitle), getString(R.string.errorDocumentNotInCache));
           	BT_debugger.showIt(fragmentName + ":handleEmailDocumentButton Cannot email document, document not in the cache?");
			
		}
		
     }
	
	//handleEmailButton...
	public void handleEmailButton(){
		BT_debugger.showIt(fragmentName + ":handleEmailButton. Calling helper method in BT_viewUtilities...");	
        
    	//make sure device can send email...
		if(perfectgrade_appDelegate.rootApp.getRootDevice().canSendEmail()){
			
			//make sure doc is in cache already...
			if(BT_fileManager.doesCachedFileExist(saveAsFileName)){
				
				//use helper method in BT_viewUtilities to email document...
				BT_viewUtilities.emailDocumentInCacheWithMimeType(saveAsFileName, "text/html", this.getActivity());
				
			}else{
				
				//show document not cached alert..
				showAlert(perfectgrade_appDelegate.getApplication().getString(R.string.cannotEmailDocument), getString(R.string.errorDocumentNotInCache));
	           	BT_debugger.showIt(fragmentName + ":handleEmailButton Cannot email document, document not in the cache?");
				
			}
			
		}else{
			
			//show cannot email alert...
			showAlert(perfectgrade_appDelegate.getApplication().getString(R.string.cannotEmailDocument), getString(R.string.noNativeAppDescription));
           	BT_debugger.showIt(fragmentName + ":handleEmailButton Cannot email document, device cannot send emails?");
			
		}
	 		
	}
	
    //refresh button...
    public void handleRefreshButton(){
    	if(dataURL.length() > 1){
        	BT_debugger.showIt(fragmentName + ":handleRefreshButton Current URL: " + dataURL);
    	
			//we have a dataURL, confirm if we have already downloaded it...
			if(BT_fileManager.doesCachedFileExist(saveAsFileName)){

				//confirm...
				confirmRefreshDocument();
	    		
			}else{
				
				//download document...
				downloadAndSaveFile(dataURL, saveAsFileName);
				
			}

    	}else{
    		
    		//show no URL alert...
			showAlert(perfectgrade_appDelegate.getApplication().getString(R.string.cannotRefreshDocument), getString(R.string.errorNoURL));
    		BT_debugger.showIt(fragmentName + ":handleRefreshButton cannot refresh document, no dataURL provided");
    	}
    }
    
    //confirmRefreshDocument...
	public void confirmRefreshDocument(){
		confirmRefreshDialogue = new AlertDialog.Builder(this.getActivity()).create();
		confirmRefreshDialogue.setTitle(getString(R.string.confirm));
		confirmRefreshDialogue.setMessage(getString(R.string.confirmRefreshDocument));
		confirmRefreshDialogue.setIcon(R.drawable.icon);
		
		//YES
		confirmRefreshDialogue.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	    	  confirmRefreshDialogue.dismiss();
				downloadAndSaveFile(dataURL, saveAsFileName);
	    } }); 
		
		//NO
		confirmRefreshDialogue.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				confirmRefreshDialogue.dismiss();
		    } }); 
		
		//show the confirmation box...
		confirmRefreshDialogue.show();
	}		    
    
    

    //download and save file....
    public void downloadAndSaveFile(String dataURL, String saveAsFileName){
    	
       	//hide graphic...
       	documentIconView.setVisibility(View.GONE);
       	
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
			documentIconView.setVisibility(View.VISIBLE);
			openWithButton.setEnabled(true);
			downloadButton.setText(getString(R.string.refreshFromURL));
			
			//change click handler of download button to confirm refresh...
			downloadButton.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					handleRefreshButton();
	            }
	            
			});
			
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
			
			 //downloader will fetch and save data..
			 String useURL = BT_strings.mergeBTVariablesInString(dataURL);
			 BT_debugger.showIt(fragmentName + ":downloading binary data from " + useURL + " Saving As: " + saveAsFileName);
			 BT_downloader objDownloader = new BT_downloader(useURL);
			 objDownloader.setSaveAsFileName(saveAsFileName);
			 @SuppressWarnings("unused")
			 String result = objDownloader.downloadAndSaveBinaryData();
			
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
	 	
		//refresh page...
		if(showBrowserBarRefresh.equalsIgnoreCase("1")){
			menuItem = menu.add(Menu.NONE, 1, 0, getString(R.string.browserRefresh));
			menuItem.setIcon(BT_fileManager.getDrawableByName("bt_refresh.png"));
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}		
		
		//email document...
		if(showBrowserBarEmailDocument.equalsIgnoreCase("1")){
			menuItem = menu.add(Menu.NONE, 2, 0, getString(R.string.browserEmailDocument));
			menuItem.setIcon(BT_fileManager.getDrawableByName("bt_email.png"));
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
	 			//refresh button...
	 	  		handleRefreshButton();
	 			return true;

	 	  	case 2:
	 	  		//email button...
	 	  		handleEmailButton();
	 	  		return true;
	 			
	 	}
	   
	 	//return...
	 	return super.onOptionsItemSelected(item);
	   	   
	   
	}
		
	
	
	
	
	
}


 



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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class BT_fragment_load_config_data extends BT_fragment{

	public String configData = "";
	public boolean needsRefreshed = false;
	
	
	//default constructor
	public BT_fragment_load_config_data(){
		
		/*
			we use an empty constructor when instantiating this fragment so we can create an instance of it and
			set it's "needsRefreshedProperty" before the "onCreateView()" method runs...
		*/
	
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	//fragment life-cycle events.
	
	//onCreate...
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.fragmentName = "BT_fragment_load_config_data";
        BT_debugger.showIt(fragmentName + ":onCreate");	
		
		//allow modification of host activities options menu...
		setHasOptionsMenu(true);

	}
	
	//onCreateView...	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
		BT_debugger.showIt(fragmentName + ":onCreateView");	
		View view = inflater.inflate(R.layout.bt_fragment_load_config_data, container, false);
		
		/*
			loadAppConfigDataAfterDelay will trigger "loadAppData" after a delay. This keeps the UI from
			flashing the loading graphic too fast when it only takes a second to load the JSON...
		*/
		loadAppConfigDataAfterDelay(2000);
		
		//return...
		return view;
	 }		
	
	 //END fragment life-cycle events...
	 /////////////////////////////////////////////////////

	 //loadAppConfigDataAfterDelay...
	 public void loadAppConfigDataAfterDelay(int delayMilliseconds){
		BT_debugger.showIt(fragmentName + ":loadAppConfigDataAfterDelay: Delaying " + delayMilliseconds + " milliseconds");			

		//show progress...
		this.showProgress(null, null);
		
		//this is a simple "pause" to make sure the loading indicator lasts at least 2 seconds...
    	Handler loadDataHandler = new Handler();
    	loadDataHandler.postDelayed(new Runnable(){
            public void run(){
            	loadAppConfigData();
            }
        }, delayMilliseconds);		

	 }
    
    
	 //loadAppConfigData...
	 public void loadAppConfigData(){
		BT_debugger.showIt(fragmentName + ":loadAppConfigData");			

		//start a new thread to load data in the background...
		new Thread(){
			
			@Override
			public void run(){
				
		   		//prepare looper...
	    		Looper.prepare();

	    		//update device connection type...
				perfectgrade_appDelegate.rootApp.getRootDevice().updateDeviceConnectionType();
				
				//update device size...
				perfectgrade_appDelegate.rootApp.getRootDevice().updateDeviceSize();
				
	 			//fill string with config.txt data included in the project...
				configData = "";
						
				//configuration file locations are stored in the app delegate so they are available globally...
				String configurationFileName = perfectgrade_appDelegate.configurationFileName;
				String cachedConfigDataFileName = perfectgrade_appDelegate.cachedConfigDataFileName;
				String cachedConfigModifiedFileName = perfectgrade_appDelegate.cachedConfigModifiedFileName;
				
				String projectConfigData = "";
				boolean checkForCachedData = true;
				
				try{
					BT_debugger.showIt(fragmentName + ":loadAppConfigData loading \"" + perfectgrade_appDelegate.configurationFileName + "\" from /assests folder in project...");			
					if(BT_fileManager.doesProjectAssetExist("", configurationFileName)){
						projectConfigData = BT_fileManager.readTextFileFromAssets("", configurationFileName);
						BT_debugger.showIt(fragmentName + ":loadAppConfigData loaded \"" + perfectgrade_appDelegate.configurationFileName + "\" from /assets folder successfully...");			
					}
				}catch(Exception e){
					BT_debugger.showIt(fragmentName + ":loadAppConfigData EXCEPTION loading \"" + perfectgrade_appDelegate.configurationFileName + "\" from the /assets folder " + e.toString());			
				}
				
				//did we find any local data...
				if(projectConfigData.length() < 1){
					BT_debugger.showIt(fragmentName + ":loadAppConfigData ERROR loading \"" + perfectgrade_appDelegate.configurationFileName + "\" from the /assets folder?");			
					
					//tell hander we're done. It's on the main UI thread so we can show an alert...
					sendMessageToMainThread(0);

				}else{

					//look for dataURL in BT_conifg.txt in the project...
					if(perfectgrade_appDelegate.rootApp.getDataURLFromAppData(projectConfigData).length() < 1){

						//because the configuration data in the project does not use a dataURL we need to remove a
						//possible cached version. Unusual for this to happen but possible...
							
						BT_debugger.showIt(fragmentName + ":loadAppConfigData \"" + perfectgrade_appDelegate.configurationFileName + "\" file does not use a dataURL for remote updates...");			
						BT_fileManager.deleteFile(cachedConfigDataFileName);
						
						//no sense in checking for cached data if we dont' use a dataURL...
						checkForCachedData = false;
						
						//configData is the data from BT_config.txt in the project, it's available outside this thread now...
						configData = projectConfigData;
		    			BT_debugger.showIt(fragmentName + ":loadAppConfigData continuing to load with \"" + perfectgrade_appDelegate.configurationFileName + "\" data in project...");	
							
					}else{
						
		    			BT_debugger.showIt(fragmentName + ":loadAppConfigData \"" + perfectgrade_appDelegate.configurationFileName + "\" file does use a dataURL for remote updates...");	
						checkForCachedData = true;
						
					}
					
				}//if projectConfigData.length() 
				
				//check for cached version of app's config data (we only do this if we have a dataURL...
				if(checkForCachedData){
					if(BT_fileManager.doesCachedFileExist(cachedConfigDataFileName)){
						
						configData = BT_fileManager.readTextFileFromCache(cachedConfigDataFileName);
						BT_debugger.showIt(fragmentName + ":loadAppConfigData reading " + cachedConfigDataFileName + " from the applications download cache...");
		    			BT_debugger.showIt(fragmentName + ":loadAppConfigData ignoring BT_config.txt file included in the project (using cached version instead)...");	
					
					}else{
						
						//configData becomes the projectConfigData...
						configData = projectConfigData;
		    			BT_debugger.showIt(fragmentName + ":loadAppConfigData " + cachedConfigDataFileName + " does not exist in the cache, using the " + configurationFileName + " file included in project...");	
					
					}
				}

				
				//at this point config data is from cache or from the config file included in the project ...
				if(configData.length() > 1){
					
					try{
					
						//validate the data...
			    	    if(perfectgrade_appDelegate.rootApp.validateApplicationData(configData)){
			    			BT_debugger.showIt(fragmentName + ":loadAppConfigData application data appears to be valid JSON...");	

			    			//ask the rootApp object to parse this data. This places it in memory for fast access...
			    	    	perfectgrade_appDelegate.rootApp.parseAppJSONData(configData);

			    	    }else{
			    	    	
			    			BT_debugger.showIt(fragmentName + ":loadAppConfigData application data is not valid JOSN data? You could try to use an online JSON validator. Several good ones exist online. Exiting App.");	
			    	    	configData = "";
			    	    
			    	    }
						
			    	    //Download new data if we have a dataURL and needsRefreshed = true. The needsRefreshed flag will equal true
			    	    //when the user taps a refresh option...
						String dataURL = perfectgrade_appDelegate.rootApp.getDataURL();
						dataURL = BT_strings.mergeBTVariablesInString(dataURL);
						if(dataURL.length() > 5 && needsRefreshed){
						
							//if we have a currentMode, append it to the end of the URL...
							if(perfectgrade_appDelegate.rootApp.getCurrentMode().length() > 1){
								//dataURL += "&currentMode=" + perfectgrade_appDelegate.rootApp.getCurrentMode();
							}					

				 			BT_debugger.showIt(fragmentName + ":loadAppConfigData downloading app data from: " + dataURL);
		
				 			//downloader object..
		    			 	BT_downloader objDownloader = new BT_downloader(dataURL);
		    			 	objDownloader.setSaveAsFileName("");
		    			 	String downloadedData = objDownloader.downloadTextData();
		    			 	if(downloadedData.length() > 5){
		    			 		
		    			 		
		      			 		//if it's valid, remove the previously cached version...
		    			 		if(perfectgrade_appDelegate.rootApp.validateApplicationData(downloadedData)){
		        	    			    			 		
		    			 			BT_debugger.showIt(fragmentName + ":loadAppConfigData Done downloading, JSON appears valid.");
		        	    			perfectgrade_appDelegate.rootApp.parseAppJSONData(downloadedData);
		        	    			configData = downloadedData;
		 
		        	    			//delete previously cached data (this will not delete the apps "last modified" timestamp file)...
		        	    			BT_fileManager.deleteAllCachedData(cachedConfigModifiedFileName);
		        	    			
		        	    			//save downloaded configuration data to cache...
		        	    			BT_fileManager.saveTextFileToCache(downloadedData, cachedConfigDataFileName);
		        	    			
		        					//flag needs refreshed as false...
		        					needsRefreshed = false;

		        	    			
		    			 		}else{
		        	    			BT_debugger.showIt(fragmentName + ":loadAppConfigData done downloading. Newly downloaded data is NOT valid, not caching. Try a JSON validator?");
		    			 		}
		    			 		
		    			 		
		        			}else{
		        				showAlert(getString(R.string.errorDownloadingData), "long");
		        				BT_debugger.showIt(fragmentName + ":loadAppConfigData ERROR (7) downloading data from: " + dataURL);
		    			 	}
		    			 	
		    			 	
						} //downloadAppData
					
					}catch(Exception e){
						BT_debugger.showIt(fragmentName + ":loadAppConfigData An exception occurred (55). " + e.toString());
						configData = "";
					}
				
				}//if configData 
				
				//send message to main thread...
				sendMessageToMainThread(0);
				
			}
			
			//send message....
			private void sendMessageToMainThread(int what){
				BT_debugger.showIt(fragmentName + ":loadAppConfigData:sendMessageToMainThread " + what);

				Message msg = Message.obtain();
				msg.what = what;
				loadAppDataDoneHandler.sendMessage(msg);
			}
			
		}.start();
		
	 }
        
	 //handles messages after load data thread completes...
	 private Handler loadAppDataDoneHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
 			
			//hide progress...
 			hideProgress();
 			
 			//config data must be loaded at this point...
			if(configData.length() < 1){
			
				showAlert(getString(R.string.errorTitle), getString(R.string.errorConfigData) + " (2)");
			
			}else{

				
 		 	    //((BT_activity_start)getActivity()).configureEnvironment();

				
				
				/*
					This loading fragment can be included in any activity that implements
					a method named "configureEnvironment". This allows app owners to create
					customizable host-activities that run all the plugins (fragments)...
				*/

		        //use reflection to find the method...
				java.lang.reflect.Method setupMethod = null;
				Activity hostActivity = getActivity();
				try{
					setupMethod = hostActivity.getClass().getMethod("configureEnvironment");
				}catch(SecurityException e){
					BT_debugger.showIt(fragmentName + ":loadAppDataDoneHandler. EXCEPTION (0): " + e.toString());
				}catch(NoSuchMethodException e){
					BT_debugger.showIt(fragmentName + ":loadAppDataDoneHandler. Current host activity does not have an \"configureEnvironment\" method implemented");
				}
				
				//fire the handleBackButton method if we found it...
				if(setupMethod != null){
			    	try{
			    		setupMethod.invoke(hostActivity);
					}catch(Exception e){
						BT_debugger.showIt(fragmentName + ":loadAppDataDoneHandler. EXCEPTION (1): " + e.toString());
					}
				}
				
				
			}		
		}
	 };
	
	
	 /////////////////////////////////////////////////////////
	 //menu methods...
	
	 //onCreateOptionsMenu...
	 @Override
	 public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	 	BT_debugger.showIt(fragmentName + ":onCreateOptionsMenu");

		//each menu item gets a unique id so we can figure out which one was tapped...
		int menuItemId = 0;
		
		//create each menu item...
	 	MenuItem menuItem = null;
	 	
		menuItem = menu.add(Menu.NONE, menuItemId, 0, getString(R.string.refreshAppData));
		menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
	    
		//call super AFTER adding this screen's menu items...
		super.onCreateOptionsMenu(menu, inflater);

	 }
	
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item) {
	 	BT_debugger.showIt(fragmentName + ":onOptionsItemSelected. Selected Item: " + item.getItemId());
	 	
		//handle item selection
	 	switch (item.getItemId()){
	   		case 0:
	   		this.needsRefreshed = true;
	   		this.loadAppConfigData();
	   		
	   		//bail...
	   		return true;
	   		
	 	}
	   
	 	//call super AFTER handling this menu item tap...
	 	return super.onOptionsItemSelected(item);
	   	   
	   
	 }	
	
	 //end menu methods...
	 /////////////////////////////////////////////////////////
	
	
	
}

































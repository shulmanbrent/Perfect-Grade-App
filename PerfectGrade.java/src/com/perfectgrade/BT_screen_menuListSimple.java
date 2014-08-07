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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;



public class BT_screen_menuListSimple extends BT_fragment implements OnScrollListener{
	
	boolean didCreate = false;
	boolean didLoadData = false;
	DownloadScreenDataWorker downloadScreenDataWorker;
	String JSONData = "";
	ArrayList<BT_item> childItems = null;
	ChildItemAdapter childItemAdapter;
	ListView myListView = null;
	int selectedIndex = -1;
	int currentFirstVisibleItem = -1;
	int currentVisibleItemCount = -1;
	int currentTotalItemCount = -1;
	int currentScrollState = -1;
	
	//properties from JSON
	String dataURL = "";
	String saveAsFileName = "";
	
	String listStyle = "";
	String preventAllScrolling = "";
	String listBackgroundColor = "";
	String listRowBackgroundColor = "";
	String listRowSelectionStyle = "";
	String listTitleFontColor = "";
	String listRowSeparatorColor = "";
	
	//these depend on large or small device...
	int listRowHeight = 0;
	int listTitleHeight = 0;
	int listTitleFontSize = 0;


	//////////////////////////////////////////////////////////////////////////
	//fragment life-cycle events.
	
	//onCreateView...	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        
		/*
 			Note: fragmentName property is already setup in the parent class (BT_fragment). This allows us 
 			to add the 	name of this class file to the LogCat console using the BT_debugger.
		*/
		//show life-cycle event in LogCat console...
		BT_debugger.showIt(fragmentName + ":onCreateView JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");

		

		//inflate the layout file for this screen...
		View thisScreensView = inflater.inflate(R.layout.bt_screen_menulistsimple, container, false);
		
		//////////////////////////////////////////////////////////////////////////////////////////
		//Load all the variables for this screen using the screenData's JSON values...
		
		childItems = new ArrayList<BT_item>();
		dataURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dataURL", "");
		saveAsFileName = this.screenData.getItemId() + "_screenData.txt";
		if(dataURL.length() < 1) BT_fileManager.deleteFile(saveAsFileName);
		
		///////////////////////////////////////////////////////////////////
		//properties for both large and small devices...
	
		preventAllScrolling = BT_strings.getStyleValueForScreen(this.screenData, "preventAllScrolling", "0");
		listBackgroundColor = BT_strings.getStyleValueForScreen(this.screenData, "listBackgroundColor", "clear");
		listRowBackgroundColor = BT_strings.getStyleValueForScreen(this.screenData, "listRowBackgroundColor", "clear");
		listRowSelectionStyle = BT_strings.getStyleValueForScreen(this.screenData, "listRowSelectionStyle", "");
		listTitleFontColor = BT_strings.getStyleValueForScreen(this.screenData, "listTitleFontColor", "#999999");
		listRowSeparatorColor = BT_strings.getStyleValueForScreen(this.screenData, "listRowSeparatorColor", "#999999");
		
		//settings that depend on large or small devices...
		if(perfectgrade_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
			
			//large devices...
			listRowHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listRowHeightLargeDevice", "70"));
			listTitleHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleHeightLargeDevice", "60"));
			listTitleFontSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleFontSizeLargeDevice", "25"));
		
		}else{
			
			//small devices...
			listRowHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listRowHeightSmallDevice", "50"));
			listTitleHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleHeightSmallDevice", "50"));
			listTitleFontSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleFontSizeSmallDevice", "22"));
			
		}
		
		
		//the list view will not show the last divider under the last element. We have a view here to trick it. 
		View lastDivider = (View) thisScreensView.findViewById(R.id.listViewEnd);
		lastDivider.setBackgroundColor(BT_color.getColorFromHexString(listRowSeparatorColor));
		
		//reference to ListView in the layout file...
		myListView = (ListView) thisScreensView.findViewById(R.id.listView);
		myListView.setOnScrollListener(this);
		myListView.setVerticalScrollBarEnabled(false);
		myListView.setHorizontalScrollBarEnabled(false);
		myListView.setFastScrollEnabled(false);
		myListView.setSmoothScrollbarEnabled(false);

		//setup adapter for data...
    	childItemAdapter = new ChildItemAdapter();
		myListView.setAdapter(childItemAdapter);
		
		//prevent scrolling....
		preventAllScrolling = "1";
		if(preventAllScrolling.equalsIgnoreCase("1")){
			//prevent scrolling...
		}
		
		if(listBackgroundColor.length() > 0){
			myListView.setCacheColorHint(BT_color.getColorFromHexString(listBackgroundColor));
			myListView.setBackgroundColor(BT_color.getColorFromHexString(listBackgroundColor));
		}
		
		//separator color...
		if(listRowSeparatorColor.length() > 0){
			ColorDrawable dividerColor = new ColorDrawable(BT_color.getColorFromHexString(listRowSeparatorColor));
			myListView.setDivider(dividerColor);
			myListView.setDividerHeight(1);
		}

        //flag as created..
        didCreate = true;	
       
		//return view...
		return thisScreensView;
		
	 }		
	
	//onActivityCreated...
	@Override 
	public void onActivityCreated (Bundle savedInstanceState){
		 super.onActivityCreated(savedInstanceState);
		 BT_debugger.showIt(fragmentName + ":onActivityCreated Screen Id: \"" + this.screenData.getItemId() + "\"");

	}	
	
	
	//onStart
	@Override 
	public void onStart() {
		BT_debugger.showIt(fragmentName + ":onStart");	
		super.onStart();
		
		//make sure data adapter is set...
		if(childItemAdapter == null){
			childItemAdapter = new ChildItemAdapter();
		}
		
		//setup items...
		if(saveAsFileName.length() > 1){
			
			//check cache...
			String parseData = "";
			if(BT_fileManager.doesCachedFileExist(saveAsFileName)){
				BT_debugger.showIt(fragmentName + ":onStart using cached screen data");	
				parseData = BT_fileManager.readTextFileFromCache(saveAsFileName);
				parseScreenData(parseData);
			}else{
				//get data from URL if we have one...
				if(this.dataURL.length() > 1){
					BT_debugger.showIt(fragmentName + ":onStart downloading screen data from URL");	
					refreshScreenData();
				}else{
					//parse with "empty" data...
					BT_debugger.showIt(fragmentName + ":onStart using data from app's configuration file");	
					parseScreenData("");
				}
			}
				
		}//saveAsFileName
		
		
	}
	
    //onResume
    @Override
    public void onResume() {
       super.onResume();
       	BT_debugger.showIt(fragmentName + ":onResume");
		
	   //select previous item if coming "back"...
       if(didCreate && didLoadData){
	   		if(selectedIndex > -1){
				if(myListView != null){
					if(myListView.getAdapter().getCount() >= selectedIndex){

						myListView.requestFocusFromTouch();
						myListView.setSelection(selectedIndex);
 						myListView.setSelectionFromTop(selectedIndex, (listRowHeight * selectedIndex));


					}
				}	
			}
       }
       
       
    }
    
    //onPause
    @Override
    public void onPause() {
        super.onPause();
        //BT_debugger.showIt(fragmentName + ":onPause");	
    }
    
	
	//onStop
	@Override 
	public void onStop(){
		super.onStop();
        //BT_debugger.showIt(fragmentName + ":onStop");	
		if(downloadScreenDataWorker != null){
			boolean retry = true;
			downloadScreenDataWorker.setThreadRunning(false);
			while(retry){
				try{
					downloadScreenDataWorker.join();
					retry = false;
				}catch (Exception je){
				}
			}
		}
	}	

	//onDestroy...
	@Override
	public void onDestroy() {
        super.onDestroy();
       	BT_debugger.showIt(fragmentName + ":onDestroy Screen Id: \"" + this.screenData.getItemId() + "\"");
       	
       	//remember the UI state...
       	this.onSaveInstanceState(null);
       	
	}
    
	//onSaveInstanceState...
	@Override
	public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
		BT_debugger.showIt(fragmentName + ":onSaveInstanceState Screen Id: \"" + this.screenData.getItemId() + "\"");
    	
	}      
	//end fragment life-cycle events
	//////////////////////////////////////////////////////////////////////////
  
    //handles onScrollStateChanged..
	public void onScrollStateChanged(AbsListView view, int scrollState) {
        //BT_debugger.showIt(fragmentName + ":onScrollStateChanged");	
        
        this.currentScrollState = scrollState;
        this.isScrollCompleted();
        
    }

	//onScroll...
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,int totalItemCount){
        //BT_debugger.showIt(fragmentName + ":onScroll");	
		
		this.currentFirstVisibleItem = firstVisibleItem;
	    this.currentVisibleItemCount = visibleItemCount;
	    this.currentTotalItemCount = totalItemCount;
	    
    }
    
	//isScrollCompleted...
	public void isScrollCompleted() {
	    if (currentFirstVisibleItem + currentVisibleItemCount >= currentTotalItemCount) {
	        if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE) {

	            //add something to end of list when scroll is done?...
	        	
	        }
	    }
	}	
    
  
    //parse screenData...
    public void parseScreenData(String theJSONString){
        BT_debugger.showIt(fragmentName + ":parseScreenData");	
        //BT_debugger.showIt(fragmentName + ":parseScreenData " + theJSONString);
		//parse JSON string
    	try{

    		//empty data if previously filled...
    		childItems.clear();

            //if theJSONString is empty, look for child items in this screen's config data..
    		JSONArray items = null;
    		if(theJSONString.length() < 1){
    			if(this.screenData.getJsonObject().has("childItems")){
        			items =  this.screenData.getJsonObject().getJSONArray("childItems");
    			}
    		}else{
        		JSONObject raw = new JSONObject(theJSONString);
        		if(raw.has("childItems")){
        			items =  raw.getJSONArray("childItems");
        		}
    		}
  
    		//loop items..
    		if(items != null){
                for (int i = 0; i < items.length(); i++){
                	
                	JSONObject tmpJson = items.getJSONObject(i);
                	BT_item tmpItem = new BT_item();
                	if(tmpJson.has("itemId")) tmpItem.setItemId(tmpJson.getString("itemId"));
                	if(tmpJson.has("itemType")) tmpItem.setItemType(tmpJson.getString("itemType"));
                	tmpItem.setJsonObject(tmpJson);
                	childItems.add(tmpItem);
                	
                }//for
                
                
                //flag data loaded...
                didLoadData = true;
    			
    		}else{
            	showAlert(getString(R.string.errorTitle), getString(R.string.errorItems));
    			BT_debugger.showIt(fragmentName + ":parseScreenData NO CHILD ITEMS?");
    		}
    	}catch(Exception e){
			BT_debugger.showIt(fragmentName + ":parseScreenData EXCEPTION " + e.toString());
    	}
        
    	
 	    //setup list click listener
    	final OnItemClickListener listItemClickHandler = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
            	
            	//remember the selected item's index...
           		selectedIndex = position;
            	
              	//the BT_item
	        	BT_item tappedItem = (BT_item) childItems.get(position);
				String loadScreenWithItemId = BT_strings.getJsonPropertyValue(tappedItem.getJsonObject(), "loadScreenWithItemId", "");
				String loadScreenWithNickname = BT_strings.getJsonPropertyValue(tappedItem.getJsonObject(), "loadScreenWithNickname", "");
				
  				//bail if none...
				if(loadScreenWithItemId.equalsIgnoreCase("none")){
					return;
				}				
				
				
	           	//itemId, nickname or object...
	            BT_item tapScreenLoadObject = null;
	        	if(loadScreenWithItemId.length() > 1 && !loadScreenWithItemId.equalsIgnoreCase("none")){
	    			BT_debugger.showIt(fragmentName + ":handleItemTap loads screen with itemId: \"" + loadScreenWithItemId + "\"");
	        		tapScreenLoadObject = perfectgrade_appDelegate.rootApp.getScreenDataByItemId(loadScreenWithItemId);
	        	}else{
	        		if(loadScreenWithNickname.length() > 1){
	    				BT_debugger.showIt(fragmentName + ":handleItemTap loads screen with nickname: \"" + loadScreenWithNickname + "\"");
	        			tapScreenLoadObject = perfectgrade_appDelegate.rootApp.getScreenDataByItemNickname(loadScreenWithNickname);
	        		}else{
	        			try{
	    	    			JSONObject obj = tappedItem.getJsonObject();
	    		            if(obj.has("loadScreenObject")){
	    						BT_debugger.showIt(fragmentName + ":handleItemTap button loads screen object configured with JSON object.");
	    		            	JSONObject tmpLoadScreen = obj.getJSONObject("loadScreenObject");
	    		            	tapScreenLoadObject = new BT_item();
	        		            if(tmpLoadScreen.has("itemId")) tapScreenLoadObject.setItemId(tmpLoadScreen.getString("itemId"));
	        		            if(tmpLoadScreen.has("itemNickname")) tapScreenLoadObject.setItemNickname(tmpLoadScreen.getString("itemNickname"));
	        		            if(tmpLoadScreen.has("itemType")) tapScreenLoadObject.setItemType(tmpLoadScreen.getString("itemType"));
	        		            if(obj.has("loadScreenObject")) tapScreenLoadObject.setJsonObject(tmpLoadScreen);
	    		            }
	        			}catch(Exception e){
	    					BT_debugger.showIt(fragmentName + ":handleItemTap EXCEPTION reading screen-object for item: " + e.toString());
	        			}
	        		}
	        	}

	        	//if we have a screen object to load from the right-button tap, build a BT_itme object...
	        	if(tapScreenLoadObject != null){
	        		
	            	//call loadScreenObject in BT_fragment class...
	       			loadScreenObject(tappedItem, tapScreenLoadObject);
	       			
	        	}else{
	    			BT_debugger.showIt(fragmentName + ":handleItemTap ERROR. No screen is connected to this item?");	
	    			showAlert(perfectgrade_appDelegate.getApplication().getString(R.string.errorTitle), perfectgrade_appDelegate.getApplication().getString(R.string.errorNoScreenConnected));
	        	}
	        	            	
           	}
        };    
        myListView.setOnItemClickListener(listItemClickHandler);             

        //set the item adapter...
		myListView.setAdapter(childItemAdapter);
		myListView.setSelectionFromTop(currentFirstVisibleItem, 0);
        
        //invalidate list so it repaints...
        myListView.invalidate();
        
        //hide progress...
        hideProgress();
        
    }
    
 
   
    //refresh screenData
    public void refreshScreenData(){
        BT_debugger.showIt(fragmentName + ":refreshScreenData");	
    	showProgress(null, null);

    	if(dataURL.length() > 1){
	      	downloadScreenDataWorker = new DownloadScreenDataWorker();
        	downloadScreenDataWorker.setDownloadURL(dataURL);
        	downloadScreenDataWorker.setSaveAsFileName(saveAsFileName);
        	downloadScreenDataWorker.setThreadRunning(true);
        	downloadScreenDataWorker.start();
        }else{
            BT_debugger.showIt(fragmentName + ":refreshScreenData NO DATA URL for this screen? Not downloading.");	
        }
        
    }    
       
    ///////////////////////////////////////////////////////////////////
    //Adapter for Child Items
    private class ChildItemAdapter extends BaseAdapter{
 
        public int getCount(){
            return childItems.size();
        }

		public Object getItem(int position){
			if (position == childItems.size()){
				return null;
			}else{
				return childItems.get(position);
			}
		}

		public long getItemId(int position){
			if(position == childItems.size()){
				return -1;
			}else{
				return childItems.get(position).getItemIndex();
			}
		}
  
		
		//returns the view for each row...only inflate once to enable view recyling..
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			
	       	//the BT_item values for this item..
        	BT_item tmpItem = childItems.get(position);
			String titleText = BT_strings.getJsonPropertyValue(tmpItem.getJsonObject(), "titleText", "");
			
			//inflate the layout and set size / position properties only when we have to...
			if(rowView == null) {
				
				//inflate the view so we can get a reference to it's parts..				
				LayoutInflater inflater = (LayoutInflater) perfectgrade_appDelegate.getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.bt_screen_menulistsimple_item, parent, false);		
				
			}//rowView is null
			
			//get layout parts for this row..
			TextView titleView = (TextView) rowView.findViewById(R.id.titleView);
			titleView.setTextSize(listTitleFontSize);
			titleView.setHeight(listRowHeight);

			//tag the view...
			rowView.setTag(tmpItem);
		
			//title font color				
			if(listTitleFontColor.length() > 0){
				titleView.setTextColor(BT_color.getColorFromHexString(listTitleFontColor));
			}
			
			//title font size
			if(listTitleFontSize > 0){
				titleView.setTextSize(listTitleFontSize);
			}
			
			//title text...
			titleView.setText(titleText);
			
			//return
			return rowView;
		}

      
        
    }

   //END child items adapter...
    ///////////////////////////////////////////////////////////////////
         

    
    ///////////////////////////////////////////////////////////////////
	//DownloadScreenDataThread and Handler
	Handler downloadScreenDataHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			if(JSONData.length() < 1){
				hideProgress();
				showAlert(getString(R.string.errorTitle), getString(R.string.errorDownloadingData));
			}else{
				parseScreenData(JSONData);
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
			 perfectgrade_appDelegate.rootApp.setCurrentScreenData(screenData);
			 String useURL = BT_strings.mergeBTVariablesInString(dataURL);
			 BT_debugger.showIt(fragmentName + ": downloading screen data from " + useURL);
			 BT_downloader objDownloader = new BT_downloader(useURL);
			 objDownloader.setSaveAsFileName(saveAsFileName);
			 JSONData = objDownloader.downloadTextData();
			
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
	 	
		//refresh screen data...
		if(dataURL.length() > 1){
			menuItem = menu.add(Menu.NONE, 1, 0, getString(R.string.refreshScreenData));
			menuItem.setIcon(this.getActivity().getResources().getDrawable(R.drawable.bt_refresh));
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
	 	  		//refreshScreenData...
	 	  		refreshScreenData();
	 			return true;

	 	}
	   
	 	//return...
	 	return super.onOptionsItemSelected(item);
	   	   
	   
	}	
	
    
}














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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class BT_plugin_sample extends BT_fragment{

	/*
	 
	 	These are declared in the parent class (BT_fragment) and available in this class
	 	--------------------------------------------------------------------------------
	 	String fragmentName (added to the LogCat console when debugging)
		String screenItemId (the itemId from the JSON in the BT_config.txt file)
		BT_item screenData (the JSON data for this screen from the BT_config.txt file)
		ArrayList<BT_item> actionBarMenuItems (Action Bar menu items if a BT_menu is associated with this screen)
	*/
	 

	//onCreateView...BT_activity_host needs a view to display when it creates this fragment...
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState){
        
		/*
 			Note: fragmentName property is already setup in the parent class (BT_fragment). This allows us 
 			to add the 	name of this class file to the LogCat console using the BT_debugger.
		*/
		//show life-cycle event in LogCat console...
		BT_debugger.showIt(fragmentName + ":onCreateView JSON itemId: \"" + screenData.getItemId() + "\" itemType: \"" + screenData.getItemType() + "\" itemNickname: \"" + screenData.getItemNickname() + "\"");

		//inflate the layout file for this screen...		
		View thisScreensView = inflater.inflate(R.layout.bt_plugin_sample, container, false);
		
		//find the TextView in this screen's layout file...
		TextView tmpTextView = (TextView)thisScreensView.findViewById(R.id.dynamicText);
		
		//use the BT_strings class to find the value of the "dynamicText" property in this screen's JSON...
		String tmpText = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dynamicText", "");
		
		//if we didn't find any dynamic text, because the property was not provided or it's value was blank....
		if(tmpText.length() < 1){
			tmpText = "See the README-PLUGINS.txt\nfile for an exlanation";
		}
		
		//set the text...
		tmpTextView.setText(tmpText);
		
		//find the ButtonView in this screen's layout file...
		Button tmpButton = (Button)thisScreensView.findViewById(R.id.dynamicButton);

		//use the BT_strings class to find a possible "buttonText" property in this screen's JSON data (it may not exist)...
		String tmpButtonText = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dynamicButtonText", "Tap Me");
		if(tmpButtonText.length() > 1){
			
			//if we found the button text in this screen's JSON data, set it's value on the button...
			tmpButton.setText(tmpButtonText);
			
		}else{
			
			//not button text was found in the JSON data for this screen, hide the button...
			tmpButton.setVisibility(View.GONE);
		}
		
		//show a simple alert when the button's tapped...
		tmpButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				showAlert("Button Tapped", "You tapped a button that was setup in the .xml layout file for this plugin");
            }
	            
		});
			
		
		//setup a wobble animation on the success graphic for fun...
		final ImageView successImage = (ImageView)thisScreensView.findViewById(R.id.successImage);
		successImage.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Animation wobble = AnimationUtils.loadAnimation(getActivity(), R.anim.bt_wobble);
				successImage.startAnimation(wobble);
            }
		});

			
		//return the layout file as the view for this screen...
		return thisScreensView;
	}			
	

	
	/////////////////////////////////////////////////////////
	//Custom Action Bar Items...
	
	//onCreateOptionsMenu...
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	 	BT_debugger.showIt(fragmentName + ":onCreateOptionsMenu");

	 	/*
 	 		Creating custom Action Bar items: This screen extends BT_fragment which implements
 	 		it's own onCreateOptionsMenu() method. That method (see BT_fragment.onCreateOptionsMenu()) 
 	 		creates some Action Bar items automatically. These items may be...
 
 	 		--1) A list of options associated with a BT_menu setup in the BT_config.txt config file. 
  	 		--2) A "Refresh App Data" button (to reload the apps data). This item will have an itemId = -1
 	 		--3) A "Close" button (to close the menu). This item will have an itemId = -2
 	 		
 	 		The onOptionsItemSelected()	handles menu item taps, itemId's are used to "figure out what 
 	 		option was tapped." This means custom Action Bar items will never have id's greater than 50.
 	 	
 	 	 */
	 	
	    
		//call super AFTER adding custom Action Bar items...
		super.onCreateOptionsMenu(menu, inflater);

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	 	BT_debugger.showIt(fragmentName + ":onOptionsItemSelected. Selected Item: " + item.getItemId());
	 	
		//perform the appropriate action based on what item was selected...
	 	switch (item.getItemId()){
	   		case 51:
	   			//do something...
	   			return true;
	   		case 52:
	   			//do something...
	   			return true;
	   		
	   		//etc, etc...
	   			
	   	}
	   
	 	//call super AFTER handling this menu item tap...
	 	return super.onOptionsItemSelected(item);
	   	   
	   
	}	
	
	//End Custom Action Bar items...
	/////////////////////////////////////////////////////////
	
	
	
	  
}

































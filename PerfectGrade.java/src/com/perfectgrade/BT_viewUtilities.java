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

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class BT_viewUtilities{

	private static String objectName = "BT_viewUtilities";
	
	//background image processing runs on a different thread...
	public static BackgroundImageThread backgroundImageThread = null;
	public static Handler backgroundImageHandler = null;

	
	//updates a screens background color...
	public static void updateBackgroundColorsForScreen(final Activity theActivity, final BT_item theScreenData){
		BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen. Screen with JSON itemId: \"" + theScreenData.getItemId() + "\" itemType: \"" + theScreenData.getItemType() + "\" itemNickname: \"" + theScreenData.getItemNickname() + "\"");
    	
		//will hold background color...
		String backgroundColor = "";
		RelativeLayout backgroundSolidColorView = null;
		
		try{
		
			//reference to background color relative layout in act_base.xml (this layout file is used by all the plugins)...
			backgroundSolidColorView = (RelativeLayout) theActivity.findViewById(R.id.backgroundSolidColorView);
			
			//solid background color
			backgroundColor = BT_strings.getStyleValueForScreen(theScreenData, "backgroundColor", "clear");
			
		}catch (Exception e){
			BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen: EXCEPTION (1) " + e.toString());
		}			
		
		
		//update the background color if it's different than the previous screen...
		try{
			if(backgroundColor.length() > 4 && backgroundSolidColorView != null){
				//BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen: setting background color to: \"" + backgroundColor + "\"");
				try{
					if(backgroundColor.equalsIgnoreCase("clear")){
						backgroundSolidColorView.setBackgroundColor(Color.TRANSPARENT);
						backgroundSolidColorView.setVisibility(0);
					}else{
						backgroundSolidColorView.setBackgroundColor(BT_color.getColorFromHexString(backgroundColor));
						backgroundSolidColorView.setVisibility(1);
					}
					backgroundSolidColorView.invalidate();
				}catch(Exception e){
					BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen: Exception setting background color (\"" + backgroundColor + "\") " + e.toString());
				}
			}
		
		}catch (Exception e){
			BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen: EXCEPTION (2)" + e.toString());
		}
	
	}
	
	//updates a screens background image...
	public static void updateBackgroundImageForScreen(final ImageView theImageView, final BT_item theScreenData){
		BT_debugger.showIt(objectName + ":updateBackgroundImageForScreen. Screen with JSON itemId: \"" + theScreenData.getItemId() + "\" itemType: \"" + theScreenData.getItemType() + "\" itemNickname: \"" + theScreenData.getItemNickname() + "\"");
		
		//init the handler...
		backgroundImageHandler = new Handler();
		
		BT_viewUtilities tmpUtils = new BT_viewUtilities();
 		backgroundImageThread = tmpUtils.new BackgroundImageThread();
 		backgroundImageThread.imageView = theImageView;
 		backgroundImageThread.screenData = theScreenData;
 		backgroundImageThread.start();		
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Background image processing. Loads a possible image (from bundle, cache, or a URL) then calls handler..

    public class BackgroundImageThread extends Thread{
    	public BT_item screenData = null;
    	public ImageView imageView = null;
    	public void run(){
			if(screenData != null && imageView != null){
	    		try{
					
	    			//image to get...
	    			Drawable backgroundImage = null;
	    			
					//background image name or url, small or large device...
					String backgroundImageName = "";
					String backgroundImageURL = "";
					if(perfectgrade_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
						
						//large device background...
						backgroundImageName = BT_strings.getStyleValueForScreen(screenData, "backgroundImageNameLargeDevice", "");
						backgroundImageURL = BT_strings.getStyleValueForScreen(screenData, "backgroundImageURLLargeDevice", "");
								
					}else{
					
						//large device background...
						backgroundImageName = BT_strings.getStyleValueForScreen(screenData, "backgroundImageNameSmallDevice", "");
						backgroundImageURL = BT_strings.getStyleValueForScreen(screenData, "backgroundImageURLSmallDevice", "");
						
					}//small or large device...
					
					//use a local or cached image if we have one, else, download...
					String useImageName = "";
					if(backgroundImageName.length() > 1){
						useImageName = backgroundImageName;
					}else{
						if(backgroundImageURL.length() > 1){
							useImageName = BT_strings.getSaveAsFileNameFromURL(backgroundImageURL);
						}
					}
					
					//if no name or URL is used, default to blank.png...
					if(useImageName.length() < 1 && backgroundImageURL.length() < 1){
						useImageName = "bt_blank.png";
					}
					
			    	//continue if the background image needs updated...
					if(useImageName.length() > 1){
						
						//does image exist in /res/drawable folder...
						if(BT_fileManager.getResourceIdFromBundle("drawable", useImageName) > 0){
							
							//BT_debugger.showIt(objectName + ":backgroundImageThread: using image from project bundle: \"" + useImageName + "\" JSON itemId: \"" + screenData.getItemId() + "\"");
							backgroundImage = BT_fileManager.getDrawableByName(useImageName);
							
						}else{
							
			        		//does file exist in cache...
			        		if(BT_fileManager.doesCachedFileExist(useImageName)){
				        		
			        			//BT_debugger.showIt(objectName + ":backgroundImageThread: using image from cache: \"" + useImageName + "\" JSON itemId: \"" + screenData.getItemId() + "\"");
				        		backgroundImage = BT_fileManager.getDrawableFromCache(useImageName);
			        		
			        		}else{
			        			
			        			//download from URL if we have one...
			        			if(backgroundImageURL.length() > 1){
			        	    		
			        			 	BT_downloader objDownloader = new BT_downloader(backgroundImageURL);
			        			 	objDownloader.setSaveAsFileName(useImageName);
			        			 	backgroundImage = objDownloader.downloadDrawable();
			        			 	
			        			 	//print to log of failed...
			        			 	if(backgroundImage == null){
			        	    			BT_debugger.showIt(objectName + ":backgroundImageThread: NOT SAVING iamge to cache (null). JSON itemId: \"" + screenData.getItemId() + "\"");
			        			 	}
			        			}
			        			
			        		}//cached file exists
						}//bundle file exists
					}else{//usesImageName
						//BT_debugger.showIt(objectName + ":backgroundImageThread: does not use a background image. JSON itemId: \"" + screenData.getItemId() + "\"");
					}
					
					//fire handler on UI thread if we have an image...
					if(backgroundImage != null && imageView != null){
						BackgroundImageHandler imgLoader = new BackgroundImageHandler(backgroundImage, screenData, imageView);
						backgroundImageHandler.post(imgLoader);						
					}
					
				}catch(Exception e){
	    			BT_debugger.showIt(objectName + ":backgroundImageThread Exception (1): " + e.toString() + " JSON itemId: \"" + screenData.getItemId() + "\"");
				}
			
			}else{
    			BT_debugger.showIt(objectName + ":backgroundImageThread. Could not update background image, imageView is null");
			}//screenData or imageView == null...	
    	}//run...
    };
    
	//handles background image...
    public class BackgroundImageHandler implements Runnable {
    	private Drawable backgroundImage;
    	private BT_item screenData;
    	private ImageView imageView;
    	public BackgroundImageHandler(Drawable theImage, BT_item theScreenData, ImageView theImageView){
    		this.backgroundImage = theImage;
    		this.screenData = theScreenData;
    		this.imageView = theImageView;
    	}

    	//run, called from BackgroundImageThread...
    	public void run(){
    		
			//global theme used if this screen doesn't override a setting...
			BT_item theThemeData = perfectgrade_appDelegate.rootApp.getRootTheme();
			
			//does the file exist in the project bundle...
			if(backgroundImage != null && imageView != null){
				
				//make sure imageView is visible..
				imageView.setVisibility(1);
				
	    		//background scale...
				String backgroundImageScale = "";
				if(BT_strings.getJsonPropertyValue(screenData.getJsonObject(), "backgroundImageScale", "").length() > 1){
					backgroundImageScale = BT_strings.getJsonPropertyValue(screenData.getJsonObject(), "backgroundImageScale", "");
				}else{
					backgroundImageScale = BT_strings.getJsonPropertyValue(theThemeData.getJsonObject(), "backgroundImageScale", "");
				}
					
				if(backgroundImageScale.equalsIgnoreCase("center")) imageView.setScaleType(ScaleType.CENTER);
				if(backgroundImageScale.equalsIgnoreCase("fullScreen")) imageView.setScaleType(ScaleType.FIT_XY);
				if(backgroundImageScale.equalsIgnoreCase("fullScreenPreserve")) imageView.setScaleType(ScaleType.FIT_CENTER);
				if(backgroundImageScale.equalsIgnoreCase("top")) imageView.setScaleType(ScaleType.FIT_START);
				if(backgroundImageScale.equalsIgnoreCase("bottom")) imageView.setScaleType(ScaleType.FIT_END);
				if(backgroundImageScale.equalsIgnoreCase("topLeft")) imageView.setScaleType(ScaleType.FIT_START);
				if(backgroundImageScale.equalsIgnoreCase("topRight")) imageView.setScaleType(ScaleType.FIT_START);
				if(backgroundImageScale.equalsIgnoreCase("bottomLeft")) imageView.setScaleType(ScaleType.FIT_END);
				if(backgroundImageScale.equalsIgnoreCase("bottomRight")) imageView.setScaleType(ScaleType.FIT_END);
				
				//set the image...
				imageView.setImageDrawable(backgroundImage);
				imageView.invalidate();
				
				/*
					//uncomment this to fade in the background image...
					Animation animation = new AlphaAnimation(0.0f, 1.0f);
					animation.setDuration(500);
					imageView.startAnimation(animation); 
   				*/
				
			}else{
				//no background image used by this screen...
			}
    		
    	}
    }	
    	
     
	//END background image processing
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
 		

	//round corners of bitmap - return bitmap
	public static Bitmap getRoundedImage(Bitmap bitmap, int pixels){
		BT_debugger.showIt(objectName + ":getRoundedImage rounding image with radius: \"" + pixels + "\"");
		Bitmap output = null;
		try{
			output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(output);

	        final int color = 0xff424242;
	        final Paint paint = new Paint();
	        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	        final RectF rectF = new RectF(rect);
	        final float roundPx = pixels;
	
	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(color);
	        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	        
	        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	        canvas.drawBitmap(bitmap, rect, rect, paint);
	        
		}catch(java.lang.OutOfMemoryError nm){
			BT_debugger.showIt(objectName + ":getRoundedImage EXCEPTION (1) " + nm.toString());
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":getRoundedImage EXCEPTION (2) " + e.toString());
		}
		
		//return...
		return output;
    }

	
	//resize bitmap - return bitmap
	public static Bitmap getScaledBitmap(Bitmap image, int maxHeight, int maxWidth){
		BT_debugger.showIt(objectName + ":getScaledBitmap setting max width: \"" + maxWidth + "\" max height: \"" + maxHeight + "\"");
		Bitmap scaledImage = null;
		try{
			int imgWidth = image.getWidth();
			int imgHeight = image.getHeight();
	
			//keep aspect ratio
			float scaleFactor = Math.min(((float) maxWidth) / imgWidth, ((float) maxHeight) / imgHeight);
	
			Matrix scale = new Matrix();
			scale.postScale(scaleFactor, scaleFactor);
			scaledImage = Bitmap.createBitmap(image, 0, 0, imgWidth, imgHeight, scale, false);
	
		
		}catch(java.lang.OutOfMemoryError nm){
			BT_debugger.showIt(objectName + ":getScaledBitmap EXCEPTION (1) " + nm.toString());
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":getScaledBitmap EXCEPTION (2) " + e.toString());
		}

		
		//return...
		return scaledImage;
    }	
	

	//convertToPixels...
	public static int convertToPixels(int theIntValue){
		BT_debugger.showIt(objectName + ":convertToPixels");
		int ret = 0;
		try{
			ret = (int)theIntValue;
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":convertToPixels EXCEPTION " + e.toString());
		}	
		
		//return...
		return ret;
		
	}
	
	//returns right side nav bar icon from JSON data...
	public static Drawable getRightNavBarIconForScreen(BT_item screenData){
		BT_debugger.showIt(objectName + ":getRightNavBarIconForScreen. JSON itemId: \"" + screenData.getItemId() + "\"");
	
		//return null if no button type was found for this "navBarRightButtonType"...
		String navBarRightButtonType = BT_strings.getJsonPropertyValue(screenData.getJsonObject(), "navBarRightButtonType", "");
		Drawable d = null;
		
		//these button types show text...
		if(navBarRightButtonType.equalsIgnoreCase("next") ||
			navBarRightButtonType.equalsIgnoreCase("done") ||
			navBarRightButtonType.equalsIgnoreCase("cancel") ||
			navBarRightButtonType.equalsIgnoreCase("edit") ||
			navBarRightButtonType.equalsIgnoreCase("save")){
			return d;
		}else{
	
			//these button types show an image...
			if(navBarRightButtonType.equalsIgnoreCase("details")) d = BT_fileManager.getDrawableByName("bt_info_light.png");
			if(navBarRightButtonType.equalsIgnoreCase("home")) d = BT_fileManager.getDrawableByName("bt_house.png");
			if(navBarRightButtonType.equalsIgnoreCase("infoLight")) d = BT_fileManager.getDrawableByName("bt_info_light.png");
			if(navBarRightButtonType.equalsIgnoreCase("infoDark")) d = BT_fileManager.getDrawableByName("bt_info_dark.png");
			if(navBarRightButtonType.equalsIgnoreCase("addBlue")) d = BT_fileManager.getDrawableByName("bt_add.png");
			if(navBarRightButtonType.equalsIgnoreCase("add")) d = BT_fileManager.getDrawableByName("bt_add.png");
			if(navBarRightButtonType.equalsIgnoreCase("compose")) d = BT_fileManager.getDrawableByName("bt_compose.png");
			if(navBarRightButtonType.equalsIgnoreCase("reply")) d = BT_fileManager.getDrawableByName("bt_reply.png");
			if(navBarRightButtonType.equalsIgnoreCase("action")) d = BT_fileManager.getDrawableByName("bt_action.png");
			if(navBarRightButtonType.equalsIgnoreCase("organize")) d = BT_fileManager.getDrawableByName("bt_box.png");
			if(navBarRightButtonType.equalsIgnoreCase("bookmark")) d = BT_fileManager.getDrawableByName("bt_bookmark.png");
			if(navBarRightButtonType.equalsIgnoreCase("search")) d = BT_fileManager.getDrawableByName("bt_search.png");
			if(navBarRightButtonType.equalsIgnoreCase("refresh")) d = BT_fileManager.getDrawableByName("bt_refresh.png");
			if(navBarRightButtonType.equalsIgnoreCase("camera")) d = BT_fileManager.getDrawableByName("bt_camera.png");
			if(navBarRightButtonType.equalsIgnoreCase("trash")) d = BT_fileManager.getDrawableByName("bt_trash.png");
			if(navBarRightButtonType.equalsIgnoreCase("play")) d = BT_fileManager.getDrawableByName("bt_play.png");
			if(navBarRightButtonType.equalsIgnoreCase("pause")) d = BT_fileManager.getDrawableByName("bt_pause.png");
			if(navBarRightButtonType.equalsIgnoreCase("stop")) d = BT_fileManager.getDrawableByName("bt_stop.png");
			if(navBarRightButtonType.equalsIgnoreCase("rewind")) d = BT_fileManager.getDrawableByName("bt_rewind.png");
			if(navBarRightButtonType.equalsIgnoreCase("fastForward")) d = BT_fileManager.getDrawableByName("bt_fastforward.png");
		
		}
		
		//return...
		return d;
		
		
	}
	
	//returns right side nav bar text when not using an icon...
	public static String getRightNavBarTextForScreen(BT_item screenData){
		BT_debugger.showIt(objectName + ":getRightNavBarTextForScreen. JSON itemId: \"" + screenData.getItemId() + "\"");
		
		//return......
		String ret = "";
    	if(screenData != null){
    		String navBarRightButtonType = BT_strings.getJsonPropertyValue(screenData.getJsonObject(), "navBarRightButtonType", "");
    	
    		//these button types show text...
			if(navBarRightButtonType.equalsIgnoreCase("next")) ret = perfectgrade_appDelegate.getApplication().getString(R.string.next);
			if(navBarRightButtonType.equalsIgnoreCase("done")) ret = perfectgrade_appDelegate.getApplication().getString(R.string.done);
			if(navBarRightButtonType.equalsIgnoreCase("cancel")) ret = perfectgrade_appDelegate.getApplication().getString(R.string.cancel);
			if(navBarRightButtonType.equalsIgnoreCase("edit")) ret = perfectgrade_appDelegate.getApplication().getString(R.string.edit);
			if(navBarRightButtonType.equalsIgnoreCase("save")) ret = perfectgrade_appDelegate.getApplication().getString(R.string.save);
    	}
    	
		//return...
		return ret;
			
	}
	
	
	//getEnterTransitionFromType...
    public static int getEnterTransitionFromType(String transitionType){
	 	BT_debugger.showIt(objectName + ":getEnterTransitionFromType Transition Type: \"" + transitionType + "\"");
    	int ret = 0;
    	
    	//figure out animation resource to use...
    	if(transitionType.toUpperCase().equals("FADE")){
    		ret = R.anim.bt_fadein;
    	}
    	if(transitionType.toUpperCase().equals("FLIP")){
    		ret = R.anim.bt_flipin;
    	}
    	if(transitionType.toUpperCase().equals("CURL")){
    		ret = R.anim.bt_curlin;
    	}
    	if(transitionType.toUpperCase().equals("GROW")){
    		ret = R.anim.bt_growin;
    	}
    	if(transitionType.toUpperCase().equals("SLIDELEFT")){
    		ret = R.anim.bt_slideleft;
    	}
    	if(transitionType.toUpperCase().equals("SLIDERIGHT")){
    		ret = R.anim.bt_slideright;
    	}
    	if(transitionType.toUpperCase().equals("SLIDEUP")){
    		ret = R.anim.bt_slideup;
    	}
    	if(transitionType.toUpperCase().equals("SLIDEDOWN")){
    		ret = R.anim.bt_slidedown;
    	}
    	
    	//return..
    	return ret;	
    	
    }
    
    //getExitTransitionFromType...
    public static int getExitTransitionFromType(String transitionType){
	 	BT_debugger.showIt(objectName + ":getExitTransitionFromType Transition Type: \"" + transitionType + "\"");

    	int ret = 0;
    	
    	//figure out animation resource to use...
    	if(transitionType.toUpperCase().equals("FADE")){
    		ret = R.anim.bt_fadeout;
    	}
    	if(transitionType.toUpperCase().equals("FLIP")){
    		ret = R.anim.bt_flipout;
    	}
    	if(transitionType.toUpperCase().equals("CURL")){
    		ret = R.anim.bt_curlout;
    	}
    	if(transitionType.toUpperCase().equals("GROW")){
    		ret = R.anim.bt_growout;
    	}
       	if(transitionType.toUpperCase().equals("SLIDELEFT")){
    		ret = R.anim.bt_slideright;
    	}
    	if(transitionType.toUpperCase().equals("SLIDERIGHT")){
    		ret = R.anim.bt_slideleft;
    	}
    	if(transitionType.toUpperCase().equals("SLIDEUP")){
    		ret = R.anim.bt_slidedown;
    	}
    	if(transitionType.toUpperCase().equals("SLIDEDOWN")){
    		ret = R.anim.bt_slideup;
    	}
    	
    	//return..
    	return ret;	
    	
    }   
    
    
    //openDocInCache...
    public static void openDocInCacheWithMimeType(String fileName, String mimeType, Activity parentActivity){
    	BT_debugger.showIt(objectName + ":openDocInCacheWithMimeType MIME Type: \"" + mimeType + "\"");
		try{
			
			//make sure it copied...
			if(BT_fileManager.doesCachedFileExist(fileName)){
				
	    		PackageManager pm = perfectgrade_appDelegate.getApplication().getPackageManager();
	    		ApplicationInfo appInfo = pm.getApplicationInfo(perfectgrade_appDelegate.getApplication().getPackageName(), 0);
				File theFile = new File(appInfo.dataDir + "/files/" + fileName);
				
				Intent intent = new Intent();
			    intent.setAction(Intent.ACTION_VIEW);
			    Uri uri = Uri.fromFile(theFile);
			    intent.setDataAndType(uri, mimeType);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    parentActivity.startActivity(Intent.createChooser(intent, parentActivity.getString(R.string.openWithWhatApp)));
			
			}else{
		    	BT_debugger.showIt(objectName + ":openDocInCacheWithMimeType File does not exist in cache?");
			}
			
		}catch(Exception e){
           	BT_debugger.showIt(objectName + ":openDocInCache EXCEPTION " + e.toString());
		}	
     	
    }
    
    
    
    //emailDocumentInCacheWithMimeType...
    public static void emailDocumentInCacheWithMimeType(String fileName, String mimeType, Activity parentActivity){
    	BT_debugger.showIt(objectName + ":emailDocumentInCacheWithMimeType MIME Type: \"" + mimeType + "\"");
	
    	//make sure device can send email...
		if(perfectgrade_appDelegate.rootApp.getRootDevice().canSendEmail()){

			//must have already downloaded the document
			if(BT_fileManager.doesCachedFileExist(fileName)){
		
				try{
		    		  
		    		//copy file from cache to SDCard so emailer can access it (can't email from internal files directory)...
		    		BT_fileManager.copyFileFromCacheToSDCard(fileName);
		    		  
		  		   	//get path to file...
		            File file = new File(perfectgrade_appDelegate.getApplication().getExternalCacheDir(), fileName);
		    		String savedToPath = file.getAbsolutePath();
		            
		    		//make sure file exists...
		    		if(file.exists()){
		    		
		    			//send from path...THIS IS REQUIRED OR GMAIL CLIENT WILL NOT INCLUDE ATTACHMENT
		    			String sendFromPath = "file:///sdcard/Android/data/com.perfectgrade/cache/" + fileName;
	
		    			//tell Android launch the native email application...
		    			Intent intent = new Intent(android.content.Intent.ACTION_SEND);  
		    			intent.setType("text/plain");
		    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, parentActivity.getString(R.string.sharingWithYou));  
		    			intent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\n" + parentActivity.getString(R.string.attachedFile));  	    	  
		    			intent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(sendFromPath));
	                  
		    			//open users email app...
					    parentActivity.startActivity(Intent.createChooser(intent, parentActivity.getString(R.string.openWithWhatApp)));
		    		
		    		}else{
			           	BT_debugger.showIt(objectName + ":emailDocumentInCacheWithMimeType Cannot email document, file does not exist: " + savedToPath);
		    			
		    		}
			    	  
		    	}catch(Exception e){
		           	BT_debugger.showIt(objectName + ":emailDocumentInCacheWithMimeType EXCEPTION " + e.toString());
		    	  
		    	}
		    	  
			}else{
	           	BT_debugger.showIt(objectName + ":handleEmailDocumentButton Cannot email document, document not in the cache?");
			}
    	
		}else{
           	BT_debugger.showIt(objectName + ":handleEmailDocumentButton Cannot email document, device cannot send emails?");
		}
	
    }   
    
    	

}








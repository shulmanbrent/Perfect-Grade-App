/*  File Version: 3.1 06/10/2013
 * 	File Version: 3.0
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
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;



public class BT_device {

	private static String objectName = "BT_device";
	private String deviceId = "";  
	private String deviceModel = "";  
	private String deviceVersion = "";  
	private String deviceLatitude = "0";  
	private String deviceLongitude = "0";  
	private String deviceAccuracy = "";
	private String deviceConnectionType = "";  
	private String deviceCarrier = "";
	private int deviceWidth = 0;
	private int deviceHeight = 0;
	private int deviceScreenDensity = 0;
	private String deviceOrientation = "";
	private boolean isLargeDevice;
	private boolean isShaking;
	
	//capabilities...
	private boolean canMakeCalls = false; 
	private boolean canTakePictures = false; 
	private boolean canTakeVideos = false; 
	private boolean canSendEmail = false; 
	private boolean canSendSMS = false; 
	private boolean canUseGPS = false; 
	private boolean canVibrate = false;
	private boolean canDetectShaking = false;
	private boolean canRecordAudio = false;
	
	//constructor
	public BT_device(){
		
		BT_debugger.showIt(objectName + ": Creating root-device object.");
		
		try{
			
			//app delegate
			Application appDelegate = perfectgrade_appDelegate.getApplication();

			//display info
			Display display = ((WindowManager)appDelegate.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
			
			//set screen size...
	    	this.deviceHeight = this.getScreenHeight();
	    	this.deviceWidth = this.getScreenWidth();
	    	
	    	//are we landscape?
	    	if(appDelegate.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
		    	deviceOrientation = "landscape";
	    	}else{
		    	deviceOrientation = "portrait";
	    	}
	    	
	    	//large or small device?
	    	if(this.deviceWidth > 600){
	    		this.isLargeDevice = true;
	    	}else{
	    		this.isLargeDevice = false;
	    	}
			
			//set screen density...
			DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);			
			this.deviceScreenDensity = metrics.densityDpi;
			BT_debugger.showIt(objectName + ": This device uses an Android display density of: " + deviceScreenDensity + "dpi (dots per inch)");
	    	
			//package manager detects some device capabilities...
			PackageManager pm = perfectgrade_appDelegate.getApplication().getPackageManager();

			//telephony info.. DO NOT QUERY FOR A USERS PHONE NUMBER - BAD BAD!!! - PLEASE RESPECT USERS PRIVACY
			if(pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)){
				
				TelephonyManager TelephonyManager = (TelephonyManager)appDelegate.getSystemService(Context.TELEPHONY_SERVICE);  
				String uuid = TelephonyManager.getDeviceId();
			    this.deviceId = uuid;

		    	SmsManager smsManager = SmsManager.getDefault();
		    	if(smsManager != null){
		    		this.canSendSMS = true;
					BT_debugger.showIt(objectName + ": This device can send SMS / Text messages.");
		    	}else{
		    		this.canSendSMS = false;
					BT_debugger.showIt(objectName + ": This device cannot send SMS / Text messages.");
		    	}
					
				if(TelephonyManager.getPhoneType() == android.telephony.TelephonyManager.PHONE_TYPE_NONE){
					this.canMakeCalls = false;
					BT_debugger.showIt(objectName + ": This device cannot make phone calls.");
				}else{
					this.canMakeCalls = true;
					BT_debugger.showIt(objectName + ": This device cant make phone calls.");
				}
			}
			
			
			//camera..
			if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
				this.canTakePictures = true;
				BT_debugger.showIt(objectName + ": This device can take pictures.");
				BT_debugger.showIt(objectName + ": This device can take videos.");
			}else{
				this.canTakePictures = false;
				BT_debugger.showIt(objectName + ": This device cannot take pictures.");
				BT_debugger.showIt(objectName + ": This device cannot take videos.");
			}
			
			//send email (all android devices can send email)...
			this.canSendEmail = true;
			BT_debugger.showIt(objectName + ": This device can send emails.");
					
			//is GPS capable..
     		try{
     	        perfectgrade_appDelegate.getApplication();
				LocationManager lm = (LocationManager) perfectgrade_appDelegate.getApplication().getSystemService(Context.LOCATION_SERVICE);  
     	        if(lm != null){
 					this.canUseGPS = true;
 					BT_debugger.showIt(objectName + ": This device is GPS capable.");
 	        	}
     		}catch(Exception e){
				this.canUseGPS = false;
				BT_debugger.showIt(objectName + ": This device is not GPS capable.");
     		}
     		
     		//can vibrate?
    		Vibrator shaker = (Vibrator)perfectgrade_appDelegate.getContext().getSystemService(Context.VIBRATOR_SERVICE);
    		if(shaker != null){
    			if(shaker.hasVibrator()){
    				BT_debugger.showIt(objectName + ": This device does support vibrating.");
    				this.canVibrate = true;
    			}else{
    				BT_debugger.showIt(objectName + ": This device does not support vibrating.");
    				this.canVibrate = false;
    			}
    		}else{
   				BT_debugger.showIt(objectName + ": This device does not support vibrating.");
				this.canVibrate = false;
    		}
     		
     		//can detect shaking?
    		SensorManager sensorManager = (SensorManager)perfectgrade_appDelegate.getContext().getSystemService(Context.SENSOR_SERVICE);
    		if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
				BT_debugger.showIt(objectName + ": This device has an accelerometer (it can detect shaking).");
				this.canDetectShaking = true;
     		}else{
				BT_debugger.showIt(objectName + ": This device does not have an accelerometer (it cannot detect shaking).");
				this.canDetectShaking = false;
     		}    		
    		
    		//can record audio?
			if(pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
				BT_debugger.showIt(objectName + ": This device can record audio.");
				this.canRecordAudio = true;
			}else{
				BT_debugger.showIt(objectName + ": This device cannot record audio.");
				this.canRecordAudio = false;
				
			}
    		
     		//set remaining default values...
		    this.deviceModel = Build.BRAND + "-" + Build.MODEL;
		    this.deviceVersion = Build.VERSION.RELEASE;
		    this.deviceCarrier = Build.BRAND;
		    this.deviceConnectionType = "";
			this.deviceLatitude = "";
		    this.deviceLongitude = "";
		    this.isShaking = false;
			     		
	
	    }catch (Exception je){
			BT_debugger.showIt(objectName + ": Error initializing: " + je.getMessage());
        }
		
		
	}	

	//updates devices connection type
	public void updateDeviceConnectionType(){
		//BT_debugger.showIt(objectName + ":updateDeviceConnectionType");
		try{
			
			//app delegate
			Application appDelegate = perfectgrade_appDelegate.getApplication();

			//connection info...
			String connectionType = "NONE";
			ConnectivityManager mConnectivity = (ConnectivityManager)appDelegate.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = mConnectivity.getActiveNetworkInfo(); 
			if (info == null){
				connectionType = "NONE";
			}else{			
				int netType = info.getType();
				if (netType == ConnectivityManager.TYPE_WIFI) {
					connectionType = "WIFI";
				} else if (netType == ConnectivityManager.TYPE_MOBILE) {
			       connectionType = "CELL";
				} else {
					connectionType = "NONE";
				}
			}
			BT_debugger.showIt(objectName + ":updateDeviceConnectionType: ConnectionType: " + connectionType);
			
			//update connection type...
			this.deviceConnectionType = connectionType;
						
		}catch (Exception je){
			BT_debugger.showIt(objectName + ":updateDeviceConnectionType ERROR: " + je.toString());
		}		
		
	}
	
	//updates devices orientation. This is fired on screen rotations..
	public void updateDeviceOrientation(String theOrientation){
		BT_debugger.showIt(objectName + ":updateDeviceOrientation Setting to: " + theOrientation);
		try{
			this.deviceOrientation = theOrientation;
		}catch (Exception je){
			BT_debugger.showIt(objectName + ":updateDeviceOrientation ERROR: " + je.toString());
		}		
		
	}	
	
	
	//updates device size
	public void updateDeviceSize(){
		//BT_debugger.showIt(objectName + ":updateDeviceSize");
		try{
			
			//set screen size...it changes after rotation...
	    	this.deviceHeight = this.getScreenHeight();
	    	this.deviceWidth = this.getScreenWidth();
			
			
	    	//debug
			BT_debugger.showIt(objectName + ":updateDeviceSize This device has a screen size of: " + this.deviceWidth + " (width) x " + this.deviceHeight + " (height).");
	    	
	    	//large or small device?
	    	if(this.deviceWidth > 500){
	    		this.isLargeDevice = true;
				BT_debugger.showIt(objectName + ":updateDeviceSize This application considers this to be a \"large device\"");
	    	}else{
	    		this.isLargeDevice = false;
				BT_debugger.showIt(objectName + ":updateDeviceSize This application considers this to be a \"small device\"");
	    	}
			BT_debugger.showIt(objectName + ":updateDeviceSize This device is in \"" + this.deviceOrientation + "\" orientation.");
		}catch (Exception je){
			BT_debugger.showIt(objectName + ":updateDeviceSize ERROR: " + je.toString());
		}		
		
	}
		
	
	//getScreenWidth...
	public int getScreenWidth(){
		int ret = 0;
		try{
			Display display = ((WindowManager)perfectgrade_appDelegate.getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
			return display.getWidth();
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":getScreenWidth EXCEPTION: " + e.toString());
		}
		return ret;
	}	
	
	//getScreenHeight...
	public int getScreenHeight(){
		int ret = 0;
		try{
			Display display = ((WindowManager)perfectgrade_appDelegate.getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
			return display.getHeight();
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":getScreenHeight EXCEPTION: " + e.toString());
		}
		return ret;
	}	
	
	
	//getters, setters
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	public String getDeviceVersion() {
		return deviceVersion;
	}
	public void setDeviceVersion(String deviceVersion) {
		this.deviceVersion = deviceVersion;
	}
	public String getDeviceLatitude() {
		return deviceLatitude;
	}
	public void setDeviceLatitude(String deviceLatitude) {
		this.deviceLatitude = deviceLatitude;
	}
	public String getDeviceLongitude() {
		return deviceLongitude;
	}
	public void setDeviceLongitude(String deviceLongitude) {
		this.deviceLongitude = deviceLongitude;
	}
	public String getDeviceAccuracy() {
		return deviceAccuracy;
	}
	public void setDeviceAccuracy(String deviceAccuracy) {
		this.deviceAccuracy = deviceAccuracy;
	}
	public String getDeviceConnectionType() {
		return deviceConnectionType;
	}
	public void setDeviceConnectionType(String deviceConnectionType) {
		this.deviceConnectionType = deviceConnectionType;
	}
	public String getDeviceCarrier() {
		return deviceCarrier;
	}
	public void setDeviceCarrier(String deviceCarrier) {
		this.deviceCarrier = deviceCarrier;
	}
	public int getDeviceWidth() {
		return deviceWidth;
	}
	public void setDeviceWidth(int deviceWidth) {
		this.deviceWidth = deviceWidth;
	}
	public int getDeviceHeight() {
		return deviceHeight;
	}
	public void setDeviceHeight(int deviceHeight) {
		this.deviceHeight = deviceHeight;
	}
	public int getDeviceScreenDensity() {
		return deviceScreenDensity;
	}
	public void setDeviceScreenDensity(String deviceOrientation) {
		this.deviceOrientation = deviceOrientation;
	}	
	public void setDeviceOrientation(String deviceOrientation) {
		this.deviceOrientation = deviceOrientation;
	}
	public String getDeviceOrientation() {
		return deviceOrientation;
	}	
	public boolean getIsLargeDevice() {
		return isLargeDevice;
	}
	public void setIsLargeDevice(boolean isLargeDevice) {
		this.isLargeDevice = isLargeDevice;
	}	
	public static String getObjectName() {
		return objectName;
	}

	public static void setObjectName(String objectName) {
		BT_device.objectName = objectName;
	}

	public boolean canMakeCalls() {
		return canMakeCalls;
	}

	public void setCanMakeCalls(boolean canMakeCalls) {
		this.canMakeCalls = canMakeCalls;
	}

	public boolean canTakePictures() {
		return canTakePictures;
	}

	public void canTakePictures(boolean canTakePictures) {
		this.canTakePictures = canTakePictures;
	}

	public boolean canTakeVideos() {
		return canTakeVideos;
	}

	public void setCanTakeVideos(boolean canTakeVideos) {
		this.canTakeVideos = canTakeVideos;
	}

	public boolean canSendEmail() {
		return canSendEmail;
	}

	public void setCanSendEmail(boolean canSendEmail) {
		this.canSendEmail = canSendEmail;
	}

	public boolean canSendSMS() {
		return canSendSMS;
	}

	public void setCanSendSMS(boolean canSendSMS) {
		this.canSendSMS = canSendSMS;
	}

	public boolean canUseGPS() {
		return canUseGPS;
	}

	public void setCanUseGPS(boolean canUseGPS) {
		this.canUseGPS = canUseGPS;
	}

	public boolean canVibrate() {
		return canVibrate;
	}

	public void setCanVibrate(boolean canVibrate) {
		this.canVibrate = canVibrate;
	}	
	
	public boolean canDetectShaking() {
		return canDetectShaking;
	}

	public void setCanDetectShaking(boolean canDetectShaking) {
		this.canDetectShaking = canDetectShaking;
	}
	
	public boolean canRecordAudio() {
		return canRecordAudio;
	}

	public void setCanRecordAudio(boolean canRecordAudio) {
		this.canRecordAudio = canRecordAudio;
	}	
	
	public boolean isShaking() {
		return isShaking;
	}

	public void setIsShaking(boolean isShaking) {
		this.isShaking = isShaking;
	}	
	
	
}










Buzztouch for Android
---------------------

This document is intended for developers interested in understanding how the Buzztouch Core 
is setup, how it works, and how it can be extended. This is a technical document
and as such contains technical terms.

The Buzztouch Core for Android
------------------------------

The Buzztouch Core is a collection of .java class files, .xml layout files, and other resources 
that are compiled using the Android SDK. The compiling process is what turns this collection of 
files into a single installable Android application. Most Android developers use the Eclipse IDE
when working with Android projects.

Application projects downloaded from Buzztouch Control Panels include the Buzztouch Core files, 
along with some additional files and resources that are needed to run each Buzztouch Plugins used in
the app. Most projects include many different types of plugins and additional files. 

This document is about the Buzztouch Core, not about individual plugins. See the README-PLUGINS.txt
file included with this download to learn more about how plugins are used.


How it Works - basically
------------------------

The BT_activity_host.java file serves as the "host activity" where individual Buzztouch Plugins
run. Every Buzztouch Plugin is an instance of an Android Fragment and the fragment runs
in the BT_activity_host Activity. When developers create new Buzztouch Plugins they are 
extending the BT_fragment class. The Buzztouch Core files are used to launch individual 
Buzztouch Plugins when end users interact with the app.

Example: Tapping button 1 may load a map plugin while tapping button 2 may load a video player
plugin. The number of plugins and their relationship to each other is unlimited. Most apps use a
complex series of plugins containing menu's, buttons, content, games, and other screens. 

Typical Flow (app launch to home screen displayed)
--------------------------------------------------

1) 	User taps the app's icon on the home screen of the device. 

2) 	Android reads the AndroidManifest.xml file compiled in the app.

3)	Android learns (by way of the manifest) that the "first activity" is BT_activity_start. 

4) 	BT_activity_start Activity is started and it loads a fragment. This fragment is the
	BT_fragment_load_config_data file. 
	
	BT_fragment_load_config_data is an Android Fragment that serves as a "loading point"
	and it's primary purpose is to load the BT_config.txt file included in the project. 
	
	BT_config.txt: This highly organzed data file tells the Buzztouch Core how the app looks, 
	behaves, and how and when to launch individual Buzztouch Plugins. 
	
	The JSON data in this file includes several properties, along with several arrays. 
	The BT_themes, BT_tabs, BT_menus, and BT_screens arrays are the meat-n-potatoes of the
	configuration file. Each is a list of JSON objects containing important details about
	the apps traits. 
	
	
5)	After BT_fragment_load_config_data reads the config data, it tells it's host activity
	(BT_activity_start) to continue...BT_activity_start then determines if it should show a 
	splash screen (a plugin) or if it should transition to BT_activity_host. 
	
	If a splash screen is used BT_activity_start will load the fragment for the splash screen. 
	If no splash screen is used it will finish and start BT_activity_host.

	BT_activity_host: This Android Activity serves as the host for all the plugins used by the app. 
	The lifecycle of this activity begins with the onCreate() method. At the end of the onCreate()
	method it called configureEnvironment()...
	
6)	BT_activity_host.configureEnvironment(): This methods primary responsibility is to "figure out"
	how to "setup" the app. Should the GPS on the device turn on? Should the device register 
	for push notifications? The configureEnvironment() method walks through a series of 
	these options and acts accordingly. 
	
7)	BT_activity_host.setupTabs(): This method is called when the configureEnvironment() method
	completes. The primary purpose of this method is to setup the app's main Android Action Bar. 
	The Action Bar may or may not includes actual tabs.
	
8)	At this point BT_activity_host has setup some environment variables and some options
	tabs. It's time to show the app's home screen. The home screen for the app (a plugin) 
	loads using the showFragmentForPlugin() method. 	
	
	

##########################################################################################
AT THIS POINT NO USER INTERACTION HAS OCCURRED AND THE APP IS DISPLAYING IT'S HOME SCREEN
##########################################################################################

Recap, this is what happened so far:

BT_activity_start launched. It asked BT_fragment_load_config_data to parse the app's config data. 
Next, it started BT_activity_host (in the event no splash screen was used). 
BT_activity_host ran it's configureEnvironment() method, then the setupTabs() method, then loaded
the home screen with showFragmentForPlugin()

Moving on...


9)	User Interaction: The app relies on user interaction from this point forward. Tapping a button
	or a tab, or a menu-item instructs the Buzztouch Core to load a specific screen. Each screen
	that loads is an instance of a Buzztouch Plugin (or a custom class file created by a developer).
	The plugin showing as the home screen may also take advantage of "shaking" to trigger navigation. 
	The point is that the app is running and not "doing anything" until the user interacts with it.
	
	Loading screens is a two-step process. 
	
	a) Find the id of a screen to load by looking at the app's JSON data.
	b) Pass this screen's itemId to a helper method included in the Buzztcouh Core.
	
	In most cases one of these two methods are used to load new screens...
	
	loadScreenWithItemId();
	loadScreenWithItemNickname();
	
	a) Use the BT_strings class to find the itemId of a screen configured in the app's JSON data...
	String loadScreenWithItemId = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "loadScreenWithItemId", "");
	
	b) Use the BT_fragment class to load the fragment associated with the plugin screen...
	loadScreenWithItemId(tmpLoadScreenItemId);
	
     	
10)	Loading Buzztouch Plugins: Buzztouch Plugins (not part of the Buzztouch Core) are .java 
	class files that extend the BT_fragment class. The BT_fragment class has some useful methods
	and properties that make it easier for developers to take advantage of the Buzztouch Core. 
	
	Open the BT_plugin_sample.java file to see how it extends the BT_fragment class, loads
	it's own .xml layout file, then displays some simple text. It also displays a button
	that loads another screen. This sample shows how to use some common methods in the
	Buzztouch Core and serves as getting-started place for developers interested in making
	Buzztouch Plugins for Android.
	
	
	 
	
Using methods in BT_fragment and BT_activity_host
-------------------------------------------------

When working with plugins it's common to trigger methods found in the BT_fragment.java class and 
the BT_activity_host.java class. These two classes contain all sorts of useful "helper methods" 
designed to help a developer accomplish common tasks.  

Triggering methods in the BT_fragment.java class from code in a plugin
------------------------------------------------------------------------

	This is easy because the parent class (remember, all plugins extend BT_fragment) has methods
	that are already "available" to the child class (the plugin). 
		
	Example: BT_fragment contains a method called showProgress() that requires two arguments. It's
	method signature looks like this...
	
	public void showProgress(String theTitle, String theMessage);
	
	From inside a fragment that extends BT_fragment (a plugin) you could simply do...
	
	//show a progress message...
	this.showProgress("Hang on a minute", "This may take a bit so chill out");
	
Triggering methods in the BT_activity_host.java class from code in a plugin
---------------------------------------------------------------------------

	This is a bit trickier but not difficult. The idea is that you need to first get a reference
	to the "host activity" before calling any of it's methods from the plugin code 
	
	Example: BT_activity_host has a method called refreshAppData() that requires zero arguments. It's
	method signature looks like this...
	
	public void refreshAppData();
	
	From inside a fragment that extends BT_fragment (a plugin) you could simply do...
	
	//refresh the app's configuration data...
	((BT_activity_host)getActivity()).refreshAppData();
	
	Take notice of how we first got a reference to the fragments host Android Activity then
	"cast" the activity to BT_activity_host. This "cast" process is necessary because the
	compiler has no idea that the host activity contains a method called refreshAppData(). If we tell
	the compiler the name of the host activity it stops yelling at us with red error messages!
	
	
Triggering methods in the other classes in the Buzztouch Core
---------------------------------------------------------------------------
	
	The Buzztouch Core contains several classes that each contain methods designed to save you time. 
	No sense in writing a "download file" routine or a "color converter" when the Buzztouch Core already
	has them - right? 
	
	Many of the methods in these additional classes are "static" - meaning you don't have to instantiate
	an instance of the class before calling the method. Others are not, it depends on which class you're
	using. Have a look a the BT_color class. It has a method with a signature like this:
	
	public static int getColorFromHexString(String hexString);
	
	The "static" keyword means you can all this method without instantiating an instance of the BT_color
	class first. Like this...
	
	int colorResource = BT_color.getColorFromHexString("#000000");
	
	In other cases you'll need to first create an object (an instance of a class), then triger it's
	method. Have a look at the BT_downloader class. It has a method with a signature like this:
	
	public Drawable downloadDrawable(); 
	
	The "static" keyword is not in this method signature so we need to first create an object from
	the BT_downloader class, then ask it to download the drawable. Like this...
	
	String useURL = "http://www.myurl.com";
	BT_downloader objDownloader = new BT_downloader("http://www.myurl.com");
	String myData = objDownloader.downloadTextData();
	
	
	
Not all methods in the Buzztouch Core are implemented
---------------------------------------------------------------------------

	The Buzztouch Core is in a state of evolution - always. Because of this you may run into
	methods inside class files that are nothing more than method placeholders for future code.
	
	THIS IS INTENTIONAL.
	
	The ideas is that we are constantly listening, paying attention, and improving as time passes. The
	ever changing Android operating system makes this super fun! Wink. The Buzztouch Core changes along
	with the mobile development ecosystem and part of this process includes exploring new ideas. If you
	see an unimplemented method, don't panic, we realize it's there and may or may not implement it in
	the future. 
	


...Happy apping from all of us at Buzztouch

















 



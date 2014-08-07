
Buzztouch Plugins for Android
------------------------------
This document is intended for developers interested in understanding what a Buzztouch Plugin is, why
they are useful, and how to create one. This is a technical document and as such contains technical 
terms. If you're uncomfortable with tech-talk, stop reading now.


What are Buzztouch Plugins?
------------------------------
Plugins help app developers save time, headache, and oftentimes lots of money. Each plugin represents 
a "screen" in an app. Example: A map screen or a menu screen or splash screen. Most plugins come 
from the Buzztouch Plugin Market but anyone can create and distribute Buzztouch Plugins.


Creating New Plugins
------------------------------	
Making new plugins isn't difficult but does require some considerations. Will the plugin be "one-off" or
do you plan to use it in multiple apps? Are you planning to sell it in the Buzztouch Plugin Market? 
Does the plugin require internet access in order to work? Does the plugin do anything useful? Does the plugin
need to be "adjustable" using the Buzztouch Control Panel or is it a hard-coded offline idea? 

In almost all cases it's best to organize your plugin files with the assumption that someday you'll want to 
use it in another project or share it (or sell it). This means you'll be well served by following the same
conventions other Buzztouch Plugin Developers do and it all starts with how you organize your files...


The Plugin Package
------------------------------
Buzztouch Plugins are organized in .zip archives (folders). Unarchiving (unzipping) a plugin reveals the
necessary files in order to make the plugin work. Individual files for the plugin are added to the
Android project before it's compiled. The number of files in the plugin package will vary from one plugin
to the next. All plugins for Android include a .java class file. This is the only required file in the
Android plugin package. 


The Plugin Package Structure
------------------------------
Most plugins contain many files and these files are organized in the plugin package in sub-directories. 
The sub-directories (folders) in the package will always be named according to the Buzztouch Plugin
naming convention rules. Names are case sensitive. 

	myCoolPlugin (top level directory for a plugin named "myCoolPlugin")
	--source-ios 
	--source-android-activity
	--source-android-fragment
		/assets/
				/BT_Audio/ (files go here)
				/BT_Docs/ (files go here)
				/BT_Video/ (files go here)
		/libs/ (files go here)
		/res/
			/anim (files go here)
			/drawable (files go here)
			/layout (files go here)
		/src
			/com
				/buzzTouch (files go here)
		
		
	The plugin package may or may not contain all these sub-directories, it depends on the plugin. But, 
	for Android, the package will always have the --source-android-fragment/src/com/buzzTouch folder. 
	This is because	all Buzztouch Plugins for Android have at least one .java file and .java 
	files go in the /src/com/buzzTouch directory. 
	

The Plugin Package Contents
------------------------------
	myCoolPlugin (top level directory, only contains sub-directories)
		/source-ios (unused for Android)
		/source-android-activity (for older plugins, new plugins should not have this directory)
		/source-android-fragment (contains sub-directories holding individual files)
			/assets/
					/BT_Audio (.mp3 and other audio files)
					/BT_Docs (.html, .pdf, .doc and other documents)
					/BT_Video (.mov and other video files)
			/libs/ (.jar files)
			/res/
				/anim (.xml animation resources)
				/drawable (image resources, .png, .jpg)
				/layout (.xml layout files)
			/src
				/com
					/buzzTouch (.java class files)
			
		
	The structure of the package closely matches the structure of an Android project. This makes it
	easier to drag files from the plugin package into the appropriate folder in the Android project.
	Note: When a Buzztouch Control panel is used to "package" a project ALL the files needed to 
	run all the plugins used in the project will be in one package. This means many .java files
	will be in the --src directory, many layout files in the --layout directory, etc. This makes it
	much easier to drag everything into the Android proejct. 
	

Plugin File Naming Conventions
----------------------------------
Because lots of different people make lots of different plugins, it's important that a file naming
convention is followed. You must follow these conventions if you use the Buzztouch Plugin Market to 
distribute (sell) plugins on your behalf.

Files Name: Every single file in a plugin package starts with the name of the plugin. In most cases
this name will begin with the plugin developers intitials. The initials don't matter but it's important to 
understand this concept. John Smith uses JS, Billy Apadaca uses BA and Mickey Mouse uses MM. No two 
files can use the same file name in an Android project and this helps avoid conflicts. Example files
names for Joe Smith's alarm clock plugin...
	Js_alarmclock.java (the required .java file, in the /src/com/buzzTouch directory)
	js_alarmclock.xml  (an .xml layout file, in the /res/layout directory)
	js_alarmclock_clockimage.png (a .png image file, in the /res/drawable directory)
	js_alarmclock_wakesound.mp3 (a sound file, in the /assets/BT_Audio directory)
	
	Note the "js_alarmclock" prefix on all files for the plugin. It is ALWAYS lower case for every file name. 
	This is important. All files in a plugin package will have lower cased names with the exception of the .java 
	class file. The .java class file will begin with an UPPER case letter, then lower case letters to follow. 
	One UPPER case letter, everything else is lower case. Why the UPPER case J? Traditionally .java class files 
	begin with a capital letter. We're honoring the .java gods and don't want any bad karma - upper case the first letter!
	
	Note: Some older Buzztouch Plugins do not conform to this convention. 
		
	

Plugin Nuts-n-Bolts
------------------------------	
The .java class file is the only required file in a Buzztouch Plugin for Android. This is because this 
is the Java Class that the Buzztouch Core references when the plugin is displayed. The .java file may be simple, 
or extraordinarily complex - it all depends on the plugin. In most cases a plugin will do several things 
after the Buzztouh Core loads it's .java class file... 

	1) Inflate an .xml layout file to use as the on-screen display for the plugin.
	2) Read some data from the BT_config.txt file.
	3) Customize the layout or behavior of the screen based on the data it read from the BT_config.txt file.
	4) Allow user interaction.
	
	Context: If Joe's plugin was an animated slideshow it could have one .java file and one
	.xml layout file. When it loaded on screen it could start an interactive slideshow. The images in 
	the slideshow could be described in the BT_config.txt file so that it was flexible and 
	dynamic. Tapping an image could allow the user to change the animation speed or style. In this case
	the JSON in the BT_config.txt file would contain properties such as "imageNames" and "animationSpeed" and
	the plugin package could look like...
	
	js_slideshow (top level directory, the name of the plugin)
		/source-android-fragment
			/res/
				/anim
					/js_slideshow_flip.xml
					/js_slideshow_spin.xml
					/js_slideshow_fade.xml
				/drawable
					/js_slideshow_sampleimage1.png
					/js_slideshow_samleimage2.png
				/layout
					/js_slideshow.xml
			/assets/BT_Audio
				/js_slideshow_whoosh.mp3
			/src/
				/com/
					/buzzTouch/				
						/js_slideshow.java	  

	Note: Additional files are required if the plugin is to be installed in a Buzztouch Control panel. This discussion
	is about how to make a Buzztouch Plugin for Android and it does NOT cover how to interact with a Buzztouch Control
	panel, how to package a plugin for the Buzztouch Plugin Market. 
	

Makking you First Plugin
------------------------------	
You should be able to follow these steps to get a custom plugin running in :05 minutes. Really, it's not hard. 

	1)  Start by copying the BT_plugin_sample.java file and the BT_plugin_sample.txt file.
	
	2) 	Rename these and add them to your project...
		Js_newplugin.java (add this to your /src/com/buzzTouch folder in an existing Buzztouch for Android project)
		js_newplugin.xml (add this to your /res/layout folder in an existing Buzztouch for Android project)
	
		Note the capital J in the .java class file name. It's lower case in the layout file name.
	
	3) 	Open the Js_newplugin.java file and change the name of the Java Class. It reads "public class BT_plugin_sample"
		and should read "public class Js_newplugin" (note the capital J in the Java Class name)
	
		Also change	the reference to the layout file your plugin uses. Change 
		R.layout.bt_plugin_sample to R.layout.js_newplugin
		
	4) 	Change the copyright notice at the top of the file. Replace "Copyright, David Book, buzztouch.com" with 
		your name. Remove all of this too, David's not doing this, you are!
		
		"Neither the name of David Book, or buzztouch.com nor the names of its contributors 
 		may be used to endorse or promote products derived from this software without specific 
		prior written permission."
		
	5) 	Change the package name. Replace "package com.buzzTouch" with the name of your project's package name. 
		Hint: You can find your project's package name in the AndroidManifest.xml file.
		
	6) 	Add a screen to the BT_config.txt file. 
		 
		{"itemId":"9999", "itemType":"Js_newplugin", "itemNickname":"My First Plugin", "navBarTitleText":"Joe is Cool", "dynamicText":"and you thought this was difficult"}

	7) 	Connect the new screen to an existing button or tab in the app and off you go....
	


	I'll expand on this at some point but that should be enough to get you going.
	
	Cheers from Monterey, 
	
	David Book
	buzztouch.com

		
	
		
		 
 










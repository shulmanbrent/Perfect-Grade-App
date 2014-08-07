/*
 *	Copyright 2013, David Book, buzztouch.com
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


#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "BT_fileManager.h"
#import "perfectgrade_appDelegate.h"
#import "BT_item.h"
#import "BT_debugger.h"
#import "BT_color.h"
#import "BT_strings.h"
#import "BT_viewUtilities.h"
#import "BT_button_view.h"
#import "BT_imageTools.h"

@implementation BT_button_view
@synthesize parentScreenData, buttonItemData, myButton, buttonBox, cacheWebImage;
@synthesize backgroundImageView, backgroundImageLoadingView;
@synthesize backgroundImageName, backgroundImageURL;


//theParentMenuScreenData...
-(id)initWithButtonItemData:(BT_item *)theButtonItemData theParentMenuScreenData:(BT_item *)theParentMenuScreenData{
    if((self = [super init])) {
		//[BT_debugger showIt:self theMessage:@"INIT"];
		
		//set buttons data from BT_item
		[self setButtonItemData:theButtonItemData];
		
		//set buttons parent screen data
		[self setParentScreenData:theParentMenuScreenData];
		
		//appDelegate
		perfectgrade_appDelegate *appDelegate = (perfectgrade_appDelegate *)[[UIApplication sharedApplication] delegate];	
		
		//button size comes from parent screen (this is background image size)
		int buttonSize = 60;
		int cornerRadius = 0;
		self.cacheWebImage = TRUE;
		UIColor *buttonBackgroundColor = [UIColor clearColor];
		
		//Start with Global Theme values, then over-ride if screen set a value.
		//Some settings come from the button data, not the theme data

		//background color
		buttonBackgroundColor = [BT_color getColorFromHexString:[BT_strings getStyleValueForScreen:theParentMenuScreenData nameOfProperty:@"buttonBackgroundColor" defaultValue:@"#CCCCCC"]];

		//assume small device.
		cornerRadius = [[BT_strings getStyleValueForScreen:theParentMenuScreenData nameOfProperty:@"buttonCornerRadiusSmallDevice" defaultValue:@"5"] intValue];
		buttonSize = [[BT_strings getStyleValueForScreen:theParentMenuScreenData nameOfProperty:@"buttonSizeSmallDevice" defaultValue:@"60"] intValue];
		
        //images comes form each button...
		self.backgroundImageName = [BT_strings getJsonPropertyValue:theButtonItemData.jsonVars nameOfProperty:@"imageNameSmallDevice" defaultValue:@""];
		self.backgroundImageURL = [BT_strings getJsonPropertyValue:theButtonItemData.jsonVars nameOfProperty:@"imageURLSmallDevice" defaultValue:@""];
		
		//cache web-images?
		if([[BT_strings getStyleValueForScreen:theParentMenuScreenData nameOfProperty:@"cacheWebImage" defaultValue:@""] isEqualToString:@"0"]){
			self.cacheWebImage = FALSE;
		}

		//some settings depend on the device's size
		if([appDelegate.rootDevice isIPad]){
			
			//use large device settings.
			cornerRadius = [[BT_strings getStyleValueForScreen:theParentMenuScreenData nameOfProperty:@"buttonCornerRadiusLargeDevice" defaultValue:@"10"] intValue];
			buttonSize = [[BT_strings getStyleValueForScreen:theParentMenuScreenData nameOfProperty:@"buttonSizeLargeDevice" defaultValue:@"60"] intValue];
			self.backgroundImageName = [BT_strings getJsonPropertyValue:theButtonItemData.jsonVars nameOfProperty:@"imageNameLargeDevice" defaultValue:@""];
			self.backgroundImageURL = [BT_strings getJsonPropertyValue:theButtonItemData.jsonVars nameOfProperty:@"imageURLLargeDevice" defaultValue:@""];
			
		}
		
		//if we have an imageURL, and no imageName, figure out a name to use...
		if(self.backgroundImageName.length < 3 && self.backgroundImageURL.length > 3){
			[self setBackgroundImageName:[BT_strings getFileNameFromURL:self.backgroundImageURL]];
		}		

		//build box to hold the icon
		buttonBox = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonSize, buttonSize)];

		//background color (if we have background image, this will be "under" the image
		[buttonBox setBackgroundColor:buttonBackgroundColor];

		//image "over the background color"
		backgroundImageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, buttonSize, buttonSize)];
		[backgroundImageView setContentMode:UIViewContentModeScaleAspectFill];
		[backgroundImageView setClipsToBounds:TRUE];
		if(cornerRadius > 0){
			backgroundImageView = [BT_viewUtilities applyRoundedCornersToImageView:backgroundImageView radius:cornerRadius];
		}
		[buttonBox addSubview:backgroundImageView];
		
		//icon name
		NSString *iconName = [BT_strings getJsonPropertyValue:theButtonItemData.jsonVars nameOfProperty:@"iconName" defaultValue:@""];
		
		//add icon on top of background view
		if([iconName length] > 0){
			UIImageView *iconImageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, buttonSize, buttonSize)];
			[iconImageView setContentMode:UIViewContentModeCenter];
			[iconImageView setImage:[UIImage imageNamed:iconName]];
			[buttonBox addSubview:iconImageView];
		}
		
		//round corners?
		if(cornerRadius > 0){
			buttonBox = [BT_viewUtilities applyRoundedCorners:buttonBox radius:cornerRadius];
		}

			
		//activity indicator on top, gets hidden after image loads
		backgroundImageLoadingView = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(0, 0, buttonSize, buttonSize)];
		backgroundImageLoadingView.backgroundColor = [UIColor clearColor];
		backgroundImageLoadingView.activityIndicatorViewStyle = UIActivityIndicatorViewStyleGray;
		[backgroundImageLoadingView setHidesWhenStopped:TRUE];
        backgroundImageLoadingView.contentMode = UIViewContentModeCenter;
		[backgroundImageLoadingView startAnimating];		
		[buttonBox addSubview:backgroundImageLoadingView];
		
		//add to view..
		[self addSubview:buttonBox];
		
    }
    return self;
}

//showDownloading...
-(void)showDownloading{
	[backgroundImageLoadingView startAnimating];
}

//initImageInThread...
-(void)initImageInThread{
	
	// We need to download the image, get it in a seperate thread!      
	thread = [[NSThread alloc] initWithTarget:self selector:@selector(downloadImage) object:nil];
	[thread start];

}


//showImage...
-(void)showImage{
	
	
    @autoreleasepool {
	
	@synchronized(self) {    
	  
		if ([[NSThread currentThread] isCancelled]) return;
		[thread cancel]; 
		thread = nil;
        
		//if we don't have an image name
		if([backgroundImageName length] < 3){
			[backgroundImageLoadingView performSelectorOnMainThread:@selector(stopAnimating) withObject:nil waitUntilDone:NO];
			return;
		}
		
		/* 
			Where is the background image?
			a) File exists in bundle. Use this image, ignore possible download URL
			b) File DOES NOT exist in bundle, but does exist in writeable data directory: Use it. (it was already downloaded and saved)
			c) File DOES NOT exist in bundle, and DOES NOT exist in writeable data directory and an imageURL is set: Download it, save it for next time, use it.
		*/
		
		//get the image
		if([backgroundImageName length] > 1){
			if([BT_fileManager doesFileExistInBundle:backgroundImageName]){
				[BT_debugger showIt:self theMessage:@"Image for button exists in bundle - not downloading."];
				[self performSelectorOnMainThread:@selector(setImageBehindButton:) withObject:[UIImage imageNamed:backgroundImageName] waitUntilDone:NO];                
				[backgroundImageLoadingView performSelectorOnMainThread:@selector(stopAnimating) withObject:nil waitUntilDone:NO];
			}else{
				if([BT_fileManager doesLocalFileExist:backgroundImageName]){
					[BT_debugger showIt:self theMessage:@"Image for button view exists locally - not downloading."];
					[self performSelectorOnMainThread:@selector(setImageBehindButton:) withObject:[BT_fileManager getImageFromFile:backgroundImageName] waitUntilDone:NO];                
					[backgroundImageLoadingView performSelectorOnMainThread:@selector(stopAnimating) withObject:nil waitUntilDone:NO];
				}else{
					
					if([backgroundImageURL length] > 3){
						[BT_debugger showIt:self theMessage:@"Image for button view does not exist locally - downloading."];
						[self performSelector:@selector(downloadImage) withObject:nil afterDelay:.5];
					}else{
						[backgroundImageLoadingView performSelectorOnMainThread:@selector(stopAnimating) withObject:nil waitUntilDone:NO];
						[self performSelectorOnMainThread:@selector(setImageBehindButton:) withObject:[UIImage imageNamed:@"noIcon.png"] waitUntilDone:NO];                
						[BT_debugger showIt:self theMessage:@"Image for button view does not exist locally - no URL provided, not downloading."];
					}
					
				}
			}
		}//backgroundImageName
	}    

	}

}

//downloadImage...
-(void)downloadImage{
	
    
    //imageURL...
    NSString *imgURL = [self backgroundImageURL];
    if([imgURL length] > 3){
        
        
        //create an NSURL from the url string...
        NSURL *useURL = [NSURL URLWithString:imgURL];
        
        //download image using Grand Central Dispath, iOS handles queue automagically...
        dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0ul);
        dispatch_async(queue, ^{
            NSData *data = [NSData dataWithContentsOfURL:useURL];
            UIImage *tmpImage = [UIImage imageWithData:data];
            dispatch_async(dispatch_get_main_queue(), ^{
                
                
                if(tmpImage != nil){
                    //downloaded image
                    [self performSelectorOnMainThread:@selector(setImageBehindButton:) withObject:tmpImage waitUntilDone:NO];
                    [BT_fileManager saveImageToFile:tmpImage fileName:backgroundImageName];
                }else{
                    //error downloading image...
                    [self performSelectorOnMainThread:@selector(setImageBehindButton:) withObject:[UIImage imageNamed:@"noIcon.png"] waitUntilDone:NO];
                }
                [backgroundImageLoadingView performSelectorOnMainThread:@selector(stopAnimating) withObject:nil waitUntilDone:NO];
                
                
                
            });
        });
        
    }
    
    
}

//setImageBehindButton...
-(void)setImageBehindButton:(UIImage *)theImage{
	[backgroundImageView setImage:theImage];
}


@end





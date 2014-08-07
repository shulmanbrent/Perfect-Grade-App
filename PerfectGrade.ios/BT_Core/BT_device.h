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

#import <CoreLocation/CoreLocation.h>
#import "BT_locationManager.h"

@interface BT_device : NSObject {
	
}

@property (nonatomic, strong) NSString *deviceId;
@property (nonatomic, strong) NSString *deviceModel;
@property (nonatomic, strong) NSString *deviceVersion;
@property (nonatomic, strong) NSString *deviceLatitude;
@property (nonatomic, strong) NSString *deviceLongitude;
@property (nonatomic, strong) NSString *deviceConnectionType;
@property (nonatomic) int deviceWidth;
@property (nonatomic) int deviceHeight;
@property (nonatomic) BOOL isIPad;
@property (nonatomic) BOOL isOnline;
@property (nonatomic) BOOL canReportLocation;
@property (nonatomic) BOOL canTakePictures;
@property (nonatomic) BOOL canTakeVideos;
@property (nonatomic) BOOL canMakePhoneCalls;
@property (nonatomic) BOOL canSendEmails;
@property (nonatomic) BOOL canSendSMS;


+(void)registerForPushNotifications:(NSString *)registerURL;
+(void)unRegisterForPushNotifications:(NSString *)registerURL;


@end




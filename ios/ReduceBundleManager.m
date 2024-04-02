//
//  ReduceBundleManager.m
//  ReduceBundle
//
//  Created by Keval Goyani on 02/04/24.
//

#import <Foundation/Foundation.h>
#import "ReduceBundleManager.h"
#import <React/RCTLog.h>
#import <UIKit/UIKit.h>

@implementation ReduceBundleManager

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(loadImageWithTag:(NSString *)tag
                  type:(NSString *)type
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  NSBundleResourceRequest *request = [[NSBundleResourceRequest alloc] initWithTags:[NSSet setWithArray:@[tag]]];
  NSBundle *bundle = request.bundle;
  NSLog(@"bundle1: %@", bundle);
  
  // Checks whether the resources marked with the tag managed by the request are already on the device
  [request conditionallyBeginAccessingResourcesWithCompletionHandler:^(BOOL resourcesAvailable) {
    if (resourcesAvailable) {
      RCTLogInfo(@"resources Available");
      [self processImageWithBundle:bundle tag:tag resolve:resolve reject:reject request:request];
      
    } else {
      RCTLogInfo(@"resources not Available");
      [request beginAccessingResourcesWithCompletionHandler:^(NSError * _Nullable error) {
        if (error) {
          NSLog(@"Error accessing resources: %@", error.localizedDescription);
          reject(@"RESOURCE_ERROR", @"Error accessing resources", error);
          return;
        }
        
        [self processImageWithBundle:bundle tag:tag resolve:resolve reject:reject request:request];
      }];
    }
  }];
}

// Custom method to process the image
- (void)processImageWithBundle:(NSBundle *)bundle
                           tag:(NSString *)tag
                       resolve:(RCTPromiseResolveBlock)resolve
                        reject:(RCTPromiseRejectBlock)reject
                       request:(NSBundleResourceRequest *)request {
  UIImage *loadedImage = [UIImage imageNamed:tag inBundle:bundle compatibleWithTraitCollection:nil];
  NSLog(@"Image loadedImage: %@", loadedImage);
  if (loadedImage) {
    NSData *imageData = UIImagePNGRepresentation(loadedImage);
    NSString *base64String = [imageData base64EncodedStringWithOptions:0];
    resolve(base64String);
  } else {
    reject(@"IMAGE_ERROR", @"Error loading image", nil);
  }
  
  // End access to the resources
  [request endAccessingResources];
}

@end

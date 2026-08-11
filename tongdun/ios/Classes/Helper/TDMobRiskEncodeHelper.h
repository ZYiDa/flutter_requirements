//
//  TDMobRiskEncodeHelper.h
//  TDMobRisk
//
//

#import <Foundation/Foundation.h>

@interface TDMobRiskEncodeHelper : NSObject
/// sha256 Encode String
+ (NSString *)sha256WithSrc:(NSString *)src;
@end

//
//  TDMobRiskAPIHelper.h
//  TDMobRisk
//
//

#import <Foundation/Foundation.h>

@interface TDMobRiskAPIHelper : NSObject
/// get SystemInfo by Name.
+ (NSString *)sysctlByName:(const char *)name isObject:(BOOL)isObject;
@end

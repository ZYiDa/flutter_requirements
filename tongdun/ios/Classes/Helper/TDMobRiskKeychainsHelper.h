//
//  TDMobRiskKeychainsHelper.h
//  TDMobRisk
//
//

#import <Foundation/Foundation.h>

@interface TDMobRiskKeychainsHelper : NSObject
+ (OSStatus)deleteValueForKey:(NSString *)key;
+ (OSStatus)saveValueForKey:(NSString *)key value:(id)value;
+ (id)loadValueForKey:(NSString *)key;
@end

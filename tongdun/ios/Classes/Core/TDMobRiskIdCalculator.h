//
//  TDMobRiskIdCalculator.h
//  TDMobRisk
//
//

#import <Foundation/Foundation.h>

@interface TDMobRiskIdCalculator : NSObject
/// Generate ID according to the collected information
+ (NSString *)generateIdByInfo:(NSDictionary *)info;
@end

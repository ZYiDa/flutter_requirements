//
//  TDMobRiskCalculator.h
//  TDMobRisk
//
//

#import <Foundation/Foundation.h>

@interface TDMobRiskCalculator : NSObject
/// Generate risk label according to the collected information
+ (NSDictionary *)generateRiskLabelByInfo:(NSDictionary *)info;
@end

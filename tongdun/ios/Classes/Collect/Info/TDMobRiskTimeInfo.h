//
//  TDMobRiskTimeInfo.h
//  TDMobRisk
//
//

#import "TDMobRiskBaseInfo.h"

@interface TDMobRiskTimeInfo : TDMobRiskBaseInfo
/** timeInfo */
/// currentTime, unit is microsecond
@property (nonatomic, assign) NSTimeInterval currentTime;
/// bootTime, unit is microsecond
@property (nonatomic, assign) NSTimeInterval bootTime;
/// systemUptime
@property (nonatomic, assign) NSTimeInterval systemUptime;
@end

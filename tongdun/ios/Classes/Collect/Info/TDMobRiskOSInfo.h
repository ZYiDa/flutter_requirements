//
//  TDMobRiskOSInfo.h
//  TDMobRisk
//
//

#import "TDMobRiskBaseInfo.h"

@interface TDMobRiskOSInfo : TDMobRiskBaseInfo
/** OS Info **/
/// osVersion
@property (nonatomic, copy) NSString *osVersion;
/// osType
@property (nonatomic, copy) NSString *osType;
/// osRelease
@property (nonatomic, copy) NSString *osRelease;
/// kernelVersion
@property (nonatomic, copy) NSString *kernelVersion;
@end

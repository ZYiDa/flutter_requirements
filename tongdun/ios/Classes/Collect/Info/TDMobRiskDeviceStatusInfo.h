//
//  TDMobRiskDeviceStatusInfo.h
//  TDMobRisk
//
//

#import "TDMobRiskBaseInfo.h"

@interface TDMobRiskDeviceStatusInfo : TDMobRiskBaseInfo
/** Device Status Info **/
/// jailbreak
@property (nonatomic, assign) BOOL jailbreak;
/// debug
@property (nonatomic, assign) BOOL debug;
/// simulator
@property (nonatomic, assign) BOOL simulator;
/// secureKernelStatus
@property (nonatomic, assign) BOOL secureKernelStatus;
/// isiOSAppOnMac
@property (nonatomic, assign) int isiOSAppOnMac;
@end

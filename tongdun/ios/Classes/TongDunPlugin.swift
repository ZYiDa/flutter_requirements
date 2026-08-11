import Flutter
import UIKit

public class TongDunPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "tong_dun", binaryMessenger: registrar.messenger())
    let instance = TongDunPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "getTongDun":
        var options: [String: NSObject] = [:]

        let responseCallback: ([String: Any]) -> Void = { response in
            let deviceId = response["device_id"] as? String ?? ""

            var riskLabelStr = ""
            if let risk = response["device_risk_label"] as? [String: Any] {
                if let jsonData = try? JSONSerialization.data(withJSONObject: risk),
                   let jsonString = String(data: jsonData, encoding: .utf8) {
                    riskLabelStr = jsonString
                }
            } else if let riskStr = response["device_risk_label"] as? String {
                riskLabelStr = riskStr
            }

            var detailStr = ""
            if let detail = response["device_detail"] as? [String: Any] {
                if let jsonData = try? JSONSerialization.data(withJSONObject: detail),
                   let jsonString = String(data: jsonData, encoding: .utf8) {
                    detailStr = jsonString
                }
            } else if let detailStrRaw = response["device_detail"] as? String {
                detailStr = detailStrRaw
            }

            // 构建返回给 Flutter 的 Map
            let deviceInfo: [String: Any] = [
                "device_id": deviceId,
                "device_risk_label": riskLabelStr,    // JSON string 或 ""
                "device_detail": detailStr            // JSON string 或 ""
            ]

            DispatchQueue.main.async {
                result(deviceInfo)
            }
        }

        options["callback"] = unsafeBitCast(responseCallback as @convention(block) ([String: Any]) -> Void,
                                            to: AnyObject.self) as? NSObject

        if let manager = TDMobRiskManager.sharedManager() {
            manager.pointee.initWithOptions(options)
        } else {
            DispatchQueue.main.async {
                result(nil)
            }
        }
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}

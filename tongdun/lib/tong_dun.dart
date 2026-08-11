import 'dart:convert';
import 'package:flutter/services.dart';

class TongDun {
  static const String name = 'tong_dun';
  static const MethodChannel _channel = MethodChannel(name);

  Future<Map<String, dynamic>?> get tongdun async {
    try {
      final dynamic raw = await _channel.invokeMethod('getTongDun');
      if (raw == null || raw is! Map) return null;

      final map = Map<String, dynamic>.from(raw);

      return {
        'deviceId': map['device_id'] as String?,
        'deviceRiskLabel': map['device_risk_label'] != null
            ? jsonDecode(map['device_risk_label'] as String) as Map<String, dynamic>?
            : null,
        'deviceDetail': map['device_detail'] != null
            ? jsonDecode(map['device_detail'] as String) as Map<String, dynamic>?
            : null,
      };
    } catch (e) {
      return null;
    }
  }
}

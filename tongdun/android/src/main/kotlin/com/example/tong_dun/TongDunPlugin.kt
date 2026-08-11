package com.example.tong_dun

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import cn.tongdun.mobrisk.TDRisk

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** TongDunPlugin */
class TongDunPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel
  private lateinit var appContext : Context
  var isInit: Boolean = false

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

    appContext = flutterPluginBinding.applicationContext

    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "tong_dun")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method == "getTongDun") {
      Thread {
        try {
          if (isInit == false) {
            TDRisk.init(appContext);
            isInit = true
          }
          val data = TDRisk.getBlackbox();

          val deviceInfo = mapOf(
            "device_id" to data.optString("device_id"),
            "device_risk_label" to data.optJSONObject("device_risk_label")?.toString(),
            "device_detail" to data.optJSONObject("device_detail")?.toString()
          )
          Handler(Looper.getMainLooper()).post {
            result.success(deviceInfo)
          }
        } catch (e: Exception) {
          e.printStackTrace()
          Handler(Looper.getMainLooper()).post {
            result.success("")
          }
        }
      }.start()
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}

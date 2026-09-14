package com.jomin.location_service_check;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** LocationServiceCheckPlugin */
public class LocationServiceCheckPlugin implements FlutterPlugin, MethodCallHandler {
  private static final long MAX_LOCATION_AGE_MS = 5 * 60 * 1000L;
  private static final long NETWORK_TIMEOUT_MS = 5 * 1000L;
  private static final long GPS_TIMEOUT_MS = 10 * 1000L;
  private final Handler locationHandler = new Handler(Looper.getMainLooper());
  private final List<LocationRequest> locationRequests = new ArrayList<>();
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Context aContext;
  private LocationManager locationManager;
  private ContentResolver contentResolver;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "location_service_check");
    channel.setMethodCallHandler(this);
    aContext = flutterPluginBinding.getApplicationContext();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    init();
    if ("checkLocationIsOpen".equals(call.method)) {
      checkLocationIsOpen(result);
    } else if ("openSetting".equals(call.method)) {
      openSetting();
    } else if ("getLocation".equals(call.method)) {
      getLocation(result);
    } else {
      result.notImplemented();
    }
  }

  /**
   * 初始化
   */
  private void init() {
    if (locationManager == null) {
      locationManager = (LocationManager) aContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }
    if (contentResolver == null) {
      contentResolver = aContext.getApplicationContext().getContentResolver();
    }
  }

  /**
   * 检查定位服务是否开启
   */
  private void checkLocationIsOpen(Result result) {
    Map<String, Object> map = new HashMap<>();
    if (isLocationEnabled()) {
      map.put("success", true);
    } else {
      map.put("success", false);
    }
    result.success(map);
  }

  /**
   * 返回定位服务开启状态
   * 因为最低支持 minSdkVersion 16 所以会有检查的步骤，使用旧的APi
   */
  private boolean isLocationEnabled() {
    int locationMode;
    String locationProviders;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      // This is new method provided in API 28
      LocationManager lm = (LocationManager) aContext.getSystemService(Context.LOCATION_SERVICE);
      return lm.isLocationEnabled();
    } else {
      // This is Deprecated in API 28
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        locationMode = Settings.Secure.getInt(contentResolver,Settings.Secure.LOCATION_MODE,Settings.Secure.LOCATION_MODE_OFF);
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
      } else {
        locationProviders = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        return !TextUtils.isEmpty(locationProviders);
      }
    }
  }

  /**
   * 打开定位服务设置页
   */
  private void openSetting() {
    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
    aContext.startActivity(intent);
  }

  /**
   * 获取定位信息
   */
  private void getLocation(Result result) {
    if (ActivityCompat.checkSelfPermission(aContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(aContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      returnLocation(result, null);
      return;
    }
    Location location = getMyLocation();
    if (location != null || locationManager == null) {
      returnLocation(result, location);
      return;
    }
    LocationRequest request = new LocationRequest(result);
    locationRequests.add(request);
    request.startProvider(false);
  }

  private void returnLocation(Result result, Location location) {
    HashMap<String, Object> map = new HashMap<>();
    if (location != null) {
      map.put("latitude", location.getLatitude());
      map.put("longitude", location.getLongitude());
    } else {
      map.put("error", "location data is null");
    }
    result.success(map);
  }

  private Location getMyLocation() {
    if (locationManager == null) {
      return null;
    }
    List<String> providers;
    try {
      providers = locationManager.getAllProviders();
    } catch (RuntimeException e) {
      return null;
    }
    Location bestLocation = null;
    for (String provider : providers) {
      Location l;
      try {
        l = locationManager.getLastKnownLocation(provider);
      } catch (RuntimeException e) {
        // 某个 Provider 不可用或权限被撤销时，继续尝试其他来源。
        continue;
      }
      if (!isUsableLocation(l)) {
        continue;
      }
      if (bestLocation == null || locationAgeMs(l) < locationAgeMs(bestLocation)
          || (locationAgeMs(l) == locationAgeMs(bestLocation)
              && locationAccuracy(l) < locationAccuracy(bestLocation))) {
        bestLocation = l;
      }
    }
    return bestLocation;
  }

  private long locationAgeMs(Location location) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1000000L;
    }
    return System.currentTimeMillis() - location.getTime();
  }

  private float locationAccuracy(Location location) {
    return location.hasAccuracy() ? location.getAccuracy() : Float.MAX_VALUE;
  }

  private boolean isUsableLocation(Location location) {
    if (location == null) {
      return false;
    }
    double latitude = location.getLatitude();
    double longitude = location.getLongitude();
    long ageMs = locationAgeMs(location);
    return !Double.isNaN(latitude) && !Double.isNaN(longitude)
        && latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180
        && ageMs >= 0 && ageMs <= MAX_LOCATION_AGE_MS;
  }

  /** 每次调用独立管理监听和超时，所有状态均在主线程处理。 */
  private final class LocationRequest {
    private final Result result;
    private final long deadlineMs = SystemClock.elapsedRealtime()
        + NETWORK_TIMEOUT_MS + GPS_TIMEOUT_MS;
    private LocationListener listener;
    private Runnable timeout;
    private boolean finished;

    LocationRequest(Result result) {
      this.result = result;
    }

    void startProvider(final boolean useGps) {
      if (finished) {
        return;
      }
      stopListening();
      long remainingMs = deadlineMs - SystemClock.elapsedRealtime();
      if (remainingMs <= 0) {
        finish(null);
        return;
      }
      String provider = useGps ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
      try {
        if (!locationManager.isProviderEnabled(provider)) {
          nextProvider(useGps);
          return;
        }
        final long stageDeadlineMs = SystemClock.elapsedRealtime()
            + Math.min(useGps ? GPS_TIMEOUT_MS : NETWORK_TIMEOUT_MS, remainingMs);
        listener = new LocationListener() {
          @Override
          public void onLocationChanged(Location location) {
            if (finished || listener != this) {
              return;
            }
            if (SystemClock.elapsedRealtime() >= stageDeadlineMs) {
              nextProvider(useGps);
            } else if (isUsableLocation(location)) {
              finish(location);
            }
          }

          @Override
          public void onProviderDisabled(String disabledProvider) {
            if (!finished && listener == this) {
              nextProvider(useGps);
            }
          }

          @Override
          public void onProviderEnabled(String enabledProvider) {}

          @Override
          public void onStatusChanged(String changedProvider, int status, Bundle extras) {}
        };
        final LocationListener stageListener = listener;
        timeout = () -> {
          if (!finished && listener == stageListener) {
            nextProvider(useGps);
          }
        };
        locationHandler.postDelayed(timeout,
            Math.max(0, stageDeadlineMs - SystemClock.elapsedRealtime()));
        locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper());
      } catch (RuntimeException e) {
        // 包括 Provider 不存在、权限变化及设备定位服务异常。
        nextProvider(useGps);
      }
    }

    private void nextProvider(boolean useGps) {
      if (useGps) {
        finish(null);
      } else {
        startProvider(true);
      }
    }

    private void stopListening() {
      if (timeout != null) {
        locationHandler.removeCallbacks(timeout);
        timeout = null;
      }
      if (listener != null) {
        LocationListener oldListener = listener;
        listener = null;
        try {
          locationManager.removeUpdates(oldListener);
        } catch (RuntimeException e) {
          // 权限或系统服务变化不能阻止请求结束和本地状态清理。
        }
      }
    }

    void finish(Location location) {
      if (finished) {
        return;
      }
      finished = true;
      stopListening();
      locationRequests.remove(this);
      returnLocation(result, location);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    for (LocationRequest request : new ArrayList<>(locationRequests)) {
      request.finish(null);
    }
    channel.setMethodCallHandler(null);
  }
}

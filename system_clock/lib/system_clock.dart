library system_clock;

import 'src/clock.dart' if (dart.library.io) 'src/clock_io.dart' as clock;

///
/// timekeeping facilities.
///
class SystemClock {
  SystemClock._internal();

  ///
  /// Duration since boot, not counting time spent in deep sleep.
  ///
  static Duration uptime() {
    return clock.uptime();
  }

  ///
  /// Duration since boot, including time spent in sleep.
  ///
  static Duration elapsedRealtime() {
    return clock.elapsedRealtime();
  }

  /// 从开机到目前的毫秒数，包括休眠时间
  static int get systemUpTime => elapsedRealtime().inMilliseconds;

  /// 从开机到目前的毫秒数，不包括休眠时间
  static int get systemActiveTime => uptime().inMilliseconds;
}

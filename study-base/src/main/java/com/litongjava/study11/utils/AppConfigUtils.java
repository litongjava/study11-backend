package com.litongjava.study11.utils;

import com.litongjava.tio.utils.environment.EnvUtils;

public class AppConfigUtils {
  public static final int app_area_code = EnvUtils.getInt("app_area_code");
  public static boolean isChina() {
    return 86 == app_area_code;
  }
}

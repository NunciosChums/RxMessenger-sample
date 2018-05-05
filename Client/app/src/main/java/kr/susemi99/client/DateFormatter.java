package kr.susemi99.client;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateFormatter {
  public static final String DEFAULT = "yyyy-MM-dd HH:mm:ss.SSS";

  /**
   * 로그 표시용 변환
   *
   * @param timeInMillis millisecond
   * @return 2016-12-31 13:24:45.678
   */
  public static String format(long timeInMillis) {
    return new SimpleDateFormat(DEFAULT, Locale.getDefault()).format(timeInMillis);
  }
}

package com.zes.device;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ZES_TimeUtil
{
    private ZES_TimeUtil() {}

    public static String convertTimestampToDateFormat(long timestamp, String dateFormat)
    {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.ofHours(9));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return localDateTime.format(formatter);
    }
}

package com.cachirulop.logmytrip.util;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by david on 3/10/15.
 */
public class FormatHelper
{
    public static String formatDate (Context ctx, Date d)
    {
        DateFormat dateFormatter;

        dateFormatter = android.text.format.DateFormat.getMediumDateFormat (ctx);

        return dateFormatter.format (d);
    }

    public static String formatTime (Context ctx, Date d)
    {
        DateFormat timeFormatter;

        timeFormatter = android.text.format.DateFormat.getTimeFormat (ctx);

        return timeFormatter.format (d);
    }

    /**
     * Format duration time in milliseconds as HH:mm:ss
     *
     * @param duration Duration in milliseconds
     * @return String with HH:mm:ss format
     */
    public static String formatDuration (long duration)
    {
        long hours;
        long minutes;
        long seconds;
        long calcDuration;

        calcDuration = duration;

        hours = calcDuration / 3600000;

        calcDuration = calcDuration - (hours * 3600000);
        minutes = calcDuration / 60000;

        calcDuration = calcDuration - (minutes * 60000);
        seconds = calcDuration / 1000;

        return String.format ("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String formatDistance (double meters)
    {
        if (meters > 1000) {
            return String.format ("%.1f Km", (meters / 1000));
        }
        else {
            return String.format ("%.1f m", meters);
        }
    }

    public static String formatSpeed (double speed)
    {
        return String.format ("%.1f km/h", speed);
    }
}

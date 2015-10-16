package com.cachirulop.logmytrip.util;

import android.util.Log;

/**
 * Created by dmagro on 15/10/2015.
 */
public class LogHelper
{
    public static final String LOG_CATEGORY = "com.cachirulop.LOG";

    public static void v (String msg)
    {
        Log.v (LOG_CATEGORY, msg);
    }

    public static void d (String msg)
    {
        Log.d (LOG_CATEGORY, msg);
    }

    public static void i (String msg)
    {
        Log.i (LOG_CATEGORY, msg);
    }

    public static void w (String msg)
    {
        Log.w (LOG_CATEGORY, msg);
    }

    public static void e (String msg)
    {
        Log.e (LOG_CATEGORY, msg);
    }

    public static void wtf (String msg)
    {
        Log.wtf (LOG_CATEGORY, msg);
    }

}

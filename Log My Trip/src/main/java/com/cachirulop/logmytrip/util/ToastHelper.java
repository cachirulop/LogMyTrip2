package com.cachirulop.logmytrip.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.cachirulop.logmytrip.LogMyTripApplication;

public class ToastHelper
{

    public static void showLong (Context ctx, String msg)
    {
        Toast.makeText (ctx, msg, Toast.LENGTH_LONG)
             .show ();

        Log.d (LogMyTripApplication.LOG_CATEGORY, msg);
    }

    public static void showShort (Context ctx, String msg)
    {
        Toast.makeText (ctx, msg, Toast.LENGTH_SHORT)
             .show ();

        Log.d (LogMyTripApplication.LOG_CATEGORY, msg);
    }

    public static void showDebug (Context ctx, String msg)
    {
/*
        Toast.makeText (ctx,
                        msg,
                        Toast.LENGTH_LONG).show ();
*/
        Log.d (LogMyTripApplication.LOG_CATEGORY, msg);
    }

    public static void showShortDebug (Context ctx, String msg)
    {
/*
        Toast.makeText (ctx,
                        msg,
                        Toast.LENGTH_SHORT).show ();
*/
        Log.d (LogMyTripApplication.LOG_CATEGORY, msg);
    }
}

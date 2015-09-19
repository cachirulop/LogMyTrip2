package com.cachirulop.logmytrip.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ToastHelper
{

    public static void showLong (Context ctx, String msg)
    {
        Toast.makeText (ctx, msg, Toast.LENGTH_LONG)
             .show ();

        Log.d (ctx.getClass ()
                  .getCanonicalName (), msg);
    }

    public static void showShort (Context ctx, String msg)
    {
        Toast.makeText (ctx, msg, Toast.LENGTH_SHORT)
             .show ();

        Log.d (ctx.getClass ()
                  .getCanonicalName (), msg);
    }

    public static void showDebug (Context ctx, String msg)
    {
/*
        Toast.makeText (ctx,
                        msg,
                        Toast.LENGTH_LONG).show ();
*/
        Log.d (ctx.getClass ()
                  .getCanonicalName (), msg);
    }

    public static void showShortDebug (Context ctx, String msg)
    {
/*
        Toast.makeText (ctx,
                        msg,
                        Toast.LENGTH_SHORT).show ();
*/
        Log.d (ctx.getClass ()
                  .getCanonicalName (), msg);
    }
}

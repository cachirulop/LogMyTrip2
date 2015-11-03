package com.cachirulop.logmytrip.util;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper
{

    public static void showLong (Context ctx, String msg)
    {
        Toast.makeText (ctx, msg, Toast.LENGTH_LONG).show ();
    }

    public static void showShort (Context ctx, String msg)
    {
        Toast.makeText (ctx, msg, Toast.LENGTH_SHORT).show ();
    }
}

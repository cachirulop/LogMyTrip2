package com.cachirulop.logmytrip.helper;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper
{

    public static void showLong (Context ctx, int msgId)
    {
        showLong (ctx, ctx.getString (msgId));
    }

    public static void showLong (Context ctx, String msg)
    {
        Toast.makeText (ctx, msg, Toast.LENGTH_LONG).show ();
    }

    public static void showShort (Context ctx, int msgId)
    {
        showShort (ctx, ctx.getString (msgId));
    }

    public static void showShort (Context ctx, String msg)
    {
        Toast.makeText (ctx, msg, Toast.LENGTH_SHORT).show ();
    }
}

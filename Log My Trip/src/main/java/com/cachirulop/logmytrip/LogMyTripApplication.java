package com.cachirulop.logmytrip;

import android.app.Application;
import android.content.Context;

/**
 * Created by dmagro on 16/09/2015.
 */
public class LogMyTripApplication
        extends Application
{
    public static final String LOG_CATEGORY = "com.cachirulop.LOG";

    private static Context _context;

    public static Context getAppContext ()
    {
        return LogMyTripApplication._context;
    }

    public void onCreate ()
    {
        super.onCreate ();
        LogMyTripApplication._context = getApplicationContext ();
    }
}

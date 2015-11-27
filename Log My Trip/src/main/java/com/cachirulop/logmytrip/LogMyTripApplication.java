package com.cachirulop.logmytrip;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * Created by dmagro on 16/09/2015.
 */
public class LogMyTripApplication
        extends Application
{
    //    private static Context _context;
    //
    //    public static Context getAppContext ()
    //    {
    //        return LogMyTripApplication._context;
    //    }

    public static void runInMainThread (Context ctx, Runnable r)
    {
        Handler main;

        main = new Handler (ctx.getMainLooper ());

        main.post (r);
    }

    public void onCreate ()
    {
        super.onCreate ();
        //        LogMyTripApplication._context = getApplicationContext ();
    }
}

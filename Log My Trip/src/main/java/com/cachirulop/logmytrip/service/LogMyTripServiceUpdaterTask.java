package com.cachirulop.logmytrip.service;

import android.content.Context;

import com.cachirulop.logmytrip.LogMyTripApplication;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.helper.LogHelper;
import com.cachirulop.logmytrip.manager.LogMyTripNotificationManager;
import com.cachirulop.logmytrip.manager.TripManager;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimerTask;

/**
 * Created by dmagro on 18/11/2015.
 */
public class LogMyTripServiceUpdaterTask
        extends TimerTask
{
    private Context _ctx;

    public LogMyTripServiceUpdaterTask (Context ctx)
    {
        _ctx = ctx;
    }

    @Override
    public void run ()
    {
        LogMyTripApplication.runInMainThread (_ctx, new Runnable ()
        {
            public void run ()
            {
                Trip t;

                LogHelper.d ("*** LogMyTripUpdaterTask: updating: " + DateFormat.getDateTimeInstance ()
                                                                                .format (new Date ()));

                t = TripManager.getActiveTrip (_ctx);

                // Update the notification information
                LogMyTripNotificationManager.updateTripLogging (_ctx, t);

                // Flush the pending locations
                TripManager.flushLocations (_ctx);
            }
        });
    }
}
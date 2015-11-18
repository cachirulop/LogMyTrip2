package com.cachirulop.logmytrip.service;

import android.content.Context;
import android.os.Handler;

import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.LogMyTripNotificationManager;
import com.cachirulop.logmytrip.manager.TripManager;

import java.util.TimerTask;

/**
 * Created by dmagro on 18/11/2015.
 */
public class NotificationUpdaterTask
        extends TimerTask
{
    private Context _ctx;
    private Handler _handler;

    public NotificationUpdaterTask (Context ctx)
    {
        _ctx = ctx;
        _handler = new Handler ();
    }

    @Override
    public void run ()
    {
        _handler.post (new Runnable ()
        {
            public void run ()
            {
                Trip t;

                t = TripManager.getActiveTrip (_ctx);

                LogMyTripNotificationManager.updateTripLogging (_ctx, t);
            }
        });
    }
}
package com.cachirulop.logmytrip.service;

import android.content.Context;

import com.cachirulop.logmytrip.LogMyTripApplication;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.manager.JourneyManager;
import com.cachirulop.logmytrip.manager.LogMyTripNotificationManager;

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
                Journey t;

                // Flush the pending locations
                //JourneyManager.flushLocations (_ctx);

                t = JourneyManager.getActiveJourney (_ctx);

                if (t != null) {
                    JourneyManager.updateJourneyStatistics (_ctx, t);
                }
            }
        });
    }
}
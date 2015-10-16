package com.cachirulop.logmytrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.manager.LocationBroadcastManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.util.LogHelper;

/**
 * Created by dmagro on 07/10/2015.
 */
public class LocationReceiver
        extends BroadcastReceiver
{
    @Override
    public void onReceive (final Context context, Intent intent)
    {
        Location loc;

        LogHelper.d ("LocationReceiver: onReceive");

        loc = (Location) intent.getExtras ()
                               .get (LocationManager.KEY_LOCATION_CHANGED);
        if (loc != null && isValidLocation (context, loc)) {
            Trip trip;

            trip = TripManager.getActiveTrip (context);
            if (trip != null) {
                TripLocation tl;

                tl = new TripLocation (trip, loc);

                TripManager.saveTripLocation (context, tl);

                LocationBroadcastManager.sendNewLocationMessage (context);
            }
        }
    }

    private boolean isValidLocation (Context ctx, Location location)
    {
        return location != null &&
                (location.hasAccuracy () && location.getAccuracy () <= SettingsManager.getGpsAccuracy (
                        ctx)) &&
                (Math.abs (location.getLatitude ()) <= 90 && Math.abs (
                        location.getLongitude ()) <= 180);
    }

}

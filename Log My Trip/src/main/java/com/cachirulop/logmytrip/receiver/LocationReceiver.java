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

/**
 * Created by dmagro on 07/10/2015.
 */
public class LocationReceiver
        extends BroadcastReceiver
{
    @Override
    public void onReceive (final Context context, Intent intent)
    {
        if (intent.hasExtra (LocationManager.KEY_LOCATION_CHANGED)) {
            onLocationChanged (context, intent);
        }
        else if (intent.hasExtra (LocationManager.KEY_PROVIDER_ENABLED)) {
            onProviderEnabled (context, intent);
        }
        else if (intent.hasExtra (LocationManager.KEY_STATUS_CHANGED)) {
            onStatusChanged (context, intent);
        }
    }

    private void onLocationChanged (Context context, Intent intent)
    {
        Location loc;

        loc = intent.getParcelableExtra (LocationManager.KEY_LOCATION_CHANGED);
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

    private void onProviderEnabled (Context context, Intent intent)
    {
        boolean enabled;

        enabled = intent.getBooleanExtra (LocationManager.KEY_PROVIDER_ENABLED, false);

        LocationBroadcastManager.sendProviderEnableChangeMessage (context, enabled);
    }

    private void onStatusChanged (Context context, Intent intent)
    {
        int status;

        status = intent.getIntExtra (LocationManager.KEY_STATUS_CHANGED, -1);
        if (status != -1) {
            LocationBroadcastManager.sendStatusChangeMessage (context, status);
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

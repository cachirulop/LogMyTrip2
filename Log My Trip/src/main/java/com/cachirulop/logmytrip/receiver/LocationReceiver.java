package com.cachirulop.logmytrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.google.android.gms.location.LocationResult;

/**
 * Created by dmagro on 07/10/2015.
 */
public class LocationReceiver
        extends BroadcastReceiver
{
    public static final String KEY_CURRENT_TRIP_ID = "com.cachirulop.logmytrip.receiver.current_trip_id";

    @Override
    public void onReceive (final Context context, Intent intent)
    {
        Trip trip;
        long tripId;

        Log.v (LocationReceiver.class.getCanonicalName (), "onReceive");

        //        if (LocationAvailability.hasLocationAvailability (intent)) {
        //            LocationAvailability la;
        //
        //            la = LocationAvailability.extractLocationAvailability (intent);
        //
        //            Log.v (LocationReceiver.class.getCanonicalName (), "la: " + la.toString () + "-.-" + la.isLocationAvailable ());
        //            if (la.isLocationAvailable ()) {
        if (LocationResult.hasResult (intent)) {
            LocationResult lr;

            trip = TripManager.getActiveTrip (context);

            lr = LocationResult.extractResult (intent);
            // tripId = intent.getExtras ().getLong (KEY_CURRENT_TRIP_ID);
            // trip = TripManager.getTrip (context, tripId);

            // Log.v (LocationReceiver.class.getCanonicalName (), "TripID: " + tripId);

            for (Location loc : lr.getLocations ()) {
                TripLocation tl;

                Log.v (LocationReceiver.class.getCanonicalName (),
                       "onReceive, location: " + loc.getProvider () + "-.-" + loc.getLatitude () + "-.-" + lr.getLocations ());
                if (isValidLocation (context, loc)) {
                    tl = new TripLocation (trip, loc);
                    TripManager.saveTripLocation (context, tl);

                    //                            new Thread ()
                    //                            {
                    //                                public void run ()
                    //                                {
                    //                                    TripManager.saveTripLocation (context, tl);
/*
                    for (OnTripLocationSavedListener l : _onTripLocationSavedListeners) {
                        l.onTripLocationSaved (tl);
                    }
*/
                    //                                }
                    //                            }.start ();
                }
            }
        }
        //            }
        //        }
        //        else {
        //            Log.v (LocationReceiver.class.getCanonicalName (), "No hay location disponible");
        //        }
    }

    private boolean isValidLocation (Context ctx, Location location)
    {
        return (location.hasAccuracy () && location.getAccuracy () <= SettingsManager.getGpsAccuracy (
                ctx)) && (location != null &&
                Math.abs (location.getLatitude ()) <= 90 &&
                Math.abs (location.getLongitude ()) <= 180);
    }

}

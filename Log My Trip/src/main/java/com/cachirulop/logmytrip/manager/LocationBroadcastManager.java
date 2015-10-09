package com.cachirulop.logmytrip.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cachirulop.logmytrip.LogMyTripApplication;
import com.cachirulop.logmytrip.entity.Trip;

/**
 * Created by david on 9/10/15.
 */
public class LocationBroadcastManager
{
    private static final String BROADCAST_PREFIX       = "com.cachirulop.logmytrip.LocationBroadcastManager.";
    private static final String ACTION_SAVE_TRIP_START = BROADCAST_PREFIX + "start";
    private static final String ACTION_SAVE_TRIP_STOP  = BROADCAST_PREFIX + "stop";
    private static final String EXTRA_TRIP             = BROADCAST_PREFIX + "trip";

    public static void sendStartSaveTripMessage (Context ctx)
    {
        Log.d (LogMyTripApplication.LOG_CATEGORY, "Sending start save trip message");
        sendBroadcastMessage (ctx, ACTION_SAVE_TRIP_START);
    }

    private static void sendBroadcastMessage (Context ctx, String action)
    {
        sendBroadcastMessage (ctx, action, null);
    }

    private static void sendBroadcastMessage (Context ctx, String action, Trip trip)
    {
        Intent intent;

        intent = new Intent (action);

        if (trip != null) {
            intent.putExtra (EXTRA_TRIP, trip);
        }

        LocalBroadcastManager.getInstance (ctx)
                             .sendBroadcast (intent);
    }

    public static void sendStopSaveTripMessage (Context ctx, Trip trip)
    {
        Log.d (LogMyTripApplication.LOG_CATEGORY, "Sending stop save trip message");
        sendBroadcastMessage (ctx, ACTION_SAVE_TRIP_STOP, trip);
    }

    public static boolean hasTrip (Intent intent)
    {
        return intent.hasExtra (EXTRA_TRIP);
    }

    public static Trip getTrip (Intent intent)
    {
        return (Trip) intent.getSerializableExtra (EXTRA_TRIP);
    }

    public static void registerSaveTripStartReceiver (Context ctx, BroadcastReceiver receiver)
    {
        LocalBroadcastManager.getInstance (ctx)
                             .registerReceiver (receiver,
                                                new IntentFilter (ACTION_SAVE_TRIP_START));
    }

    public static void registerSaveTripStopReceiver (Context ctx, BroadcastReceiver receiver)
    {
        LocalBroadcastManager.getInstance (ctx)
                             .registerReceiver (receiver, new IntentFilter (ACTION_SAVE_TRIP_STOP));
    }

    public static void unregisterReceiver (Context ctx, BroadcastReceiver receiver)
    {
        LocalBroadcastManager.getInstance (ctx)
                             .unregisterReceiver (receiver);
    }
}


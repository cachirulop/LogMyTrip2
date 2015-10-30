package com.cachirulop.logmytrip.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.cachirulop.logmytrip.entity.Trip;

import java.util.HashMap;

/**
 * Created by david on 9/10/15.
 */
public class LocationBroadcastManager
{
    private static final String BROADCAST_PREFIX              = "com.cachirulop.logmytrip.LocationBroadcastManager.";
    private static final String ACTION_TRIP_LOG_START         = BROADCAST_PREFIX + "start";
    private static final String ACTION_TRIP_LOG_STOP          = BROADCAST_PREFIX + "stop";
    private static final String ACTION_NEW_LOCATION           = BROADCAST_PREFIX + "new_location";
    private static final String ACTION_PROVIDER_ENABLE_CHANGE = BROADCAST_PREFIX + "provider_change";
    private static final String ACTION_STATUS_CHANGE          = BROADCAST_PREFIX + "status_change";
    private static final String EXTRA_TRIP                    = BROADCAST_PREFIX + "trip";
    private static final String EXTRA_PROVIDER_ENABLE         = BROADCAST_PREFIX + "enabled";
    private static final String EXTRA_STATUS                  = BROADCAST_PREFIX + "status";
    private static final String EXTRA_LOCATION = BROADCAST_PREFIX + "location";

    public static void sendStartTripLogMessage (Context ctx)
    {
        sendBroadcastMessage (ctx, ACTION_TRIP_LOG_START);
    }

    private static void sendBroadcastMessage (Context ctx, String action)
    {
        sendBroadcastMessage (ctx, action, null);
    }

    private static void sendBroadcastMessage (Context ctx, String action, HashMap<String, Object> params)
    {
        Intent intent;

        intent = new Intent (action);

        if (params != null) {
            for (String k : params.keySet ()) {
                Object value;

                value = params.get (k);
                if (value instanceof Trip) {
                    intent.putExtra (k, (Trip) params.get (k));
                }
                else if (value instanceof Integer) {
                    intent.putExtra (k, ((Integer) params.get (k)).intValue ());
                }
                else if (value instanceof Boolean) {
                    intent.putExtra (k, ((Boolean) params.get (k)).booleanValue ());
                }
                else if (value instanceof Parcelable) {
                    intent.putExtra (k, ((Parcelable) params.get (k)));
                }
            }
        }

        LocalBroadcastManager.getInstance (ctx)
                             .sendBroadcast (intent);
    }

    public static void sendStopTripLogMessage (Context ctx, Trip trip)
    {
        HashMap<String, Object> params;

        params = new HashMap<> ();
        params.put (EXTRA_TRIP, trip);

        sendBroadcastMessage (ctx, ACTION_TRIP_LOG_STOP, params);
    }

    public static void sendNewLocationMessage (Context ctx, Location loc)
    {
        HashMap<String, Object> params;

        params = new HashMap<> ();
        params.put (EXTRA_LOCATION, loc);

        sendBroadcastMessage (ctx, ACTION_NEW_LOCATION, params);
    }

    public static void sendProviderEnableChangeMessage (Context context, boolean enabled)
    {
        HashMap<String, Object> params;

        params = new HashMap<> ();
        params.put (EXTRA_PROVIDER_ENABLE, new Boolean (enabled));

        sendBroadcastMessage (context, ACTION_PROVIDER_ENABLE_CHANGE, params);
    }

    public static void sendStatusChangeMessage (Context context, int status)
    {
        HashMap<String, Object> params;

        params = new HashMap<> ();
        params.put (EXTRA_PROVIDER_ENABLE, new Integer (status));

        sendBroadcastMessage (context, ACTION_STATUS_CHANGE, params);
    }

    public static void registerTripLogStartReceiver (Context ctx, BroadcastReceiver receiver)
    {
        LocalBroadcastManager.getInstance (ctx)
                             .registerReceiver (receiver, new IntentFilter (ACTION_TRIP_LOG_START));
    }

    public static void registerTripLogStopReceiver (Context ctx, BroadcastReceiver receiver)
    {
        LocalBroadcastManager.getInstance (ctx)
                             .registerReceiver (receiver, new IntentFilter (ACTION_TRIP_LOG_STOP));
    }

    public static void registerNewLocationReceiver (Context ctx, BroadcastReceiver receiver)
    {
        LocalBroadcastManager.getInstance (ctx)
                             .registerReceiver (receiver, new IntentFilter (ACTION_NEW_LOCATION));
    }

    public static void registerProviderEnableChange (Context ctx, BroadcastReceiver receiver)
    {
        LocalBroadcastManager.getInstance (ctx)
                             .registerReceiver (receiver,
                                                new IntentFilter (ACTION_PROVIDER_ENABLE_CHANGE));
    }

    public static void registerStatusChange (Context ctx, BroadcastReceiver receiver)
    {
        LocalBroadcastManager.getInstance (ctx)
                             .registerReceiver (receiver, new IntentFilter (ACTION_STATUS_CHANGE));
    }

    public static void unregisterReceiver (Context ctx, BroadcastReceiver receiver)
    {
        LocalBroadcastManager.getInstance (ctx)
                             .unregisterReceiver (receiver);
    }

    public static boolean hasTrip (Intent intent)
    {
        return intent.hasExtra (EXTRA_TRIP);
    }

    public static Trip getTrip (Intent intent)
    {
        return (Trip) intent.getSerializableExtra (EXTRA_TRIP);
    }

    public static Location getLocation (Intent intent)
    {
        return (Location) intent.getParcelableExtra (EXTRA_LOCATION);
    }


    public static boolean hasStatus (Intent intent)
    {
        return intent.hasExtra (EXTRA_STATUS);
    }

    public static int getStatus (Intent intent)
    {
        return intent.getIntExtra (EXTRA_STATUS, -1);
    }

    public static boolean hasProviderEnable (Intent intent)
    {
        return intent.hasExtra (EXTRA_PROVIDER_ENABLE);
    }

    public static boolean getProviderEnable (Intent intent)
    {
        return intent.getBooleanExtra (EXTRA_PROVIDER_ENABLE, false);
    }
}


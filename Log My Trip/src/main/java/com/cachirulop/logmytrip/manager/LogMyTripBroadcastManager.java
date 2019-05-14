package com.cachirulop.logmytrip.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Parcelable;

import com.cachirulop.logmytrip.entity.Journey;

import java.util.HashMap;

import static androidx.localbroadcastmanager.content.LocalBroadcastManager.*;

/**
 * Created by david on 9/10/15.
 */
public class LogMyTripBroadcastManager
{
    private static final String BROADCAST_PREFIX = LogMyTripBroadcastManager.class.getPackage ()
                                                                                  .getName ();

    private static final String ACTION_LOG_START       = BROADCAST_PREFIX + "start";
    private static final String ACTION_LOG_STOP        = BROADCAST_PREFIX + "stop";
    private static final String ACTION_NEW_LOCATION           = BROADCAST_PREFIX + "new_location";
    private static final String ACTION_PROVIDER_ENABLE_CHANGE = BROADCAST_PREFIX + "provider_change";
    private static final String ACTION_STATUS_CHANGE          = BROADCAST_PREFIX + "status_change";
    private static final String ACTION_BLUETOOTH_START = BROADCAST_PREFIX + "bluetooth_start";
    private static final String ACTION_BLUETOOTH_STOP  = BROADCAST_PREFIX + "bluetooth_start";

    private static final String EXTRA_JOURNEY = BROADCAST_PREFIX + "journey";
    private static final String EXTRA_PROVIDER_ENABLE = BROADCAST_PREFIX + "enabled";
    private static final String EXTRA_STATUS          = BROADCAST_PREFIX + "status";
    private static final String EXTRA_LOCATION        = BROADCAST_PREFIX + "location";

    private static void sendBroadcastMessage (Context ctx, String action)
    {
        sendBroadcastMessage (ctx, action, null);
    }

    private static void sendBroadcastMessage (Context ctx,
                                              String action,
                                              HashMap<String, Object> params)
    {
        Intent intent;

        intent = new Intent (action);

        if (params != null) {
            for (String k : params.keySet ()) {
                Object value;

                value = params.get (k);
                if (value instanceof Journey) {
                    intent.putExtra (k, (Journey) params.get (k));
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

        getInstance (ctx).sendBroadcast (intent);
    }

    public static void sendStartLogMessage (Context ctx)
    {
        sendBroadcastMessage (ctx, ACTION_LOG_START);
    }

    public static void sendStopLogMessage (Context ctx, Journey journey)
    {
        HashMap<String, Object> params;

        params = new HashMap<> ();
        params.put (EXTRA_JOURNEY, journey);

        sendBroadcastMessage (ctx, ACTION_LOG_STOP, params);
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

    public static void sendStartBluetoothMessage (Context ctx)
    {
        sendBroadcastMessage (ctx, ACTION_BLUETOOTH_START);
    }

    public static void sendStopBluetoothMessage (Context ctx)
    {
        sendBroadcastMessage (ctx, ACTION_BLUETOOTH_STOP);
    }

    public static void registerLogStartReceiver (Context ctx, BroadcastReceiver receiver)
    {
        getInstance (ctx).registerReceiver (receiver, new IntentFilter (ACTION_LOG_START));
    }

    public static void registerLogStopReceiver (Context ctx, BroadcastReceiver receiver)
    {
        getInstance (ctx).registerReceiver (receiver, new IntentFilter (ACTION_LOG_STOP));
    }

    public static void registerNewLocationReceiver (Context ctx, BroadcastReceiver receiver)
    {
        getInstance (ctx).registerReceiver (receiver, new IntentFilter (ACTION_NEW_LOCATION));
    }

    public static void registerProviderEnableChange (Context ctx, BroadcastReceiver receiver)
    {
        getInstance (ctx).registerReceiver (receiver,
                                                new IntentFilter (ACTION_PROVIDER_ENABLE_CHANGE));
    }

    public static void registerStatusChange (Context ctx, BroadcastReceiver receiver)
    {
        getInstance (ctx).registerReceiver (receiver, new IntentFilter (ACTION_STATUS_CHANGE));
    }

    public static void registerBluetoothStartReceiver (Context ctx, BroadcastReceiver receiver)
    {
        getInstance (ctx).registerReceiver (receiver,
                                                new IntentFilter (ACTION_BLUETOOTH_START));
    }

    public static void registerBluetoothStopReceiver (Context ctx, BroadcastReceiver receiver)
    {
        getInstance (ctx).registerReceiver (receiver, new IntentFilter (ACTION_BLUETOOTH_STOP));
    }

    public static void unregisterReceiver (Context ctx, BroadcastReceiver receiver)
    {
        getInstance (ctx).unregisterReceiver (receiver);
    }

    public static boolean hasJourney (Intent intent)
    {
        return intent.hasExtra (EXTRA_JOURNEY);
    }

    public static Journey getTrip (Intent intent)
    {
        return (Journey) intent.getSerializableExtra (EXTRA_JOURNEY);
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


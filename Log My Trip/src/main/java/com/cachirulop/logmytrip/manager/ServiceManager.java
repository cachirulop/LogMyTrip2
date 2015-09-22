package com.cachirulop.logmytrip.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Messenger;

import com.cachirulop.logmytrip.service.BluetoothService;
import com.cachirulop.logmytrip.service.LogMyTripService;

public class ServiceManager
{
    public static void startSaveTrip (Context ctx, Handler h)
    {
        SettingsManager.setLogTrip (ctx, true);

        startStopService (ctx, h);

        if (SettingsManager.isAutoStartLog (ctx)) {
            ServiceManager.stopBluetooth (ctx);
        }
    }

    /**
     * Starts or stops the service.
     * <p/>
     * The service itself decide if it should start or it should stop in the
     * OnStartService event.
     *
     * @param ctx Context to start the service
     */
    public static void startStopService (Context ctx, Handler h)
    {
        Intent i;

        i = new Intent (ctx, LogMyTripService.class);

        if (h != null) {
            i.putExtra (LogMyTripService.EXTRA_SERVICE_MESSAGE_HANDLER, new Messenger (h));
        }

        ctx.startService (i);
    }

    public static void stopBluetooth (Context ctx)
    {
        ctx.stopService (new Intent (ctx, BluetoothService.class));
    }

    public static void stopSaveTrip (Context ctx, Handler h)
    {
        SettingsManager.setLogTrip (ctx, false);

        startStopService (ctx, h);

        if (SettingsManager.isAutoStartLog (ctx)) {
            ServiceManager.startBluetooth (ctx);
        }
    }

    public static void startBluetooth (Context ctx)
    {
        ctx.startService (new Intent (ctx, BluetoothService.class));
    }
}

package com.cachirulop.logmytrip.manager;

import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.service.BluetoothService;
import com.cachirulop.logmytrip.service.LogMyTripService;

public class ServiceManager
{
    public static void startSaveTrip (Context ctx)
    {
        SettingsManager.setLogTrip (ctx, true);

        ctx.startService (new Intent (ctx, LogMyTripService.class));
    }

    public static void stopBluetooth (Context ctx)
    {
        ctx.stopService (new Intent (ctx, BluetoothService.class));
    }

    public static void stopSaveTrip (Context ctx)
    {
        SettingsManager.setLogTrip (ctx, false);

        ctx.stopService (new Intent (ctx, LogMyTripService.class));
    }

    public static void startBluetooth (Context ctx)
    {
        ctx.startService (new Intent (ctx, BluetoothService.class));
    }
}

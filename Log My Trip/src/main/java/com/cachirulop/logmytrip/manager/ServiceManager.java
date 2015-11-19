package com.cachirulop.logmytrip.manager;

import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.service.BluetoothService;
import com.cachirulop.logmytrip.service.LogMyTripService;

public class ServiceManager
{
    public static void startTripLog (Context ctx)
    {
        if (!SettingsManager.isLogTrip (ctx)) {
            SettingsManager.setLogTrip (ctx, true);

            ctx.startService (new Intent (ctx, LogMyTripService.class));
        }
    }

    public static void stopTripLog (Context ctx)
    {
        if (SettingsManager.isLogTrip (ctx)) {
            SettingsManager.setLogTrip (ctx, false);

            ctx.stopService (new Intent (ctx, LogMyTripService.class));
        }
    }

    public static void startBluetooth (Context ctx)
    {
        if (!SettingsManager.isWaitingBluetooth (ctx)) {
            SettingsManager.setAutoStartLog (ctx, true);
            SettingsManager.setIsWaitingBluetooth (ctx, true);

            ctx.startService (new Intent (ctx, BluetoothService.class));
        }
    }

    public static void stopBluetooth (Context ctx)
    {
        if (SettingsManager.isWaitingBluetooth (ctx)) {
            SettingsManager.setAutoStartLog (ctx, false);
            SettingsManager.setIsWaitingBluetooth (ctx, false);

            ctx.stopService (new Intent (ctx, BluetoothService.class));
        }
    }
}

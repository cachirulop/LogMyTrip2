package com.cachirulop.logmytrip.manager;

import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.service.BluetoothService;
import com.cachirulop.logmytrip.service.LogMyTripServiceGooglePlayServices;

public class ServiceManager
{
    public static void startLog (Context ctx)
    {
        SettingsManager.setLogJourney (ctx, true);

        ctx.startService (new Intent (ctx, LogMyTripServiceGooglePlayServices.class));
    }

    public static void stopLog (Context ctx)
    {
        if (SettingsManager.isLogJourney (ctx)) {
            SettingsManager.setLogJourney (ctx, false);

            ctx.stopService (new Intent (ctx, LogMyTripServiceGooglePlayServices.class));
        }
    }

    public static void startBluetooth (Context ctx)
    {
        SettingsManager.setAutoStartLog (ctx, true);
        SettingsManager.setIsWaitingBluetooth (ctx, true);

        ctx.startService (new Intent (ctx, BluetoothService.class));
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

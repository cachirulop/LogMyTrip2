package com.cachirulop.logmytrip.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.service.LogMyTripService;

public class BootCompletedBroadcastReceiver
        extends BroadcastReceiver
{
    @Override
    public void onReceive (Context context, Intent intent)
    {
        if (Intent.ACTION_BOOT_COMPLETED.equals (intent.getAction ())) {
            if (SettingsManager.getAutoStartLog (context)) {
                ComponentName comp = new ComponentName (context.getPackageName (),
                                                        LogMyTripService.class.getName ());
                ComponentName service = context.startService (new Intent ().setComponent (comp));

                if (null == service) {
                    // something really wrong here
                    Log.e (BootCompletedBroadcastReceiver.class.getName (),
                           "Could not start service " + comp.toString ());
                }
            }
        }
    }
}

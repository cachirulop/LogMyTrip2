package com.cachirulop.logmytrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;

public class BootCompletedBroadcastReceiver
        extends BroadcastReceiver
{
    @Override
    public void onReceive (Context context, Intent intent)
    {
        if (Intent.ACTION_BOOT_COMPLETED.equals (intent.getAction ())) {
            if (SettingsManager.isAutoStartLog (context)) {
                ServiceManager.startBluetooth (context);
            }
        }
    }
}

package com.cachirulop.logmytrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.manager.ServiceManager;

/**
 * Created by david on 11/11/15.
 */
public class NotifyReceiver
        extends BroadcastReceiver
{
    public final static String ACTION_STOP_LOG       = NotifyReceiver.class.getPackage ()
                                                                           .getName () + ".STOP_LOG";
    public final static String ACTION_STOP_BLUETOOTH = NotifyReceiver.class.getPackage ()
                                                                           .getName () + ".STOP_BLUETOOTH";

    public void onReceive (Context context, Intent intent)
    {
        if (ACTION_STOP_LOG.equals (intent.getAction ())) {
            ServiceManager.stopTripLog (context);
        }
        else if (ACTION_STOP_BLUETOOTH.equals (intent.getAction ())) {
            ServiceManager.stopBluetooth (context);
        }
    }
}

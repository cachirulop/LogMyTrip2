
package com.cachirulop.logmytrip.manager;

import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.service.LogMyTripService;

public class ServiceManager
{
    /**
     * Starts or stops the service. 
     * 
     * The service itself decide if it should start or it should stop in the 
     * OnStartService event.
     * 
     * @param ctx Context to start the service
     */
    public static void startStopService (Context ctx)
    {
        ctx.startService (new Intent (ctx,
                                      LogMyTripService.class));
    }
    
    public static void startSaveTrip (Context ctx) 
    {
        SettingsManager.setLogTrip (ctx, true);
        
        startStopService (ctx);
    }

    public static void stopSaveTrip (Context ctx) 
    {
        SettingsManager.setLogTrip (ctx, false);
        
        startStopService (ctx);
    }
}

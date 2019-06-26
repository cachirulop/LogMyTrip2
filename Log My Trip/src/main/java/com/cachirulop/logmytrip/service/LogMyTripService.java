package com.cachirulop.logmytrip.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;

import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.helper.ToastHelper;
import com.cachirulop.logmytrip.manager.JourneyManager;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.LogMyTripNotificationManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.receiver.LocationReceiver;


public class LogMyTripService
        extends Service
{
    private static boolean _started;
    private LogMyTripServiceBinder _binder = null;

    public static boolean isRunning ()
    {
        return _started;
    }

    @Override
    public void onCreate ()
    {
        super.onCreate ();

        _started = false;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        Journey current;

        super.onStartCommand (intent, flags, startId);

        current = startLog ();
        if (current != null) {
            startForegroundService ();
            _started = true;
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        if (_binder == null) {
            _binder = new LogMyTripServiceBinder ();
        }
        return _binder;
    }

    @Override
    public void onDestroy ()
    {
        if (_started) {
            stopLog ();
            stopForegroundService ();

            _started = false;
        }

        super.onDestroy ();
    }

    @SuppressLint ("MissingPermission")
    private Journey startLog ()
    {
        LocationManager locationMgr;
        Journey result = null;

        locationMgr = (LocationManager) this.getSystemService (LOCATION_SERVICE);

        if (locationMgr.isProviderEnabled (LocationManager.GPS_PROVIDER)) {
            result = JourneyManager.startJourney (this);

            LogMyTripBroadcastManager.sendStartLogMessage (this);

            locationMgr.requestLocationUpdates (LocationManager.GPS_PROVIDER,
                                                SettingsManager.getGpsTimeInterval (this),
                                                SettingsManager.getGpsDistanceInterval (this),
                                                getLocationIntent ());
        }
        else {
            ToastHelper.showLong (this, "No GPS activated");
        }

        return result;
    }

    private void startForegroundService ()
    {
        Notification note;

        note = LogMyTripNotificationManager.createLogging (this);

        startForeground (LogMyTripNotificationManager.NOTIFICATION_LOGGING, note);
    }

    private PendingIntent getLocationIntent ()
    {
        Intent intent;

        intent = new Intent (this, LocationReceiver.class);

        return PendingIntent.getBroadcast (getApplicationContext (),
                                           0,
                                           intent,
                                           PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void stopLog ()
    {
        Journey journey;
        LocationManager locationMgr;

        // JourneyManager.flushLocations (this);

        journey = JourneyManager.getActiveJourney (this);
        if (journey != null) {
            JourneyManager.updateJourneyStatistics (this, journey);

            LogMyTripBroadcastManager.sendStopLogMessage (this, journey);
        }

        locationMgr = (LocationManager) this.getSystemService (LOCATION_SERVICE);
        locationMgr.removeUpdates (getLocationIntent ());

        JourneyManager.unsetActiveJourney (this);

        _started = false;
    }

    private void stopForegroundService ()
    {
        stopForeground (true);
        stopSelf ();
    }

    public class LogMyTripServiceBinder
            extends Binder
    {
        public LogMyTripService getService ()
        {
            return LogMyTripService.this;
        }
    }
}

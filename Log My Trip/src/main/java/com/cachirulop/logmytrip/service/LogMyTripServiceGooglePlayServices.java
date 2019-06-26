package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.helper.ToastHelper;
import com.cachirulop.logmytrip.manager.JourneyManager;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.LogMyTripNotificationManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class LogMyTripServiceGooglePlayServices
        extends Service
{
    private FusedLocationProviderClient _provider;
    private LocationRequestCallback _callback;
    private LogMyTripGooglePlayServicesServiceBinder _binder = null;

    @Override
    public void onCreate ()
    {
        super.onCreate ();

        _provider = LocationServices.getFusedLocationProviderClient (this);
        _callback = new LocationRequestCallback ();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        Journey current;

        super.onStartCommand (intent, flags, startId);

        if (startLog ()) {
            startForegroundService ();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        if (_binder == null) {
            _binder = new LogMyTripGooglePlayServicesServiceBinder ();
        }

        return _binder;
    }

    @Override
    public void onDestroy ()
    {
        stopLog ();
        stopForegroundService ();

        super.onDestroy ();
    }

    private boolean startLog ()
    {
        LocationRequest request;

        // TODO: Remove configuration for the distance interval, just is deprecated
        request = LocationRequest.create ();
        request.setPriority (LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval (SettingsManager.getGpsTimeInterval (this));

        try {
            _provider.requestLocationUpdates (request, _callback,null);

            JourneyManager.startJourney (this);

            return true;
        }
        catch (SecurityException e) {
            ToastHelper.showLong (this, getString (R.string.msg_not_enough_permissions));

            return false;
        }
    }

    private void startForegroundService ()
    {
        Notification note;

        note = LogMyTripNotificationManager.createLogging (this);

        startForeground (LogMyTripNotificationManager.NOTIFICATION_LOGGING, note);
    }

    private void stopLog ()
    {
        Journey journey;

        journey = JourneyManager.getActiveJourney (this);
        if (journey != null) {
            JourneyManager.updateJourneyStatistics (this, journey);

            LogMyTripBroadcastManager.sendStopLogMessage (this, journey);
        }

        if (_provider != null) {
            _provider.removeLocationUpdates (_callback);
        }

        JourneyManager.unsetActiveJourney (this);
    }

    private void stopForegroundService ()
    {
        stopForeground (true);
        stopSelf ();
    }

    public class LogMyTripGooglePlayServicesServiceBinder
            extends Binder
    {
        public LogMyTripServiceGooglePlayServices getService ()
        {
            return LogMyTripServiceGooglePlayServices.this;
        }
    }

    private class LocationRequestCallback
        extends LocationCallback
    {
        @Override
        public void onLocationResult (LocationResult result)
        {
            for (android.location.Location l : result.getLocations ()) {
                com.cachirulop.logmytrip.entity.Location lmtLocation;

                lmtLocation = new com.cachirulop.logmytrip.entity.Location (l);
                JourneyManager.saveLocation (LogMyTripServiceGooglePlayServices.this, lmtLocation);

                LogMyTripBroadcastManager.sendNewLocationMessage (LogMyTripServiceGooglePlayServices.this, l);
            }
        }
    }
}

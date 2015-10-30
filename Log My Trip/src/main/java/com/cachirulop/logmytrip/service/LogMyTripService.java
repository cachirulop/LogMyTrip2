package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.LocationBroadcastManager;
import com.cachirulop.logmytrip.manager.NotifyManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.receiver.LocationReceiver;
import com.cachirulop.logmytrip.util.ToastHelper;


public class LogMyTripService
        extends Service
{
    private static boolean _started;
    private LocationManager _locationMgr;
    private LogMyTripServiceBinder _binder = new LogMyTripServiceBinder ();

    public static boolean isRunning ()
    {
        return _started;
    }

    @Override
    public void onCreate ()
    {
        super.onCreate ();

        _started = false;
        _locationMgr = (LocationManager) this.getSystemService (LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        super.onStartCommand (intent, flags, startId);

        startLog ();
        startForegroundService ();

        _started = true;

        return START_STICKY;
    }

    private void startLog ()
    {
        if (_locationMgr.isProviderEnabled (LocationManager.GPS_PROVIDER)) {
            TripManager.startTrip (this);

            LocationBroadcastManager.sendStartTripLogMessage (this);

            _locationMgr.requestLocationUpdates (LocationManager.GPS_PROVIDER,
                                                 SettingsManager.getGpsTimeInterval (this),
                                                 SettingsManager.getGpsDistanceInterval (this),
                                                 getLocationIntent ());
        }
        else {
            ToastHelper.showLong (this, "No GPS activated");
        }
    }

    private void startForegroundService ()
    {
        Notification note;

        note = NotifyManager.createTripLogging (this,
                                                this.getText (R.string.notif_ContentLoggingTrip));

        startForeground (NotifyManager.NOTIFICATION_TRIP_LOGGING, note);
    }

    private PendingIntent getLocationIntent ()
    {
        Intent intent;

        intent = new Intent (this, LocationReceiver.class);

        return PendingIntent.getBroadcast (getApplicationContext (), 0, intent,
                                           PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        return _binder;
    }

    @Override
    public void onDestroy ()
    {
        if (_started) {
            stopLog ();
            stopForegroundService ();
        }

        super.onDestroy ();
    }

    private void stopLog ()
    {
        Trip trip;

        trip = TripManager.getActiveTrip (this);

        TripManager.unsetActiveTrip (this);

        LocationBroadcastManager.sendStopTripLogMessage (this, trip);

        _locationMgr.removeUpdates (getLocationIntent ());

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

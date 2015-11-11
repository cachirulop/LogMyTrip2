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
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.NotifyManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.receiver.LocationReceiver;
import com.cachirulop.logmytrip.util.ToastHelper;


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
        super.onStartCommand (intent, flags, startId);

        startLog ();
        startForegroundService ();

        _started = true;

        return START_STICKY;
    }

    private void startLog ()
    {
        LocationManager locationMgr;

        locationMgr = (LocationManager) this.getSystemService (LOCATION_SERVICE);

        if (locationMgr.isProviderEnabled (LocationManager.GPS_PROVIDER)) {
            TripManager.startTrip (this);

            LogMyTripBroadcastManager.sendStartTripLogMessage (this);

            locationMgr.requestLocationUpdates (LocationManager.GPS_PROVIDER,
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
        CharSequence msg;
        Trip         current;

        current = TripManager.getActiveTrip (this);
        if (current != null) {
            msg = current.getTitle ();
        }
        else {
            msg = this.getText (R.string.notif_ContentLoggingTrip);
        }


        note = NotifyManager.createTripLogging (this, msg);

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
        }

        super.onDestroy ();
    }

    private void stopLog ()
    {
        Trip trip;
        LocationManager locationMgr;

        trip = TripManager.getActiveTrip (this);

        LogMyTripBroadcastManager.sendStopTripLogMessage (this, trip);

        locationMgr = (LocationManager) this.getSystemService (LOCATION_SERVICE);
        locationMgr.removeUpdates (getLocationIntent ());

        TripManager.unsetActiveTrip (this);

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

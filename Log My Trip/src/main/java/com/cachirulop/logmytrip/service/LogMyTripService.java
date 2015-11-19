package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;

import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.LogMyTripNotificationManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.receiver.LocationReceiver;
import com.cachirulop.logmytrip.util.ToastHelper;

import java.util.Timer;


public class LogMyTripService
        extends Service
{
    private static boolean _started;
    private LogMyTripServiceBinder _binder = null;
    private Timer                   _notificationTimer;
    private NotificationUpdaterTask _updaterTask;

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
        Trip current;

        super.onStartCommand (intent, flags, startId);

        current = startLog ();
        if (current != null) {
            startForegroundService (current);

            _notificationTimer = new Timer ();
            _updaterTask = new NotificationUpdaterTask (this);

            _notificationTimer.schedule (_updaterTask, 0, 60000);

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

            _notificationTimer.cancel ();
        }

        super.onDestroy ();
    }

    private Trip startLog ()
    {
        LocationManager locationMgr;
        Trip result = null;

        locationMgr = (LocationManager) this.getSystemService (LOCATION_SERVICE);

        if (locationMgr.isProviderEnabled (LocationManager.GPS_PROVIDER)) {
            result = TripManager.startTrip (this);

            LogMyTripBroadcastManager.sendStartTripLogMessage (this);

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

    private void startForegroundService (Trip t)
    {
        Notification note;

        note = LogMyTripNotificationManager.createTripLogging (this, t);

        startForeground (LogMyTripNotificationManager.NOTIFICATION_TRIP_LOGGING, note);
    }

    private PendingIntent getLocationIntent ()
    {
        Intent intent;

        intent = new Intent (this, LocationReceiver.class);

        return PendingIntent.getBroadcast (getApplicationContext (), 0, intent,
                                           PendingIntent.FLAG_UPDATE_CURRENT);
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

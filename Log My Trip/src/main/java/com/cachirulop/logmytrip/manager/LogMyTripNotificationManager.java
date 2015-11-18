package com.cachirulop.logmytrip.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.activity.MainActivity;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.receiver.NotifyReceiver;
import com.cachirulop.logmytrip.util.FormatHelper;

import java.util.Set;

public class LogMyTripNotificationManager
{
    public static final int NOTIFICATION_TRIP_LOGGING = 1133;
    public static final int NOTIFICATION_WAITING_BLUETOOTH = 1134;


    public static Notification createTripLogging (Context ctx, Trip t)
    {
        return createTripLoggingNotification (ctx, t);
    }

    private static Notification createTripLoggingNotification (Context ctx, Trip t)
    {
        Notification.Builder    builder;
        Intent                  notificationIntent;
        PendingIntent           pi;
        Notification.InboxStyle style;
        TripSegment             currentSegment;

        builder = new Notification.Builder (ctx);
        builder.setContentTitle (ctx.getText (R.string.notif_Title));
        builder.setTicker (ctx.getText (R.string.notif_Tricker));
        builder.setContentText (t.getTitle ());

        notificationIntent = new Intent (ctx, MainActivity.class);
        pi = PendingIntent.getActivity (ctx,
                                        0,
                                        notificationIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent (pi);

        addAction (ctx,
                   builder,
                   NotifyReceiver.ACTION_STOP_LOG,
                   android.R.drawable.ic_media_pause,
                   R.string.action_stop_log);


        style = new Notification.InboxStyle ();
        style.setBigContentTitle (ctx.getText (R.string.notif_Title));
        style.addLine (t.getTitle ());
        if (!"".equals (t.getDescription ())) {
            style.addLine (t.getDescription ());
        }

        if (t.getSegments () != null && t.getSegments ().size () > 0) {
            currentSegment = t.getSegments ().get (t.getSegments ().size () - 1);

            style.addLine (String.format ("%s: %s",
                                          ctx.getText (R.string.title_start),
                                          FormatHelper.formatDateTime (ctx,
                                                                       currentSegment.getStartDate ())));
            style.addLine (String.format ("%s: %s",
                                          ctx.getText (R.string.title_total_time),
                                          FormatHelper.formatDuration (currentSegment.computeTotalTime ())));
            style.addLine (String.format ("%s: %s",
                                          ctx.getText (R.string.title_total_distance),
                                          FormatHelper.formatDistance (currentSegment.computeTotalDistance ())));
        }

        builder.setStyle (style);

        builder.setSmallIcon (R.mipmap.ic_trip_status_logging);

        return builder.build ();
    }

    private static void addAction (Context ctx,
                                   Notification.Builder builder,
                                   String action,
                                   int icon,
                                   int title)
    {
        PendingIntent pi;
        Intent        i;

        i = new Intent (ctx, NotifyReceiver.class);
        i.setAction (action);
        pi = PendingIntent.getBroadcast (ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction (icon, ctx.getString (title), pi);
    }

    public static void updateTripLogging (Context ctx, Trip t)
    {
        Notification note;

        note = createTripLoggingNotification (ctx, t);

        getManager (ctx).notify (NOTIFICATION_TRIP_LOGGING, note);
    }

    private static NotificationManager getManager (Context ctx)
    {
        return (NotificationManager) ctx.getSystemService (Context.NOTIFICATION_SERVICE);
    }

    public static Notification createWaitingBluetooth (Context ctx)
    {
        return createWaitingBluetoothNotification (ctx);
    }

    private static Notification createWaitingBluetoothNotification (Context ctx)
    {
        Notification.Builder builder;
        Intent               notificationIntent;
        PendingIntent        pi;
        Notification.InboxStyle style;
        Set<String>             cfgDevices;
        Set<BluetoothDevice>    pairedDevices;
        BluetoothAdapter        bta;

        builder = new Notification.Builder (ctx);
        builder.setContentTitle (ctx.getText (R.string.notif_Title));
        builder.setTicker (ctx.getText (R.string.notif_Tricker));
        builder.setContentText (getWaitingBluetoothText (ctx));

        notificationIntent = new Intent (ctx, MainActivity.class);
        pi = PendingIntent.getActivity (ctx,
                                        0,
                                        notificationIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent (pi);

        addAction (ctx,
                   builder,
                   NotifyReceiver.ACTION_STOP_BLUETOOTH,
                   android.R.drawable.stat_sys_data_bluetooth,
                   R.string.action_stop_bluetooth);

        if (!SettingsManager.isLogTrip (ctx)) {
            addAction (ctx,
                       builder,
                       NotifyReceiver.ACTION_START_LOG,
                       android.R.drawable.ic_menu_save,
                       R.string.action_start_log);
        }

        // Add the configured bluetooth device list
        bta = BluetoothAdapter.getDefaultAdapter ();
        if (bta != null) {
            pairedDevices = bta.getBondedDevices ();
            cfgDevices = SettingsManager.getBluetoothDeviceList (ctx);

            if (cfgDevices != null && pairedDevices != null) {
                style = new Notification.InboxStyle ();
                style.setBigContentTitle (ctx.getText (R.string.notif_Title));
                style.addLine (ctx.getText (R.string.notif_configured_devices));

                for (BluetoothDevice d : pairedDevices) {
                    if (cfgDevices.contains (d.getAddress ())) {
                        style.addLine ("     " + d.getName ());
                    }
                }

                builder.setStyle (style);
            }
        }

        builder.setSmallIcon (R.mipmap.ic_waiting_bluetooth);

        return builder.build ();
    }

    private static String getWaitingBluetoothText (Context ctx)
    {
        String mode;
        String action;

        if (SettingsManager.isAutostartOnConnect (ctx)) {
            mode = ctx.getText (R.string.notif_ContentWaitingBluetooth_mode_connect).toString ();
        }
        else {
            mode = ctx.getText (R.string.notif_ContentWaitingBluetooth_mode_disconnect).toString ();
        }

        if (SettingsManager.isLogTrip (ctx)) {
            action = ctx.getText (R.string.notif_ContentWaitingBluetooth_action_stop).toString ();
        }
        else {
            action = ctx.getText (R.string.notif_ContentWaitingBluetooth_action_start).toString ();
        }

        return String.format (ctx.getText (R.string.notif_ContentWaitingBluetooth).toString (),
                              mode,
                              action);
    }

    public static void updateWaitingBluetooth (Context ctx)
    {
        Notification note;

        note = createWaitingBluetoothNotification (ctx);

        getManager (ctx).notify (NOTIFICATION_WAITING_BLUETOOTH, note);
    }
}

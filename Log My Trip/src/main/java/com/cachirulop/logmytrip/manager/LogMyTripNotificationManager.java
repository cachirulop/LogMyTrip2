package com.cachirulop.logmytrip.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.activity.MainActivity;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.entity.JourneySegment;
import com.cachirulop.logmytrip.helper.FormatHelper;
import com.cachirulop.logmytrip.helper.LogHelper;
import com.cachirulop.logmytrip.receiver.NotifyReceiver;

import java.util.Set;

public class LogMyTripNotificationManager
{
    public static final int NOTIFICATION_LOGGING = 1133;
    public static final int NOTIFICATION_WAITING_BLUETOOTH = 1134;

    public static Notification createLogging (Context ctx, Journey t)
    {
        return createLoggingNotification (ctx, t);
    }

    private static Notification createLoggingNotification (Context ctx, Journey t)
    {
        NotificationCompat.Builder builder;
        Intent                     notificationIntent;
        PendingIntent              pi;
        NotificationCompat.InboxStyle    style;
        JourneySegment             currentSegment;

        try {
            initNotificationChannel (ctx);

            builder = new NotificationCompat.Builder (ctx, ctx.getText (R.string.notif_Title).toString ());
            builder.setContentTitle (ctx.getText (R.string.notif_Title));
            builder.setTicker (ctx.getText (R.string.notif_Tricker));
            builder.setContentText (t.getTitle ());

            notificationIntent = new Intent (ctx, MainActivity.class);
            pi = PendingIntent.getActivity (ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent (pi);

            addAction (ctx,
                       builder,
                       NotifyReceiver.ACTION_STOP_LOG,
                       android.R.drawable.ic_media_pause,
                       R.string.action_stop_log);


            style = new NotificationCompat.InboxStyle ();
            style.setBigContentTitle (ctx.getText (R.string.notif_Title));
            style.addLine (t.getTitle ());
            if (!"".equals (t.getDescription ())) {
                style.addLine (t.getDescription ());
            }

            if (t.getSegments () != null && t.getSegments ().size () > 0) {
                currentSegment = t.getSegments ().get (t.getSegments ().size () - 1);

                style.addLine (String.format ("%s: %s",
                                              ctx.getText (R.string.title_start),
                                              FormatHelper.formatDateTime (ctx, currentSegment.getStartDate ())));
                style.addLine (String.format ("%s: %s",
                                              ctx.getText (R.string.title_total_time),
                                              FormatHelper.formatDuration (currentSegment.computeTotalTime ())));
                style.addLine (String.format ("%s: %s",
                                              ctx.getText (R.string.title_total_distance),
                                              FormatHelper.formatDistance (currentSegment.computeTotalDistance ())));
            }

            builder.setStyle (style);

            builder.setSmallIcon (R.drawable.ic_loggin_notify);
            builder.setLargeIcon (BitmapFactory.decodeResource (ctx.getResources (), R.mipmap.ic_status_logging));

            return builder.build ();
        }
        catch (Exception e) {
            LogHelper.d ("Error: " + e.getLocalizedMessage ());

            throw e;
        }
    }

    public static Notification createWaitingBluetooth (Context ctx)
    {
        return createWaitingBluetoothNotification (ctx);
    }

    private static Notification createWaitingBluetoothNotification (Context ctx)
    {
        NotificationCompat.Builder builder;
        Intent               notificationIntent;
        PendingIntent        pi;
        NotificationCompat.InboxStyle style;
        Set<String>             cfgDevices;
        Set<BluetoothDevice>    pairedDevices;
        BluetoothAdapter        bta;

        try {
            initNotificationChannel (ctx);

            builder = new NotificationCompat.Builder (ctx, ctx.getText (R.string.notif_Title).toString ());
            builder.setContentTitle (ctx.getText (R.string.notif_Title));
            builder.setTicker (ctx.getText (R.string.notif_Tricker));
            builder.setContentText (getWaitingBluetoothText (ctx));

            notificationIntent = new Intent (ctx, MainActivity.class);
            pi = PendingIntent.getActivity (ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent (pi);

            addAction (ctx,
                       builder,
                       NotifyReceiver.ACTION_STOP_BLUETOOTH,
                       android.R.drawable.stat_sys_data_bluetooth,
                       R.string.action_stop_bluetooth);

            if (!SettingsManager.isLogJourney (ctx)) {
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
                    style = new NotificationCompat.InboxStyle ();
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

            builder.setSmallIcon (R.drawable.ic_waiting_bluetooth_notify);
            builder.setLargeIcon (BitmapFactory.decodeResource (ctx.getResources (), R.mipmap.ic_waiting_bluetooth));

            return builder.build ();
        }
        catch (Exception e) {
            LogHelper.d ("Error: " + e.getLocalizedMessage ());

            throw e;
        }
    }


    private static void initNotificationChannel (Context ctx)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel;
            NotificationManager notificationManager;

            channel = new NotificationChannel(ctx.getText (R.string.notif_ChannelID).toString (),
                                              ctx.getText (R.string.notif_Channel_Name).toString (),
                                              NotificationManager.IMPORTANCE_LOW);

            channel.setDescription(ctx.getText (R.string.notif_Channel_Description).toString ());

            notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static void addAction (Context ctx,
                                   NotificationCompat.Builder builder,
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

    public static void updateLogging (Context ctx, Journey t)
    {
        Notification note;

        note = createLoggingNotification (ctx, t);

        getManager (ctx).notify (NOTIFICATION_LOGGING, note);
    }

    private static NotificationManager getManager (Context ctx)
    {
        return (NotificationManager) ctx.getSystemService (Context.NOTIFICATION_SERVICE);
    }

    private static String getWaitingBluetoothText (Context ctx)
    {
        String mode;
        String action;

        if (SettingsManager.isAutoStartOnConnect (ctx)) {
            mode = ctx.getString (R.string.notif_ContentWaitingBluetooth_mode_connect);
        }
        else {
            mode = ctx.getString (R.string.notif_ContentWaitingBluetooth_mode_disconnect);
        }

        if (SettingsManager.isLogJourney (ctx)) {
            action = ctx.getString (R.string.notif_ContentWaitingBluetooth_action_stop);
        }
        else {
            action = ctx.getString (R.string.notif_ContentWaitingBluetooth_action_start);
        }

        return String.format (ctx.getString (R.string.notif_ContentWaitingBluetooth),
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

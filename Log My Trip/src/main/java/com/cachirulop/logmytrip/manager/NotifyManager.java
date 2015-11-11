package com.cachirulop.logmytrip.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.activity.MainActivity;
import com.cachirulop.logmytrip.receiver.NotifyReceiver;

public class NotifyManager
{
    public static final int NOTIFICATION_TRIP_LOGGING = 1133;
    public static final int NOTIFICATION_WAITING_BLUETOOTH = 1134;

    public static void hideTripLogging (Context ctx)
    {
        hideNotification (ctx, NOTIFICATION_TRIP_LOGGING);
    }

    private static void hideNotification (Context ctx, int id)
    {
        getManager (ctx).cancel (id);
    }

    private static NotificationManager getManager (Context ctx)
    {
        return (NotificationManager) ctx.getSystemService (Context.NOTIFICATION_SERVICE);
    }

    public static void hideWaitingBluetooth (Context ctx)
    {
        hideNotification (ctx, NOTIFICATION_WAITING_BLUETOOTH);
    }

    public static void showTripLogging (Context ctx, CharSequence contentText)
    {
        showNotification (ctx, contentText, NOTIFICATION_TRIP_LOGGING);
    }

    private static void showNotification (Context ctx, CharSequence contentText, int id)
    {
        Notification note;

        note = createNotification (ctx, contentText, id);

        getManager (ctx).notify (id, note);
    }

    private static Notification createNotification (Context ctx, CharSequence contextText, int id)
    {
        Notification.Builder builder;
        Intent               notificationIntent;
        PendingIntent        pi;

        builder = new Notification.Builder (ctx);
        builder.setContentTitle (ctx.getText (R.string.notif_Title));
        builder.setTicker (ctx.getText (R.string.notif_Tricker));
        builder.setContentText (contextText);

        notificationIntent = new Intent (ctx, MainActivity.class);
        pi = PendingIntent.getActivity (ctx,
                                        0,
                                        notificationIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent (pi);

        switch (id) {
            case NOTIFICATION_TRIP_LOGGING:
                addAction (ctx,
                           builder,
                           NotifyReceiver.ACTION_STOP_LOG,
                           android.R.drawable.ic_media_pause,
                           R.string.action_stop_log);

                builder.setSmallIcon (R.mipmap.ic_trip_status_logging);
                break;

            case NOTIFICATION_WAITING_BLUETOOTH:
                addAction (ctx,
                           builder,
                           NotifyReceiver.ACTION_STOP_BLUETOOTH,
                           android.R.drawable.stat_sys_data_bluetooth,
                           R.string.action_stop_bluetooth);

                builder.setSmallIcon (R.mipmap.ic_waiting_bluetooth);
                break;
        }

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

    public static Notification createTripLogging (Context ctx, CharSequence contentText)
    {
        return createNotification (ctx, contentText, NOTIFICATION_TRIP_LOGGING);
    }

    public static Notification createWaitingBluetooth (Context ctx, CharSequence contentText)
    {
        return createNotification (ctx, contentText, NOTIFICATION_WAITING_BLUETOOTH);
    }

}

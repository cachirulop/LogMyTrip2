package com.cachirulop.logmytrip.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.activity.MainActivity;

public class NotifyManager
{
    public static final int NOTIFICATION_SAVING_TRIP       = 1133;
    public static final int NOTIFICATION_WAITING_BLUETOOTH = 1134;

    public static void hideSavingTrip (Context ctx)
    {
        hideNotification (ctx, NOTIFICATION_SAVING_TRIP);
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

    public static void showSavingTrip (Context ctx, CharSequence contentText)
    {
        showNotification (ctx, contentText, NOTIFICATION_SAVING_TRIP);
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

        switch (id) {
            case NOTIFICATION_SAVING_TRIP:
                builder.setSmallIcon (R.mipmap.ic_trip_status_saving);
                break;

            case NOTIFICATION_WAITING_BLUETOOTH:
                builder.setSmallIcon (R.mipmap.ic_waiting_bluetooth);
                break;
        }

        notificationIntent = new Intent (ctx, MainActivity.class);
        pi = PendingIntent.getActivity (ctx, 0, notificationIntent, 0);

        builder.setContentIntent (pi);

        return builder.build ();
    }

    public static void showWaitingBluetooth (Context ctx, CharSequence contentText)
    {
        showNotification (ctx, contentText, NOTIFICATION_WAITING_BLUETOOTH);
    }

    public static Notification createSavingTrip (Context ctx, CharSequence contentText)
    {
        return createNotification (ctx, contentText, NOTIFICATION_SAVING_TRIP);
    }

    public static Notification createWaitingBluetooth (Context ctx, CharSequence contentText)
    {
        return createNotification (ctx, contentText, NOTIFICATION_WAITING_BLUETOOTH);
    }

}

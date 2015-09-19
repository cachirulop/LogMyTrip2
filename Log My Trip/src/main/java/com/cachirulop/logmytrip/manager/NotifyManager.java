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
    public static final int NOTIFICATION_ID = 1133;

    public static void hideNotification (Context ctx)
    {
        getManager (ctx).cancel (NOTIFICATION_ID);
    }

    private static NotificationManager getManager (Context ctx)
    {
        return (NotificationManager) ctx.getSystemService (Context.NOTIFICATION_SERVICE);
    }

    public static void showNotification (Context ctx, CharSequence contentText)
    {
        Notification note;

        note = createNotification (ctx, contentText);

        getManager (ctx).notify (NOTIFICATION_ID, note);
    }

    public static Notification createNotification (Context ctx, CharSequence contextText)
    {
        Notification.Builder builder;
        Intent        notificationIntent;
        PendingIntent pi;

        builder = new Notification.Builder (ctx);
        // builder.setSmallIcon (R.drawable.ic_launcher);
        builder.setSmallIcon (R.mipmap.ic_trip_status_saving);
        builder.setContentTitle (ctx.getText (R.string.notif_Title));
        builder.setTicker (ctx.getText (R.string.notif_Tricker));
        builder.setContentText (contextText);

        notificationIntent = new Intent (ctx, MainActivity.class);
        pi = PendingIntent.getActivity (ctx, 0, notificationIntent, 0);

        builder.setContentIntent (pi);

        return builder.build ();
    }

}

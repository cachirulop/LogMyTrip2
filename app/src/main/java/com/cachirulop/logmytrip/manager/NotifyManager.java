
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

    private static NotificationManager getManager (Context ctx)
    {
        return (NotificationManager) ctx.getSystemService (Context.NOTIFICATION_SERVICE);
    }

    public static Notification createNotification (Context ctx,
                                                   int contentId)
    {
        Notification.Builder builder;
        Intent notificationIntent;
        PendingIntent pi;

        builder = new Notification.Builder (ctx);
        builder.setSmallIcon (R.drawable.ic_launcher);
        builder.setContentTitle (ctx.getText (R.string.notif_Title));
        builder.setTicker (ctx.getText (R.string.notif_Tricker));
        builder.setContentText (ctx.getText (contentId));

        notificationIntent = new Intent (ctx,
                                         MainActivity.class);
        pi = PendingIntent.getActivity (ctx,
                                        0,
                                        notificationIntent,
                                        0);

        builder.setContentIntent (pi);

        return builder.build ();
    }

    public static void hideNotification (Context ctx)
    {
        getManager(ctx).cancel (NOTIFICATION_ID);
    }

    public static void showNotification (Context ctx,
                                         int contentId)
    {
        Notification note;

        note = createNotification (ctx,
                                   contentId);

        getManager (ctx).notify (NOTIFICATION_ID,
                                 note);
    }

}

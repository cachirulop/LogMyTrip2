package com.cachirulop.logmytrip.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;

/**
 * Created by david on 22/11/15.
 */
public class DialogHelper
{

    public static void showErrorDialog (Context ctx, int titleId, int messageId)
    {
        showErrorDialog (ctx, titleId, messageId, null);
    }

    public static void showErrorDialog (Context ctx,
                                        int titleId,
                                        int messageId,
                                        Object... formatArguments)
    {
        AlertDialog.Builder builder;
        AlertDialog         dlg;
        String              msg;

        if (formatArguments != null && formatArguments.length > 0) {
            msg = String.format (ctx.getString (messageId, formatArguments));
        }
        else {
            msg = ctx.getString (messageId);
        }

        builder = new AlertDialog.Builder (ctx);
        builder.setTitle (titleId);
        builder.setMessage (msg);
        builder.setIconAttribute (android.R.attr.alertDialogIcon);

        builder.setPositiveButton (android.R.string.ok, null);

        dlg = builder.create ();
        dlg.show ();
    }

    public static void showErrorDialogMainThread (final Context ctx,
                                                  final int titleId,
                                                  final int messageId)
    {
        showErrorDialogMainThread (ctx, titleId, messageId);
    }

    public static void showErrorDialogMainThread (final Context ctx,
                                                  final int titleId,
                                                  final int messageId,
                                                  final Object... formatArguments)
    {
        Handler  main;
        Runnable runInMain;

        main = new Handler (ctx.getMainLooper ());

        runInMain = new Runnable ()
        {
            @Override
            public void run ()
            {
                showErrorDialog (ctx, titleId, messageId, formatArguments);
            }
        };

        main.post (runInMain);
    }

}

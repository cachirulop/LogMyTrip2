package com.cachirulop.logmytrip.helper;

import android.content.Context;
import android.os.Environment;

import com.cachirulop.logmytrip.R;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by david on 21/11/15.
 */
public class ExportHelper
{
    public static void exportToFile (Context ctx, String content, String fileName)
    {
        try {
            // TODO: Write in another thread
            File file;
            FileWriter writer;

            file = new File (getFilePath (ctx, fileName, true));
            writer = new FileWriter (file, false);

            writer.append (content);

            writer.flush ();
            writer.close ();
        }
        catch (IOException e) {
            // TODO: Show error dialog
            ToastHelper.showLong (ctx,
                                  ctx.getText (R.string.error_cant_write_file)
                                     .toString () + ": " + e.getMessage ());
        }

    }

    private static String getFilePath (Context ctx, String fileName, boolean local)
    {
        StringBuilder result;

        result = new StringBuilder ();

        if (local) {
            result.append (Environment.getExternalStorageDirectory ());
            result.append (File.separator);
        }

        result.append (ctx.getString (R.string.app_name));

        if (fileName.endsWith (".gpx")) {
            result.append (File.separator);
            result.append ("GPX");
        }
        else {
            result.append (File.separator);
            result.append ("KML");
        }

        result.append (File.separator);
        result.append (fileName);

        return result.toString ();
    }

    public static void exportToGoogleDrive (final Context ctx,
                                            final String content,
                                            final String fileName,
                                            final GoogleApiClient client)
    {
        GoogleDriveHelper helper;
        String            path;

        path = getFilePath (ctx, fileName, false);

        helper = new GoogleDriveHelper (client);

        helper.saveFileAsync (fileName, content, new GoogleDriveHelper.IGoogleDriveHelperListener ()
        {
            @Override
            public void onSaveFileSuccess ()
            {
                LogHelper.d ("*** exportToGoogleDrive OK");
            }

            @Override
            public void onSaveFileFails ()
            {
                LogHelper.d ("*** exportToGoogleDrive FAILS");
            }
        });
    }
}

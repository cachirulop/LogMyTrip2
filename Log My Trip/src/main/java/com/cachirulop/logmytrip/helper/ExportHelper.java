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
    public static void exportToFile (final Context ctx,
                                     final String content,
                                     final String fileName,
                                     final IExportHelperListener listener)
    {
        new Thread ()
        {
            @Override
            public void run ()
            {
                try {
                    File file;
                    FileWriter writer;

                    file = new File (getFilePath (ctx, fileName, true));
                    writer = new FileWriter (file, false);

                    writer.append (content);

                    writer.flush ();
                    writer.close ();

                    listener.onExportSuccess (fileName);
                }
                catch (IOException e) {
                    listener.onExportFails (R.string.msg_error_exporting);
                }
            }
        }.start ();
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
                                            final GoogleApiClient client,
                                            final IExportHelperListener listener)
    {
        String path;

        path = getFilePath (ctx, fileName, false);

        GoogleDriveHelper.saveFile (client,
                                    path,
                                    content,
                                    new GoogleDriveHelper.IGoogleDriveHelperListener ()
                                    {
                                        @Override
                                        public void onSaveFileSuccess ()
                                        {
                                            listener.onExportSuccess (fileName);
                                        }

                                        @Override
                                        public void onSaveFileFails (int messageId)
                                        {
                                            listener.onExportFails (messageId);
                                        }
                                    });
    }

    public interface IExportHelperListener
    {
        void onExportSuccess (String fileName);

        void onExportFails (int messageId);
    }
}

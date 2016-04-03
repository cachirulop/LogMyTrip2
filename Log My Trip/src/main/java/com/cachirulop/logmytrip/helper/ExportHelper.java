package com.cachirulop.logmytrip.helper;

import android.content.Context;
import android.os.Environment;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.manager.JourneyManager;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by david on 21/11/15.
 */
public class ExportHelper
{
    public static void exportToFile (final Context ctx, final Journey journey,
                                     final String fileName,
                                     final IExportHelperListener listener)
    {
        Thread t;

        t = new Thread ()
        {
            @Override
            public void run ()
            {
                try {
                    File file;
                    FileWriter writer;

                    file = new File (getFilePath (ctx, fileName, true));
                    file.getParentFile ().mkdirs ();

                    writer = new FileWriter (file, false);

                    JourneyManager.exportJourney (ctx,
                                                  journey,
                                                  getFileExtension (fileName),
                                                  writer);

                    writer.flush ();
                    writer.close ();
                }
                catch (IOException e) {
                    listener.onExportFails (R.string.msg_error_exporting);
                }
            }
        };

        t.start ();

        try {
            t.join ();

            listener.onExportSuccess (fileName);
        }
        catch (InterruptedException e) {
            listener.onExportFails (R.string.msg_error_exporting);
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

        result.append (File.separator);
        result.append (getFileExtension (fileName));

        result.append (File.separator);
        result.append (fileName);

        return result.toString ();
    }

    public static void exportToGoogleDrive (final Context ctx, final Journey journey,
                                            final String fileName,
                                            final GoogleApiClient client,
                                            final IExportHelperListener listener)
    {
        String path;

        path = getFilePath (ctx, fileName, false);

        GoogleDriveHelper.saveFile (client,
                                    ctx,
                                    path,
                                    journey,
                                    new GoogleDriveHelper.IGoogleDriveWriterListener ()
                                    {
                                        @Override
                                        public void onWriteContents (Writer w)
                                        {
                                            try {
                                                JourneyManager.exportJourney (ctx,
                                                                              journey,
                                                                              getFileExtension (
                                                                                      fileName),
                                                                              w);
                                            }
                                            catch (IOException e) {
                                                listener.onExportFails (R.string.msg_error_exporting);
                                            }
                                        }

                                        @Override
                                        public void onSaveFileSuccess ()
                                        {
                                            listener.onExportSuccess (fileName);
                                        }

                                        @Override
                                        public void onSaveFileFails (int messageId,
                                                                     Object... formatArgs)
                                        {
                                            listener.onExportFails (messageId, formatArgs);
                                        }
                                    });
    }

    private static String getFileExtension (String fileName)
    {
        return fileName.substring (fileName.lastIndexOf (".") + 1).toUpperCase ();
    }

    public interface IExportHelperListener
    {
        void onExportSuccess (String fileName);

        void onExportFails (int messageId, Object... formatArgs);
    }
}

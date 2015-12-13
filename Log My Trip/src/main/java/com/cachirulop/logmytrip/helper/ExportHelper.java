package com.cachirulop.logmytrip.helper;

import android.content.Context;
import android.os.Environment;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.TripManager;
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
    public static void exportToFile (final Context ctx, final Trip trip,
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

                    TripManager.exportTrip (ctx, trip, getFileExtension (fileName), writer);

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

    //    public static void exportToGoogleDrive (final Context ctx, final Trip trip,
    //                                            final String fileName,
    //                                            final GoogleApiClient client,
    //                                            final IExportHelperListener listener)
    //    {
    //        String path;
    //
    //        path = getFilePath (ctx, fileName, false);
    //
    //        GoogleDriveHelper.saveFile (client,
    //                                    ctx,
    //                                    path,
    //                                    trip,
    //                                    new GoogleDriveHelper.IGoogleDriveHelperListener ()
    //                                    {
    //                                        @Override
    //                                        public void onWriteContents (Writer w)
    //                                        {
    //                                            try {
    //                                                TripManager.exportTrip (ctx,
    //                                                                        trip,
    //                                                                        getFileExtension (fileName),
    //                                                                        w);
    //                                            }
    //                                            catch (IOException e) {
    //                                                listener.onExportFails (R.string.msg_error_exporting);
    //                                            }
    //                                        }
    //
    //                                        @Override
    //                                        public void onSaveFileSuccess ()
    //                                        {
    //                                            listener.onExportSuccess (fileName);
    //                                        }
    //
    //                                        @Override
    //                                        public void onSaveFileFails (int messageId)
    //                                        {
    //                                            listener.onExportFails (messageId);
    //                                        }
    //                                    });
    //    }

    public static void exportToGoogleDrive (final Context ctx,
                                            final Trip trip,
                                            final String fileName,
                                            final GoogleApiClient client,
                                            final IExportHelperListener listener)
    {
        String path;

        path = getFilePath (ctx, fileName, false);

        GoogleDriveHelper.saveFile (client,
                                    ctx,
                                    path,
                                    trip,
                                    new GoogleDriveHelper.IGoogleDriveHelperListener ()
                                    {
                                        @Override
                                        public void onWriteContents (Writer w)
                                        {
                                            try {
                                                TripManager.exportTrip (ctx,
                                                                        trip,
                                                                        getFileExtension (fileName),
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
                                        public void onSaveFileFails (int messageId)
                                        {
                                            listener.onExportFails (messageId);
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

        void onExportFails (int messageId);
    }
}

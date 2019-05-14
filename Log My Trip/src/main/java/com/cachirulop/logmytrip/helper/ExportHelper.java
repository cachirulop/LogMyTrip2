package com.cachirulop.logmytrip.helper;

import android.content.Context;
import android.os.Environment;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.manager.JourneyManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

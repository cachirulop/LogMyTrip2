package com.cachirulop.logmytrip.helper;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by dmagro on 15/10/2015.
 */
public class LogHelper
{
    public static final String LOG_CATEGORY = "com.cachirulop.LOG";

    private static final boolean LOG_TO_FILE = true;

    private static final String NEW_LINE =  System.getProperty("line.separator") ;
    final static         File   _logFile;

    static {
        _logFile = new File ("/sdcard/LogMyTrip/", "logs.log" );
        if (!_logFile.exists()) {
            try {
                _logFile.createNewFile();
            }
            catch (final IOException e) {
                e.printStackTrace();
            }
        }
        else {
            if (_logFile.length () > (10 * 1024 * 1024)) {
                try {
                    _logFile.delete ();
                    _logFile.createNewFile ();
                }
                catch (Exception e) {
                    e.printStackTrace ();
                }
            }
        }
    }

    public static void v (String msg)
    {
        fileLog (msg);
        Log.v (LOG_CATEGORY, msg);
    }

    public static void d (String msg)
    {
        fileLog (msg);
        Log.d (LOG_CATEGORY, msg);
    }

    public static void i (String msg)
    {
        fileLog (msg);
        Log.i (LOG_CATEGORY, msg);
    }

    public static void w (String msg)
    {
        fileLog (msg);
        Log.w (LOG_CATEGORY, msg);
    }

    public static void e (String msg)
    {
        fileLog (msg);
        Log.e (LOG_CATEGORY, msg);
    }

    public static void e (String msg, Throwable t)
    {
        fileLog (msg);
        Log.e (LOG_CATEGORY, msg, t);
    }

    public static void wtf (String msg)
    {
        fileLog (msg);
        Log.wtf (LOG_CATEGORY, msg);
    }

    public static void fileLog (String msg)
    {
        if (!LOG_TO_FILE) {
            return;
        }

        StackTraceElement [] stack;
        StringBuffer fullMsg;

        fullMsg = new StringBuffer ();
        fullMsg.append ("*** ");

        stack = Thread.currentThread ().getStackTrace ();
        if (stack != null && stack.length > 0) {
            boolean next;

            next = false;
            for (StackTraceElement elem : stack) {
                String method;

                method = String.format ("%s.%s", elem.getClassName (), elem.getMethodName ());
                if ((Logger.class.getCanonicalName () + ".log").equals (method)) {
                    next = true;
                }
                else if (next) {
                    fullMsg.append ("[").append (method).append ("]: ");
                    break;
                }
            }
        }

        fullMsg.append (msg);

        final SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");

        try {
            final FileWriter fileOut = new FileWriter (_logFile, true );
            fileOut.append ( sdf.format (new Date ()) + " : " + fullMsg.toString () + NEW_LINE);
            fileOut.close();
        }
        catch ( final IOException e ) {
            e.printStackTrace();
        }
    }
}

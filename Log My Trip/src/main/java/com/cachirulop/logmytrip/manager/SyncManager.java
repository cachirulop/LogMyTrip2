package com.cachirulop.logmytrip.manager;

import android.content.Context;

import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.entity.Location;
import com.cachirulop.logmytrip.helper.GoogleDriveHelper;
import com.cachirulop.logmytrip.helper.GoogleDriveHelperException;
import com.cachirulop.logmytrip.helper.LogHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;


/**
 * Created by dmagro on 11/12/2015.
 */
public class SyncManager
{
    public static void syncDatabase (final Context ctx)
    {
        Thread t;

        t = new Thread ()
        {
            public void run ()
            {
                doSync (ctx);
            }
        };

        t.start ();
    }

    private static void doSync (final Context ctx)
    {
        List<String>     remoteFiles;
        List<Journey> journeys;
        GoogleApiClient  client;
        ConnectionResult connResult;

        LogHelper.d ("*** Syncing with GDrive");

        try {
            client = GoogleDriveHelper.createClient (ctx, null, null);
            connResult = client.blockingConnect ();
            if (!connResult.isSuccess ()) {
                LogHelper.d ("*** Error connecting to google drive: " + connResult.getErrorMessage ());

                return;
            }

            remoteFiles = Arrays.asList (GoogleDriveHelper.listFolder (client,
                                                                       ctx,
                                                                       "Log My Journey/APP/sync"));
            journeys = JourneyManager.loadJourneys (ctx);

            syncLocal (ctx, client, remoteFiles, journeys);
            syncRemote (ctx, client, remoteFiles, journeys);

        }
        catch (GoogleDriveHelperException ex) {
            LogHelper.e ("*** Error accessing to google drive: " + ex.getLocalizedMessage ());
        }
    }

    private static void syncLocal (final Context ctx,
                                   final GoogleApiClient client,
                                   List<String> remoteFiles, List<Journey> journeys)
    {
        for (String file : remoteFiles) {
            if (!file.contains ("_locations")) {
                if (!findJourney (file, journeys)) {
                    // Only in remote
                    importJourney (file, journeys);
                }
                else {
                    // Remote and local, compare locations
                }
            }
        }
    }

    private static void syncRemote (final Context ctx,
                                    final GoogleApiClient client,
                                    List<String> remoteFiles, List<Journey> journeys)
    {
        final Gson generator;

        generator = new Gson ();

        for (final Journey t : journeys) {
            final List<Location> locations;
            final String fileName;

            fileName = getTripFileName (t);
            if (!remoteFiles.contains (fileName)) {
                locations = JourneyManager.createLocationList (ctx, t);

                GoogleDriveHelper.saveFile (client,
                                            ctx, "Log My Journey/APP/sync/" + fileName,
                                            t, new GoogleDriveHelper.IGoogleDriveWriterListener ()
                                            {
                                                @Override
                                                public void onWriteContents (Writer w)
                                                {
                                                    JsonWriter writer;

                                                    writer = new JsonWriter (w);
                                                    generator.toJson (t, Journey.class, writer);

                                                    try {
                                                        writer.close ();
                                                    }
                                                    catch (IOException e) {
                                                    }
                                                }

                                                @Override
                                                public void onSaveFileSuccess ()
                                                {
                                                    LogHelper.d ("*** save file successfully: " + fileName);
                                                }

                                                @Override
                                                public void onSaveFileFails (int messageId,
                                                                             Object... formatArgs)
                                                {
                                                    LogHelper.e ("*** Error saving file: " + fileName + ":" + ctx
                                                            .getString (messageId, formatArgs));
                                                }
                                            });

                GoogleDriveHelper.saveFile (client,
                                            ctx,
                                            "Log My Journey/APP/sync/" + getLocationsFileName (t),
                                            t, new GoogleDriveHelper.IGoogleDriveWriterListener ()
                                            {
                                                @Override
                                                public void onWriteContents (Writer w)
                                                {
                                                    JsonWriter writer;

                                                    try {
                                                        writer = new JsonWriter (w);
                                                        writer.beginArray ();

                                                        for (Location tl : locations) {
                                                            generator.toJson (tl, Location.class,
                                                                              writer);
                                                        }

                                                        writer.endArray ();

                                                        writer.close ();
                                                    }
                                                    catch (IOException e) {
                                                    }
                                                }

                                                @Override
                                                public void onSaveFileSuccess ()
                                                {
                                                    LogHelper.d ("*** save file successfully: " + fileName);
                                                }

                                                @Override
                                                public void onSaveFileFails (int messageId,
                                                                             Object... formatArgs)
                                                {
                                                    LogHelper.e ("*** Error saving file: " + fileName + ":" + ctx
                                                            .getString (messageId, formatArgs));
                                                }
                                            });

            }
        }
    }

    private static boolean findJourney (String name, List<Journey> journeys)
    {
        for (Journey t : journeys) {
            if (name.equals (getTripFileName (t))) {
                return true;
            }
        }

        return false;
    }

    private static void importJourney (String file, List<Journey> journeys)
    {
        // Read trip file
        // Read locations
        // Save trip
        // Save locations
        // Add new trip to trip list
    }

    private static String getTripFileName (Journey journey)
    {
        StringBuilder result;
        DateFormat    dfFileName;

        dfFileName = new SimpleDateFormat ("yyyy-MM-dd");

        result = new StringBuilder ();
        result.append (dfFileName.format (journey.getJouneyDate ())).append (".json");

        return result.toString ();
    }

    private static String getLocationsFileName (Journey journey)
    {
        StringBuilder result;
        DateFormat    dfFileName;

        dfFileName = new SimpleDateFormat ("yyyy-MM-dd");

        result = new StringBuilder ();
        result.append (dfFileName.format (journey.getJouneyDate ())).append ("_locations.json");

        return result.toString ();
    }
}

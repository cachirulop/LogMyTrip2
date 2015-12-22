package com.cachirulop.logmytrip.fragment;

import android.content.Context;

import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.helper.GoogleDriveHelper;
import com.cachirulop.logmytrip.helper.GoogleDriveHelperException;
import com.cachirulop.logmytrip.helper.LogHelper;
import com.cachirulop.logmytrip.manager.TripManager;
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
        List<Trip>       trips;
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
                                                                       "Log My Trip/APP/sync"));
            trips = TripManager.loadTrips (ctx);

            syncLocal (ctx, client, remoteFiles, trips);
            syncRemote (ctx, client, remoteFiles, trips);

        }
        catch (GoogleDriveHelperException ex) {
            LogHelper.e ("*** Error accessing to google drive: " + ex.getLocalizedMessage ());
        }
    }

    private static void syncLocal (final Context ctx,
                                   final GoogleApiClient client,
                                   List<String> remoteFiles,
                                   List<Trip> trips)
    {
        for (String file : remoteFiles) {
            if (!file.contains ("_locations")) {
                if (!findTrip (file, trips)) {
                    // Only in remote
                }
                else {
                    // Remote and local, compare locations
                }
            }
        }
    }

    private static void syncRemote (final Context ctx,
                                    final GoogleApiClient client,
                                    List<String> remoteFiles,
                                    List<Trip> trips)
    {
        final Gson generator;

        generator = new Gson ();

        for (final Trip t : trips) {
            final List<TripLocation> locations;
            final String fileName;

            fileName = getTripFileName (t);
            if (!remoteFiles.contains (fileName)) {
                locations = TripManager.createLocationList (ctx, t.getId ());

                GoogleDriveHelper.saveFile (client,
                                            ctx,
                                            "Log My Trip/APP/sync/" + fileName,
                                            t,
                                            new GoogleDriveHelper.IGoogleDriveHelperListener ()
                                            {
                                                @Override
                                                public void onWriteContents (Writer w)
                                                {
                                                    JsonWriter writer;

                                                    writer = new JsonWriter (w);
                                                    generator.toJson (t, Trip.class, writer);

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
                                                public void onSaveFileFails (int messageId)
                                                {
                                                    LogHelper.e ("*** Error saving file: " + fileName + ":" + ctx
                                                            .getString (messageId));
                                                }
                                            });

                GoogleDriveHelper.saveFile (client,
                                            ctx,
                                            "Log My Trip/APP/sync/" + getLocationsFileName (t),
                                            t,
                                            new GoogleDriveHelper.IGoogleDriveHelperListener ()
                                            {
                                                @Override
                                                public void onWriteContents (Writer w)
                                                {
                                                    JsonWriter writer;

                                                    try {
                                                        writer = new JsonWriter (w);
                                                        writer.beginArray ();

                                                        for (TripLocation tl : locations) {
                                                            generator.toJson (tl,
                                                                              TripLocation.class,
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
                                                public void onSaveFileFails (int messageId)
                                                {
                                                    LogHelper.e ("*** Error saving file: " + fileName + ":" + ctx
                                                            .getString (messageId));
                                                }
                                            });

            }
        }
    }

    private static boolean findTrip (String name, List<Trip> trips)
    {
        for (Trip t : trips) {
            if (name.equals (getTripFileName (t))) {
                return true;
            }
        }

        return false;
    }

    private static String getTripFileName (Trip trip)
    {
        StringBuilder result;
        DateFormat    dfFileName;

        dfFileName = new SimpleDateFormat ("yyyy-MM-dd");

        result = new StringBuilder ();
        result.append (dfFileName.format (trip.getTripDate ())).append (".json");

        return result.toString ();
    }

    private static String getLocationsFileName (Trip trip)
    {
        StringBuilder result;
        DateFormat    dfFileName;

        dfFileName = new SimpleDateFormat ("yyyy-MM-dd");

        result = new StringBuilder ();
        result.append (dfFileName.format (trip.getTripDate ())).append ("_locations.json");

        return result.toString ();
    }
}

package com.cachirulop.logmytrip.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cachirulop.logmytrip.LogMyTripApplication;
import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.data.LogMyTripDataHelper;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.util.FormatHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TripManager
{
    private static final String CONST_TRIP_TABLE_NAME = "trip";
    private static final String CONST_LOCATION_TABLE_NAME = "trip_location";

    public static TripLocation saveTripLocation (Context ctx, TripLocation tl)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            ContentValues values;

            values = new ContentValues ();

            values.put ("id_trip", tl.getIdTrip ());
            values.put ("location_time", tl.getLocationTime ());
            values.put ("latitude", tl.getLatitude ());
            values.put ("longitude", tl.getLongitude ());
            values.put ("altitude", tl.getAltitude ());
            values.put ("speed", tl.getSpeed ());
            values.put ("accuracy", tl.getAccuracy ());
            values.put ("bearing", tl.getBearing ());
            values.put ("provider", tl.getProvider ());

            db.insert (CONST_LOCATION_TABLE_NAME, null, values);

            tl.setId (getLastIdTripLocation (ctx));

            return tl;
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
    }

    private static long getLastIdTripLocation (Context ctx)
    {
        return new LogMyTripDataHelper (ctx).getLastId (CONST_LOCATION_TABLE_NAME);
    }

    public static List<Trip> LoadTrips (Context ctx)
    {
        SQLiteDatabase db = null;
        Cursor     c = null;
        List<Trip> result;

        result = new ArrayList<Trip> ();

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.rawQuery (ctx.getString (R.string.SQL_get_all_trips), null);

            if (c.moveToFirst ()) {
                while (!c.isAfterLast ()) {
                    result.add (createTripFromCursor (c));

                    c.moveToNext ();
                }
            }

            return result;
        }
        finally {
            if (c != null) {
                c.close ();
            }

            if (db != null) {
                db.close ();
            }
        }
    }

    private static Trip createTripFromCursor (Cursor c)
    {
        Trip result;

        result = new Trip ();

        result.setId (c.getLong (c.getColumnIndex ("id")));
        result.setTitle (c.getString (c.getColumnIndex ("title")));
        result.setDescription (c.getString (c.getColumnIndex ("description")));
        result.setTripDate (new Date (c.getLong (c.getColumnIndex ("trip_date"))));

        return result;
    }

    public static void deleteTrip (Context ctx, Trip t)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            db.delete (CONST_LOCATION_TABLE_NAME, "id_trip = ?",
                       new String[]{ Long.toString (t.getId ()) });

            db.delete (CONST_TRIP_TABLE_NAME, "id = ?", new String[]{ Long.toString (t.getId ()) });
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
    }

    public static void deleteSegment (Context ctx, TripSegment segment)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            for (TripLocation l : segment.getLocations ()) {
                db.delete (CONST_LOCATION_TABLE_NAME, "id = ?",
                           new String[]{ Long.toString (l.getId ()) });
            }
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
    }

    public static List<TripLocation> getTripLocationList (Trip trip)
    {
        Cursor c = null;
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (
                    LogMyTripApplication.getAppContext ()).getReadableDatabase ();

            c = db.query (CONST_LOCATION_TABLE_NAME, null, "id_trip = ?",
                          new String[]{ Long.toString (trip.getId ()) }, null, null,
                          "location_time ASC");

            return createLocationList (c);
        }
        finally {
            if (c != null) {
                c.close ();
            }

            if (db != null) {
                db.close ();
            }
        }
    }

    private static List<TripLocation> createLocationList (Cursor c)
    {
        ArrayList<TripLocation> result;

        result = new ArrayList<TripLocation> ();

        if (c != null) {
            if (c.moveToFirst ()) {
                do {
                    result.add (createTripLocation (c));
                }
                while (c.moveToNext ());
            }
        }

        return result;
    }

    private static TripLocation createTripLocation (Cursor c)
    {
        TripLocation result;

        result = new TripLocation ();

        result.setId (c.getLong (c.getColumnIndex ("id")));
        result.setIdTrip (c.getLong (c.getColumnIndex ("id_trip")));
        result.setLocationTime (c.getLong (c.getColumnIndex ("location_time")));
        result.setLatitude (c.getDouble (c.getColumnIndex ("latitude")));
        result.setLongitude (c.getDouble (c.getColumnIndex ("longitude")));
        result.setAltitude (c.getDouble (c.getColumnIndex ("altitude")));
        result.setSpeed (c.getFloat (c.getColumnIndex ("speed")));
        result.setAccuracy (c.getFloat (c.getColumnIndex ("accuracy")));
        result.setBearing (c.getFloat (c.getColumnIndex ("bearing")));
        result.setProvider (c.getString (c.getColumnIndex ("provider")));

        return result;
    }

    public static Trip getActiveTrip (Context ctx)
    {
        long current;

        current = SettingsManager.getCurrentTripId (ctx);
        if (current == 0) {
            return null;
        }
        else {
            return getTrip (ctx, current);
        }
    }

    public static Trip getTrip (Context ctx, long idTrip)
    {
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.query (CONST_TRIP_TABLE_NAME, null, "id = ?",
                          new String[]{ Long.toString (idTrip) }, null, null, null);

            if (c != null && c.moveToFirst ()) {
                return createTripFromCursor (c);
            }
            else {
                return null;
            }
        }
        finally {
            if (c != null) {
                c.close ();
            }

            if (db != null) {
                db.close ();
            }
        }
    }

    public static Trip startTrip (Context ctx)
    {
        Trip result;

        result = getTodayTrip (ctx);
        if (result == null) {
            result = createTodayTrip (ctx);
        }

        SettingsManager.setCurrentTripId (ctx, result.getId ());

        return result;
    }

    public static Trip getTodayTrip (Context ctx)
    {
        SQLiteDatabase db = null;
        Cursor         c  = null;

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.rawQuery (ctx.getString (R.string.SQL_get_last_active_trip), null);

            if (c != null && c.moveToFirst ()) {
                return createTripFromCursor (c);
            }
            else {
                return null;
            }
        }
        finally {
            if (c != null) {
                c.close ();
            }

            if (db != null) {
                db.close ();
            }
        }
    }

    private static Trip createTodayTrip (Context ctx)
    {
        Trip result;

        result = new Trip ();
        result.setTripDate (new Date ());
        result.setTitle (FormatHelper.formatDate (ctx, result.getTripDate ()));

        return insertTrip (ctx, result);
    }

    public static Trip insertTrip (Context ctx, Trip t)
    {
        return saveTrip (ctx, t, true);
    }

    private static Trip saveTrip (Context ctx, Trip t, boolean isInsert)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            ContentValues values;

            values = new ContentValues ();

            values.put ("trip_date", t.getTripDate ()
                                      .getTime ());
            values.put ("title", t.getTitle ());
            values.put ("description", t.getDescription ());

            if (isInsert) {
                db.insert (CONST_TRIP_TABLE_NAME, null, values);
            }
            else {
                db.update (CONST_TRIP_TABLE_NAME, values, "id = ?",
                           new String[]{ Long.toString (t.getId ()) });
            }

            t.setId (getLastIdTrip (ctx));

            return t;
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
    }

    /**
     * Gets the maximum identifier of the trips table
     *
     * @return the maximum trip identifier
     */
    private static long getLastIdTrip (Context ctx)
    {
        return new LogMyTripDataHelper (ctx).getLastId (CONST_TRIP_TABLE_NAME);
    }

    public static Trip updateTrip (Context ctx, Trip t)
    {
        return saveTrip (ctx, t, false);
    }

    public static void unsetActiveTrip (Context ctx)
    {
        SettingsManager.setCurrentTripId (ctx, 0);
    }

}


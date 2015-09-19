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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TripManager
{
    private static final String CONST_TRIP_TABLE_NAME = "trip";
    private static final String CONST_LOCATION_TABLE_NAME = "trip_location";

    public static Trip getCurrentTrip (Context ctx)
    {
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.rawQuery (ctx.getString (R.string.SQL_get_last_active_trip), null);

            if (c != null && c.moveToFirst ()) {
                return createTrip (c);
            }
            else {
                Trip result;

                result = new Trip ();
                result.setTripDate (new Date ());
                result.setDescription (getDefaultDescription (ctx, result.getTripDate ()));

                return saveTrip (ctx, result, true);
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

    private static Trip createTrip (Cursor c)
    {
        Trip result;

        result = new Trip ();

        result.setId (c.getLong (c.getColumnIndex ("id")));
        result.setDescription (c.getString (c.getColumnIndex ("description")));
        result.setTripDate (new Date (c.getLong (c.getColumnIndex ("trip_date"))));

        return result;
    }

    private static String getDefaultDescription (Context ctx, Date d)
    {
        DateFormat timeFormatter;
        DateFormat dateFormatter;

        timeFormatter = android.text.format.DateFormat.getTimeFormat (ctx);
        dateFormatter = android.text.format.DateFormat.getMediumDateFormat (ctx);

        return String.format ("[%s - %s]", dateFormatter.format (d), timeFormatter.format (d));
    }

    public static Trip saveTrip (Context ctx, Trip t, boolean isInsert)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            ContentValues values;

            values = new ContentValues ();

            values.put ("trip_date", t.getTripDate ()
                                      .getTime ());
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
                    result.add (createTrip (c));

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

    public static Trip getTrip (Context ctx, long idTrip)
    {
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.query (CONST_TRIP_TABLE_NAME, null, "id = ?",
                          new String[]{ Long.toString (idTrip) }, null, null, null);

            if (c != null && c.moveToFirst ()) {
                return createTrip (c);
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

    public static List<TripLocation> getTripLocationList (Trip trip)
    {
        Cursor c = null;
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (
                    LogMyTripApplication.getAppContext ()).getReadableDatabase ();

            c = db.query (CONST_LOCATION_TABLE_NAME, null, "id_trip = ?",
                          new String[]{ Long.toString (trip.getId ()) }, null, null, "id ASC");

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

        return result;
    }
}


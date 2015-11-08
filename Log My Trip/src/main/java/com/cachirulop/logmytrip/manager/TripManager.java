package com.cachirulop.logmytrip.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.data.LogMyTripDataHelper;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.util.FormatHelper;
import com.cachirulop.logmytrip.util.LogHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TripManager
{
    private static TripManager _instance = new TripManager ();

    private static final String CONST_TRIP_TABLE_NAME = "trip";
    private static final String CONST_LOCATION_TABLE_NAME = "trip_location";

    private ArrayList<Trip> _trips = null;

    private TripManager ()
    {
    }

    public synchronized static TripManager getInstance ()
    {
        return _instance;
    }

    public TripLocation saveTripLocation (Context ctx, TripLocation tl)
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

            Trip t;

            t = getTrip (tl.getIdTrip ());
            t.addLocation (tl);

            return tl;
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
    }

    public Trip getTrip (long id)
    {
        for (Trip t : _trips) {
            if (t.getId () == id) {
                return t;
            }
        }

        return null;
    }

    private long getLastIdTripLocation (Context ctx)
    {
        return new LogMyTripDataHelper (ctx).getLastId (CONST_LOCATION_TABLE_NAME);
    }

    public List<Trip> loadTrips (Context ctx)
    {
        if (_trips == null) {
            SQLiteDatabase db = null;
            Cursor c = null;

            _trips = new ArrayList<Trip> ();

            try {
                db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

                c = db.rawQuery (ctx.getString (R.string.SQL_get_all_trips), null);

                if (c.moveToFirst ()) {
                    while (!c.isAfterLast ()) {
                        _trips.add (createTripFromCursor (db, c));

                        c.moveToNext ();
                    }
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

        return _trips;
    }

    public List<Trip> reloadTrips (Context ctx)
    {
        _trips = null;

        return loadTrips (ctx);
    }


    private Trip createTripFromCursor (SQLiteDatabase db, Cursor c)
    {
        Trip result;

        LogHelper.d ("*** createTripFromCursor");

        result = new Trip ();

        result.setId (c.getLong (c.getColumnIndex ("id")));
        result.setTitle (c.getString (c.getColumnIndex ("title")));
        result.setDescription (c.getString (c.getColumnIndex ("description")));
        result.setTripDate (new Date (c.getLong (c.getColumnIndex ("trip_date"))));

        LogHelper.d ("*** createTripFromCursor loadTripSegments");

        loadTripSegments (db, result);

        LogHelper.d ("*** createTripFromCursor DONE");

        return result;
    }

    public void deleteTrip (Context ctx, Trip t)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            db.delete (CONST_LOCATION_TABLE_NAME, "id_trip = ?",
                       new String[]{ Long.toString (t.getId ()) });

            db.delete (CONST_TRIP_TABLE_NAME, "id = ?", new String[]{ Long.toString (t.getId ()) });

            _trips.remove (t);
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
    }

    public void deleteSegment (Context ctx, TripSegment segment)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            for (TripLocation l : segment.getLocations ()) {
                db.delete (CONST_LOCATION_TABLE_NAME, "id = ?",
                           new String[]{ Long.toString (l.getId ()) });
            }

            segment.getTrip ().removeSegment (segment);
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
    }

    private List<TripLocation> createLocationList (SQLiteDatabase db, Trip t)
    {
        ArrayList<TripLocation> result;
        Cursor c = null;

        c = db.query (CONST_LOCATION_TABLE_NAME,
                      null,
                      "id_trip = ?",
                      new String[]{ Long.toString (t.getId ()) },
                      null,
                      null,
                      "location_time ASC");

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

    private TripLocation createTripLocation (Cursor c)
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

    public Trip getActiveTrip (Context ctx)
    {
        long current;

        current = SettingsManager.getCurrentTripId (ctx);
        if (current == 0) {
            return null;
        }
        else {
            return getTrip (current);
        }
    }

    //    public Trip getTrip (Context ctx, long idTrip)
    //    {
    //        SQLiteDatabase db = null;
    //        Cursor c = null;
    //        Trip result = null;
    //
    //        try {
    //            LogHelper.d ("*** Loading trip");
    //            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();
    //
    //            c = db.query (CONST_TRIP_TABLE_NAME,
    //                          null,
    //                          "id = ?",
    //                          new String[]{ Long.toString (idTrip) },
    //                          null,
    //                          null,
    //                          null);
    //
    //            if (c != null && c.moveToFirst ()) {
    //                result = createTripFromCursor (db, c);
    //            }
    //
    //            LogHelper.d ("*** Loading trip DONE");
    //
    //            return result;
    //        }
    //        finally {
    //            if (c != null) {
    //                c.close ();
    //            }
    //
    //            if (db != null) {
    //                db.close ();
    //            }
    //        }
    //    }

    private void loadTripSegments (SQLiteDatabase db, Trip t)
    {
        List<TripSegment>  result;
        List<TripLocation> all;
        TripLocation       last;
        Calendar           cal;
        TripSegment        current = null;

        LogHelper.d ("*** loadTripSegments");

        cal = Calendar.getInstance ();

        result = new ArrayList<> ();

        all = createLocationList (db, t);
        last = null;

        LogHelper.d ("*** loadTripSegments processing segments");

        for (TripLocation l : all) {
            boolean newSegment;

            if (last == null) {
                newSegment = true;
            }
            else {
                cal.setTime (last.getLocationTimeAsDate ());
                cal.add (Calendar.HOUR, 2);

                newSegment = (l.getLocationTimeAsDate ().after (cal.getTime ()));
            }

            if (newSegment) {
                current = new TripSegment (t);
                current.getLocations ().add (l);

                result.add (current);
            }
            else {
                current.getLocations ().add (l);
            }

            last = l;
        }

        t.setSegments (result);

        LogHelper.d ("*** loadTripSegments DONE");
    }


    public Trip startTrip (Context ctx)
    {
        Trip result;

        result = getTodayTrip ();
        if (result == null) {
            result = createTodayTrip (ctx);
        }

        SettingsManager.setCurrentTripId (ctx, result.getId ());

        return result;
    }

    public Trip getTodayTrip ()
    {
        Date today;

        today = Calendar.getInstance ().getTime ();

        for (Trip t : _trips) {
            if (isSameDay (t.getTripDate (), today)) {
                return t;
            }
        }

        return null;
    }

    private static boolean isSameDay (Date date1, Date date2)
    {
        if (date1 == null && date2 == null) {
            return true;
        }

        if (date1 == null || date2 == null) {
            return false;
        }

        SimpleDateFormat sdf;

        sdf = new SimpleDateFormat ("yyyyMMdd");

        return sdf.format (date1).equals (sdf.format (date2));
    }

    //    public static Trip getTodayTrip (Context ctx)
    //    {
    //        SQLiteDatabase db = null;
    //        Cursor         c  = null;
    //
    //        try {
    //            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();
    //
    //            c = db.rawQuery (ctx.getString (R.string.SQL_get_last_active_trip), null);
    //
    //            if (c != null && c.moveToFirst ()) {
    //                return createTripFromCursor (db, c);
    //            }
    //            else {
    //                return null;
    //            }
    //        }
    //        finally {
    //            if (c != null) {
    //                c.close ();
    //            }
    //
    //            if (db != null) {
    //                db.close ();
    //            }
    //        }
    //    }

    private Trip createTodayTrip (Context ctx)
    {
        Trip result;

        result = new Trip ();
        result.setTripDate (new Date ());
        result.setTitle (FormatHelper.formatDate (ctx, result.getTripDate ()));
        result.setSegments (new ArrayList<TripSegment> ());

        return insertTrip (ctx, result);
    }

    public Trip insertTrip (Context ctx, Trip t)
    {
        return saveTrip (ctx, t, true);
    }

    private Trip saveTrip (Context ctx, Trip t, boolean isInsert)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            ContentValues values;

            values = new ContentValues ();

            values.put ("trip_date", t.getTripDate ().getTime ());
            values.put ("title", t.getTitle ());
            values.put ("description", t.getDescription ());

            if (isInsert) {
                db.insert (CONST_TRIP_TABLE_NAME, null, values);
                _trips.add (0, t);
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
    private long getLastIdTrip (Context ctx)
    {
        return new LogMyTripDataHelper (ctx).getLastId (CONST_TRIP_TABLE_NAME);
    }

    public Trip updateTrip (Context ctx, Trip t)
    {
        return saveTrip (ctx, t, false);
    }

    public void unsetActiveTrip (Context ctx)
    {
        SettingsManager.setCurrentTripId (ctx, 0);
    }
}


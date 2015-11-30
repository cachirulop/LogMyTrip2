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
import com.cachirulop.logmytrip.helper.FormatHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    public static List<Trip> loadTrips (Context ctx)
    {
        SQLiteDatabase db = null;
        Cursor         c  = null;
        List<Trip>     result;

        result = new ArrayList<Trip> ();

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.rawQuery (ctx.getString (R.string.SQL_get_all_trips), null);

            if (c.moveToFirst ()) {
                while (!c.isAfterLast ()) {
                    result.add (createTripFromCursor (db, c));

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

    private static Trip createTripFromCursor (SQLiteDatabase db, Cursor c)
    {
        Trip result;

        result = new Trip ();

        result.setId (c.getLong (c.getColumnIndex ("id")));
        result.setTitle (c.getString (c.getColumnIndex ("title")));
        result.setDescription (c.getString (c.getColumnIndex ("description")));
        result.setTripDate (new Date (c.getLong (c.getColumnIndex ("trip_date"))));

        loadTripSegments (db, result);

        return result;
    }

    private static void loadTripSegments (SQLiteDatabase db, Trip t)
    {
        List<TripSegment>  result;
        List<TripLocation> all;
        TripLocation       last;
        Calendar           cal;
        TripSegment        current = null;

        cal = Calendar.getInstance ();

        result = new ArrayList<> ();

        all = createLocationList (db, t);
        last = null;
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
    }

    private static List<TripLocation> createLocationList (SQLiteDatabase db, Trip t)
    {
        ArrayList<TripLocation> result;
        Cursor                  c = null;

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

    public static void deleteTrip (Context ctx, Trip t)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            db.delete (CONST_LOCATION_TABLE_NAME,
                       "id_trip = ?",
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
                db.delete (CONST_LOCATION_TABLE_NAME,
                           "id = ?",
                           new String[]{ Long.toString (l.getId ()) });
            }
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
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
        Cursor         c  = null;

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.query (CONST_TRIP_TABLE_NAME,
                          null,
                          "id = ?",
                          new String[]{ Long.toString (idTrip) },
                          null,
                          null,
                          null);

            if (c != null && c.moveToFirst ()) {
                return createTripFromCursor (db, c);
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
                return createTripFromCursor (db, c);
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

            values.put ("trip_date", t.getTripDate ().getTime ());
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

    public static String generateGPX (Context ctx, Trip t)
    {
        DateFormat    df;
        StringBuilder result;

        result = new StringBuilder ();

        result.append ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n\n");
        result.append ("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\"");
        result.append (" creator=\"LogMyTrip 1.0.0\" version=\"1.1\"");
        result.append (" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        result.append (" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1");
        result.append (" http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");


        result.append ("<metadata>\n");

        result.append ("<name><![CDATA[").append (t.getTitle ()).append ("]]></name>\n");
        if (t.getDescription () != null) {
            result.append ("<description><![CDATA[");
            result.append (t.getDescription ());
            result.append ("]]></description>\n");
        }

        result.append ("</metadata>\n");

        df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ssZ");

        for (TripSegment s : t.getSegments ()) {
            result.append ("<trk>\n");
            result.append ("<name><![CDATA[").append (s.getTitle (ctx)).append ("]]></name>\n");

            result.append ("<trkseg>\n");

            for (TripLocation l : s.getLocations ()) {
                result.append ("<trkpt");
                result.append (" lat=\"").append (l.getLatitude ()).append ("\"");
                result.append (" lon=\"").append (l.getLongitude ()).append ("\"");
                result.append (">\n");

                result.append ("<ele>").append (l.getAltitude ()).append ("</ele>\n");
                result.append ("<time>").append (df.format (l.getLocationTimeAsDate ()));
                result.append ("</time>\n");
                result.append ("<magvar>").append (l.getBearing ()).append ("</magvar>\n");

                result.append ("</trkpt>\n");
            }

            result.append ("</trkseg>\n");
            result.append ("</trk>\n");
        }

        result.append ("</gpx>\n");

        return result.toString ();
    }

    public static String generateKML (Context ctx, Trip t)
    {
        DateFormat    df;
        StringBuilder result;

        result = new StringBuilder ();
        df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ssZ");

        result.append ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n\n");
        result.append ("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");

        result.append ("<Document>\n");
        result.append ("<open>1</open>\n");
        result.append ("<visibility>1</visibility>\n");
        result.append (
                "<atom:author><atom:name><![CDATA[Android Log My Trip]]></atom:name></atom:author>\n");
        result.append ("<name><![CDATA[").append (t.getTitle ()).append ("]]></name>\n");
        if (t.getDescription () != null) {
            result.append ("<description><![CDATA[");
            result.append (t.getDescription ());
            result.append ("]]></description>\n");
        }

        // Start Placemark
        result.append ("<Placemark>\n");
        result.append ("<name><![CDATA[")
              .append (t.getTitle ())
              .append (" - ")
              .append ("</name>\n");
        result.append (ctx.getString (R.string.text_start)).append ("]]>\n");

        if (t.getDescription () != null) {
            result.append ("<description><![CDATA[");
            result.append (t.getDescription ());
            result.append ("]]></description>\n");
        }

        result.append ("<TimeStamp><when>");
        result.append (df.format (t.getStartLocation ().getLocationTimeAsDate ()));
        result.append ("</when></TimeStamp>\n");
        result.append ("<Point>");
        result.append ("<coordinates>");
        result.append (t.getStartLocation ().getLatitude ()).append (",");
        result.append (t.getStartLocation ().getLongitude ()).append (",");
        result.append (t.getStartLocation ().getAltitude ());
        result.append ("</coordinates>\n");
        result.append ("</Point>\n");
        result.append ("</Placemark>\n");

        // Tracks
        result.append ("<Placemark>\n");
        result.append ("<gx:MultiTrack>\n");
        result.append ("<altitudeMode>absolute</altitudeMode>\n");
        result.append ("<gx:interpolate>1</gx:interpolate>\n");

        for (TripSegment s : t.getSegments ()) {
            StringBuffer speed;
            StringBuffer bearing;
            StringBuffer accuracy;

            speed = new StringBuffer ();
            bearing = new StringBuffer ();
            accuracy = new StringBuffer ();

            result.append ("<gx:Track>\n");

            speed.append ("<gx:SimpleArrayData name=\"speed\">\n");
            bearing.append ("<gx:SimpleArrayData name=\"bearing\">\n");
            accuracy.append ("<gx:SimpleArrayData name=\"accuracy\">\n");

            for (TripLocation l : s.getLocations ()) {
                result.append ("<when>");
                result.append (df.format (l.getLocationTimeAsDate ()));
                result.append ("</when>\n");

                result.append ("<gx:coord>");
                result.append (l.getLatitude ()).append (" ");
                result.append (l.getLongitude ()).append (" ");
                result.append (l.getAltitude ());
                result.append ("</gx:coord>\n");

                speed.append ("<gx:value>").append (l.getSpeed ()).append ("</gx:value>\n");
                bearing.append ("<gx:value>").append (l.getBearing ()).append ("</gx:value>\n");
                accuracy.append ("<gx:value>").append (l.getAccuracy ()).append ("</gx:value>\n");
            }

            speed.append ("</gx:SimpleArrayData>\n");
            bearing.append ("</gx:SimpleArrayData>\n");
            accuracy.append ("</gx:SimpleArrayData>\n");

            result.append ("<ExtendedData>\n");
            result.append ("<SchemaData schemaUrl=\"#schema\">\n");
            result.append (speed.toString ());
            result.append (bearing.toString ());
            result.append (accuracy.toString ());
            result.append ("</SchemaData>\n");
            result.append ("</ExtendedData>\n");
            result.append ("</gx:Track>\n");
        }

        result.append ("</gx:MultiTrack>\n");
        result.append ("</Placemark>\n");

        // End Placemark
        result.append ("<Placemark>\n");
        result.append ("<name><![CDATA[")
              .append (t.getTitle ())
              .append (" - ")
              .append ("</name>\n");
        result.append (ctx.getString (R.string.text_end)).append ("]]>\n");

        if (t.getDescription () != null) {
            result.append ("<description><![CDATA[");
            result.append (t.getDescription ());
            result.append ("]]></description>\n");
        }

        result.append ("<TimeStamp><when>");
        result.append (df.format (t.getEndLocation ().getLocationTimeAsDate ()));
        result.append ("</when></TimeStamp>\n");
        result.append ("<Point>");
        result.append ("<coordinates>");
        result.append (t.getEndLocation ().getLatitude ()).append (",");
        result.append (t.getEndLocation ().getLongitude ()).append (",");
        result.append (t.getEndLocation ().getAltitude ());
        result.append ("</coordinates>\n");
        result.append ("</Point>\n");
        result.append ("</Placemark>\n");

        result.append ("</Document>\n");
        result.append ("</kml>\n");

        return result.toString ();
    }
}


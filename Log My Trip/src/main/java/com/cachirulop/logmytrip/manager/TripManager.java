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

import java.io.IOException;
import java.io.Writer;
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

    private static ArrayList<TripLocation> _tripLocationsPool = new ArrayList<> ();

    public static TripLocation saveTripLocation (Context ctx, TripLocation tl)
    {
        synchronized (_tripLocationsPool) {
            _tripLocationsPool.add (tl);
        }

        return tl;
    }

    public static void flushLocations (Context ctx)
    {
        synchronized (_tripLocationsPool) {
            for (TripLocation tl : _tripLocationsPool) {
                insertTripLocation (ctx, tl);
            }

            _tripLocationsPool.clear ();
        }
    }

    public static void mergePendingLocations (Trip trip)
    {
        synchronized (_tripLocationsPool) {
            for (TripLocation tl : _tripLocationsPool) {
                trip.addLocation (tl);
            }
        }
    }


    public static TripLocation insertTripLocation (Context ctx, TripLocation tl)
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
        result.setTotalTime (c.getLong (c.getColumnIndex ("total_time")));
        result.setTotalDistance (c.getDouble (c.getColumnIndex ("total_distance")));

        //loadTripSegments (db, result);

        return result;
    }

    public static void loadTripSegments (Context ctx, Trip trip)
    {
        List<TripSegment>  result;
        List<TripLocation> all;
        TripLocation       last;
        Calendar           cal;
        TripSegment        current = null;

        cal = Calendar.getInstance ();

        result = new ArrayList<> ();

        all = createLocationList (ctx, trip.getId ());
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
                current = new TripSegment (trip);
                current.getLocations ().add (l);

                result.add (current);
            }
            else {
                current.getLocations ().add (l);
            }

            last = l;
        }

        trip.setSegments (result);
    }

    private static List<TripLocation> createLocationList (Context ctx, Long tripId)
    {
        SQLiteDatabase db = null;
        ArrayList<TripLocation> result;
        Cursor                  c = null;


        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();
            c = db.query (CONST_LOCATION_TABLE_NAME,
                          null,
                          "id_trip = ?",
                          new String[]{ Long.toString (tripId) },
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
        finally {
            if (c != null) {
                c.close ();
            }

            if (db != null) {
                db.close ();
            }
        }
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
            Trip result;

            result = getTrip (ctx, current);

            return result;
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
            values.put ("total_time", t.getTotalTime ());
            values.put ("total_distance", t.getTotalDistance ());

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

    public static void updateTripStatistics (Context ctx, Trip t)
    {
        t.computeTotalTime (ctx);
        t.computeTotalDistance (ctx);

        updateTrip (ctx, t);
    }

    public static void unsetActiveTrip (Context ctx)
    {
        SettingsManager.setCurrentTripId (ctx, 0);
    }

    public static void exportTrip (Context ctx, Trip t, String format, Writer writer)
            throws IOException
    {
        TripManager.loadTripSegments (ctx, t);

        if (format.toUpperCase ().equals ("GPX")) {
            exportTripToGPX (ctx, t, writer);
        }
        else {
            exportTripToKML (ctx, t, writer);
        }
    }

    private static void exportTripToGPX (Context ctx, Trip t, Writer writer)
            throws IOException
    {
        DateFormat    df;

        writer.write ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n\n");
        writer.write ("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\"");
        writer.write (" creator=\"LogMyTrip 1.0.0\" version=\"1.1\"");
        writer.write (" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        writer.write (" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1");
        writer.write (" http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");

        writer.write ("<metadata>\n");

        writer.write ("<name><![CDATA[");
        writer.write (t.getTitle ());
        writer.write ("]]></name>\n");
        if (t.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (t.getDescription ());
            writer.write ("]]></description>\n");
        }

        writer.write ("</metadata>\n");

        df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss'Z'");

        for (TripSegment s : t.getSegments ()) {
            writer.write ("<trk>\n");
            writer.write ("<name><![CDATA[");
            writer.write (s.getTitle (ctx));
            writer.write ("]]></name>\n");

            writer.write ("<trkseg>\n");

            for (TripLocation l : s.getLocations ()) {
                writer.write ("<trkpt");
                writer.write (" lat=\"");
                writer.write (Double.toString (l.getLatitude ()));
                writer.write ("\"");
                writer.write (" lon=\"");
                writer.write (Double.toString (l.getLongitude ()));
                writer.write ("\"");
                writer.write (">\n");

                writer.write ("<ele>");
                writer.write (Double.toString (l.getAltitude ()));
                writer.write ("</ele>\n");
                writer.write ("<time>");
                writer.write (df.format (l.getLocationTimeAsDate ()));
                writer.write ("</time>\n");
                writer.write ("<magvar>");
                writer.write (Double.toString (l.getBearing ()));
                writer.write ("</magvar>\n");

                writer.write ("</trkpt>\n");
            }

            writer.write ("</trkseg>\n");
            writer.write ("</trk>\n");
        }

        writer.write ("</gpx>\n");
    }

    private static void exportTripToKML (Context ctx, Trip t, Writer writer)
            throws IOException
    {
        DateFormat    df;
        StringBuilder result;

        result = new StringBuilder ();
        df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss'Z'");

        writer.write ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
        writer.write (
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n");

        writer.write ("<Document>\n");
        writer.write ("<open>1</open>\n");
        writer.write ("<visibility>1</visibility>\n");

        writer.write ("<atom:author><atom:name><![CDATA[Android ");
        writer.write (ctx.getString (R.string.app_name));
        writer.write ("]]></atom:name></atom:author>\n");

        writer.write ("<Style id=\"track\">\n");
        writer.write ("<LineStyle>\n");
        writer.write ("<color>7f0000ff</color>\n");
        writer.write ("<width>4</width>\n");
        writer.write ("</LineStyle>\n");
        writer.write ("<IconStyle>\n");
        writer.write ("<scale>1.3</scale>\n");
        writer.write ("<Icon>\n");
        writer.write (
                "<href>http://earth.google.com/images/kml-icons/track-directional/track-0.png</href>\n");
        writer.write ("</Icon>\n");
        writer.write ("</IconStyle>\n");
        writer.write ("</Style>\n");
        writer.write ("<Style id=\"start\">\n");
        writer.write ("<IconStyle>\n");
        writer.write ("<scale>1.3</scale>\n");
        writer.write ("<Icon>\n");
        writer.write ("<href>http://maps.google.com/mapfiles/kml/paddle/grn-circle.png</href>\n");
        writer.write ("</Icon>\n");
        writer.write ("<hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\" />\n");
        writer.write ("</IconStyle>\n");
        writer.write ("</Style>\n");
        writer.write ("<Style id=\"end\">\n");
        writer.write ("<IconStyle>\n");
        writer.write ("<scale>1.3</scale>\n");
        writer.write ("<Icon>\n");
        writer.write ("<href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href>\n");
        writer.write ("</Icon>\n");
        writer.write ("<hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\" />\n");
        writer.write ("</IconStyle>\n");
        writer.write ("</Style>\n");

        writer.write ("<name><![CDATA[");
        writer.write (t.getTitle ());
        writer.write ("]]></name>\n");
        if (t.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (t.getDescription ());
            writer.write ("]]></description>\n");
        }

        // Start Placemark
        writer.write ("<Placemark>\n");
        writer.write ("<name><![CDATA[");
        writer.write (t.getTitle ());
        writer.write (" - ");
        writer.write (ctx.getString (R.string.text_start));
        writer.write ("]]>");
        writer.write ("</name>\n");

        if (t.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (t.getDescription ());
            writer.write ("]]></description>\n");
        }

        writer.write ("<TimeStamp><when>");
        writer.write (df.format (t.getStartLocation ().getLocationTimeAsDate ()));
        writer.write ("</when></TimeStamp>\n");
        writer.write ("<styleUrl>#start</styleUrl>\n");
        writer.write ("<Point>\n");
        writer.write ("<coordinates>");
        writer.write (Double.toString (t.getStartLocation ().getLongitude ()));
        writer.write (",");
        writer.write (Double.toString (t.getStartLocation ().getLatitude ()));
        writer.write (",");
        writer.write (Double.toString (t.getStartLocation ().getAltitude ()));
        writer.write ("</coordinates>\n");
        writer.write ("</Point>\n");
        writer.write ("</Placemark>\n");

        // Tracks
        writer.write ("<Placemark id=\"tour\">\n");
        writer.write ("<styleUrl>#track</styleUrl>\n");
        writer.write ("<name><![CDATA[");
        writer.write (t.getTitle ());
        writer.write ("]]>");
        writer.write ("</name>\n");

        if (t.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (t.getDescription ());
            writer.write ("]]></description>\n");
        }

        writer.write ("<gx:MultiTrack>\n");
        writer.write ("<altitudeMode>absolute</altitudeMode>\n");
        writer.write ("<gx:interpolate>1</gx:interpolate>\n");

        for (TripSegment s : t.getSegments ()) {
            StringBuffer speed;
            StringBuffer bearing;
            StringBuffer accuracy;

            speed = new StringBuffer ();
            bearing = new StringBuffer ();
            accuracy = new StringBuffer ();

            writer.write ("<gx:Track>\n");

            speed.append ("<gx:SimpleArrayData name=\"speed\">\n");
            bearing.append ("<gx:SimpleArrayData name=\"bearing\">\n");
            accuracy.append ("<gx:SimpleArrayData name=\"accuracy\">\n");

            for (TripLocation l : s.getLocations ()) {
                writer.write ("<when>");
                writer.write (df.format (l.getLocationTimeAsDate ()));
                writer.write ("</when>\n");

                writer.write ("<gx:coord>");
                writer.write (Double.toString (l.getLongitude ()));
                writer.write (" ");
                writer.write (Double.toString (l.getLatitude ()));
                writer.write (" ");
                writer.write (Double.toString (l.getAltitude ()));
                writer.write ("</gx:coord>\n");

                speed.append ("<gx:value>").append (l.getSpeed ()).append ("</gx:value>\n");
                bearing.append ("<gx:value>").append (l.getBearing ()).append ("</gx:value>\n");
                accuracy.append ("<gx:value>").append (l.getAccuracy ()).append ("</gx:value>\n");
            }

            speed.append ("</gx:SimpleArrayData>\n");
            bearing.append ("</gx:SimpleArrayData>\n");
            accuracy.append ("</gx:SimpleArrayData>\n");

            writer.write ("<ExtendedData>\n");
            writer.write ("<SchemaData schemaUrl=\"#schema\">\n");
            writer.write (speed.toString ());
            writer.write (bearing.toString ());
            writer.write (accuracy.toString ());
            writer.write ("</SchemaData>\n");
            writer.write ("</ExtendedData>\n");
            writer.write ("</gx:Track>\n");
        }

        writer.write ("</gx:MultiTrack>\n");
        writer.write ("</Placemark>\n");

        // End Placemark
        writer.write ("<Placemark>\n");
        writer.write ("<name><![CDATA[");
        writer.write (t.getTitle ());
        writer.write (" - ");
        writer.write (ctx.getString (R.string.text_end));
        writer.write ("]]>");
        writer.write ("</name>\n");

        if (t.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (t.getDescription ());
            writer.write ("]]></description>\n");
        }

        writer.write ("<TimeStamp><when>");
        writer.write (df.format (t.getEndLocation ().getLocationTimeAsDate ()));
        writer.write ("</when></TimeStamp>\n");
        writer.write ("<styleUrl>#end</styleUrl>\n");
        writer.write ("<Point>\n");
        writer.write ("<coordinates>");
        writer.write (Double.toString (t.getEndLocation ().getLongitude ()));
        writer.write (",");
        writer.write (Double.toString (t.getEndLocation ().getLatitude ()));
        writer.write (",");
        writer.write (Double.toString (t.getEndLocation ().getAltitude ()));
        writer.write ("</coordinates>\n");
        writer.write ("</Point>\n");
        writer.write ("</Placemark>\n");

        writer.write ("</Document>\n");
        writer.write ("</kml>\n");
    }
}


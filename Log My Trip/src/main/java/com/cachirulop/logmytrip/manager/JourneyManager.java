package com.cachirulop.logmytrip.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.data.LogMyTripDataHelper;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.entity.JourneySegment;
import com.cachirulop.logmytrip.entity.Location;
import com.cachirulop.logmytrip.helper.FormatHelper;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class JourneyManager
{
    private static final String CONST_JOURNEY_TABLE_NAME  = "journey";
    private static final String CONST_LOCATION_TABLE_NAME = "location";

    private static ArrayList<Location> _LocationsPool = new ArrayList<> ();

    public static Location saveLocation (Context ctx, Location tl)
    {
        synchronized (_LocationsPool) {
            _LocationsPool.add (tl);
        }

        return tl;
    }

    public static void flushLocations (Context ctx)
    {
        synchronized (_LocationsPool) {
            SQLiteDatabase db = null;

            try {
                db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

                for (Location tl : _LocationsPool) {
                    insertLocation (ctx, db, tl);
                }

                _LocationsPool.clear ();
            }
            finally {
                if (db != null) {
                    db.close ();
                }
            }
        }
    }

    private static Location insertLocation (Context ctx, SQLiteDatabase db, Location tl)
    {
        ContentValues values;

        values = new ContentValues ();

        values.put ("location_time", tl.getLocationTime ());
        values.put ("latitude", tl.getLatitude ());
        values.put ("longitude", tl.getLongitude ());
        values.put ("altitude", tl.getAltitude ());
        values.put ("speed", tl.getSpeed ());
        values.put ("accuracy", tl.getAccuracy ());
        values.put ("bearing", tl.getBearing ());
        values.put ("provider", tl.getProvider ());

        db.insert (CONST_LOCATION_TABLE_NAME, null, values);

        tl.setId (getLastLocationId (db, ctx));

        return tl;
    }

    private static long getLastLocationId (SQLiteDatabase db, Context ctx)
    {
        return new LogMyTripDataHelper (ctx).getLastId (db, CONST_LOCATION_TABLE_NAME);
    }

    public static void mergePendingLocations (Journey journey)
    {
        synchronized (_LocationsPool) {
            for (Location tl : _LocationsPool) {
                journey.addLocation (tl);
            }
        }
    }

    public static List<Journey> loadJourneys (Context ctx)
    {
        SQLiteDatabase db = null;
        Cursor         c  = null;
        List<Journey> result;

        result = new ArrayList<Journey> ();

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.rawQuery (ctx.getString (R.string.SQL_get_all_journeys), null);

            if (c.moveToFirst ()) {
                while (!c.isAfterLast ()) {
                    result.add (createJourneyFromCursor (db, c));

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

    private static Journey createJourneyFromCursor (SQLiteDatabase db, Cursor c)
    {
        Journey result;

        result = new Journey ();

        result.setId (c.getLong (c.getColumnIndex ("id")));
        result.setTitle (c.getString (c.getColumnIndex ("title")));
        result.setDescription (c.getString (c.getColumnIndex ("description")));
        result.setJouneyDate (new Date (c.getLong (c.getColumnIndex ("journey_date"))));
        result.setTotalTime (c.getLong (c.getColumnIndex ("total_time")));
        result.setTotalDistance (c.getDouble (c.getColumnIndex ("total_distance")));

        return result;
    }

    public static void deleteJourney (Context ctx, Journey j)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            db.execSQL (ctx.getString (R.string.SQL_delete_journey_locations),
                        new Object[]{ j.getJouneyDate () });

            db.delete (CONST_JOURNEY_TABLE_NAME,
                       "id = ?",
                       new String[]{ Long.toString (j.getId ()) });
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
    }

    public static void deleteSegment (Context ctx, JourneySegment segment)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            for (Location l : segment.getLocations ()) {
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

        JourneyManager.updateJourneyStatistics (ctx, segment.getJourney ());
    }

    public static void updateJourneyStatistics (Context ctx, Journey j)
    {
        j.computeTotalTime (ctx);
        j.computeTotalDistance (ctx);

        updateJourney (ctx, j);
    }

    public static Journey updateJourney (Context ctx, Journey j)
    {
        return saveJourney (ctx, j, false);
    }

    private static Journey saveJourney (Context ctx, Journey j, boolean isInsert)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            ContentValues values;

            values = new ContentValues ();

            values.put ("journey_date", j.getJouneyDate ().getTime ());
            values.put ("title", j.getTitle ());
            values.put ("description", j.getDescription ());
            values.put ("total_time", j.getTotalTime ());
            values.put ("total_distance", j.getTotalDistance ());

            if (isInsert) {
                db.insert (CONST_JOURNEY_TABLE_NAME, null, values);
            }
            else {
                db.update (CONST_JOURNEY_TABLE_NAME,
                           values,
                           "id = ?", new String[]{ Long.toString (j.getId ()) });
            }

            j.setId (getLastJourneyId (db, ctx));

            return j;
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }
    }

    /**
     * Gets the maximum identifier of the journeys table
     *
     * @return the maximum journey identifier
     */
    private static long getLastJourneyId (SQLiteDatabase db, Context ctx)
    {
        return new LogMyTripDataHelper (ctx).getLastId (db, CONST_JOURNEY_TABLE_NAME);
    }

    public static Journey getActiveJourney (Context ctx)
    {
        long current;

        current = SettingsManager.getCurrentJourneyId (ctx);
        if (current == 0) {
            return null;
        }
        else {
            Journey result;

            result = getJourney (ctx, current);

            return result;
        }
    }

    public static Journey getJourney (Context ctx, long idJourney)
    {
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.query (CONST_JOURNEY_TABLE_NAME,
                          null,
                          "id = ?", new String[]{ Long.toString (idJourney) },
                          null,
                          null,
                          null);

            if (c != null && c.moveToFirst ()) {
                return createJourneyFromCursor (db, c);
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

    public static Journey startJourney (Context ctx)
    {
        Journey result;

        result = getTodayJourney (ctx);
        if (result == null) {
            result = createTodayJourney (ctx);
        }

        SettingsManager.setCurrentJourneyId (ctx, result.getId ());

        return result;
    }

    public static Journey getTodayJourney (Context ctx)
    {
        SQLiteDatabase db = null;
        Cursor         c  = null;

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            c = db.rawQuery (ctx.getString (R.string.SQL_get_last_active_journey), null);

            if (c != null && c.moveToFirst ()) {
                return createJourneyFromCursor (db, c);
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

    private static Journey createTodayJourney (Context ctx)
    {
        Journey result;

        result = new Journey ();
        result.setJouneyDate (new Date ());
        result.setTitle (FormatHelper.formatDate (ctx, result.getJouneyDate ()));

        return insertJourney (ctx, result);
    }

    public static Journey insertJourney (Context ctx, Journey j)
    {
        return saveJourney (ctx, j, true);
    }

    public static void unsetActiveJourney (Context ctx)
    {
        SettingsManager.setCurrentJourneyId (ctx, 0);
    }

    public static void exportJourney (Context ctx, Journey j, String format, Writer writer)
            throws IOException
    {
        JourneyManager.loadJourneySegments (ctx, j);

        if (format.toUpperCase ().equals ("GPX")) {
            exportJourneyToGPX (ctx, j, writer);
        }
        else {
            exportJourneyToKML (ctx, j, writer);
        }
    }

    public static void loadJourneySegments (Context ctx, Journey journey)
    {
        List<JourneySegment> result;
        List<Location>       all;
        Location             last;
        Calendar           cal;
        JourneySegment       current = null;

        cal = Calendar.getInstance ();

        result = new ArrayList<> ();

        all = createLocationList (ctx, journey);
        last = null;
        for (Location l : all) {
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
                current = new JourneySegment (journey);
                current.getLocations ().add (l);

                result.add (current);
            }
            else {
                current.getLocations ().add (l);
            }

            last = l;
        }

        journey.setSegments (result);
    }

    private static void exportJourneyToGPX (Context ctx, Journey journey, Writer writer)
            throws IOException
    {
        DateFormat df;

        writer.write ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n\n");
        writer.write ("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\"");
        writer.write (" creator=\"LogMyTrip 1.0.0\" version=\"1.1\"");
        writer.write (" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        writer.write (" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1");
        writer.write (" http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");

        writer.write ("<metadata>\n");

        writer.write ("<name><![CDATA[");
        writer.write (journey.getTitle ());
        writer.write ("]]></name>\n");
        if (journey.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (journey.getDescription ());
            writer.write ("]]></description>\n");
        }

        writer.write ("</metadata>\n");

        df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss'Z'");

        for (JourneySegment s : journey.getSegments ()) {
            writer.write ("<trk>\n");
            writer.write ("<name><![CDATA[");
            writer.write (s.getTitle (ctx));
            writer.write ("]]></name>\n");

            writer.write ("<trkseg>\n");

            for (Location l : s.getLocations ()) {
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

    private static void exportJourneyToKML (Context ctx, Journey journey, Writer writer)
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
        writer.write (journey.getTitle ());
        writer.write ("]]></name>\n");
        if (journey.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (journey.getDescription ());
            writer.write ("]]></description>\n");
        }

        // Start Placemark
        writer.write ("<Placemark>\n");
        writer.write ("<name><![CDATA[");
        writer.write (journey.getTitle ());
        writer.write (" - ");
        writer.write (ctx.getString (R.string.text_start));
        writer.write ("]]>");
        writer.write ("</name>\n");

        if (journey.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (journey.getDescription ());
            writer.write ("]]></description>\n");
        }

        writer.write ("<TimeStamp><when>");
        writer.write (df.format (journey.getStartLocation ().getLocationTimeAsDate ()));
        writer.write ("</when></TimeStamp>\n");
        writer.write ("<styleUrl>#start</styleUrl>\n");
        writer.write ("<Point>\n");
        writer.write ("<coordinates>");
        writer.write (Double.toString (journey.getStartLocation ().getLongitude ()));
        writer.write (",");
        writer.write (Double.toString (journey.getStartLocation ().getLatitude ()));
        writer.write (",");
        writer.write (Double.toString (journey.getStartLocation ().getAltitude ()));
        writer.write ("</coordinates>\n");
        writer.write ("</Point>\n");
        writer.write ("</Placemark>\n");

        // Tracks
        writer.write ("<Placemark id=\"tour\">\n");
        writer.write ("<styleUrl>#track</styleUrl>\n");
        writer.write ("<name><![CDATA[");
        writer.write (journey.getTitle ());
        writer.write ("]]>");
        writer.write ("</name>\n");

        if (journey.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (journey.getDescription ());
            writer.write ("]]></description>\n");
        }

        writer.write ("<gx:MultiTrack>\n");
        writer.write ("<altitudeMode>absolute</altitudeMode>\n");
        writer.write ("<gx:interpolate>1</gx:interpolate>\n");

        for (JourneySegment s : journey.getSegments ()) {
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

            for (Location l : s.getLocations ()) {
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
        writer.write (journey.getTitle ());
        writer.write (" - ");
        writer.write (ctx.getString (R.string.text_end));
        writer.write ("]]>");
        writer.write ("</name>\n");

        if (journey.getDescription () != null) {
            writer.write ("<description><![CDATA[");
            writer.write (journey.getDescription ());
            writer.write ("]]></description>\n");
        }

        writer.write ("<TimeStamp><when>");
        writer.write (df.format (journey.getEndLocation ().getLocationTimeAsDate ()));
        writer.write ("</when></TimeStamp>\n");
        writer.write ("<styleUrl>#end</styleUrl>\n");
        writer.write ("<Point>\n");
        writer.write ("<coordinates>");
        writer.write (Double.toString (journey.getEndLocation ().getLongitude ()));
        writer.write (",");
        writer.write (Double.toString (journey.getEndLocation ().getLatitude ()));
        writer.write (",");
        writer.write (Double.toString (journey.getEndLocation ().getAltitude ()));
        writer.write ("</coordinates>\n");
        writer.write ("</Point>\n");
        writer.write ("</Placemark>\n");

        writer.write ("</Document>\n");
        writer.write ("</kml>\n");
    }

    public static List<Location> createLocationList (Context ctx, Journey journey)
    {
        SQLiteDatabase          db = null;
        ArrayList<Location> result;
        Cursor                  c  = null;
        DateFormat          df;

        try {
            db = new LogMyTripDataHelper (ctx).getReadableDatabase ();

            df = new SimpleDateFormat ("yyyy-MM-dd");

            c = db.rawQuery (ctx.getString (R.string.SQL_get_journey_locations),
                             new String[]{ df.format (journey.getJouneyDate ()) });

            result = new ArrayList<Location> ();

            if (c != null) {
                if (c.moveToFirst ()) {
                    do {
                        result.add (createLocation (c));
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

    private static Location createLocation (Cursor c)
    {
        Location result;

        result = new Location ();

        result.setId (c.getLong (c.getColumnIndex ("id")));
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
}


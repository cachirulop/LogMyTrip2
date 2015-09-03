
package com.cachirulop.logmytrip.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cachirulop.logmytrip.R;

/**
 * Provides the methods to access to the database.
 * 
 * @author David
 * 
 */
public class LogMyTripDataHelper
        extends SQLiteOpenHelper
{
    /** Name of the database */
    private static final String DATABASE_NAME    = "logmytrip.db";

    /** Current version */
    private static final int DATABASE_VERSION = 5;

    /** Context where the object is created */
    private final Context       _ctx;

    /**
     * Constructor that receives the context.
     */
    public LogMyTripDataHelper (Context ctx)
    {
        super (ctx,
               DATABASE_NAME,
               null,
               DATABASE_VERSION);

        _ctx = ctx;
    }

    /**
     * Creates the tables of the database, executing the SQL_on_create sentence
     * defined in the application resources.
     */
    @Override
    public void onCreate (SQLiteDatabase db)
    {
        String[] sql = _ctx.getString (R.string.SQL_on_create).split (";");

        db.beginTransaction ();
        try {
            // Create tables
            execMultipleSQL (db,
                             sql);

            db.setTransactionSuccessful ();
        }
        catch (SQLException e) {
            Log.e ("Error creating tables",
                   e.toString ());
            throw e;
        }
        finally {
            db.endTransaction ();
        }
    }

    /**
     * Drop the tables and recreate it calling onCreate method. To drop
     * the tables uses the SQL_on_upgrade sentences defined in the application
     * resources.
     */
    @Override
    public void onUpgrade (SQLiteDatabase db,
                           int oldVersion,
                           int newVersion)
    {
        String[] sql = _ctx.getString (R.string.SQL_on_upgrade).split (";");

        db.beginTransaction ();

        try {
            execMultipleSQL (db,
                             sql);
            db.setTransactionSuccessful ();
        }
        catch (SQLException e) {
            Log.e ("Error upgrading tables",
                   e.toString ());
            throw e;
        }
        finally {
            db.endTransaction ();
        }

        onCreate (db);
    }

    /**
     * Execute all of the SQL statements in the String[] array
     * 
     * @param db
     *            The database on which to execute the statements
     * @param sql
     *            An array of SQL statements to execute
     */
    private void execMultipleSQL (SQLiteDatabase db,
                                  String[] sql)
    {
        for (String s : sql) {
            if (s.trim ().length () > 0) {
                db.execSQL (s);
            }
        }
    }

    /**
     * Returns the last identifier value of an specified table
     * 
     * @return The last identifier of the table in the sqlite_sequence table
     */
    public long getLastId (String table)
    {
        long index = 0;
        SQLiteDatabase sdb = null;
        Cursor cursor = null;

        try {
            sdb = getReadableDatabase ();
            cursor = sdb.query ("sqlite_sequence",
                                new String[] { "seq" },
                                "name = ?",
                                new String[] { table },
                                null,
                                null,
                                null,
                                null);

            if (cursor.moveToFirst ()) {
                index = cursor.getLong (cursor.getColumnIndex ("seq"));
            }

            return index;
        }
        finally {
            if (cursor != null) {
                cursor.close ();
            }

            if (sdb != null) {
                sdb.close ();
            }
        }
    }

    /**
     * Reset the identifier of a table to restart its count
     * 
     * @param table
     *            Name of the table which restart counter
     */
    public void resetId (Context ctx, String table)
    {
        SQLiteDatabase db = null;

        try {
            db = new LogMyTripDataHelper (ctx).getWritableDatabase ();

            db.delete ("sqlite_sequence",
                       "name = ?",
                       new String[] { table });
        }
        finally {
            if (db != null) {
                db.close ();
            }
        }

    }

}

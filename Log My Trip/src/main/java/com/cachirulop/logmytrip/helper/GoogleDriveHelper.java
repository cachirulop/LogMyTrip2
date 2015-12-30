package com.cachirulop.logmytrip.helper;

import android.content.Context;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by david on 22/11/15.
 */
public class GoogleDriveHelper
{
    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 10201;

    public static GoogleApiClient createClient (Context ctx,
                                                GoogleApiClient.ConnectionCallbacks connectionListener,
                                                GoogleApiClient.OnConnectionFailedListener failedListener)
    {
        GoogleApiClient.Builder builder;

        builder = new GoogleApiClient.Builder (ctx);
        builder.addApi (Drive.API);
        builder.addScope (Drive.SCOPE_FILE);

        if (connectionListener != null) {
            builder.addConnectionCallbacks (connectionListener);
        }

        if (failedListener != null) {
            builder.addOnConnectionFailedListener (failedListener);
        }

        if (!"".equals (SettingsManager.getAutoSyncGoogleDriveAccount (ctx))) {
            builder.setAccountName (SettingsManager.getAutoSyncGoogleDriveAccount (ctx));
        }

        return builder.build ();
    }

    public static String[] listFolder (final GoogleApiClient client,
                                       final Context ctx,
                                       final String path)
            throws GoogleDriveHelperException
    {
        DriveFolder                   folder;
        DriveApi.MetadataBufferResult result;
        MetadataBuffer                metadataBuffer;
        ArrayList<String>             list;

        folder = getFolder (client, path);

        result = folder.listChildren (client).await ();
        if (result == null || !result.getStatus ().isSuccess ()) {
            throw new GoogleDriveHelperException (R.string.msg_error_gd_reading_folder);
        }

        list = new ArrayList<> ();
        metadataBuffer = result.getMetadataBuffer ();
        for (Metadata m : metadataBuffer) {
            if (m.isDataValid ()) {
                list.add (m.getTitle ());
            }
        }

        metadataBuffer.release ();

        return list.toArray (new String[0]);
    }

    private static DriveFolder getFolder (GoogleApiClient client, String filePath)
            throws GoogleDriveHelperException
    {
        return getFolder (client, filePath, true);
    }


    private static DriveFolder getFolder (GoogleApiClient client, String filePath, boolean create)
            throws GoogleDriveHelperException
    {
        String[]    path;
        DriveFolder last;

        last = Drive.DriveApi.getRootFolder (client);
        path = filePath.split (File.separator);
        for (String s : path) {
            DriveFolder current;

            current = (DriveFolder) findDriveResource (client, last, s, true);
            if (current == null && create) {
                current = createFolder (client, last, s);
            }
            else {
                throw new GoogleDriveHelperException (R.string.msg_folder_not_found);
            }

            last = current;
        }

        return last;
    }

    private static DriveResource findDriveResource (GoogleApiClient client,
                                                    DriveFolder parent,
                                                    String title,
                                                    boolean isFolder)
            throws GoogleDriveHelperException
    {
        Query.Builder                 qBuilder;
        Query                         q;
        DriveApi.MetadataBufferResult queryResult;

        qBuilder = new Query.Builder ();
        qBuilder.addFilter (Filters.eq (SearchableField.TITLE, title));
        qBuilder.addFilter (Filters.and (Filters.eq (SearchableField.TRASHED, false)));

        q = qBuilder.build ();

        queryResult = parent.queryChildren (client, q).await ();
        if (queryResult != null && queryResult.getStatus ().isSuccess ()) {
            MetadataBuffer metadataBuffer;
            Metadata metadata;
            DriveResource result;

            result = null;
            metadata = null;
            metadataBuffer = queryResult.getMetadataBuffer ();

            for (Metadata m : metadataBuffer) {
                if (m.isDataValid () && ((isFolder && m.isFolder ()) || (!isFolder && !m.isFolder ()))) {
                    metadata = m;

                    break;
                }
            }

            if (metadata != null) {
                if (isFolder) {
                    result = metadata.getDriveId ().asDriveFolder ();
                }
                else {
                    result = metadata.getDriveId ().asDriveFile ();
                }
            }

            metadataBuffer.release ();

            return result;
        }
        else {
            return null;
        }
    }

    private static DriveFolder createFolder (GoogleApiClient client,
                                             DriveFolder parent,
                                             String title)
            throws GoogleDriveHelperException
    {
        MetadataChangeSet.Builder     newFolderBuilder;
        MetadataChangeSet             newFolder;
        DriveFolder.DriveFolderResult result;

        newFolderBuilder = new MetadataChangeSet.Builder ();
        newFolderBuilder.setTitle (title);
        newFolder = newFolderBuilder.build ();

        result = parent.createFolder (client, newFolder).await ();

        if (result.getStatus ().isSuccess ()) {
            return result.getDriveFolder ();
        }
        else {
            throw new GoogleDriveHelperException (R.string.msg_error_gd_creating_new_folder);
        }
    }

    public static void saveFile (final GoogleApiClient client,
                                 final Context ctx,
                                 final String filePath,
                                 final Trip trip, final IGoogleDriveWriterListener listener)
    {
        Thread t;

        t = new Thread ()
        {
            @Override
            public void run ()
            {
                try {
                    realSaveFile (client, ctx, filePath, trip, listener);
                }
                catch (GoogleDriveHelperException e) {
                    listener.onSaveFileFails (e.getMessageId ());
                }

            }
        };

        t.start ();

        try {
            t.join ();

            listener.onSaveFileSuccess ();
        }
        catch (InterruptedException e) {
            listener.onSaveFileFails (R.string.msg_error_exporting);
        }
    }

    private static void realSaveFile (final GoogleApiClient client,
                                      final Context ctx,
                                      final String filePath,
                                      final Trip trip, final IGoogleDriveWriterListener listener)
            throws GoogleDriveHelperException
    {
        DriveFolder folder;
        File        file;

        file = new File (filePath);
        folder = getFolder (client, file.getParent ());

        createFile (client, ctx, folder, file.getName (), trip, listener);
    }

    private static void createFile (GoogleApiClient client,
                                    Context ctx,
                                    DriveFolder folder,
                                    String name,
                                    Trip trip, IGoogleDriveWriterListener listener)
            throws GoogleDriveHelperException
    {
        // If file exists, remove it
        DriveFile                    file;
        DriveApi.DriveContentsResult driveResult;

        file = (DriveFile) findDriveResource (client, folder, name, false);
        if (file != null) {
            Status result;

            result = file.delete (client).await ();
            if (!result.isSuccess ()) {
                throw new GoogleDriveHelperException (R.string.msg_error_gd_deleting_file);
            }
        }

        driveResult = Drive.DriveApi.newDriveContents (client).await ();
        if (driveResult.getStatus ().isSuccess ()) {
            DriveContents contents;
            OutputStream outputStream;
            Writer writer;
            DriveFolder.DriveFileResult fileResult;

            contents = driveResult.getDriveContents ();

            outputStream = contents.getOutputStream ();
            writer = new OutputStreamWriter (outputStream);
            try {
                listener.onWriteContents (writer);
                writer.close ();
            }
            catch (IOException e) {
                throw new GoogleDriveHelperException (R.string.msg_error_gd_creating_new_file);
            }

            MetadataChangeSet.Builder changeSetBuilder;
            MetadataChangeSet changeSet;

            changeSetBuilder = new MetadataChangeSet.Builder ();
            changeSetBuilder.setTitle (name);

            if (getFileExtension (name).equals ("GPX")) {
                changeSetBuilder.setMimeType ("application/gpx");
            }
            else {
                changeSetBuilder.setMimeType ("application/vnd.google-earth.kml+xml");
            }

            changeSet = changeSetBuilder.build ();

            fileResult = folder.createFile (client, changeSet, contents).await ();
            if (!fileResult.getStatus ().isSuccess ()) {
                throw new GoogleDriveHelperException (R.string.msg_error_gd_creating_new_file);
            }
        }
        else {
            throw new GoogleDriveHelperException (R.string.msg_error_gd_creating_new_file);
        }
    }

    public static void readFile (final GoogleApiClient client,
                                 final Context ctx,
                                 final String filePath,
                                 final IGoogleDriveReaderListener listener)
    {
        Thread t;

        t = new Thread ()
        {
            @Override
            public void run ()
            {
                try {
                    realReadFile (client, ctx, filePath, listener);
                }
                catch (GoogleDriveHelperException e) {
                    listener.onReadFileFails (e.getMessageId ());
                }

            }
        };

        t.start ();

        try {
            t.join ();

            listener.onReadFileSuccess ();
        }
        catch (InterruptedException e) {
            listener.onReadFileFails (R.string.msg_error_exporting);
        }
    }

    private static void realReadFile (GoogleApiClient client,
                                      Context ctx,
                                      String filePath,
                                      IGoogleDriveReaderListener listener)
            throws GoogleDriveHelperException
    {
        DriveFolder folder;
        File        file;

        file = new File (filePath);
        folder = getFolder (client, file.getParent (), false);

        readFileContents (client, ctx, folder, file.getName (), listener);
        // createFile (client, ctx, folder, file.getName (), trip, listener);
    }

    private static void readFileContents (GoogleApiClient client,
                                          Context ctx,
                                          DriveFolder folder,
                                          String name,
                                          IGoogleDriveReaderListener listener)
    {

    }


    private static String getFileExtension (String fileName)
    {
        return fileName.substring (fileName.lastIndexOf (".") + 1).toUpperCase ();
    }

    public interface IGoogleDriveWriterListener
    {
        void onWriteContents (Writer w);
        void onSaveFileSuccess ();
        void onSaveFileFails (int messageId);
    }

    public interface IGoogleDriveReaderListener
    {
        void onReadContents (Reader r);

        void onReadFileSuccess ();

        void onReadFileFails (int messageId);
    }
}

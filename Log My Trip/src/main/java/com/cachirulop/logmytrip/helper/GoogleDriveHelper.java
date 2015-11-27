package com.cachirulop.logmytrip.helper;

import com.cachirulop.logmytrip.R;
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
import java.io.Writer;

/**
 * Created by david on 22/11/15.
 */
public class GoogleDriveHelper
{
    public static void saveFile (final GoogleApiClient client,
                                 final String filePath,
                                 final String fileContents,
                                 final IGoogleDriveHelperListener listener)
    {
        new Thread ()
        {
            @Override
            public void run ()
            {
                try {
                    realSaveFile (client, filePath, fileContents);

                    listener.onSaveFileSuccess ();
                }
                catch (GoogleDriveHelperException e) {
                    listener.onSaveFileFails (e.getMessageId ());
                }

            }
        }.start ();
    }

    private static void realSaveFile (final GoogleApiClient client,
                                      final String filePath,
                                      final String fileContents)
            throws GoogleDriveHelperException
    {
        DriveFolder folder;
        File        file;

        file = new File (filePath);
        folder = getFolder (client, file.getParent ());

        createFile (client, folder, file.getName (), fileContents);
    }

    private static DriveFolder getFolder (GoogleApiClient client, String filePath)
            throws GoogleDriveHelperException
    {
        String[]    path;
        DriveFolder last;

        last = Drive.DriveApi.getRootFolder (client);
        path = filePath.split (File.separator);
        for (String s : path) {
            DriveFolder current;

            current = (DriveFolder) findDriveResource (client, last, s, true);
            if (current == null) {
                current = createFolder (client, last, s);
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
        Query.Builder qBuilder;
        Query         q;
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
        MetadataChangeSet.Builder newFolderBuilder;
        MetadataChangeSet         newFolder;
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

    private static void createFile (GoogleApiClient client,
                                    DriveFolder folder,
                                    String name,
                                    String fileContents)
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
                writer.write (fileContents);
                writer.close ();
            }
            catch (IOException e) {
                throw new GoogleDriveHelperException (R.string.msg_error_gd_creating_new_file);
            }

            MetadataChangeSet.Builder changeSetBuilder;
            MetadataChangeSet changeSet;

            changeSetBuilder = new MetadataChangeSet.Builder ();
            changeSetBuilder.setTitle (name);

            if (name.endsWith (".gpx")) {
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

    public interface IGoogleDriveHelperListener
    {
        void onSaveFileSuccess ();

        void onSaveFileFails (int messageId);
    }
}

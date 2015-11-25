package com.cachirulop.logmytrip.helper;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
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
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by david on 22/11/15.
 */
public class GoogleDriveHelper
{
    private Queue<String>                          _processingPath;
    private String _fileName;
    private GoogleApiClient                        _client;
    private DriveId                                _result;
    private DriveFolder _rootFolder = null;

    private ResultCallback<DriveApi.DriveIdResult>        _findFolderResultCallback;
    private ResultCallback<DriveApi.MetadataBufferResult> _queryCallback;

    public GoogleDriveHelper (GoogleApiClient client)
    {
        _queryCallback = new QueryCallback (this);
        _client = client;
    }

    public void saveFileAsync (String path,
                               final String content,
                               final IGoogleDriveHelperListener listener)
    {
        init (path);

        new Thread ()
        {
            @Override
            public void run ()
            {

            }
        }.start ();
    }

    private void init (String path)
    {
        String[] pathItems;

        _rootFolder = Drive.DriveApi.getRootFolder (_client);

        _processingPath = new ArrayDeque<> ();

        pathItems = path.split (File.separator);

        // Don't add the file name, only folders
        for (int i = 0 ; i < (pathItems.length - 1) ; i++) {
            _processingPath.add (pathItems[i]);
        }

        _fileName = pathItems[pathItems.length - 1];
    }

    public void saveFileAsync2 (String path,
                                final String content,
                                final IGoogleDriveHelperListener listener)
    {
        init (path);

        findFolderAsync (new ResultCallback<DriveApi.DriveIdResult> ()
        {
            @Override
            public void onResult (DriveApi.DriveIdResult driveIdResult)
            {
                if (driveIdResult.getStatus ().isSuccess ()) {
                    LogHelper.d ("*** Folder obtained");

                    DriveFolder folder;
                    NewDriveContentsCallback newDrive;

                    folder = Drive.DriveApi.getFolder (_client, driveIdResult.getDriveId ());

                    newDrive = new NewDriveContentsCallback (_client,
                                                             _fileName,
                                                             content,
                                                             folder,
                                                             listener);

                    Drive.DriveApi.newDriveContents (_client).setResultCallback (newDrive);
                }
                else {
                    listener.onSaveFileFails ();
                }
            }
        });
    }

    private void findFolderAsync (ResultCallback<DriveApi.DriveIdResult> callback)
    {
        LogHelper.d ("*** Processing: " + _processingPath.peek ());

        _findFolderResultCallback = callback;

        launchFindFolderQuery ();
    }

    private void launchFindFolderQuery ()
    {
        Query.Builder qBuilder;
        Query         q;

        qBuilder = new Query.Builder ();
        qBuilder.addFilter (Filters.eq (SearchableField.TITLE, _processingPath.peek ()));

        q = qBuilder.build ();

        _rootFolder.queryChildren (_client, q).setResultCallback (_queryCallback);
    }

    private DriveFolder findFolderRecurse (DriveFolder root, String folder)
    {
        Query.Builder qBuilder;
        Query         q;
        DriveApi.MetadataBufferResult result;

        qBuilder = new Query.Builder ();
        qBuilder.addFilter (Filters.eq (SearchableField.TITLE, folder));

        q = qBuilder.build ();

        result = root.queryChildren (_client, q).await ();
        if (result.getStatus ().isSuccess ()) {
            return
        }
    }

    private void createFolder ()
    {
        MetadataChangeSet.Builder newFolderBuilder;
        MetadataChangeSet         newFolder;

        LogHelper.d ("*** createFolder " + _processingPath.peek ());

        newFolderBuilder = new MetadataChangeSet.Builder ();
        newFolderBuilder.setTitle (_processingPath.peek ());
        newFolder = newFolderBuilder.build ();

        _rootFolder.createFolder (_client, newFolder)
                   .setResultCallback (new ResultCallback<DriveFolder.DriveFolderResult> ()
                   {
                       @Override
                       public void onResult (DriveFolder.DriveFolderResult driveFolderResult)
                       {
                           LogHelper.d ("*** createFolder callback: " + _processingPath.peek ());

                           if (!driveFolderResult.getStatus ().isSuccess ()) {
                               LogHelper.d ("*** error: " + driveFolderResult.getStatus ()
                                                                             .getStatusMessage ());

                               onGetFolderAsyncNotFound ();
                           }
                           else {
                               LogHelper.d ("*** fetchIdCallback: created " + _processingPath.peek ());

                               _result = driveFolderResult.getDriveFolder ().getDriveId ();

                               onGetFolderAsyncSuccess ();
                           }
                       }
                   });
    }

    private void onGetFolderAsyncNotFound ()
    {
        _findFolderResultCallback.onResult (new DriveApi.DriveIdResult ()
        {
            @Override
            public DriveId getDriveId ()
            {
                return null;
            }

            @Override
            public Status getStatus ()
            {
                return new Status (CommonStatusCodes.ERROR);
            }
        });
    }

    private void onGetFolderAsyncSuccess ()
    {
        _processingPath.poll ();

        if (_processingPath.isEmpty ()) {
            _findFolderResultCallback.onResult (new DriveApi.DriveIdResult ()
            {
                @Override
                public DriveId getDriveId ()
                {
                    return _result;
                }

                @Override
                public Status getStatus ()
                {
                    return new Status (CommonStatusCodes.SUCCESS);
                }
            });
        }
        else {
            _rootFolder = Drive.DriveApi.getFolder (_client, _result);

            launchFindFolderQuery ();
        }
    }

    public void setResult (DriveId result)
    {
        _result = result;
    }

    public interface IGoogleDriveHelperListener
    {
        void onSaveFileSuccess ();

        void onSaveFileFails ();
    }


    private class QueryCallback
            implements ResultCallback<DriveApi.MetadataBufferResult>
    {
        GoogleDriveHelper _parent;

        public QueryCallback (GoogleDriveHelper parent)
        {
            _parent = parent;
        }

        @Override
        public void onResult (DriveApi.MetadataBufferResult metadataBufferResult)
        {
            if (metadataBufferResult.getStatus ().isSuccess ()) {
                MetadataBuffer buffer;

                buffer = metadataBufferResult.getMetadataBuffer ();

                boolean exists = false;
                for (Metadata m : metadataBufferResult.getMetadataBuffer ()) {
                    if (!m.isTrashed ()) {
                        _parent.setResult (m.getDriveId ());
                        exists = true;

                        continue;
                    }
                }

                buffer.release ();

                if (exists) {
                    LogHelper.d ("*** _queryCallback: OK");

                    _parent.onGetFolderAsyncSuccess ();
                }
                else {
                    LogHelper.d ("*** _queryCallback: not exists");

                    _parent.createFolder ();
                }
            }
            else {
                LogHelper.d ("*** folder not found");

                _parent.createFolder ();
            }
        }
    }

    private class NewDriveContentsCallback
            implements ResultCallback<DriveApi.DriveContentsResult>
    {
        private GoogleApiClient            _client;
        private String                     _fileContents;
        private IGoogleDriveHelperListener _listener;
        private DriveFolder                _folder;
        private String                     _fileName;

        public NewDriveContentsCallback (GoogleApiClient client,
                                         String fileName,
                                         String fileContents,
                                         DriveFolder folder,
                                         IGoogleDriveHelperListener listener)
        {
            _client = client;
            _fileContents = fileContents;
            _listener = listener;
            _folder = folder;
            _fileName = fileName;
        }

        @Override
        public void onResult (final DriveApi.DriveContentsResult driveContentsResult)
        {
            if (!driveContentsResult.getStatus ().isSuccess ()) {
                _listener.onSaveFileFails ();

            }
            else {
                // Perform I/O off the UI thread.
                new Thread ()
                {
                    @Override
                    public void run ()
                    {
                        DriveContents contents;
                        OutputStream  outputStream;
                        Writer        writer;

                        contents = driveContentsResult.getDriveContents ();

                        outputStream = contents.getOutputStream ();
                        writer = new OutputStreamWriter (outputStream);
                        try {
                            writer.write (_fileContents);
                            writer.close ();
                        }
                        catch (IOException e) {
                            _listener.onSaveFileFails ();

                            return;
                        }

                        MetadataChangeSet.Builder changeSetBuilder;
                        MetadataChangeSet         changeSet;

                        changeSetBuilder = new MetadataChangeSet.Builder ();
                        changeSetBuilder.setTitle (_fileName);
                        changeSetBuilder.setMimeType ("application/gpx");
                        changeSet = changeSetBuilder.build ();

                        _folder.createFile (_client, changeSet, contents)
                               .setResultCallback (new ResultCallback<DriveFolder.DriveFileResult> ()
                               {
                                   @Override
                                   public void onResult (DriveFolder.DriveFileResult driveFileResult)
                                   {
                                       if (!driveFileResult.getStatus ().isSuccess ()) {
                                           LogHelper.d ("*** createFile Error");
                                       }
                                       else {
                                           LogHelper.d ("*** createFile OK");
                                       }
                                   }
                               });
                    }
                }.start ();
            }
        }
    }
}

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
    private ResultCallback<DriveApi.DriveIdResult> _findFolderResultCallback;
    private Queue<String>                          _processingPath;
    private GoogleApiClient                        _client;
    private DriveId                                _result;
    private DriveFolder _rootFolder = null;
    ResultCallback<DriveApi.MetadataBufferResult> _queryCallback = new ResultCallback<DriveApi.MetadataBufferResult> ()
    {
        @Override
        public void onResult (DriveApi.MetadataBufferResult metadataBufferResult)
        {
            if (metadataBufferResult.getStatus ().isSuccess ()) {
                MetadataBuffer buffer;

                buffer = metadataBufferResult.getMetadataBuffer ();

                boolean exists = false;
                for (Metadata m : metadataBufferResult.getMetadataBuffer ()) {
                    if (!m.isTrashed ()) {
                        _result = m.getDriveId ();
                        exists = true;

                        continue;
                    }
                }

                buffer.release ();

                if (exists) {
                    LogHelper.d ("*** _queryCallback: OK");

                    onGetFolderAsyncSuccess ();
                }
                else {
                    LogHelper.d ("*** _queryCallback: not exists");

                    createFolder ();
                }
            }
            else {
                LogHelper.d ("*** folder not found");

                createFolder ();
            }
        }
    };

    public GoogleDriveHelper (GoogleApiClient client)
    {
        _client = client;
    }

    public void saveFileAsync (String path,
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

                    final DriveFolder folder;

                    folder = Drive.DriveApi.getFolder (_client, driveIdResult.getDriveId ());

                    Drive.DriveApi.newDriveContents (_client)
                                  .setResultCallback (new ResultCallback<DriveApi.DriveContentsResult> ()
                                  {
                                      @Override
                                      public void onResult (DriveApi.DriveContentsResult result)
                                      {
                                          if (!result.getStatus ().isSuccess ()) {
                                              listener.onSaveFileFails ();

                                              return;
                                          }

                                          final DriveContents contents;

                                          contents = result.getDriveContents ();

                                          // Perform I/O off the UI thread.
                                          new Thread ()
                                          {
                                              @Override
                                              public void run ()
                                              {
                                                  OutputStream outputStream;
                                                  Writer       writer;

                                                  outputStream = contents.getOutputStream ();
                                                  writer = new OutputStreamWriter (outputStream);
                                                  try {
                                                      writer.write (content);
                                                      writer.close ();
                                                  }
                                                  catch (IOException e) {
                                                      listener.onSaveFileFails ();

                                                      return;
                                                  }

                                                  MetadataChangeSet.Builder changeSetBuilder;
                                                  MetadataChangeSet         changeSet;

                                                  changeSetBuilder = new MetadataChangeSet.Builder ();
                                                  changeSetBuilder.setTitle (fileName);
                                                  changeSetBuilder.setMimeType ("application/gpx");
                                                  changeSet = changeSetBuilder.build ();

                                                  folder.createFile (client, changeSet, contents)
                                                        .setResultCallback (new ResultCallback<DriveFolder.DriveFileResult> ()
                                                        {
                                                            @Override
                                                            public void onResult (DriveFolder.DriveFileResult driveFileResult)
                                                            {
                                                                if (!driveFileResult.getStatus ()
                                                                                    .isSuccess ()) {
                                                                    LogHelper.d (
                                                                            "*** createFile Error");
                                                                }
                                                                else {
                                                                    LogHelper.d ("*** createFile OK");
                                                                }
                                                            }
                                                        });
                                              }
                                          }.start ();

                                      }
                                  });

                }
                else {
                    listener.onSaveFileFails ();
                }
            }
        });
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

    public interface IGoogleDriveHelperListener
    {
        void onSaveFileSuccess ();

        void onSaveFileFails ();
    }
}

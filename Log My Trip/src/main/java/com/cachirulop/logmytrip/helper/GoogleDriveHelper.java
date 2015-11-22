package com.cachirulop.logmytrip.helper;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by david on 22/11/15.
 */
public class GoogleDriveHelper
{
    ResultCallback<DriveApi.DriveIdResult> _fetchIdCallback = new ResultCallback<DriveApi.DriveIdResult> ()
    {
        @Override
        public void onResult (final DriveApi.DriveIdResult driveIdResult)
        {
            if (driveIdResult.getStatus ().isSuccess ()) {
                _result = driveIdResult;
                _processingPath.poll ();

                LogHelper.d ("*** fetchIdCallback: OK");

                onFetchIdSuccess ();
            }
            else {
                LogHelper.d ("*** folder not found");
                //                MetadataChangeSet.Builder newFolderBuilder;
                //                MetadataChangeSet newFolder;
                //
                //                LogHelper.d ("*** fetchIdCallback: creating " + _processingPath.peek ());
                //
                //                newFolderBuilder = new MetadataChangeSet.Builder ();
                //                newFolderBuilder.setTitle (_processingPath.peek ());
                //                newFolder = newFolderBuilder.build ();
                //
                //                _rootFolder.createFolder (_client, newFolder)
                //                           .setResultCallback (new ResultCallback<DriveFolder.DriveFolderResult> ()
                //                           {
                //                               @Override
                //                               public void onResult (DriveFolder.DriveFolderResult driveFolderResult)
                //                               {
                //                                   LogHelper.d ("*** fetchIdCallback: creating folder " + _processingPath.peek ());
                //
                //                                   if (!driveFolderResult.getStatus ().isSuccess ()) {
                //                                       LogHelper.d ("*** error: " + driveFolderResult.getStatus ()
                //                                                                                     .getStatusMessage ());
                //                                   }
                //                                   else {
                //                                       LogHelper.d ("*** fetchIdCallback: created " + _processingPath.peek ());
                //                                       Drive.DriveApi.fetchDriveId (_client,
                //                                                                    _processingPath.peek ())
                //                                                     .setResultCallback (_fetchIdCallback);
                //                                   }
                //                               }
                //                           });

            }
        }
    };

    private ResultCallback<DriveApi.DriveIdResult> _resultCallback;
    private Queue<String>                          _processingPath;
    private GoogleApiClient                        _client;
    private DriveApi.DriveIdResult                 _result;
    private DriveFolder                            _rootFolder;

    private void onFetchIdSuccess ()
    {
        if (_processingPath.isEmpty ()) {
            _resultCallback.onResult (_result);
        }
        else {
            _rootFolder = Drive.DriveApi.getFolder (_client, _result.getDriveId ());

            Drive.DriveApi.fetchDriveId (_client, _processingPath.peek ())
                          .setResultCallback (_fetchIdCallback);
        }
    }

    public GoogleDriveHelper (GoogleApiClient client,
                              String path,
                              ResultCallback<DriveApi.DriveIdResult> callback)
    {
        _client = client;
        _resultCallback = callback;
        _rootFolder = Drive.DriveApi.getRootFolder (_client);

        String[] pathItems;

        _processingPath = new ArrayDeque<> ();

        pathItems = path.split (File.separator);
        for (String s : pathItems) {
            _processingPath.add (s);
        }
    }

    public void getFolderIdAsync ()
    {
        LogHelper.d ("*** Processing: " + _processingPath.peek ());
        Drive.DriveApi.fetchDriveId (_client, _processingPath.peek ())
                      .setResultCallback (_fetchIdCallback);
/*
                      .setResultCallback (new ResultCallback<DriveApi.DriveIdResult> ()
                      {
                          @Override
                          public void onResult (DriveApi.DriveIdResult driveIdResult)
                          {
                              if (!driveIdResult.getStatus ().isSuccess ()) {
                                  DriveFolder rootFolder;
                                  MetadataChangeSet.Builder newFolderBuilder;
                                  MetadataChangeSet newFolder;

                                  rootFolder = Drive.DriveApi.getRootFolder (client);

                                  newFolderBuilder = new MetadataChangeSet.Builder ();
                                  newFolderBuilder.setTitle ("LogMyTrip/kk");
                                  newFolder = newFolderBuilder.build ();

                                  rootFolder.createFolder (client, newFolder)
                                            .setResultCallback (new ResultCallback<DriveFolder.DriveFolderResult> ()
                                            {
                                                @Override
                                                public void onResult (DriveFolder.DriveFolderResult driveFolderResult)
                                                {
                                                    if (!driveFolderResult.getStatus ()
                                                                          .isSuccess ()) {
                                                        LogHelper.d ("*** error: " + driveFolderResult
                                                                .getStatus ()
                                                                .getStatusMessage ());
                                                    }
                                                    else {
                                                        driveFolderResult.getDriveFolder ();
                                                    }
                                                }
                                            });
                              }
                              else {
                                  Drive.DriveApi.getFolder (client, driveIdResult.getDriveId ());
                              }
                          }
                      });
*/
    }
}

package com.cachirulop.logmytrip.helper;

import android.content.Context;
import android.os.Environment;

import com.cachirulop.logmytrip.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by david on 21/11/15.
 */
public class ExportHelper
{
    public static void exportToFile (Context ctx, String content, String fileName)
    {
        try {
            File file;
            FileWriter writer;

            file = new File (getFilePath (ctx, fileName, true, true));
            writer = new FileWriter (file, false);

            writer.append (content);

            writer.flush ();
            writer.close ();
        }
        catch (IOException e) {
            // TODO: Show error dialog
            ToastHelper.showLong (ctx,
                                  ctx.getText (R.string.error_cant_write_file)
                                     .toString () + ": " + e.getMessage ());
        }

    }

    public static void exportToGoogleDrive (final Context ctx,
                                            final String content,
                                            final String fileName,
                                            final GoogleApiClient client)
    {
        GoogleDriveHelper helper;
        String            path;

        path = getFilePath (ctx, fileName, false, false);

        // helper = new GoogleDriveHelper (client, path, new ResultCallback<DriveApi.DriveIdResult> ()
        helper = new GoogleDriveHelper (client,
                                        "Android",
                                        new ResultCallback<DriveApi.DriveIdResult> ()
                                        {
                                            @Override
                                            public void onResult (DriveApi.DriveIdResult driveIdResult)
                                            {
                                                LogHelper.d ("*** Folder created");
                                            }
                                        });

        helper.getFolderIdAsync ();

        //        Drive.DriveApi.fetchDriveId (client, "LogMyTrip/kk").setResultCallback (new ResultCallback<DriveApi.DriveIdResult> ()
        //        {
        //            @Override
        //            public void onResult (DriveApi.DriveIdResult driveIdResult)
        //            {
        //                if (!driveIdResult.getStatus ().isSuccess ()) {
        //                    DriveFolder rootFolder;
        //                    MetadataChangeSet.Builder newFolderBuilder;
        //                    MetadataChangeSet newFolder;
        //
        //                    rootFolder = Drive.DriveApi.getRootFolder (client);
        //
        //                    newFolderBuilder = new MetadataChangeSet.Builder ();
        //                    newFolderBuilder.setTitle ("LogMyTrip/kk");
        //                    newFolder = newFolderBuilder.build ();
        //
        //                    rootFolder.createFolder (client, newFolder).setResultCallback (new ResultCallback<DriveFolder.DriveFolderResult> ()
        //                    {
        //                        @Override
        //                        public void onResult (DriveFolder.DriveFolderResult driveFolderResult)
        //                        {
        //                            if (!driveFolderResult.getStatus ().isSuccess ()) {
        //                                LogHelper.d ("*** error: " + driveFolderResult.getStatus ().getStatusMessage ());
        //                            }
        //                            else {
        //                                LogHelper.d ("*** OK");
        //                            }
        //                        }
        //                    });
        //
        //
        //                    return;
        //                }
        //            }
        //        });
/*
        Drive.DriveApi.newDriveContents(client).setResultCallback (new ResultCallback<DriveApi.DriveContentsResult> ()
        {
            @Override
            public void onResult (DriveApi.DriveContentsResult result)
            {
                if (!result.getStatus ().isSuccess ()) {
                    DialogHelper.showErrorDialog (ctx, R.string.title_export_to_google_drive, R.string.msg_error_exporting, result.getStatus ().getStatusMessage ());

                    return;
                }

                final DriveContents contents;

                contents = result.getDriveContents ();

                // Perform I/O off the UI thread.
                new Thread() {
                    @Override
                    public void run() {
                        OutputStream outputStream;
                        Writer writer;

                        outputStream = contents.getOutputStream();
                        writer = new OutputStreamWriter (outputStream);
                        try {
                            writer.write(content);
                            writer.close();
                        }
                        catch (IOException e) {
                            DialogHelper.showErrorDialogMainThread (ctx, R.string.title_export_to_google_drive, R.string.msg_error_exporting, e.getMessage ());
                        }

                        MetadataChangeSet.Builder changeSetBuilder;
                        MetadataChangeSet changeSet;

                        changeSetBuilder = new MetadataChangeSet.Builder();
                        changeSetBuilder.setTitle (filePath);
                        // changeSetBuilder.setMimeType ("application/gpx");
                        changeSetBuilder.setMimeType (MimeTypeMap.getSingleton ()
                                                                 .getMimeTypeFromExtension (
                                                                         MimeTypeMap.getFileExtensionFromUrl (
                                                                                 filePath)));
                        changeSetBuilder.setStarred (true);

                        changeSet = changeSetBuilder.build();

                        // create a file on root folder

                        Drive.DriveApi.getFolder (client, )
                        Drive.DriveApi.getRootFolder (client)
                                      .createFile (client, changeSet, contents)
                                      .setResultCallback (new ResultCallback<DriveFolder.DriveFileResult> ()
                                      {
                                          @Override
                                          public void onResult (DriveFolder.DriveFileResult driveFileResult)
                                          {
                                              if (!driveFileResult.getStatus ().isSuccess ()) {
                                                  LogHelper.d ("*** Error");
                                              }
                                              else {
                                                  LogHelper.d ("*** OK");
                                              }
                                          }
                                      });
                    }
                }.start();

            }
        });
*/
    }

    private static String getFilePath (Context ctx,
                                       String fileName,
                                       boolean local,
                                       boolean includeFileName)
    {
        StringBuilder result;

        result = new StringBuilder ();

        if (local) {
            result.append (Environment.getExternalStorageDirectory ());
            result.append (File.separator);
        }

        result.append (ctx.getString (R.string.app_name));

        if (includeFileName) {
            result.append (File.separator);
            result.append (fileName);
        }

        return result.toString ();
    }
}

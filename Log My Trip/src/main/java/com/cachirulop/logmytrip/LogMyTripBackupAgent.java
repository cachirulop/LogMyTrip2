package com.cachirulop.logmytrip;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

import com.cachirulop.logmytrip.helper.LogHelper;

import java.io.IOException;

/**
 * Created by dmagro on 07/12/2015.
 */
public class LogMyTripBackupAgent
        extends BackupAgentHelper
{
    static final String PREFS_BACKUP_NAME = "com.cachirulop.logmytrip_preferences";
    static final String PREFS_BACKUP_KEY  = "prefs";

    @Override
    public void onBackup (ParcelFileDescriptor oldState,
                          BackupDataOutput data,
                          ParcelFileDescriptor newState)
            throws IOException
    {
        super.onBackup (oldState, data, newState);

        LogHelper.d ("*** LogMyTripBackupAgent onBackup");
    }

    @Override
    public void onRestore (BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState)
            throws IOException
    {
        super.onRestore (data, appVersionCode, newState);

        LogHelper.d ("*** LogMyTripBackupAgent onRestore");
    }

    @Override
    public void onCreate ()
    {
        SharedPreferencesBackupHelper helper;

        LogHelper.d ("*** LogMyTripBackupAgent onCreate");

        helper = new SharedPreferencesBackupHelper (this, PREFS_BACKUP_NAME);

        addHelper (PREFS_BACKUP_KEY, helper);
    }
}
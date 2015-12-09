package com.cachirulop.logmytrip;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Created by dmagro on 07/12/2015.
 */
public class LogMyTripBackupAgent
        extends BackupAgentHelper
{
    static final String PREFS_BACKUP_NAME = "com.cachirulop.logmytrip_preferences";
    static final String PREFS_BACKUP_KEY  = "prefs";

    @Override
    public void onCreate ()
    {
        SharedPreferencesBackupHelper helper;

        helper = new SharedPreferencesBackupHelper (this, PREFS_BACKUP_NAME);

        addHelper (PREFS_BACKUP_KEY, helper);
    }
}
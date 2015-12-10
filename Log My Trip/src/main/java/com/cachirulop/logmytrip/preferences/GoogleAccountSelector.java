package com.cachirulop.logmytrip.preferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * Created by dmagro on 10/12/2015.
 */
public class GoogleAccountSelector
        extends ListPreference
{
    public GoogleAccountSelector (Context context)
    {
        this (context, null);
    }

    public GoogleAccountSelector (Context context, AttributeSet attrs)
    {
        super (context, attrs);

        CharSequence[] entries;

        entries = getGoogleAccountList (context);

        setEntries (entries);
        setEntryValues (entries);
    }

    public static CharSequence[] getGoogleAccountList (Context ctx)
    {
        AccountManager          manager;
        ArrayList<CharSequence> accounts;

        accounts = new ArrayList<> ();

        manager = (AccountManager) ctx.getSystemService (Activity.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts ();
        for (Account account : list) {
            if (account.type.equalsIgnoreCase ("com.google")) {
                accounts.add (account.name);
            }
        }

        return accounts.toArray (new CharSequence[0]);
    }

    @Override
    protected void onPrepareDialogBuilder (AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder (builder);

        builder.setNegativeButton (null, null);
    }

    public void show ()
    {
        showDialog (null);
    }

}

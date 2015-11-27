package com.cachirulop.logmytrip.helper;

/**
 * Created by david on 26/11/15.
 */
public class GoogleDriveHelperException
        extends Exception
{
    private int _messageId;

    public GoogleDriveHelperException ()
    {
        super ();
    }

    public GoogleDriveHelperException (int messageId)
    {
        _messageId = messageId;
    }

    public int getMessageId ()
    {
        return _messageId;
    }
}

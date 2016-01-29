package com.cachirulop.logmytrip.helper;

/**
 * Created by david on 26/11/15.
 */
public class GoogleDriveHelperException
        extends Exception
{
    private int _messageId;
    private Object[] _formatArgs = null;

    public GoogleDriveHelperException ()
    {
        super ();
    }

    public GoogleDriveHelperException (int messageId)
    {
        _messageId = messageId;
    }

    public GoogleDriveHelperException (int messageId, Object... formatArgs)
    {
        _messageId = messageId;
        _formatArgs = formatArgs;
    }

    public int getMessageId ()
    {
        return _messageId;
    }

    public Object[] getFormatArgs ()
    {
        return _formatArgs;
    }
}

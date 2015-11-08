package com.cachirulop.logmytrip.manager;

/**
 * Created by david on 8/11/15.
 */
public class Test
{
    private static Test ourInstance = new Test ();

    public static Test getInstance ()
    {
        return ourInstance;
    }

    private Test ()
    {
    }
}

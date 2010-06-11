/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.james.mailrepository.filepair;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.apache.avalon.cornerstone.services.store.ObjectRepository;
import org.apache.avalon.excalibur.io.ClassLoaderObjectInputStream;

/**
 * This is a simple implementation of persistent object store using
 * object serialization on the file system.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:fede@apache.org">Federico Barbieri</a>
 * @author <a href="mailto:paul_hammant@yahoo.com">Paul Hammant</a>
 */
public class File_Persistent_Object_Repository
    extends AbstractFileRepository
    implements ObjectRepository
{
    protected String getExtensionDecorator()
    {
        return ".FileObjectStore";
    }

    /**
     * Get the object associated to the given unique key.
     */
    public synchronized Object get( final String key )
    {
        try
        {
            final InputStream inputStream = getInputStream( key );

            if( inputStream == null )
                  throw new NullPointerException("Null input stream returned for key: " + key );
            try
            {
                final ObjectInputStream stream = new ObjectInputStream( inputStream );

                if( stream == null )
                  throw new NullPointerException("Null stream returned for key: " + key );

                final Object object = stream.readObject();
                if( DEBUG )
                {
                    getLogger().debug( "returning object " + object + " for key " + key );
                }
                return object;
            }
            finally
            {
                inputStream.close();
            }
        }
        catch( final Throwable e )
        {
            throw new RuntimeException(
              "Exception caught while retrieving an object, cause: " + e.toString() );
        }
    }

    public synchronized Object get( final String key, final ClassLoader classLoader )
    {
        try
        {
            final InputStream inputStream = getInputStream( key );

            if( inputStream == null )
                  throw new NullPointerException("Null input stream returned for key: " + key );

            try
            {
                final ObjectInputStream stream = new ClassLoaderObjectInputStream( classLoader, inputStream );

                if( stream == null )
                  throw new NullPointerException("Null stream returned for key: " + key );

                final Object object = stream.readObject();

                if( DEBUG )
                {
                    getLogger().debug( "returning object " + object + " for key " + key );
                }
                return object;
            }
            finally
            {
                inputStream.close();
            }
        }
        catch( final Throwable e )
        {
            throw new RuntimeException( "Exception caught while retrieving an object: " + e );
        }

    }

    /**
     * Store the given object and associates it to the given key
     */
    public synchronized void put( final String key, final Object value )
    {
        try
        {
            final OutputStream outputStream = getOutputStream( key );

            try
            {
                final ObjectOutputStream stream = new ObjectOutputStream( outputStream );
                stream.writeObject( value );
                if( DEBUG ) getLogger().debug( "storing object " + value + " for key " + key );
            }
            finally
            {
                outputStream.close();
            }
        }
        catch( final Exception e )
        {
            throw new RuntimeException( "Exception caught while storing an object: " + e );
        }
    }

}

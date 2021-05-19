/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * $Header: /m/jws/appserv-tests/devtests/ee/admin/mbeanapi/samples/com/sun/appserv/management/sample/LineReaderImpl.java,v 1.1 2004/10/12 22:49:10 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/12 22:49:10 $
 */

package com.sun.appserv.management.sample;


import java.io.InputStream;
import java.io.InputStreamReader;

/**
    Reads a line from the specified input stream, outputs
    the prompt to System.out.
 */
public class LineReaderImpl
{
    final InputStreamReader    mInputStreamReader;

        public
    LineReaderImpl( InputStream inputStream )
    {
        mInputStreamReader    = new InputStreamReader( inputStream );
    }

        public String
    readLine( String prompt )
        throws java.io.IOException
    {
        final StringBuffer    line    = new StringBuffer();

        if ( prompt != null )
        {
            System.out.print( prompt );
        }

        while ( true )
        {
            final int    value    = mInputStreamReader.read();
            if ( value < 0 )
            {
                if ( line.length() != 0 )
                {
                    // read a line but saw EOF before a newline
                    break;
                }
                return( null );
            }

            final char    theChar    = (char)value;
            if ( theChar == '\n' )
                break;

            line.append( theChar );
        }

        return( line.toString().trim() );
    }
}





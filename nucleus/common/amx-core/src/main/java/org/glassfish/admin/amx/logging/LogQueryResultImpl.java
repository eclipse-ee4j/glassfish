/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.admin.amx.logging;

import java.io.Serializable;
import java.util.List;

import org.glassfish.admin.amx.util.ArrayUtil;
import org.glassfish.admin.amx.util.ObjectUtil;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
    <b>INTERNAL USE ONLY--not part of the API</b>
        @since AS 9.0
 */
@Taxonomy(stability = Stability.EXPERIMENTAL)
public final class LogQueryResultImpl
implements LogQueryResult
{
    private String[]         mFieldNames;
    private LogQueryEntry[]  mEntries;

    public
    LogQueryResultImpl(
        final String[]          fieldNames,
        final LogQueryEntry[]   entries )
    {
        mFieldNames   = fieldNames;
        mEntries      = entries;
    }

    /**
        Instantiate using result from {@link Logging#queryServerLog}.
        The first Object[] is a String[] of the field names.
        Subsequent Object[] are the data values.
     */
    public
    LogQueryResultImpl( final List<Serializable[]> records )
    {
        mFieldNames   = (String[])records.get( 0 );

        mEntries    = new LogQueryEntry[ records.size() - 1 ];
        for( int i = 0; i < mEntries.length; ++i )
        {
            mEntries[ i ]   = new LogQueryEntryImpl( records.get( i+1 ) );
        }
    }

    public String[]
        getFieldNames()
    {
        return mFieldNames;
    }

    public LogQueryEntry[]
        getEntries()
    {
        return mEntries;
    }

    private static final String    FIELD_DELIM = "\t";
    private static final String    NEWLINE = System.getProperty( "line.separator" );;
    /**
        Output a tab-delimited String with a header line. Each
        subsequent line represents another log record.
     */
    public String
    toString()
    {
        final StringBuilder builder = new StringBuilder();

        for( final String s : getFieldNames() )
        {
            builder.append( s );
            builder.append( FIELD_DELIM );
        }
        builder.replace( builder.length() - 1, builder.length(), NEWLINE );

        for ( final LogQueryEntry entry : getEntries() )
        {
            final Object[]  fields  = entry.getFields();
            for( final Object o : fields )
            {
                builder.append( o.toString() );
                builder.append( FIELD_DELIM );
            }
            builder.replace( builder.length() - 1, builder.length(), NEWLINE );
        }

        return builder.toString();
    }

    public int
    hashCode()
    {
        return ObjectUtil.hashCode( getFieldNames(), getEntries() );
    }

    public boolean
    equals( final Object rhs )
    {
        boolean equal   = rhs instanceof LogQueryResult;

        if ( equal )
        {
            final LogQueryResult    r   = (LogQueryResult)rhs;

            equal   = ArrayUtil.arraysEqual( getFieldNames(), r.getFieldNames() ) &&
                ArrayUtil.arraysEqual( getEntries(), r.getEntries() );

        }

        return equal;
    }


}






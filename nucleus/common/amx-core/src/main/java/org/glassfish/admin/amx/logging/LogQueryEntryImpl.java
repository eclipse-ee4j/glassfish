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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.admin.amx.util.ArrayUtil;
import org.glassfish.admin.amx.util.ObjectUtil;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

import static org.glassfish.admin.amx.logging.LogRecordFields.DATE_INDEX;
import static org.glassfish.admin.amx.logging.LogRecordFields.LEVEL_INDEX;
import static org.glassfish.admin.amx.logging.LogRecordFields.MESSAGE_ID_INDEX;
import static org.glassfish.admin.amx.logging.LogRecordFields.MESSAGE_INDEX;
import static org.glassfish.admin.amx.logging.LogRecordFields.MODULE_INDEX;
import static org.glassfish.admin.amx.logging.LogRecordFields.NAME_VALUE_PAIRS_INDEX;
import static org.glassfish.admin.amx.logging.LogRecordFields.NUM_FIELDS;
import static org.glassfish.admin.amx.logging.LogRecordFields.OBJECTNAME_KEY;
import static org.glassfish.admin.amx.logging.LogRecordFields.PRODUCT_NAME_INDEX;
import static org.glassfish.admin.amx.logging.LogRecordFields.RECORD_NUMBER_INDEX;


/**
 * <b>INTERNAL USE ONLY--not part of the API</b>
 *
 * @since AS 9.0
 */
@Taxonomy(stability = Stability.EXPERIMENTAL)
public final class LogQueryEntryImpl
implements LogQueryEntry
{
    private transient Map<String,String>    mNameValuePairsMap;

    final long      mRecordNumber;
    final Date      mDate;
    final String    mLevel;
    final String    mProductName;
    final String    mMessage;
    final String    mMessageID;
    final String    mModule;
    final String    mNameValuePairs;

    public
    LogQueryEntryImpl(
        final long      recordNumber,
        final Date      date,
        final String    level,
        final String    productName,
        final String    module,
        final String    nameValuePairs,
        final String    messageID,
        final String    message)
    {
        if ( date == null || level == null || message == null ||
            nameValuePairs == null )
        {
            throw new IllegalArgumentException();
        }

        mRecordNumber   = recordNumber;
        mDate           = date;
        mLevel          = Level.parse( level ).toString();
        mProductName    = productName;
        mModule         = module;
        mMessage        = message;
        mMessageID      = messageID;
        mNameValuePairs = nameValuePairs;
    }

    public
    LogQueryEntryImpl( final Object[] values )
    {
        if ( values.length != NUM_FIELDS )
        {
            throw new IllegalArgumentException( "wrong number of fields: " + values.length);
        }

        mRecordNumber   = (Long)values[ RECORD_NUMBER_INDEX ];
        mDate           = (Date)values[ DATE_INDEX ];
        mLevel          = Level.parse( (String)values[ LEVEL_INDEX ] ).toString();
        mProductName    = (String)values[ PRODUCT_NAME_INDEX ];
        mMessageID      = (String)values[ MESSAGE_ID_INDEX ];
        mModule         = (String)values[ MODULE_INDEX ];
        mMessage        = (String)values[ MESSAGE_INDEX ];
        mNameValuePairs = (String)values[ NAME_VALUE_PAIRS_INDEX ];
    }

    public Object[]
        getFields()
    {
        final Object[]  fields  = new Object[ NUM_FIELDS ];

        fields[ RECORD_NUMBER_INDEX ]  = mRecordNumber;
        fields[ DATE_INDEX ]           = mDate;
        fields[ LEVEL_INDEX ]          = mLevel;
        fields[ PRODUCT_NAME_INDEX ]   = mProductName;
        fields[ MESSAGE_ID_INDEX ]     = mMessageID;
        fields[ MODULE_INDEX ]         = mModule;
        fields[ MESSAGE_INDEX ]        = mMessage;
        fields[ NAME_VALUE_PAIRS_INDEX ]= mNameValuePairs;

        return fields;
    }

    /*
        public
    LogQueryEntryImpl( final CompositeData data )
    {
        this( OpenMBeanUtil.compositeDataToMap( data ) );
    }
        public CompositeType
    getCompositeType()
        throws OpenDataException
    {
        return OpenMBeanUtil.mapToCompositeType( getMapClassName(),
            getMapClassName(), asMap(), null );
    }

        public CompositeData
    asCompositeData()
        throws OpenDataException
    {
        return new CompositeDataSupport( getCompositeType(), asMap() );
    }

     */


    public long
    getRecordNumber()
    {
        return mRecordNumber;
    }

    public Date
    getDate()
    {
        return mDate;
    }

    public String
    getModule()
    {
        return mModule;
    }

    public String
    getLevel()
    {
        return mLevel;
    }

    public String
    getProductName()
    {
        return mProductName;
    }

    public String
    getMessage()
    {
        return mMessage;
    }

    public String
    getMessageID()
    {
        return mMessageID;
    }

    public String
    getNameValuePairs()
    {
        return mNameValuePairs;
    }

    /** delimiter between name/value pairs */
    private static final String NVP_PAIRS_DELIM = ";";
    /** delimiter between name and value */
    private static final String PAIR_DELIM = "=";

    private Map<String,String>
    parseNameValuePairs()
    {
        final String src    = getNameValuePairs();
        final Map<String,String> m   = new HashMap<String,String>();

        final String[]  pairs   = src.split( NVP_PAIRS_DELIM );

        for( String pair : pairs )
        {
            final int   idx = pair.indexOf( PAIR_DELIM );
            if ( idx < 0 )
            {
                throw new IllegalArgumentException( src );
            }
            final String    name    = pair.substring( 0, idx ).trim();
            final String    value   = pair.substring( idx + 1, pair.length() ).trim();

            m.put( name, value );
        }

        return m;
    }

    public Map<String,String>
    getNameValuePairsMap()
    {
        if ( mNameValuePairsMap == null )
        {
            mNameValuePairsMap  = parseNameValuePairs();
        }

        return mNameValuePairsMap;
    }

    public String
    getThreadID()
    {
        return getNameValuePairsMap().get( THREAD_ID_KEY );
    }

    public String
    getObjectName()
    {
        return getNameValuePairsMap().get( OBJECTNAME_KEY );
    }

    public String
    toString()
    {
        final String D = "|";

        //  [#|DATE|LEVEL|PRODUCT_NAME|MODULE|NAME_VALUE_PAIRS|MESSAGE|#]
        return "[#" +
        getRecordNumber() + D +
        getDate() + D +
        getLevel() + D +
        getProductName() + D +
        getModule() + D +
        getNameValuePairs() + D +
        getMessage() + D +
        getMessageID() + D +
        "]";
    }

    public int
    hashCode()
    {
        return ObjectUtil.hashCode( mDate, mLevel,
            mProductName, mMessage, mMessageID, mModule, mNameValuePairs) ^
            ObjectUtil.hashCode( mRecordNumber );
    }

    public boolean
    equals( final Object rhs )
    {
        boolean  equal   = false;

        if ( this == rhs )
        {
            equal   = true;
        }
        else if ( rhs instanceof LogQueryEntry )
        {
            final LogQueryEntry e   = (LogQueryEntry)rhs;

            equal    = ArrayUtil.arraysEqual( getFields(), e.getFields() );
        }

        return equal;
    }
}







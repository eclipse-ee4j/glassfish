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
import java.util.Map;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
        An individual result representing a log entry found
        by {@link LogQuery#queryServerLog}.

        @since AS 9.0
        @see LogQueryResult
 */
@Taxonomy(stability = Stability.EXPERIMENTAL)
public interface LogQueryEntry
{
    /**
        Get the fields associated with this entry.
        The fields are indexed by the values found in
        {@link LogRecordFields}.  A field is always non-null.
     */
    public Object[] getFields();

    /**
        The record number within the log file (first one is 0).
     */
    public long getRecordNumber();

    /**
        The name of the product.
     */
    public String getProductName();


    /**
        The Date that the log entry was emitted.
     */
    public Date     getDate();

    /**
        The module or Logger that emitted the entry.
     */
    public String   getModule();

    /**
        The Level of the entry.
     */
    public String    getLevel();

    /**
        The unique message ID identifying the entry.
     */
    public String   getMessageID();

    /**
        The free-form message.
     */
    public String   getMessage();


    /**
        Key for the thread ID within the Map returned by {@link #getNameValuePairsMap}.
        Value is of type java.lang.String.
     */
    public static final String  THREAD_ID_KEY   = "_ThreadID";

    /**
        Key for the ObjectName within the Map returned by {@link #getNameValuePairsMap}.
        Value is of type javax.management.ObjectName.
     */
    public static final String  OBJECT_NAME_KEY   = "_ObjectName";

    /**
        A Map containing name/value pairs as parsed
        from the String given by {@link #getNameValuePairs}.
        Values which are available for public use are:
        <ul>
        <li>{@link #THREAD_ID_KEY}</li>
        <li>{@link #OBJECT_NAME_KEY}</li>
        </ul>
     */
    public Map<String,String>   getNameValuePairsMap();

    /**
        The raw name/value pair String for this log entry.  Each
        pair is separated by the ';' character.
     */
    public String   getNameValuePairs();

    /**
        The ID of the thread that emitted the entry (may be null).
     */
    public String   getThreadID();
}







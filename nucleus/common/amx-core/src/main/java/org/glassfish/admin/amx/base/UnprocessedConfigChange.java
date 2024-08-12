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

package org.glassfish.admin.amx.base;

import java.beans.PropertyChangeEvent;

import org.glassfish.admin.amx.util.ObjectUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
<em>Note: this API is highly volatile and subject to change<em>.
<p>
Class representing a change to a configuration attribute.
A PropertyChangeEvent is unsuitable, as its 'source' is transient.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
public final class UnprocessedConfigChange
{
    private final String mName;

    private final String mOldValue;

    private final String mNewValue;

    private final Object mSource;

    private final String mReason;

    /** indicates that the change represents more than one property. The old/new values are arbitrary */
    public static final String MULTI = "*";

    public Object[] toArray()
    {
        return new Object[]
                {
                    mName, mOldValue, mNewValue, mSource, mReason
                };
    }

    /** must match the order in {@link #toArray} */
    public UnprocessedConfigChange(final Object[] data)
    {
        this((String) data[0], (String) data[1], (String) data[2], data[3], (String) data[4]);

        // nice to do this first, but compiler won't allow it!
        if (data.length != 5)
        {
            throw new IllegalArgumentException();
        }
    }

    public UnprocessedConfigChange(
            final String name,
            final String oldValue,
            final String newValue,
            final Object source,
            final String reason)
    {
        mReason = reason == null ? "unspecified" : reason;
        mName = name;
        mSource = source;
        mOldValue = oldValue;
        mNewValue = newValue;
    }

    public UnprocessedConfigChange(final String reason, final PropertyChangeEvent e)
    {
        this(e.getPropertyName(), "" + e.getOldValue(), "" + e.getNewValue(), e.getSource(), reason);
    }

    /** The (human readable) reason the change could not be made. */
    public String getReason()
    {
        return mReason;
    }

    /** name of the property.  Can be null */
    public String getPropertyName()
    {
        return mName;
    }

    /**
    Preferred value is an ObjectName, otherwise a String suitable for a user to understand
    what might have been affected.  Can be null.
     */
    public Object getSource()
    {
        return mSource;
    }

    /** Old value of the property.  Can be null */
    public String getOldValue()
    {
        return mOldValue;
    }

    /** New value of the property.  Can be null */
    public String getNewValue()
    {
        return mNewValue;
    }

    @Override
    public String toString()
    {
        return "UnprocessedConfigChange: name = " + getPropertyName() +
               ", source = " + getSource() +
               ", oldValue = " + StringUtil.quote( getOldValue() ) +
               ", newValue = " + StringUtil.quote( getNewValue() ) +
               ", reason = " + StringUtil.quote("" + getReason());
    }

    private boolean eq(final Object lhs, final Object rhs)
    {
        if (lhs == rhs)
        {
            return true;
        }

        return lhs != null ? lhs.equals(rhs) : false;

    }

    @Override
    public boolean equals(final Object rhs)
    {
        if (!(rhs instanceof UnprocessedConfigChange))
        {
            return false;
        }

        final UnprocessedConfigChange x = (UnprocessedConfigChange) rhs;

        return eq(mName, x.mName) &&
               eq(mOldValue, x.mOldValue) &&
               eq(mNewValue, x.mNewValue) &&
               eq(mSource, x.mSource) &&
               eq(mReason, x.mReason);
    }

    @Override
    public int hashCode()
    {
        return ObjectUtil.hashCode(mName, mOldValue, mNewValue, mSource, mReason);
    }

}




























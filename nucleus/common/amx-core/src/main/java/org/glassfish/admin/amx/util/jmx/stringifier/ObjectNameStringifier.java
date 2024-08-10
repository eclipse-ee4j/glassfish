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

package org.glassfish.admin.amx.util.jmx.stringifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.TypeCast;
import org.glassfish.admin.amx.util.stringifier.Stringifier;

/**
Stringifier for an ObjectName which sorts the properties in the ObjectName
for more consistent and readable output.
 */
public final class ObjectNameStringifier implements Stringifier
{
    public final static ObjectNameStringifier DEFAULT = new ObjectNameStringifier();

    private static List<String> PROPS = null;

    private synchronized static List<String> getPROPS()
    {
        if (PROPS == null)
        {
            PROPS = Collections.unmodifiableList(ListUtil.newListFromArray(new String[]
                    {
                        "j2eeType", "type",
                        "name",
                        "J2EEDomain",
                        "J2EEServer",
                        "JVM",
                        "Node",
                        "J2EEApplication",
                        "AppClientModule",
                        "EJBModule",
                        "EntityBean",
                        "StatefulSessionBean",
                        "StatelessSessionBean",
                        "MessageDrivenBean",
                        "WebModule", "Servlet",
                        "ResourceAdapterModule",
                        "JavaMailResource",
                        "JCAResource",
                        "JCAConnectionFactory",
                        "JCAManagedConnectionFactory",
                        "JDBCResource",
                        "JDBCDataSource",
                        "JDBCDriver",
                        "JMSResource",
                        "JNDIResource",
                        "JTAResource",
                        "RMI_IIOPResource",
                        "URL_Resource",
                    }));
        }
        return (PROPS);
    }

    private List<String> mOrderedProps;

    private boolean mOmitDomain;

    public ObjectNameStringifier()
    {
        this(getPROPS());
    }

    public ObjectNameStringifier(final List<String> props)
    {
        mOrderedProps = props;
        mOmitDomain = false;
    }

    public ObjectNameStringifier(final String[] props)
    {
        this(ListUtil.newListFromArray(props));
    }

    private String makeProp(final String name, final String value)
    {
        return (name + "=" + value);
    }

    public String stringify(Object o)
    {
        if (o == null)
        {
            return ("null");
        }


        final ObjectName on = (ObjectName) o;

        final StringBuffer buf = new StringBuffer();
        if (!mOmitDomain)
        {
            buf.append(on.getDomain() + ":");
        }

        final Map<String, String> props = TypeCast.asMap(on.getKeyPropertyList());

        final List<String> ordered = new ArrayList<String>(mOrderedProps);
        ordered.retainAll(props.keySet());

        // go through each ordered property, and if it exists, emit it
        final Iterator<String> iter = ordered.iterator();
        while (iter.hasNext() && props.keySet().size() >= 2)
        {
            final String key = iter.next();
            final String value = props.get(key);
            if (value != null)
            {
                buf.append(makeProp(key, value) + ",");
                props.remove(key);
            }
        }

        // emit all remaining properties in order
        final Set<String> remainingSet = props.keySet();
        final String[] remaining = new String[remainingSet.size()];
        remainingSet.toArray(remaining);
        Arrays.sort(remaining);

        for (int i = 0; i < remaining.length; ++i)
        {
            final String key = remaining[i];
            final String value = props.get(key);
            buf.append(makeProp(key, value) + ",");
        }

        final String result = StringUtil.stripSuffix(buf.toString(), ",");

        return (result);
    }

    public List getProps()
    {
        return (mOrderedProps);
    }

    public void setProps(final List<String> props)
    {
        mOrderedProps = props;
    }

    public boolean getOmitDomain()
    {
        return (mOmitDomain);
    }

    public void setOmitDomain(final boolean omit)
    {
        mOmitDomain = omit;
    }

}

























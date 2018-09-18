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

package org.glassfish.admin.amxtest.base;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import org.glassfish.admin.amxtest.AMXTestBase;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 */
public final class GetSetAttributeTest
        extends AMXTestBase {
    public GetSetAttributeTest() {
    }


    private static final Set<String> SKIP_IDENTITY_SET_TEST =
            Collections.unmodifiableSet(GSetUtil.newStringSet(
                    "DynamicReconfigurationEnabled"
            ));

    private void
    testGetSetAttributes(final AMX amx)
            throws Exception {
        final ObjectName objectName = Util.getObjectName(amx);

        boolean skipIdentitySet = false;
        if (amx.getJ2EEType().equals(XTypes.CONFIG_DOTTED_NAMES)) {
            skipIdentitySet = true;
            trace("GetSetAttributeTest.testGetSetAttributes: skipping identity set for " + objectName +
                    " because too many Attributes misbehave.");
        }

        final MBeanServerConnection conn = getMBeanServerConnection();
        final MBeanInfo mbeanInfo = Util.getExtra(amx).getMBeanInfo();

        final Map<String, MBeanAttributeInfo> attrInfos =
                JMXUtil.attributeInfosToMap(mbeanInfo.getAttributes());
        final String[] attrNames = GSetUtil.toStringArray(attrInfos.keySet());

        // get all the Attributes
        final AttributeList values = conn.getAttributes(objectName, attrNames);

        final Map<String, Object> valuesMap = JMXUtil.attributeListToValueMap(values);

        final Set<String> getFailed = new HashSet<String>();
        final Map<String, Object> setFailed = new HashMap<String, Object>();

        for (final MBeanAttributeInfo attrInfo : attrInfos.values()) {
            final String name = attrInfo.getName();
            if (!valuesMap.keySet().contains(name)) {
                getFailed.add(name);
                continue;
            }

            if (attrInfo.isReadable()) {
                final Object value = valuesMap.get(name);

                if (attrInfo.isWritable() && (!skipIdentitySet)) {
                    if (SKIP_IDENTITY_SET_TEST.contains(name)) {
                        trace("Skipping identity-set check for known problem attribute " +
                                StringUtil.quote(name) +
                                " of MBean " + JMXUtil.toString(objectName));
                    } else {
                        // set it to the same value as before
                        try {
                            final Attribute attr = new Attribute(name, value);
                            conn.setAttribute(objectName, attr);
                        }
                        catch (Exception e) {
                            setFailed.put(name, value);

                            warning("Could not set Attribute " + name + " of MBean " +
                                    StringUtil.quote(objectName) +
                                    " to the same value: " +
                                    StringUtil.quote("" + value));
                        }
                    }
                }
            }
        }

        if (getFailed.size() != 0) {
            warning("(SUMMARY) Could not get Attributes for " +
                    StringUtil.quote(objectName) + NEWLINE +
                    CollectionUtil.toString(getFailed, NEWLINE));

            for (final String attrName : getFailed) {
                try {
                    final Object value = conn.getAttribute(objectName, attrName);
                    warning("Retry of Attribute " +
                            attrName + " succeed with value " + value);
                }
                catch (Exception e) {
                    warning("Attribute " + attrName + " failed with " +
                            e.getClass() + ": " + e.getMessage());
                }
            }
        }

        if (setFailed.size() != 0) {
            warning("(SUMMARY) Could not identity-set Attributes for " +
                    StringUtil.quote(objectName) + NEWLINE +
                    MapUtil.toString(setFailed, NEWLINE));
        }
    }


    public void
    testGetSetAttributes()
            throws Exception {
        final Set<AMX> all = getAllAMX();

        for (final AMX amx : all) {
            testGetSetAttributes(amx);
        }
    }
}

















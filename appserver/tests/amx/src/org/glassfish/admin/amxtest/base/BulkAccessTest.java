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

import com.sun.appserv.management.base.AMXAttributes;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ArrayUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.HashSet;
import java.util.Set;

/**
 */
public final class BulkAccessTest
        extends AMXTestBase {
    public BulkAccessTest() {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(true);
    }


    public void
    testGetBulkAccess() {
        assert (getBulkAccess() != null);
    }

    public void
    testBulkGetMBeanAttributeInfos()
            throws Exception {
        final long start = now();

        final ObjectName[] objectNames = getTestUtil().getAllAMXArray();

        // get everything in bulk....
        final Object[] infos =
                getBulkAccess().bulkGetMBeanAttributeInfo(objectNames);

        // now verify that getting it singly yields the same result.
        final MBeanServerConnection conn = getConnection();
        for (int i = 0; i < infos.length; ++i) {

            final MBeanAttributeInfo[] bulkAttributes = (MBeanAttributeInfo[]) infos[i];

            final MBeanInfo info = conn.getMBeanInfo(objectNames[i]);
            assert (ArrayUtil.arraysEqual(info.getAttributes(), bulkAttributes));
        }
        printElapsed("testBulkGetMBeanAttributeInfos", objectNames.length, start);
    }

    public void
    testBulkGetMBeanOperationInfos()
            throws Exception {
        final long start = now();

        final ObjectName[] objectNames = getTestUtil().getAllAMXArray();

        final Object[] infos =
                getBulkAccess().bulkGetMBeanOperationInfo(objectNames);

        // now verify that getting it singly yields the same result.
        final MBeanServerConnection conn = getConnection();
        for (int i = 0; i < infos.length; ++i) {

            final MBeanOperationInfo[] bulkOperations = (MBeanOperationInfo[]) infos[i];

            final MBeanInfo info = conn.getMBeanInfo(objectNames[i]);
            assert (ArrayUtil.arraysEqual(info.getOperations(), bulkOperations));
        }
        printElapsed("testBulkGetMBeanOperationInfos", objectNames.length, start);
    }

    public void
    testAttributeNamesAttributeCorrect()
            throws Exception {
        final long start = now();

        final ObjectName[] objectNames = getTestUtil().getAllAMXArray();

        final Object[] nameArrays =
                getBulkAccess().bulkGetAttributeNames(objectNames);

        final Set<ObjectName> failed = new HashSet<ObjectName>();
        // now verify that getting it singly yields the same result.
        for (int i = 0; i < nameArrays.length; ++i) {
            final String[] bulkNames = (String[]) nameArrays[i];

            // verify that the AttributeNames Attribute contains all the names
            final String[] attrNames = (String[])
                    getConnection().getAttribute(objectNames[i], "AttributeNames");

            final Set<String> bulkSet = GSetUtil.newStringSet(bulkNames);
            final Set<String> attrsSet = GSetUtil.newStringSet(attrNames);
            if (!bulkSet.equals(attrsSet)) {
                warning("testAttributeNamesAttributeCorrect failed for " + objectNames[i]);
                failed.add(objectNames[i]);
            }
        }

        if (failed.size() != 0) {
            assert false : "Failures: " + NEWLINE + CollectionUtil.toString(failed, NEWLINE);
        }

        printElapsed("testAttributeNamesAttributeCorrect", objectNames.length, start);
    }

    public void
    testBulkGetMBeanAttributeNames()
            throws Exception {
        final long start = now();

        final ObjectName[] objectNames = getTestUtil().getAllAMXArray();

        final Object[] nameArrays =
                getBulkAccess().bulkGetAttributeNames(objectNames);

        for (int i = 0; i < nameArrays.length; ++i) {
            final String[] bulkNames = (String[]) nameArrays[i];

            final MBeanInfo info =
                    getConnection().getMBeanInfo(objectNames[i]);

            final String[] names =
                    JMXUtil.getAttributeNames(info.getAttributes());

            assert (ArrayUtil.arraysEqual(names, bulkNames));
        }

        printElapsed("testBulkGetMBeanAttributeNames", objectNames.length, start);
    }

    public void
    testBulkGetAttribute()
            throws Exception {
        final long start = now();

        final String attrName = AMXAttributes.ATTR_OBJECT_NAME;
        final ObjectName[] objectNames = getTestUtil().getAllAMXArray();

        final Object[] values =
                getBulkAccess().bulkGetAttribute(objectNames, attrName);

        final MBeanServerConnection conn = getConnection();
        for (int i = 0; i < objectNames.length; ++i) {
            final Object value = conn.getAttribute(objectNames[i], attrName);

            assertEquals(values[i], value);
        }

        printElapsed("testBulkGetAttribute", objectNames.length, start);
    }


    public void
    testBulkGetAttributes()
            throws Exception {
        final long start = now();

        final String[] attrNames = new String[]{
                "FullType", "Group", "Name", "DomainRootObjectName", "ContainerObjectName"};
        final ObjectName[] objectNames = getTestUtil().getAllAMXArray();

        final Object[] values =
                getBulkAccess().bulkGetAttributes(objectNames, attrNames);

        final MBeanServerConnection conn = getConnection();
        for (int i = 0; i < objectNames.length; ++i) {
            final AttributeList bulkAttrs = (AttributeList) values[i];

            final AttributeList attrs = (AttributeList) conn.getAttributes(objectNames[i], attrNames);

            assertEquals(bulkAttrs, attrs);
        }
        printElapsed("testBulkGetAttributes", objectNames.length, start);
    }

}



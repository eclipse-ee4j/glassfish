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

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.glassfish.admin.amx.util.stringifier.StringifierRegistry;

/**
Registers all included stringifiers with the default registry.
 */
public class StringifierRegistryIniter extends org.glassfish.admin.amx.util.stringifier.StringifierRegistryIniterImpl
{
    public StringifierRegistryIniter(StringifierRegistry registry)
    {
        super(registry);

        add(ObjectName.class, ObjectNameStringifier.DEFAULT);
        add(MBeanInfo.class, MBeanInfoStringifier.DEFAULT);
        add(ModelMBeanInfo.class, ModelMBeanInfoStringifier.DEFAULT);

        add(MBeanOperationInfo.class, MBeanOperationInfoStringifier.DEFAULT);
        add(ModelMBeanOperationInfo.class, ModelMBeanOperationInfoStringifier.DEFAULT);

        add(MBeanAttributeInfo.class, MBeanAttributeInfoStringifier.DEFAULT);
        add(ModelMBeanAttributeInfo.class, ModelMBeanAttributeInfoStringifier.DEFAULT);

        add(MBeanParameterInfo.class, MBeanParameterInfoStringifier.DEFAULT);

        add(MBeanNotificationInfo.class, MBeanNotificationInfoStringifier.DEFAULT);
        add(ModelMBeanNotificationInfo.class, ModelMBeanNotificationInfoStringifier.DEFAULT);

        add(MBeanConstructorInfo.class, MBeanConstructorInfoStringifier.DEFAULT);
        add(ModelMBeanConstructorInfo.class, ModelMBeanConstructorInfoStringifier.DEFAULT);

        add(Attribute.class, AttributeStringifier.DEFAULT);
        add(AttributeList.class, AttributeListStringifier.DEFAULT);

        add(Notification.class, NotificationStringifier.DEFAULT);
        add(AttributeChangeNotification.class, AttributeChangeNotificationStringifier.DEFAULT);
        add(MBeanServerNotification.class, MBeanServerNotificationStringifier.DEFAULT);


        add(CompositeData.class, CompositeDataStringifier.DEFAULT);
        add(CompositeDataSupport.class, CompositeDataStringifier.DEFAULT);
        add(TabularData.class, TabularDataStringifier.DEFAULT);
        add(TabularDataSupport.class, TabularDataStringifier.DEFAULT);
    }

}




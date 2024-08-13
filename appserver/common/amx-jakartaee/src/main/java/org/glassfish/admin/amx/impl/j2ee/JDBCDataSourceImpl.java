/*
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.j2ee;

import javax.management.ObjectName;

import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.j2ee.J2EEManagedObject;
import org.glassfish.admin.amx.j2ee.J2EETypes;
import org.glassfish.admin.amx.j2ee.JDBCDataSource;

public final class JDBCDataSourceImpl extends J2EEManagedObjectImplBase {
    public static final Class<? extends J2EEManagedObject> INTF = JDBCDataSource.class;

    public JDBCDataSourceImpl(final ObjectName parentObjectName, final Metadata meta) {
        super(parentObjectName, meta, INTF);
    }

    public String getjdbcDriver()
    {
        final ObjectName objectName = child( J2EETypes.JDBC_DRIVER );

        return objectName == null ? null : objectName.toString();
    }

    @Override
    protected void registerChildren()
    {
        super.registerChildren();

        // register a JDBCDriver as per JSR 77 spec requirements.  We have only one.
        final JDBCDriverImpl driverImpl = new JDBCDriverImpl( getObjectName(), defaultChildMetadata() );
        ObjectName driverON = new ObjectNameBuilder( getMBeanServer(), getObjectName()).buildChildObjectName(J2EETypes.JDBC_DRIVER, getName());
        registerChild( driverImpl, driverON );
    }

}

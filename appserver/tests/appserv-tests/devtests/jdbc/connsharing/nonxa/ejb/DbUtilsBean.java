/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.s1asdev.jdbc.connsharing.nonxa.ejb;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.Set;
import java.util.HashSet;

@Singleton
public class DbUtilsBean {

    @Resource(mappedName="jdbc/connsharing")
    private DataSource myworkDatabase;

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Connection getConnection() throws SQLException {
        Connection connection = myworkDatabase.getConnection();
        // Here additional checks could be added for application usage:
        // like call statistic counters or thread stack validation.
        SimpleSessionBean.getPhysicalConnectionAndLog(myworkDatabase, connection);
        return connection;
    }
}

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

/*
 * DeploymentHelper.java
 *
 * Created on September 30, 2003.
 */

package com.sun.jdo.spi.persistence.support.sqlstore.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.sql.DataSource;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.jdo.api.persistence.support.JDOFatalUserException;
import com.sun.jdo.spi.persistence.support.sqlstore.LogHelperPersistenceManager;
import com.sun.jdo.spi.persistence.utility.StringHelper;
import com.sun.jdo.spi.persistence.utility.logging.Logger;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.persistence.common.DatabaseConstants;
import org.glassfish.persistence.common.I18NHelper;
import org.glassfish.persistence.common.Java2DBProcessorHelper;

/**
 * This class is used for static method invocations to avoid unnecessary
 * registration requirements to use EJBHelper and/or CMPHelper from
 * deploytool, verifier, or any other stand-alone client.
 *
 */
public class DeploymentHelper
    {

    /** I18N message handler */
    private final static ResourceBundle messages = I18NHelper.loadBundle(
        "com.sun.jdo.spi.persistence.support.sqlstore.Bundle", // NOI18N
        DeploymentHelper.class.getClassLoader());

    /** The logger */
    private static Logger logger = LogHelperPersistenceManager.getLogger();

    /**
     * Returns name prefix for DDL files extracted from the info instance by the
     * Sun-specific code.
     *
     * @param info the instance to use for the name generation.
     * @return name prefix as String.
     */
    public static String getDDLNamePrefix(Object info) {
        return Java2DBProcessorHelper.getDDLNamePrefix(info);
    }

    /**
     * Returns boolean value for the <code>DatabaseConstants.JAVA_TO_DB_FLAG</code>
     * flag in this Properties object.
     * @param prop a Properties object where flag is located
     * @return true if there is a property value that contains "true" as
     * the value for the <code>DatabaseConstants.JAVA_TO_DB_FLAG</code>
     * key.
     */
    public static boolean isJavaToDatabase(Properties prop) {
        if (prop != null) {
            String value = prop.getProperty(DatabaseConstants.JAVA_TO_DB_FLAG);
            if (! StringHelper.isEmpty(value)) {
                 if (logger.isLoggable(Logger.FINE))
                     logger.fine(DatabaseConstants.JAVA_TO_DB_FLAG + " property is set."); // NOI18N
                 return Boolean.valueOf(value).booleanValue();
            }
        }
        return false;
    }

    /** Get a Connection from the resource specified by the JNDI name
     * of a CMP resource.
     * This connection is aquired from a non-transactional resource which does not
     * go through transaction enlistment/delistment.
     * The deployment processing is required to use only those connections.
     *
     * @param name JNDI name of a cmp-resource for the connection.
     * @return a Connection.
     * @throws JDOFatalUserException if name cannot be looked up, or we
     * cannot get a connection based on the name.
     * @throws SQLException if can not get a Connection.
     */
    public static Connection getConnection(String name) throws SQLException {
        if (logger.isLoggable(logger.FINE)) {
            logger.fine("ejb.DeploymentHelper.getconnection", name); //NOI18N
        }

        // TODO - pass Habitat or ConnectorRuntime as an argument.

        ServiceLocator habitat = Globals.getDefaultHabitat();
        DataSource ds = null;
        try {
            ConnectorRuntime connectorRuntime = habitat.getService(ConnectorRuntime.class);
            ds = DataSource.class.cast(connectorRuntime.lookupNonTxResource(name, true));
        } catch (Exception e) {
            throw new JDOFatalUserException(
                I18NHelper.getMessage(messages,
                        "ejb.jndi.lookupfailed", name)); //NOI18N
        }
        return ds.getConnection();
    }

    /** Create a RuntimeException for unexpected instance returned
     * from JNDI lookup.
     *
     * @param name the JNDI name that had been looked up.
     * @param value the value returned from the JNDI lookup.
     * @throws JDOFatalUserException.
     */
    private static void handleUnexpectedInstance(String name, Object value) {
        RuntimeException e = new JDOFatalUserException(
                I18NHelper.getMessage(messages,
                        "ejb.jndi.unexpectedinstance", //NOI18N
                        name, value.getClass().getName()));
        logger.severe(e.toString());

        throw e;

    }
}

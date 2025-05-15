/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.util;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.util.net.NetUtils;

import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.config.TransactionFailure;

import static com.sun.enterprise.config.util.PortConstants.ADMIN;
import static com.sun.enterprise.config.util.PortConstants.DEBUG;
import static com.sun.enterprise.config.util.PortConstants.HTTP;
import static com.sun.enterprise.config.util.PortConstants.HTTPS;
import static com.sun.enterprise.config.util.PortConstants.IIOP;
import static com.sun.enterprise.config.util.PortConstants.IIOPM;
import static com.sun.enterprise.config.util.PortConstants.IIOPS;
import static com.sun.enterprise.config.util.PortConstants.JMS;
import static com.sun.enterprise.config.util.PortConstants.JMX;
import static com.sun.enterprise.config.util.PortConstants.OSGI;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_ADMINPORT_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_DEBUG_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_HTTPSSL_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_IIOPMUTUALAUTH_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_IIOPSSL_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_IIOP_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_INSTANCE_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_JMS_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_JMX_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_OSGI_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORT_MAX_VAL;

/**
 * Port base utilities used by create-local-instance. Similar to create-domain.
 *
 * @author Jennifer
 */
public class PortBaseHelper {

    final private static LocalStringsImpl strings = new LocalStringsImpl(PortBaseHelper.class);

    public PortBaseHelper(Server instance, String portbase, boolean checkports, Logger logger) {
        portBase = portbase;
        checkPorts = checkports;
        _logger = logger;
        _server = instance;
    }

    public void verifyPortBase() throws TransactionFailure {
        if (usePortBase()) {
            final int portbase = convertPortStr(portBase);
            setOptionsWithPortBase(portbase);
        }
    }

    public String getAdminPort() {
        return adminPort;
    }

    public String getInstancePort() {
        return instancePort;
    }

    public String getHttpsPort() {
        return httpsPort;
    }

    public String getIiopPort() {
        return iiopPort;
    }

    public String getIiopsPort() {
        return iiopsPort;
    }

    public String getIiopmPort() {
        return iiopmPort;
    }

    public String getJmsPort() {
        return jmsPort;
    }

    public String getJmxPort() {
        return jmxPort;
    }

    public String getOsgiPort() {
        return osgiPort;
    }

    public String getDebugPort() {
        return debugPort;
    }

    /**
     * Converts the port string to port int
     *
     * @param port the port number
     * @return the port number as an int
     * @throws TransactionFailure if port string is not numeric
     */
    private int convertPortStr(final String port) throws TransactionFailure {
        try {
            return Integer.parseInt(port);
        } catch (Exception e) {
            throw new TransactionFailure(strings.get("InvalidPortNumber", port));
        }
    }

    /**
     * Check if portbase option is specified.
     */
    private boolean usePortBase() throws TransactionFailure {
        if (portBase != null) {
            return true;
        }
        return false;
    }

    private void setOptionsWithPortBase(final int portbase) throws TransactionFailure {
        // set the option name and value in the options list
        verifyPortBasePortIsValid(ADMIN, portbase + PORTBASE_ADMINPORT_SUFFIX);
        adminPort = String.valueOf(portbase + PORTBASE_ADMINPORT_SUFFIX);

        verifyPortBasePortIsValid(HTTP, portbase + PORTBASE_INSTANCE_SUFFIX);
        instancePort = String.valueOf(portbase + PORTBASE_INSTANCE_SUFFIX);

        verifyPortBasePortIsValid(HTTPS, portbase + PORTBASE_HTTPSSL_SUFFIX);
        httpsPort = String.valueOf(portbase + PORTBASE_HTTPSSL_SUFFIX);

        verifyPortBasePortIsValid(IIOPS, portbase + PORTBASE_IIOPSSL_SUFFIX);
        iiopsPort = String.valueOf(portbase + PORTBASE_IIOPSSL_SUFFIX);

        verifyPortBasePortIsValid(IIOPM, portbase + PORTBASE_IIOPMUTUALAUTH_SUFFIX);
        iiopmPort = String.valueOf(portbase + PORTBASE_IIOPMUTUALAUTH_SUFFIX);

        verifyPortBasePortIsValid(JMS, portbase + PORTBASE_JMS_SUFFIX);
        jmsPort = String.valueOf(portbase + PORTBASE_JMS_SUFFIX);

        verifyPortBasePortIsValid(IIOP, portbase + PORTBASE_IIOP_SUFFIX);
        iiopPort = String.valueOf(portbase + PORTBASE_IIOP_SUFFIX);

        verifyPortBasePortIsValid(JMX, portbase + PORTBASE_JMX_SUFFIX);
        jmxPort = String.valueOf(portbase + PORTBASE_JMX_SUFFIX);

        verifyPortBasePortIsValid(OSGI, portbase + PORTBASE_OSGI_SUFFIX);
        osgiPort = String.valueOf(portbase + PORTBASE_OSGI_SUFFIX);

        verifyPortBasePortIsValid(DEBUG, portbase + PORTBASE_DEBUG_SUFFIX);
        debugPort = String.valueOf(portbase + PORTBASE_DEBUG_SUFFIX);
    }

    /**
     * Verify that the portbase port is valid Port must be greater than 0 and less than 65535. This method will also check
     * if the port is in used.
     *
     * @param portNum the port number to verify
     * @throws TransactionFailure if Port is not valid
     * @throws TransactionFailure if port number is not a numeric value.
     */
    private void verifyPortBasePortIsValid(String portName, int portNum) throws TransactionFailure {
        if (portNum <= 0 || portNum > PORT_MAX_VAL) {
            throw new TransactionFailure(strings.get("InvalidPortBaseRange", portNum, portName));
        }
        if (checkPorts && !NetUtils.isPortFree(portNum)) {
            throw new TransactionFailure(strings.get("PortBasePortInUse", portNum, portName));
        }
        _logger.log(Level.FINER, ConfigApiLoggerInfo.portBaseHelperPort, portNum);
    }

    public void setPorts() throws TransactionFailure, PropertyVetoException {
        if (portBase != null) {
            setSystemProperty(ADMIN, getAdminPort());
            setSystemProperty(HTTP, getInstancePort());
            setSystemProperty(HTTPS, getHttpsPort());
            setSystemProperty(IIOP, getIiopPort());
            setSystemProperty(IIOPM, getIiopmPort());
            setSystemProperty(IIOPS, getIiopsPort());
            setSystemProperty(JMS, getJmsPort());
            setSystemProperty(JMX, getJmxPort());
            setSystemProperty(OSGI, getOsgiPort());
            setSystemProperty(DEBUG, getDebugPort());
        }
    }

    private void setSystemProperty(String name, String value) throws TransactionFailure, PropertyVetoException {
        SystemProperty sp = _server.getSystemProperty(name);
        if (sp == null) {
            SystemProperty newSP = _server.createChild(SystemProperty.class);
            newSP.setName(name);
            newSP.setValue(value);
            _server.getSystemProperty().add(newSP);
        } else {
            //Don't change the system property if it already exists - leave the original port assignment
            //sp.setName(name);
            //sp.setValue(value);
        }
    }

    private String portBase;
    private boolean checkPorts;
    private String adminPort;
    private String instancePort;
    private String httpsPort;
    private String iiopPort;
    private String iiopmPort;
    private String iiopsPort;
    private String jmsPort;
    private String jmxPort;
    private String osgiPort;
    private String debugPort;
    private Logger _logger;
    private Server _server;
}

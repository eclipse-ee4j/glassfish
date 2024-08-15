/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;

import java.text.NumberFormat;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

/**
 */
public class JVMInformationCollector extends StandardMBean implements JVMInformationMBean {

    static final String SERVER_NAME_KEY_IN_ON = "server"; // the key to identify the server
    private MBeanServerConnection mbsc;
    private static final StringManager sm = StringManager.getManager(JVMInformationCollector.class);
    public JVMInformationCollector() throws NotCompliantMBeanException {
        super(JVMInformationMBean.class);
    }
    @Override
    public String getThreadDump(final String processName) {
        final ObjectName on = processTarget(processName);
        final String title = sm.getString("thread.dump.title", getInstanceNameFromObjectName(on));
        final String td = title + "\n" + invokeMBean(on, "getThreadDump");
        return ( td );
    }

    @Override
    public String getSummary(final String processName) {
        final ObjectName on = processTarget(processName);
        final String title = sm.getString("summary.title", getInstanceNameFromObjectName(on));
        final String s = title + "\n" + invokeMBean(on, "getSummary");
        return ( s );
    }

    @Override
    public String getMemoryInformation(final String processName) {
        final ObjectName on = processTarget(processName);
        final String title = sm.getString("memory.info.title", getInstanceNameFromObjectName(on));
        final String mi = title + "\n" + invokeMBean(on, "getMemoryInformation");
        return ( mi );
    }

    @Override
    public String getClassInformation(final String processName) {
        final ObjectName on = processTarget(processName);
        final String title = sm.getString("class.info.title", getInstanceNameFromObjectName(on));
        final String ci = title + "\n " + invokeMBean(on, "getClassInformation");
        return ( ci );
    }
    @Override
    public String getLogInformation(String processName) {
        ObjectName on  = processTarget(processName);
        String title   = sm.getString("log.info.title", getInstanceNameFromObjectName(on));
        String li      = title + "\n" + invokeMBean(on, "getLogInformation");
        return ( li );
    }

    private ObjectName processTarget(final String processName) throws RuntimeException {
        try {
            //get the object-name of the "other" real implementation of JVMInformationMBean interface :)
            final String sn = processName == null ? SERVER_NAME_KEY_IN_ON : processName;
            final String cn = JVMInformation.class.getSimpleName();
            final ObjectName on = formObjectName(sn, cn);
            if (! this.mbsc.isRegistered(on)) {
                final String msg = sm.getString("server.unreachable", sn);
                throw new RuntimeException(msg);
            }
            return (on);
        } catch (final RuntimeException re) {
            throw(re);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeMBean(final ObjectName jvm, final String method) throws RuntimeException {
        try {
            //though proxies work fine, for now (jul 2005/8), I am not going to use them because I am not sure how they work with cascading
            //it is okay to assume that the methods in this mbean take String as parameter
            final Object[] params   = {null};
            final String[] sign     = {"java.lang.String"};
            final Object ret        = this.mbsc.invoke(jvm, method, params, sign);

            return ( (String) ret );

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void postRegister(Boolean registrationDone) {
    }

    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception {
        this.mbsc = server;
        final String sn = System.getProperty(SystemPropertyConstants.SERVER_NAME);
        final ObjectName on = formObjectName(sn, JVMInformationCollector.class.getSimpleName());
        return ( on );
    }

    @Override
    public void preDeregister() throws Exception {
    }

    @Override
    public void postDeregister() {
    }

    /* package private */ static ObjectName formObjectName(final String sn, final String cName) throws Exception {
        /* domain:type=impl-class,server=target-server*/
        final String domain = "amx-internal";
        final Hashtable<String, String> props = new Hashtable<String, String> ();
        props.put("type", cName);
        props.put("category", "monitor");
        final String snk = SERVER_NAME_KEY_IN_ON;
        props.put(snk, sn);
        return ( new ObjectName(domain, props) );
    }

    private String getInstanceNameFromObjectName(ObjectName on) {
        return ( on.getKeyProperty(SERVER_NAME_KEY_IN_ON) );
    }

    static String millis2HoursMinutesSeconds(final long millis) {
        final long secmin = millis / (long) 1000;
        final long sec = secmin % 60;
        final long minhr = secmin / 60;
        final long min = minhr % 60;
        final long hr = minhr / 60;
        final String msg = sm.getString("m2hms", hr, min, sec);

        return ( msg );
    }
    static String millis2SecondsMillis(final long millis) {
        final long sec    = millis / (long) 1000;
        final long ms     = millis % 1000;
        final String msg  = sm.getString("m2sms", sec, ms);
        return ( msg );
    }
    static String formatLong(final long sayBytes) {
        final NumberFormat n = NumberFormat.getInstance();
        return ( n.format(sayBytes) );
    }
}

/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.amx.impl.mbean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.*;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.AMXValidator;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.util.AMXLoggerInfo;
import org.glassfish.admin.amx.util.jmx.JMXUtil;

/**
Validates AMX MBeans as they are registered.  Problems are emitted as WARNING to the server log.
 */
public final class ComplianceMonitor implements NotificationListener {

    private static ComplianceMonitor INSTANCE = null;
    private final DomainRoot mDomainRoot;
    private final MBeanServer mServer;
    private volatile boolean mStarted = false;
    private volatile String mValidationLevel;
    private volatile boolean mUnregisterNonCompliant;
    private volatile boolean mLogInaccessibleAttributes;
    /** offloads the validation so as not to block during Notifications */
    private final ValidatorThread mValidatorThread;

    private final Logger mLogger = AMXLoggerInfo.getLogger();

    private ComplianceMonitor(final DomainRoot domainRoot) {
        mDomainRoot = domainRoot;

        mServer = (MBeanServer) domainRoot.extra().mbeanServerConnection();


        mValidationLevel = "full";
        mUnregisterNonCompliant = false;
        mLogInaccessibleAttributes = true;

        mValidatorThread = new ValidatorThread(mServer, mValidationLevel, mUnregisterNonCompliant, mLogInaccessibleAttributes);

        mLogger.log(Level.INFO, AMXLoggerInfo.aMXComplianceMonitorLevel, new Object[] {mValidationLevel, mUnregisterNonCompliant,
                mLogInaccessibleAttributes});
    }

    public Map<ObjectName, AMXValidator.ProblemList> getComplianceFailures() {
        return mValidatorThread.getComplianceFailures();
    }

    private void listen() {
        try {
            JMXUtil.listenToMBeanServerDelegate(mServer, this, null, null);

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        // queue all existing MBeans
        final Set<ObjectName> existing = JMXUtil.queryLocalMBeans(mServer, mDomainRoot.objectName().getDomain(), System.getProperty("com.sun.ass.instanceName"));
        for (final ObjectName objectName : existing) {
            //debug( "Queueing for validation: " + objectName );
            validate(objectName);
        }
    }

    boolean shouldValidate() {
        return !"off".equals(mValidationLevel);
    }

    private void validate(final ObjectName objectName) {
        if (shouldValidate()) {
            mValidatorThread.add(objectName);
        }
    }

    public static synchronized ComplianceMonitor getInstance(final DomainRoot domainRoot) {
        if (INSTANCE == null) {
            INSTANCE = new ComplianceMonitor(domainRoot);
            INSTANCE.listen(); // to start queuing immediately
        }
        return INSTANCE;
    }

    public static synchronized void  removeInstance() {
        if(INSTANCE != null) {
            INSTANCE.destroy();
            INSTANCE = null;
        }
    }

    public void start() {
        if (shouldValidate() && !mStarted) {
            mValidatorThread.start();
        }
    }

    public void handleNotification(final Notification notifIn, final Object handback) {
        if ((notifIn instanceof MBeanServerNotification) &&
                notifIn.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
            final MBeanServerNotification notif = (MBeanServerNotification) notifIn;
            final ObjectName objectName = notif.getMBeanName();
            if (objectName.getDomain().equals(mDomainRoot.objectName().getDomain())) {
                validate(objectName);
            }
        }
    }

    protected void destroy() {
        mValidatorThread.quit();
        mStarted = false;
        mValidationLevel = null;
    }


    private static final class ValidatorThread extends Thread {

        private final MBeanServer mServer;
        private final LinkedBlockingQueue<ObjectName> mMBeans = new LinkedBlockingQueue<ObjectName>();
        /** total number of failures */
        private final AtomicInteger mComplianceFailures = new AtomicInteger();
        private final boolean mUnregisterNonCompliant;
        private volatile String mValidationLevel;
        private volatile boolean mLogInaccessibleAttributes;

        private final Logger mLogger = AMXLoggerInfo.getLogger();

        ValidatorThread(
                final MBeanServer server,
                final String validationLevel,
                final boolean unregisterNonCompliant,
                final boolean logInaccessibleAttributes) {
            super("ComplianceMonitor.ValidatorThread");
            mServer = server;
            mValidationLevel = validationLevel;
            mUnregisterNonCompliant = unregisterNonCompliant;
            mLogInaccessibleAttributes = logInaccessibleAttributes;

            mFailures = new ConcurrentHashMap<ObjectName, AMXValidator.ProblemList>();
        }
        /** queue poison pill */
        private static final ObjectName QUIT = JMXUtil.newObjectName("quit:type=quit");
        private final ConcurrentHashMap<ObjectName, AMXValidator.ProblemList> mFailures;

        public Map<ObjectName, AMXValidator.ProblemList> getComplianceFailures() {
            return mFailures;
        }

        void quit() {
            add(QUIT);
        }

        public void add(final ObjectName objectName) {
            mMBeans.add(objectName);
        }

        public void run() {
            try {
                doRun();
            } catch (final Throwable t) {
                mLogger.log(Level.SEVERE, AMXLoggerInfo.aMXComplianceMonitorThreadquit, t);
            }
        }

        protected void doRun() throws InterruptedException {
            //debug( "ValidatorThread.doRun(): started" );
            while (true) {
                final ObjectName next = mMBeans.take(); // BLOCK until ready
                final List<ObjectName> toValidate = new ArrayList<ObjectName>();
                toValidate.add(next);
                mMBeans.drainTo(toValidate);    // efficiently get any additional ones
                if (mMBeans.contains(QUIT)) {
                    break;  // poison, quit;
                }

                // process available MBeans as a group so we can emit summary information as a group.
                final AMXValidator validator = new AMXValidator(mServer, mValidationLevel, mUnregisterNonCompliant, mLogInaccessibleAttributes);
                try {
                    //debug( "VALIDATING MBeans: " + toValidate.size() );
                    final ObjectName[] objectNames = new ObjectName[toValidate.size()];
                    toValidate.toArray(objectNames);
                    final AMXValidator.ValidationResult result = validator.validate(objectNames);
                    if (result.numFailures() != 0) {
                        mFailures.putAll(result.failures());

                        mComplianceFailures.addAndGet(result.numFailures());
                        mLogger.log(Level.INFO, AMXLoggerInfo.validatingMbean, result.toString());
                    }
                } catch (final Throwable t) {
                    AMXLoggerInfo.getLogger().log(Level.WARNING, AMXLoggerInfo.exceptionValidatingMbean, next);
                    AMXLoggerInfo.getLogger().log(Level.WARNING, null, t);
                }
            }
        }
    }
}























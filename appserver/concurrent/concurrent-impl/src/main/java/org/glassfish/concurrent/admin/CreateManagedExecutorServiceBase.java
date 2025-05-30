/*
 * Copyright (c) 2021-2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.admin;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.util.HashMap;
import java.util.Properties;

import org.glassfish.api.Param;

import static com.sun.enterprise.config.serverbeans.ServerTags.DESCRIPTION;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO_DEFAULT_VALUE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO_ENABLED;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CORE_POOL_SIZE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.HUNG_AFTER_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.HUNG_LOGGER_INITIAL_DELAY_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.HUNG_LOGGER_INTERVAL_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.HUNG_LOGGER_PRINT_ONCE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.KEEP_ALIVE_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.LONG_RUNNING_TASKS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.THREAD_LIFETIME_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.THREAD_PRIORITY;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.USE_VIRTUAL_THREADS;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;


/**
 * Base command for creating managed executor service and managed
 * scheduled executor service
 *
 */
public class CreateManagedExecutorServiceBase {

    protected static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateManagedExecutorServiceBase.class);

    @Param(name="jndi_name", primary=true)
    protected String jndiName;

    @Param(optional=true, defaultValue="true")
    protected Boolean enabled;

    @Param(name="contextinfoenabled", alias="contextInfoEnabled", defaultValue="true", optional=true)
    private Boolean contextinfoenabled;

    @Param(name="contextinfo", alias="contextInfo", defaultValue=CONTEXT_INFO_DEFAULT_VALUE, optional=true)
    protected String contextinfo;

    @Param(name="threadpriority", alias="threadPriority", defaultValue=""+Thread.NORM_PRIORITY, optional=true)
    protected Integer threadpriority;

    @Param(name = "usevirtualthreads", alias = "useVirtualThreads", defaultValue = "false", optional = true)
    protected Boolean useVirtualThreads;

    @Param(name = "longrunningtasks", alias = "longRunningTasks", defaultValue = "false", optional = true)
    protected Boolean longrunningtasks;

    @Param(name="hungafterseconds", alias="hungAfterSeconds", defaultValue="0", optional=true)
    protected Integer hungafterseconds;

    @Param(name="hungloggerprintonce", alias="hungLoggerPrintOnce", defaultValue="false", optional=true)
    protected Boolean hungloggerprintonce;

    @Param(name = "hungloggerinitialdelayseconds", alias = "hungLoggerInitialDelaySeconds", defaultValue = "60", optional = true)
    protected Integer hungloggerinitialdelayseconds;

    @Param(name = "hungloggerintervalseconds", alias = "hungLoggerIntervalSeconds", defaultValue = "60", optional = true)
    protected Integer hungloggerintervalseconds;

    @Param(name="corepoolsize", alias="corePoolSize", defaultValue="0", optional=true)
    protected Integer corepoolsize;

    @Param(name="keepaliveseconds", alias="keepAliveSeconds", defaultValue="60", optional=true)
    protected Integer keepaliveseconds;

    @Param(name="threadlifetimeseconds", alias="threadLifetimeSeconds", defaultValue="0", optional=true)
    protected Integer threadlifetimeseconds;

    @Param(optional=true)
    protected String description;

    @Param(name="property", optional=true, separator=':')
    protected Properties properties;

    @Param(optional=true)
    protected String target = SystemPropertyConstants.DAS_SERVER_NAME;

    protected void setAttributeList(HashMap attrList) {
        attrList.put(JNDI_NAME, jndiName);
        attrList.put(CONTEXT_INFO_ENABLED, contextinfoenabled.toString());
        attrList.put(CONTEXT_INFO, contextinfo);
        attrList.put(THREAD_PRIORITY, threadpriority.toString());
        attrList.put(USE_VIRTUAL_THREADS, useVirtualThreads.toString());
        attrList.put(LONG_RUNNING_TASKS, longrunningtasks.toString());
        attrList.put(HUNG_AFTER_SECONDS, hungafterseconds.toString());
        attrList.put(HUNG_LOGGER_PRINT_ONCE, hungloggerprintonce.toString());
        attrList.put(HUNG_LOGGER_INITIAL_DELAY_SECONDS, hungloggerinitialdelayseconds.toString());
        attrList.put(HUNG_LOGGER_INTERVAL_SECONDS, hungloggerintervalseconds.toString());
        attrList.put(CORE_POOL_SIZE, corepoolsize.toString());
        attrList.put(KEEP_ALIVE_SECONDS, keepaliveseconds.toString());
        attrList.put(THREAD_LIFETIME_SECONDS, threadlifetimeseconds.toString());
        attrList.put(DESCRIPTION, description);
        attrList.put(ENABLED, enabled.toString());
    }
}

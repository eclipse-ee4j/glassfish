/*
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

import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.Param;
import org.glassfish.resources.admin.cli.ResourceConstants;
import java.util.HashMap;
import java.util.Properties;


/**
 * Base command for creating managed executor service and managed
 * scheduled executor service
 *
 */
public class CreateManagedExecutorServiceBase {

    final protected static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateManagedExecutorServiceBase.class);

    @Param(name="jndi_name", primary=true)
    protected String jndiName;

    @Param(optional=true, defaultValue="true")
    protected Boolean enabled;

    @Param(name="contextinfoenabled", alias="contextInfoEnabled", defaultValue="true", optional=true)
    private Boolean contextinfoenabled;

    @Param(name="contextinfo", alias="contextInfo", defaultValue=ResourceConstants.CONTEXT_INFO_DEFAULT_VALUE, optional=true)
    protected String contextinfo;

    @Param(name="threadpriority", alias="threadPriority", defaultValue=""+Thread.NORM_PRIORITY, optional=true)
    protected Integer threadpriority;

    @Param(name="longrunningtasks", alias="longRunningTasks", defaultValue="false", optional=true)
    protected Boolean longrunningtasks;

    @Param(name="hungafterseconds", alias="hungAfterSeconds", defaultValue="0", optional=true)
    protected Integer hungafterseconds;

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
        attrList.put(ResourceConstants.JNDI_NAME, jndiName);
        attrList.put(ResourceConstants.CONTEXT_INFO_ENABLED, contextinfoenabled.toString());
        attrList.put(ResourceConstants.CONTEXT_INFO, contextinfo);
        attrList.put(ResourceConstants.THREAD_PRIORITY,
            threadpriority.toString());
        attrList.put(ResourceConstants.LONG_RUNNING_TASKS,
            longrunningtasks.toString());
        attrList.put(ResourceConstants.HUNG_AFTER_SECONDS,
            hungafterseconds.toString());
        attrList.put(ResourceConstants.CORE_POOL_SIZE,
            corepoolsize.toString());
        attrList.put(ResourceConstants.KEEP_ALIVE_SECONDS,
            keepaliveseconds.toString());
        attrList.put(ResourceConstants.THREAD_LIFETIME_SECONDS,
            threadlifetimeseconds.toString());
        attrList.put(ServerTags.DESCRIPTION, description);
        attrList.put(ResourceConstants.ENABLED, enabled.toString());
    }
}

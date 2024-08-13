/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;

import java.beans.PropertyVetoException;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.concurrent.config.ManagedExecutorServiceBase;
import org.glassfish.concurrent.config.ManagedScheduledExecutorService;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfiguredBy;
import org.jvnet.hk2.config.TransactionFailure;

/**
 *
 * The managed scheduled executor service manager allows you to create and
 * delete the managed-scheduled-executor-service config element
 */
@Service (name=ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)
@I18n("managed.executor.scheduled.service.manager")
@ConfiguredBy(Resources.class)
public class ManagedScheduledExecutorServiceManager extends ManagedExecutorServiceBaseManager {

    public String getResourceType () {
        return ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE;
    }

    protected ManagedExecutorServiceBase createConfigBean(Resources param, Properties properties) throws PropertyVetoException, TransactionFailure {
        ManagedScheduledExecutorService managedExecutorService = param.createChild(ManagedScheduledExecutorService.class);
        setAttributesOnConfigBean(managedExecutorService, properties);
        return managedExecutorService;
    }
}

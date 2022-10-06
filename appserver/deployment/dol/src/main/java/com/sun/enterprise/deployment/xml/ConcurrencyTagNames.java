/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.xml;


public interface ConcurrencyTagNames extends TagNames {
    String MANAGED_EXECUTOR = "managed-executor";
    String MANAGED_EXECUTOR_MAX_ASYNC = "max-async";
    String MANAGED_EXECUTOR_HUNG_TASK_THRESHOLD = "hung-task-threshold";
    String MANAGED_EXECUTOR_CONTEXT_SERVICE_REF = "context-service-ref";

    String MANAGED_THREAD_FACTORY = "managed-thread-factory";
    String MANAGED_THREAD_FACTORY_CONTEXT_SERVICE_REF = "context-service-ref";
    String MANAGED_THREAD_FACTORY_PRIORITY = "priority";

    String MANAGED_SCHEDULED_EXECUTOR = "managed-scheduled-executor";
    String MANAGED_SCHEDULED_EXECUTOR_CONTEXT_SERVICE_REF = "context-service-ref";
    String MANAGED_SCHEDULED_EXECUTOR_MAX_ASYNC = "max-async";
    String MANAGED_SCHEDULED_EXECUTOR_HUNG_TASK_THRESHOLD = "hung-task-threshold";

    String CONTEXT_SERVICE = "context-service";
    String CONTEXT_SERVICE_CLEARED = "cleared";
    String CONTEXT_SERVICE_PROPAGATED = "propagated";
    String CONTEXT_SERVICE_UNCHANGED = "unchanged";


}

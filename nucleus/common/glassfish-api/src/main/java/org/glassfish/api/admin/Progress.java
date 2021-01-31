/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ProgressStatus of a command. Indicates this command generates progress status as it executes. Use this annotation to
 * inject a {@link org.glassfish.api.admin.ProgressStatus} instance. The ProgressStatus object can be used to
 * asynchronously generate ongoing progress messages and command completion information.
 *
 * A Command annotated with @Progress will also be a ManagedJob which will be managed by the Job Manager
 *
 * @see org.glassfish.api.admin.ProgressStatus
 * @see org.glassfish.api.admin.JobManager
 * @author mmares
 * @author Bhakti Mehta
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@ManagedJob
public @interface Progress {

    /**
     * Optional: Context of the progress. Generally this is the command name. The name will be included in the command's
     * progress output. Default: command name or the server instance name for replicated commands.
     */
    public String name() default "";

    /**
     * Number of steps necessary to complete the operation. Value is used to determine percentage of work completed and can
     * be changed using {@code ProgressStatus.setTotalStepCount} If the step count is not established then a completion
     * percentage will not be included in the progress output.
     *
     * @see org.glassfish.api.admin.ProgressStatus
     */
    public int totalStepCount() default -1;

}

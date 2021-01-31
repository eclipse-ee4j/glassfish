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

package org.glassfish.api.admin;

import java.lang.annotation.*;

/**
 * Annotation to qualify when an action like a command is targeted to be run on a cluster or a set of instances.
 * 
 * Some actions may run only on DAS, or only on instances, by default they run on both the DAS and the instances.
 *
 * @author Jerome Dochez
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface ExecuteOn {

    /**
     * Returns an array of process types on which the annotated action should run
     *
     * @return array of target process types
     */
    RuntimeType[] value() default { RuntimeType.DAS, RuntimeType.INSTANCE };

    /**
     * Identifies the {@link ClusterExecutor} that is responsible for remotely executing commands on the target clusters or
     * instances. The provider will be looked up in the habitat by its type.
     * 
     * @return a {@link ClusterExecutor} type or null to use the default executor that takes the "target" command parameter
     * to
     */
    Class<? extends ClusterExecutor> executor() default TargetBasedExecutor.class;

    /**
     * Identifies the expected behaviour from the framework if any of the clustered invocation could not be invoked because
     * the remote server was offline.
     *
     * @return the action the framework should perform if any of the remote invocation of this command cannot be executed
     * due to the server being offline.
     */
    FailurePolicy ifOffline() default FailurePolicy.Warn;

    /**
     * Identifies the expected behavior from the framework if any of the clustered invocation could not be invoked because
     * the remote server has never been started.
     *
     * @return the action the framework should perform if any of the remote invocation of this command cannot be executed
     * due to the server being offline.
     */
    FailurePolicy ifNeverStarted() default FailurePolicy.Ignore;

    /**
     * Identifies the expected behavior from the framework if any of the clustered invocation failed.
     *
     * @return the action the framework should perform if any of the remote invocation of this command fails.
     */
    FailurePolicy ifFailure() default FailurePolicy.Error;
}

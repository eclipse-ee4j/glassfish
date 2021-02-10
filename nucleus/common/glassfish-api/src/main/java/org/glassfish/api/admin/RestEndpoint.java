/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 *
 * @author Jason Lee
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RestEndpoint {
    enum OpType {
        GET, PUT, POST, DELETE
    }

    /**
     * ConfigBean to which to attach the AdminCommand
     *
     * @return the name of the target ConfigBean
     */
    Class<? extends ConfigBeanProxy> configBean();

    /**
     * Rest operation type that should trigger a redirect to an actual asadmin command invocation. The default is GET.
     *
     * @return the rest operation type for this redirect
     */
    OpType opType() default OpType.GET;

    /**
     * This is the value of the last segment in the generated URL. If blank, this will default to the value of the name
     * attribute on the commands @Service annotation
     *
     * @return
     */
    String path() default "";

    /**
     * The description of the endpoint. This is used primarily in the REST HTML interface.
     *
     * @return
     */
    String description() default "";

    /**
     * A list of one or more @RestParam annotations representing the parameters to be used in the AdminCommand call
     *
     * @return
     */
    RestParam[] params() default {};

    /**
     * Whether this RestEndpoint should be used for command authorization decisions automatically. Setting this to true
     * causes the admin command framework automatically to use the configBean attribute to compute the resource name and the
     * OpType to compute the action.
     *
     * @return
     */
    boolean useForAuthorization() default false;
}

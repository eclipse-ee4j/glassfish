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

import java.util.Map;

import javax.security.auth.Subject;

import org.jvnet.hk2.annotations.Contract;

/**
 * Defines the API for services which provide additional information to be used during authorization.
 * <p>
 * The implementations of this interface provide name/value pairs as Maps which will become AzAttribute objects attached
 * to the subject, the resource, or the action before command security invokes the authorization service. We use Maps
 * here rather than collections of AzAttributes to minimize dependencies on Az classes.
 *
 * @author tjquinn
 */
@Contract
public interface AuthorizationPreprocessor {

    /**
     * Optionally adds to the attributes that will be attached to the Subject, the resource, and the action used for an
     * upcoming authorization check.
     *
     * @param context map describing the authorization context (such as command parameter names and values)
     * @param subjectAttributes name/value pairs for attributes to be attached to the subject
     * @param resourceAttributes name/value pairs for attributes to be attached to the resource
     * @param actionAttributes name/value pairs for attributes to be attached to the action
     */
    void describeAuthorization(Subject subject, String resourceName, String action, AdminCommand command, Map<String, Object> context,
            Map<String, String> subjectAttributes, Map<String, String> resourceAttributes, Map<String, String> actionAttributes);

}

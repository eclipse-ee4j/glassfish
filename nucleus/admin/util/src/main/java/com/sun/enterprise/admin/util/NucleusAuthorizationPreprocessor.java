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

package com.sun.enterprise.admin.util;

import java.util.Map;

import javax.security.auth.Subject;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AuthorizationPreprocessor;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author tjquinn
 */
@Rank(Integer.MIN_VALUE)
@Service
public class NucleusAuthorizationPreprocessor implements AuthorizationPreprocessor {

    @Override
    public void describeAuthorization(Subject subject, String resourceName, String action, AdminCommand command,
            Map<String, Object> context, Map<String, String> subjectAttributes, Map<String, String> resourceAttributes,
            Map<String, String> actionAttributes) {
    }

}

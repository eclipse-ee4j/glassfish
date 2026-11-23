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

package org.glassfish.security.services.api.authorization;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Common attribute names, used in authorization and set by code using
 * the authorization service.
 *
 * @author tjquinn
 */
public interface AuthorizationAdminConstants {
    String ISDAS_ATTRIBUTE = "isDAS";

    String ADMIN_TOKEN = "adminToken";
    String REST_TOKEN = "restToken";
    String LOCAL_PASSWORD = "localPassword";
    String SERVER = "server";

    /*
     * The presence of either of these attributes means the associated
     * subject is not linked to a specific admin user but was authenticated
     * as trustworthy in some other way.
     */
    Set<String> TRUSTED_FOR_DAS_OR_INSTANCE =
            Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(LOCAL_PASSWORD, SERVER)));

    String ADMIN_GROUP = "asadmin"; // must match with value in admin/util module's AdminConstants class
}

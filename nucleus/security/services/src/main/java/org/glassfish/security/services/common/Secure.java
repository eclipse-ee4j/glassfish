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

package org.glassfish.security.services.common;

import jakarta.inject.Qualifier;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.Metadata;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier used to enable a security check at the point of service injection or lookup.
 * Security Services, which are to protected against unqualified injection/look-up, should be annotated as something below
 *   e.g.,   @Secure(accessPermissionName = "security/service/<service-type>/<some-specific-name>")
 *
 * Any caller which injects or looks up the protected security service, the caller's jar/class should be granted the following policy
 *   e.g.,
 *     grant codeBase "file:<path>/<to>/<caller-jar>" {
 *         permission org.glassfish.security.services.common.SecureServiceAccessPermission "security/service/<service-type>/<some-specific-name>";
 *     };
 *
 */
@Retention(RUNTIME)
@Qualifier
@Inherited
@Target({ TYPE })
public @interface Secure {

    public static final String NAME = "accessPermissionName";

    public static final String PERMISSION_NAME_PREFIX = "security/service/";

    public static final String DEFAULT_PERM_NAME = PERMISSION_NAME_PREFIX + "default";

    /**
     * the permission name to be protected
     * if the accessPermissionName is not specified, a default value of "security/service/default" is used.
     * @return name of the protected HK2 service
     */
    @Metadata(NAME)
    public String accessPermissionName();


}

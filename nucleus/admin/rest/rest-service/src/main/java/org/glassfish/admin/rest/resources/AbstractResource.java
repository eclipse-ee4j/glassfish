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

package org.glassfish.admin.rest.resources;

import java.util.List;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import javax.security.auth.Subject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.adapter.LocatorBridge;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.security.services.common.SubjectUtil;

/**
 *
 * @author jdlee
 */
public abstract class AbstractResource {
    protected static final Logger logger = RestLogging.restLogger;
    @Inject
    protected HttpHeaders requestHeaders;
    @Inject
    protected UriInfo uriInfo;
    @Inject
    protected Ref<Subject> subjectRef;
    @Inject
    protected LocatorBridge locatorBridge;
    @Inject
    protected SecurityContext securityContext;
    @Inject
    protected ServiceLocator serviceLocator;

    private String authenticatedUser;

    /**
     * @return the Subject associated with the current request.
     */
    protected Subject getSubject() {
        return subjectRef.get();
    }

    /**
     * @return the authenticated user associated with the current request.
     */
    protected String getAuthenticatedUser() {
        if (authenticatedUser == null) {
            Subject s = getSubject();
            if (s != null) {
                List<String> list = SubjectUtil.getUsernamesFromSubject(s);
                if (list != null) {
                    authenticatedUser = list.get(0);
                }
            }
        }

        return authenticatedUser;
    }
}

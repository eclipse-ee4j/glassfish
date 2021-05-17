/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.web;

import java.util.Set;
import com.sun.enterprise.deployment.MetadataSource;
    /** Objects exhibiting this interface represent a security constraint on the web application
    * that owns them. */

public interface SecurityConstraint {

    /** The collection of URL pattern plus HTTP methods that are constrained. */
    public Set<WebResourceCollection> getWebResourceCollections();
    public void addWebResourceCollection(WebResourceCollection webResourceCollection);
    /** The authorization constraint. */
    public AuthorizationConstraint getAuthorizationConstraint();
    public void setAuthorizationConstraint(AuthorizationConstraint authorizationConstraint);

    /** The user data constraint. */
    public UserDataConstraint getUserDataConstraint();
    public void setUserDataConstraint(UserDataConstraint userDataConstraint);

}

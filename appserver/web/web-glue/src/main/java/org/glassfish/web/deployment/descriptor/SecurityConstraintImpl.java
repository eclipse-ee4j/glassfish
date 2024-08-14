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

package org.glassfish.web.deployment.descriptor;

import com.sun.enterprise.deployment.web.AuthorizationConstraint;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.UserDataConstraint;
import com.sun.enterprise.deployment.web.WebResourceCollection;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;


/**
 * Objects exhibiting this interface represent a security constraint on the web application
 * that owns them.
 * @author Danny Coward
 */
public class SecurityConstraintImpl extends Descriptor implements SecurityConstraint {
    private Set<WebResourceCollection> webResourceCollections;
    private AuthorizationConstraint authorizationConstraint;
    private UserDataConstraint userDataConstraint;

    /** Default constructor.*/
    public SecurityConstraintImpl() {

    }

    /** Copy constructor.*/
    public SecurityConstraintImpl(SecurityConstraintImpl other) {
        if (other.webResourceCollections != null) {
            this.webResourceCollections = new HashSet<WebResourceCollection>();
            for (WebResourceCollection wrc : other.webResourceCollections) {
                webResourceCollections.add(new WebResourceCollectionImpl((WebResourceCollectionImpl)wrc));
            }
        }
        if (other.authorizationConstraint != null) {
            this.authorizationConstraint = new AuthorizationConstraintImpl((AuthorizationConstraintImpl) other.authorizationConstraint);
        }
        if (other.userDataConstraint != null) {
            this.userDataConstraint = new UserDataConstraintImpl();
            this.userDataConstraint.setTransportGuarantee(other.userDataConstraint.getTransportGuarantee());
        }
    }


    /** Return all the web resource collection.
     */
    public Set<WebResourceCollection> getWebResourceCollections() {
        if (this.webResourceCollections == null) {
            this.webResourceCollections = new HashSet<WebResourceCollection>();
        }
        return this.webResourceCollections;
    }

    /** Adds a web resource collection to this constraint.*/
    public void addWebResourceCollection(WebResourceCollection webResourceCollection) {
        this.getWebResourceCollections().add(webResourceCollection);
    }

    public void addWebResourceCollection(WebResourceCollectionImpl webResourceCollection) {
        addWebResourceCollection((WebResourceCollection) webResourceCollection);
    }

    /** Removes the given web resource collection from this constraint.*/
    public void removeWebResourceCollection(WebResourceCollection webResourceCollection) {
        this.getWebResourceCollections().remove(webResourceCollection);
    }

    /** The authorization constraint. */
    public AuthorizationConstraint getAuthorizationConstraint() {
        return this.authorizationConstraint;
    }

    /** Sets the authorization constraint.*/
    public void setAuthorizationConstraint(AuthorizationConstraint authorizationConstraint) {
        this.authorizationConstraint = authorizationConstraint;
    }

    /** Sets the authorization constraint.*/
    public void setAuthorizationConstraint(AuthorizationConstraintImpl authorizationConstraint) {
        setAuthorizationConstraint((AuthorizationConstraint) authorizationConstraint);
    }

    /** The user data constraint. */
    public UserDataConstraint getUserDataConstraint() {
        return this.userDataConstraint;
    }
    /** Sets the user data constraint. */
    public void setUserDataConstraint(UserDataConstraint userDataConstraint) {
        this.userDataConstraint = userDataConstraint;
    }

    public void setUserDataConstraint(UserDataConstraintImpl userDataConstraint) {
        setUserDataConstraint((UserDataConstraint) userDataConstraint);
    }

    /** Returns a formatted String representing of my state.*/
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("SecurityConstraint: ");
        toStringBuffer.append(" webResourceCollections: ").append(webResourceCollections);
        toStringBuffer.append(" authorizationConstraint ").append(authorizationConstraint);
        toStringBuffer.append(" userDataConstraint ").append(userDataConstraint);

    }

}

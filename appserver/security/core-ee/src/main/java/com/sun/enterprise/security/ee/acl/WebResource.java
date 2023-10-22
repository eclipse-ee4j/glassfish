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

package com.sun.enterprise.security.ee.acl;

/**
 * @author Harish Prabandham
 */
public class WebResource extends Resource {
    private transient boolean wildcard;
    private transient String path;

    public WebResource(String app, String name, String method) {
        super(app, name, method);
        init(name);
    }

    private void init(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name can't be null");
        }

        if (name.endsWith("/*") || name.equals("*")) {
            wildcard = true;
            if (name.length() == 1) {
                path = "";
            } else {
                path = name.substring(0, name.length() - 1);
            }
        } else {
            path = name;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        Resource r = (Resource) obj;

        return getApplication().equals(r.getApplication()) && getMethod().equals(r.getMethod()) && getName().equals(r.getName());
    }

    @Override
    public boolean implies(Resource resource) {
        if (resource == null || resource.getClass() != getClass()) {
            return false;
        }

        WebResource that = (WebResource) resource;

        // Application name is not an issue in implies .....
        if (!getMethod().equals(that.getMethod())) {
            return false;
        }

        if (this.wildcard) {
            if (that.wildcard) {
                // one wildcard can imply another
                return that.path.startsWith(path);
            }
            // make sure ap.path is longer so a/b/* doesn't imply a/b
            return that.path.length() > this.path.length() && that.path.startsWith(this.path);
        }
        if (that.wildcard) {
            // a non-wildcard can't imply a wildcard
            return false;
        }
        return this.path.equals(that.path);
    }
}

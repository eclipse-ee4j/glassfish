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

import com.sun.enterprise.deployment.web.CookieConfig;

import org.glassfish.deployment.common.Descriptor;

/**
 * This represents the cookie-config resided in session-config of web.xml.
 *
 * @author Shing Wai Chan
 */

public class CookieConfigDescriptor extends Descriptor implements CookieConfig {
    private String domain = null;
    private String path = null;
    private String comment = null;
    private boolean httpOnly = true;
    private boolean secure = false;
    private int maxAge = -1;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void print(StringBuffer toStringBuffer) {
        if (getName() != null) {
            toStringBuffer.append("\n cookie name ").append(getName());
        }
        if (domain != null) {
            toStringBuffer.append("\n cookie domain ").append(domain);
        }
        if (path != null) {
            toStringBuffer.append("\n cookie path ").append(path);
        }
        if (comment != null) {
            toStringBuffer.append("\n cookie comment ").append(comment);
        }
        toStringBuffer.append("\n cookie httpOnly ").append(httpOnly);
        toStringBuffer.append("\n cookie secure ").append(secure);
        toStringBuffer.append("\n cookie maxAge ").append(maxAge);
    }
}

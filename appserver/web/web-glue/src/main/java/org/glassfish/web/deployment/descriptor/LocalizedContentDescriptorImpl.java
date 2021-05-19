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

import com.sun.enterprise.deployment.web.LocalizedContentDescriptor;


public class LocalizedContentDescriptorImpl implements LocalizedContentDescriptor, java.io.Serializable {
    private String url;
    private String locale;

    public LocalizedContentDescriptorImpl() {
    }

    public LocalizedContentDescriptorImpl(String locale, String url) {
        this.locale = locale;
        this.url = url;
    }

    @Override
    public String getLocale() {
        if (this.locale == null) {
            this.locale = "";
        }
        return this.locale;
    }

    @Override
    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public String getUrl() {
        if (this.url == null) {
            this.url = "";
        }
        return this.url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("LocalizedContent: ");
        toStringBuffer.append(" locale: ").append(locale);
        toStringBuffer.append(" url: ").append(url);
    }

}

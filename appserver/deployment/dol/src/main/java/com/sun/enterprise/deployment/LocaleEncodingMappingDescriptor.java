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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

/**
 */
public class LocaleEncodingMappingDescriptor extends Descriptor {

    private String locale = "en";
    private String encoding = "UTF-8";

    /**
     * standard constructor
     */
    public LocaleEncodingMappingDescriptor() {
    }

    /**
     * copy constructor
     */
    public LocaleEncodingMappingDescriptor(LocaleEncodingMappingDescriptor other) {
        super(other);
        locale = other.locale;
        encoding = other.encoding;
    }

    /**
     * set the locale
     */
    public void setLocale(String value) {
        locale = value;
    }

    /**
     * set the encoding
     */
    public void setEncoding(String value) {
        encoding = value;
    }

    /**
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @return a string describing the values I hold
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nProp : ").append(getLocale()).append("->").append(getEncoding());
    }
}

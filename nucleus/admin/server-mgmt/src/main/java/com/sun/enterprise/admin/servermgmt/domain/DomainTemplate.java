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

package com.sun.enterprise.admin.servermgmt.domain;

import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutor;
import com.sun.enterprise.admin.servermgmt.template.TemplateInfoHolder;
import com.sun.enterprise.admin.servermgmt.xml.templateinfo.TemplateInfo;

public class DomainTemplate {

    private StringSubstitutor _stringSubstitutor;
    private String _location;
    private TemplateInfoHolder _templateInfoHolder;

    public DomainTemplate(TemplateInfoHolder tInfoHolder, StringSubstitutor substitutor, String location) {
        _templateInfoHolder = tInfoHolder;
        _stringSubstitutor = substitutor;
        _location = location;
    }

    /**
     * Checks if the template has string-subs.
     *
     * @return <code>true</code> If template has stringsubs.xml file.
     */
    public boolean hasStringsubs() {
        return _stringSubstitutor != null;
    }

    /**
     * Get's the information used to perform string subs on the template.
     *
     * @return The encapsulated string-subs
     */
    public StringSubstitutor getStringSubs() {
        return _stringSubstitutor;
    }

    /**
     * Get's the location of the actual template.
     *
     * @return Template location.
     */
    public String getLocation() {
        return _location;
    }

    public TemplateInfo getInfo() {
        return _templateInfoHolder.getTemplateInfo();
    }
}

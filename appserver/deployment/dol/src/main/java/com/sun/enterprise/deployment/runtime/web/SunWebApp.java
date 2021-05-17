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

package com.sun.enterprise.deployment.runtime.web;

import com.sun.enterprise.deployment.runtime.common.SecurityRoleMapping;
import com.sun.enterprise.deployment.runtime.common.wls.SecurityRoleAssignment;

// BEGIN_NOI18N

public interface SunWebApp {

    static public final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";    // NOI18N
    static public final String SECURITY_ROLE_ASSIGNMENT = "SecurityRoleAssignment";    // NOI18N
    static public final String SERVLET = "Servlet";    // NOI18N
    static public final String SESSION_CONFIG = "SessionConfig";    // NOI18N
    static public final String CACHE = "Cache";    // NOI18N
    static public final String CLASS_LOADER = "ClassLoader";    // NOI18N
    static public final String JSP_CONFIG = "JspConfig";    // NOI18N
    static public final String LOCALE_CHARSET_INFO = "LocaleCharsetInfo";    // NOI18N
    static public final String PARAMETER_ENCODING = "ParameterEncoding";
    static public final String FORM_HINT_FIELD = "FormHintField";
    static public final String DEFAULT_CHARSET = "DefaultCharset";
    public static final String IDEMPOTENT_URL_PATTERN = "IdempotentUrlPattern";
    public static final String ERROR_URL = "ErrorUrl";
    public static final String HTTPSERVLET_SECURITY_PROVIDER = "HttpServletSecurityProvider";
    public static final String VALVE = "Valve";

    public void setSecurityRoleMapping(int index, SecurityRoleMapping value);

    public SecurityRoleMapping getSecurityRoleMapping(int index);

    public void setSecurityRoleMapping(SecurityRoleMapping[] value);

    public SecurityRoleMapping[] getSecurityRoleMapping();

    public int sizeSecurityRoleMapping();

    public int addSecurityRoleMapping(SecurityRoleMapping value);

    public int removeSecurityRoleMapping(SecurityRoleMapping value);

    public void setSecurityRoleAssignment(int index, SecurityRoleAssignment value);

    public SecurityRoleAssignment getSecurityRoleAssignment(int index);

    public void setSecurityRoleAssignments(SecurityRoleAssignment[] value);

    public SecurityRoleAssignment[] getSecurityRoleAssignments();

    public int sizeSecurityRoleAssignment();

    public int addSecurityRoleAssignment(SecurityRoleAssignment value);

    public int removeSecurityRoleAssignment(SecurityRoleAssignment value);

    public void setIdempotentUrlPattern(int index, IdempotentUrlPattern value);

    public  IdempotentUrlPattern getIdempotentUrlPattern(int index);

    public void setIdempotentUrlPatterns(IdempotentUrlPattern[] value);

    public IdempotentUrlPattern[] getIdempotentUrlPatterns();

    public int sizeIdempotentUrlPattern();

    public int addIdempotentUrlPattern(IdempotentUrlPattern value);

    public int removeIdempotentUrlPattern(IdempotentUrlPattern value);

    public String getAttributeValue(String attributeName);

}

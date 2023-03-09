/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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


public interface SunWebApp {

    String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";
    String SECURITY_ROLE_ASSIGNMENT = "SecurityRoleAssignment";
    String SERVLET = "Servlet";
    String SESSION_CONFIG = "SessionConfig";
    String CACHE = "Cache";
    String CLASS_LOADER = "ClassLoader";
    String JSP_CONFIG = "JspConfig";
    String LOCALE_CHARSET_INFO = "LocaleCharsetInfo";
    String PARAMETER_ENCODING = "ParameterEncoding";
    String FORM_HINT_FIELD = "FormHintField";
    String DEFAULT_CHARSET = "DefaultCharset";
    String IDEMPOTENT_URL_PATTERN = "IdempotentUrlPattern";
    String ERROR_URL = "ErrorUrl";
    String HTTPSERVLET_SECURITY_PROVIDER = "HttpServletSecurityProvider";
    String VALVE = "Valve";

    void setSecurityRoleMapping(int index, SecurityRoleMapping value);

    SecurityRoleMapping getSecurityRoleMapping(int index);

    void setSecurityRoleMapping(SecurityRoleMapping[] value);

    SecurityRoleMapping[] getSecurityRoleMapping();

    int sizeSecurityRoleMapping();

    int addSecurityRoleMapping(SecurityRoleMapping value);

    int removeSecurityRoleMapping(SecurityRoleMapping value);

    void setSecurityRoleAssignment(int index, SecurityRoleAssignment value);

    SecurityRoleAssignment getSecurityRoleAssignment(int index);

    void setSecurityRoleAssignments(SecurityRoleAssignment[] value);

    SecurityRoleAssignment[] getSecurityRoleAssignments();

    int sizeSecurityRoleAssignment();

    int addSecurityRoleAssignment(SecurityRoleAssignment value);

    int removeSecurityRoleAssignment(SecurityRoleAssignment value);

    void setIdempotentUrlPattern(int index, IdempotentUrlPattern value);

    IdempotentUrlPattern getIdempotentUrlPattern(int index);

    void setIdempotentUrlPatterns(IdempotentUrlPattern[] value);

    IdempotentUrlPattern[] getIdempotentUrlPatterns();

    int sizeIdempotentUrlPattern();

    int addIdempotentUrlPattern(IdempotentUrlPattern value);

    int removeIdempotentUrlPattern(IdempotentUrlPattern value);

    String getAttributeValue(String attributeName);

}

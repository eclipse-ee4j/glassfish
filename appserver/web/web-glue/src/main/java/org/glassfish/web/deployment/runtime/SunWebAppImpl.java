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

package org.glassfish.web.deployment.runtime;

import com.sun.enterprise.deployment.runtime.common.SecurityRoleMapping;
import com.sun.enterprise.deployment.runtime.common.wls.SecurityRoleAssignment;
import com.sun.enterprise.deployment.runtime.web.IdempotentUrlPattern;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;

// BEGIN_NOI18N

public class SunWebAppImpl extends WebPropertyContainer implements SunWebApp
{
    
    public SunWebAppImpl()
    {
	// set default values
	setAttributeValue(CACHE, "MaxEntries", "4096");
	setAttributeValue(CACHE, "TimeoutInSeconds", "30");
	setAttributeValue(CACHE, "Enabled", "false");
    }
    
    // This attribute is an array, possibly empty
    public void setSecurityRoleMapping(int index, SecurityRoleMapping value)
    {
	this.setValue(SECURITY_ROLE_MAPPING, index, value);
    }
    
    //
    public SecurityRoleMapping getSecurityRoleMapping(int index)
    {
	return (SecurityRoleMapping)this.getValue(SECURITY_ROLE_MAPPING, index);
    }
    
    // This attribute is an array, possibly empty
    public void setSecurityRoleMapping(SecurityRoleMapping[] value)
    {
	this.setValue(SECURITY_ROLE_MAPPING, value);
    }
    
    //
    public SecurityRoleMapping[] getSecurityRoleMapping()
    {
	return (SecurityRoleMapping[])this.getValues(SECURITY_ROLE_MAPPING);
    }
    
    // Return the number of properties
    public int sizeSecurityRoleMapping()
    {
	return this.size(SECURITY_ROLE_MAPPING);
    }
    
    // Add a new element returning its index in the list
    public int addSecurityRoleMapping(SecurityRoleMapping value)
    {
	return this.addValue(SECURITY_ROLE_MAPPING, value);
    }
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeSecurityRoleMapping(SecurityRoleMapping value)
    {
	return this.removeValue(SECURITY_ROLE_MAPPING, value);
    }

    // This attribute is an array, possibly empty
    public void setSecurityRoleAssignment(int index, SecurityRoleAssignment value)
    {
	this.setValue(SECURITY_ROLE_ASSIGNMENT, index, value);
    }

    //
    public SecurityRoleAssignment getSecurityRoleAssignment(int index)
    {
	return (SecurityRoleAssignment)this.getValue(SECURITY_ROLE_ASSIGNMENT, index);
    }

    // This attribute is an array, possibly empty
    public void setSecurityRoleAssignments(SecurityRoleAssignment[] value)
    {
	this.setValue(SECURITY_ROLE_ASSIGNMENT, value);
    }

    //
    public SecurityRoleAssignment[] getSecurityRoleAssignments()
    {
	return (SecurityRoleAssignment[])this.getValues(SECURITY_ROLE_ASSIGNMENT);
    }

    // Return the number of properties
    public int sizeSecurityRoleAssignment()
    {
	return this.size(SECURITY_ROLE_ASSIGNMENT);
    }

    // Add a new element returning its index in the list
    public int addSecurityRoleAssignment(SecurityRoleAssignment value)
    {
	return this.addValue(SECURITY_ROLE_ASSIGNMENT, value);
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeSecurityRoleAssignment(SecurityRoleAssignment value)
    {
	return this.removeValue(SECURITY_ROLE_ASSIGNMENT, value);
    }
    
    // This attribute is an array, possibly empty
    public void setServlet(int index, Servlet value)
    {
	this.setValue(SERVLET, index, value);
    }
    
    //
    public Servlet getServlet(int index)
    {
	return (Servlet)this.getValue(SERVLET, index);
    }
    
    // This attribute is an array, possibly empty
    public void setServlet(Servlet[] value)
    {
	this.setValue(SERVLET, value);
    }
    
    //
    public Servlet[] getServlet()
    {
	return (Servlet[])this.getValues(SERVLET);
    }
    
    // Return the number of properties
    public int sizeServlet()
    {
	return this.size(SERVLET);
    }
    
    // Add a new element returning its index in the list
    public int addServlet(Servlet value)
    {
	return this.addValue(SERVLET, value);
    }
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeServlet(Servlet value)
    {
	return this.removeValue(SERVLET, value);
    }
    
    // This attribute is an array, possibly empty
    public void setIdempotentUrlPattern(int index, IdempotentUrlPattern value)
    {
        this.setValue(IDEMPOTENT_URL_PATTERN, index, value);
    }

    //
    public  IdempotentUrlPattern getIdempotentUrlPattern(int index)
    {
        return (IdempotentUrlPattern)this.getValue(IDEMPOTENT_URL_PATTERN, index);
    }

    // This attribute is an array, possibly empty
    public void setIdempotentUrlPatterns(IdempotentUrlPattern[] value)
    {
        this.setValue(IDEMPOTENT_URL_PATTERN, value);
    }

    //
    public IdempotentUrlPattern[] getIdempotentUrlPatterns()
    {
        return (IdempotentUrlPattern[])this.getValues(IDEMPOTENT_URL_PATTERN);
    }

    // Return the number of properties
    public int sizeIdempotentUrlPattern()
    {
        return this.size(IDEMPOTENT_URL_PATTERN);
    }

    // Add a new element returning its index in the list
    public int addIdempotentUrlPattern(IdempotentUrlPattern value)
    {
        return this.addValue(IDEMPOTENT_URL_PATTERN, value);
    }

    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeIdempotentUrlPattern(IdempotentUrlPattern value)
    {
        return this.removeValue(IDEMPOTENT_URL_PATTERN, value);
    }

    // This attribute is optional
    public void setSessionConfig(SessionConfig value)
    {
	this.setValue(SESSION_CONFIG, value);
    }
    
    //
    public SessionConfig getSessionConfig()
    {
	return (SessionConfig)this.getValue(SESSION_CONFIG);
    }

    // This attribute is optional
    public void setCache(Cache value)
    {
	this.setValue(CACHE, value);
    }
    
    //
    public Cache getCache()
    {
	return (Cache)this.getValue(CACHE);
    }
    
    // This attribute is optional
    public void setClassLoader(ClassLoader value)
    {
        this.setValue(CLASS_LOADER, value);
    }

    //
    public ClassLoader getClassLoader()
    {
        return (ClassLoader)this.getValue(CLASS_LOADER);
    }

    
    // This attribute is optional
    public void setJspConfig(JspConfig value)
    {
	this.setValue(JSP_CONFIG, value);
    }
    
    //
    public JspConfig getJspConfig()
    {
	return (JspConfig)this.getValue(JSP_CONFIG);
    }
    
    // This attribute is optional
    public void setLocaleCharsetInfo(LocaleCharsetInfo value)
    {
	this.setValue(LOCALE_CHARSET_INFO, value);
    }
    
    //
    public LocaleCharsetInfo getLocaleCharsetInfo()
    {
	return (LocaleCharsetInfo)this.getValue(LOCALE_CHARSET_INFO);
    }
    
    // This method verifies that the mandatory properties are set
    public boolean verify()
    {
	return true;
    }

    // This attribute is optional
    public void setParameterEncoding(boolean value)
    {
        this.setValue(PARAMETER_ENCODING, Boolean.valueOf(value));
    }

    //
    public boolean isParameterEncoding()
    {
        Boolean ret = (Boolean)this.getValue(PARAMETER_ENCODING);
        if (ret == null) {
            return false;
        }
        return ret.booleanValue();
    }

    // This attribute is a valve to be added at the specified index
    public void setValve(int index, Valve value) {
        this.setValue(VALVE, index, value);
    }

    // The return value is the valve at the specified index
    public Valve getValve(int index) {
        return (Valve)this.getValue(VALVE, index);
    }

    // This attribute is an array, possibly empty
    public void setValve(Valve[] value) {
        this.setValue(VALVE, value);
    }

    // This return value is an array, possibly empty
    public Valve[] getValve() {
        return (Valve[])this.getValues(VALVE);
    }

    // Return the number of valves
    public int sizeValve() {
        return this.size(VALVE);
    }

    // Add a new element returning its index in the list
    public int addValve(Valve value) {
        return this.addValue(VALVE, value);
    }

    // Remove an element using its reference
    public int removeValve(Valve value) {
        return this.removeValue(VALVE, value);
    }

}

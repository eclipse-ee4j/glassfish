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

/**
 * This generated bean class WebModule matches the schema element web-module
 *
 * Generated on Thu May 06 00:44:23 PDT 2004
 */
package org.glassfish.loadbalancer.admin.cli.beans;

import java.util.Vector;

import org.netbeans.modules.schema2beans.AttrProp;
import org.netbeans.modules.schema2beans.Common;

// BEGIN_NOI18N
public class WebModule extends org.netbeans.modules.schema2beans.BaseBean {

    static Vector comparators = new Vector();
    static public final String CONTEXTROOT = "ContextRoot";    // NOI18N
    static public final String ENABLED = "Enabled";    // NOI18N
    static public final String DISABLETIMEOUTINMINUTES = "DisableTimeoutInMinutes";    // NOI18N
    static public final String ERRORURL = "ErrorUrl";    // NOI18N
    static public final String IDEMPOTENT_URL_PATTERN = "IdempotentUrlPattern";    // NOI18N
    static public final String IDEMPOTENTURLPATTERNURLPATTERN = "IdempotentUrlPatternUrlPattern";    // NOI18N
    static public final String IDEMPOTENTURLPATTERNNOOFRETRIES = "IdempotentUrlPatternNoOfRetries";    // NOI18N

    public WebModule() {
        this(Common.USE_DEFAULT_VALUES);
    }

    public WebModule(int options) {
        super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
        // Properties (see root bean comments for the bean graph)
        this.createProperty("idempotent-url-pattern", // NOI18N
                IDEMPOTENT_URL_PATTERN,
                Common.TYPE_0_N | Common.TYPE_BOOLEAN | Common.TYPE_KEY,
                Boolean.class);
        this.createAttribute(IDEMPOTENT_URL_PATTERN, "url-pattern", "UrlPattern",
                AttrProp.CDATA | AttrProp.REQUIRED,
                null, null);
        this.createAttribute(IDEMPOTENT_URL_PATTERN, "no-of-retries", "NoOfRetries",
                AttrProp.CDATA,
                null, "-1");
        this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {
    }

    // This attribute is mandatory
    public void setContextRoot(java.lang.String value) {
        setAttributeValue(CONTEXTROOT, value);
    }

    //
    public java.lang.String getContextRoot() {
        return getAttributeValue(CONTEXTROOT);
    }

    // This attribute is mandatory
    public void setEnabled(java.lang.String value) {
        setAttributeValue(ENABLED, value);
    }

    //
    public java.lang.String getEnabled() {
        return getAttributeValue(ENABLED);
    }

    // This attribute is mandatory
    public void setDisableTimeoutInMinutes(java.lang.String value) {
        setAttributeValue(DISABLETIMEOUTINMINUTES, value);
    }

    //
    public java.lang.String getDisableTimeoutInMinutes() {
        return getAttributeValue(DISABLETIMEOUTINMINUTES);
    }

    // This attribute is mandatory
    public void setErrorUrl(java.lang.String value) {
        setAttributeValue(ERRORURL, value);
    }

    //
    public java.lang.String getErrorUrl() {
        return getAttributeValue(ERRORURL);
    }

    // This attribute is an array, possibly empty
    public void setIdempotentUrlPattern(int index, boolean value) {
        this.setValue(IDEMPOTENT_URL_PATTERN, index, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
    }

    //
    public boolean isIdempotentUrlPattern(int index) {
        Boolean ret = (Boolean) this.getValue(IDEMPOTENT_URL_PATTERN, index);
        if (ret == null) {
            ret = (Boolean) Common.defaultScalarValue(Common.TYPE_BOOLEAN);
        }
        return ((java.lang.Boolean) ret).booleanValue();
    }

    // This attribute is an array, possibly empty
    public void setIdempotentUrlPattern(boolean[] value) {
        Boolean[] values = null;
        if (value != null) {
            values = new Boolean[value.length];
            for (int i = 0; i < value.length; i++) {
                values[i] = Boolean.valueOf(value[i]);
            }
        }
        this.setValue(IDEMPOTENT_URL_PATTERN, values);
    }

    //
    public boolean[] getIdempotentUrlPattern() {
        boolean[] ret = null;
        Boolean[] values = (Boolean[]) this.getValues(IDEMPOTENT_URL_PATTERN);
        if (values != null) {
            ret = new boolean[values.length];
            for (int i = 0; i < values.length; i++) {
                ret[i] = values[i].booleanValue();
            }
        }
        return ret;
    }

    // Return the number of properties
    public int sizeIdempotentUrlPattern() {
        return this.size(IDEMPOTENT_URL_PATTERN);
    }

    // Add a new element returning its index in the list
    public int addIdempotentUrlPattern(boolean value) {
        return this.addValue(IDEMPOTENT_URL_PATTERN, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeIdempotentUrlPattern(boolean value) {
        return this.removeValue(IDEMPOTENT_URL_PATTERN, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
    }

    //
    // Remove an element using its index
    //
    public void removeIdempotentUrlPattern(int index) {
        this.removeValue(IDEMPOTENT_URL_PATTERN, index);
    }

    // This attribute is an array, possibly empty
    public void setIdempotentUrlPatternUrlPattern(int index, java.lang.String value) {
        // Make sure we've got a place to put this attribute.
        if (size(IDEMPOTENT_URL_PATTERN) == 0) {
            addValue(IDEMPOTENT_URL_PATTERN, "");
        }
        setAttributeValue(IDEMPOTENT_URL_PATTERN, index, "UrlPattern", value);
    }

    //
    public java.lang.String getIdempotentUrlPatternUrlPattern(int index) {
        // If our element does not exist, then the attribute does not exist.
        if (size(IDEMPOTENT_URL_PATTERN) == 0) {
            return null;
        } else {
            return getAttributeValue(IDEMPOTENT_URL_PATTERN, index, "UrlPattern");
        }
    }

    // This attribute is an array, possibly empty
    public void setIdempotentUrlPatternNoOfRetries(int index, java.lang.String value) {
        // Make sure we've got a place to put this attribute.
        if (size(IDEMPOTENT_URL_PATTERN) == 0) {
            addValue(IDEMPOTENT_URL_PATTERN, "");
        }
        setAttributeValue(IDEMPOTENT_URL_PATTERN, index, "NoOfRetries", value);
    }

    //
    public java.lang.String getIdempotentUrlPatternNoOfRetries(int index) {
        // If our element does not exist, then the attribute does not exist.
        if (size(IDEMPOTENT_URL_PATTERN) == 0) {
            return null;
        } else {
            return getAttributeValue(IDEMPOTENT_URL_PATTERN, index, "NoOfRetries");
        }
    }

    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }

    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }

    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        boolean restrictionFailure = false;
        // Validating property contextRoot
        if (getContextRoot() == null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getContextRoot() == null", "contextRoot", this);    // NOI18N
        }
        // Validating property enabled
        if (getEnabled() == null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getEnabled() == null", "enabled", this);    // NOI18N
        }
        // Validating property disableTimeoutInMinutes
        if (getDisableTimeoutInMinutes() == null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getDisableTimeoutInMinutes() == null", "disableTimeoutInMinutes", this);    // NOI18N
        }
        // Validating property errorUrl
        if (getErrorUrl() == null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getErrorUrl() == null", "errorUrl", this);    // NOI18N
        }
        // Validating property idempotentUrlPattern
        for (int _index = 0; _index < sizeIdempotentUrlPattern();
                ++_index) {
            boolean element = isIdempotentUrlPattern(_index);
        }
        // Validating property idempotentUrlPatternUrlPattern
        // Validating property idempotentUrlPatternNoOfRetries
    }

    // Dump the content of this bean returning it as a String
    @Override
    public void dump(StringBuffer str, String indent) {
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("IdempotentUrlPattern[" + this.sizeIdempotentUrlPattern() + "]");    // NOI18N
        for (int i = 0; i < this.sizeIdempotentUrlPattern(); i++) {
            str.append(indent + "\t");
            str.append("#" + i + ":");
            str.append(indent + "\t");    // NOI18N
            str.append((this.isIdempotentUrlPattern(i) ? "true" : "false"));
            this.dumpAttributes(IDEMPOTENT_URL_PATTERN, i, str, indent);
        }

    }

    @Override
    public String dumpBeanNode() {
        StringBuffer str = new StringBuffer();
        str.append("WebModule\n");    // NOI18N
        this.dump(str, "\n  ");    // NOI18N
        return str.toString();
    }
}
// END_NOI18N

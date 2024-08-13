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
 * This generated bean class Property matches the schema element property
 *
 * Generated on Thu May 06 00:44:23 PDT 2004
 */
package org.glassfish.loadbalancer.admin.cli.beans;

import java.util.Vector;

import org.netbeans.modules.schema2beans.Common;

// BEGIN_NOI18N
public class Property extends org.netbeans.modules.schema2beans.BaseBean {

    static Vector comparators = new Vector();
    static public final String NAME = "Name";    // NOI18N
    static public final String VALUE = "Value";    // NOI18N
    static public final String DESCRIPTION = "Description";    // NOI18N

    public Property() {
        this(Common.USE_DEFAULT_VALUES);
    }

    public Property(int options) {
        super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
        // Properties (see root bean comments for the bean graph)
        this.createProperty("description", // NOI18N
                DESCRIPTION,
                Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                String.class);
        this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {
    }

    // This attribute is mandatory
    public void setName(java.lang.String value) {
        setAttributeValue(NAME, value);
    }

    //
    public java.lang.String getName() {
        return getAttributeValue(NAME);
    }

    // This attribute is mandatory
    public void setValue(java.lang.String value) {
        setAttributeValue(VALUE, value);
    }

    //
    public java.lang.String getValue() {
        return getAttributeValue(VALUE);
    }

    // This attribute is optional
    public void setDescription(String value) {
        this.setValue(DESCRIPTION, value);
    }

    //
    public String getDescription() {
        return (String) this.getValue(DESCRIPTION);
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
        // Validating property name
        if (getName() == null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", "name", this);    // NOI18N
        }
        // Validating property value
        if (getValue() == null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getValue() == null", "value", this);    // NOI18N
        }
        // Validating property description
        if (getDescription() != null) {
        }
    }

    // Dump the content of this bean returning it as a String
    @Override
    public void dump(StringBuffer str, String indent) {
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Description");    // NOI18N
        str.append(indent + "\t");    // NOI18N
        str.append("<");    // NOI18N
        s = this.getDescription();
        str.append((s == null ? "null" : s.trim()));    // NOI18N
        str.append(">\n");    // NOI18N
        this.dumpAttributes(DESCRIPTION, 0, str, indent);

    }

    @Override
    public String dumpBeanNode() {
        StringBuffer str = new StringBuffer();
        str.append("Property\n");    // NOI18N
        this.dump(str, "\n  ");    // NOI18N
        return str.toString();
    }
}

// END_NOI18N


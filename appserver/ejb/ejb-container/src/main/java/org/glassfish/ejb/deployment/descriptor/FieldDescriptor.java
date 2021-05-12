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

 package org.glassfish.ejb.deployment.descriptor;

import java.lang.reflect.Field;

import org.glassfish.deployment.common.Descriptor;

/**
 * I represent a field on an ejb.
 * Either an actual field (e.g. for EJB1.1 CMP)
 * or a virtual field (e.g. for EJb2.0 CMP)
 *
 * @author Danny Coward
 */
public class FieldDescriptor extends Descriptor {

    /**
     * Constructrs an empty field descriptor
     */
    public FieldDescriptor() {
    }

    /**
     * Constructrs a field descriptor with the given name.
     */
    public FieldDescriptor(String name) {
        super(name, "no description");
    }

    /**
     * Constructrs a field descriptor with the given name and description.
     */
    public FieldDescriptor(String name, String description) {
        super(name, description);
    }

    /**
     * Constructs a field descriptor from the supplied java.lang.reflect.Field object.
     */
    public FieldDescriptor(Field field) {
        this(field.getName(), "no description");
    }

    /**
     * Equality iff the other objects is a field descriptor with the same name.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof FieldDescriptor) {
            FieldDescriptor otherFieldDescriptor = (FieldDescriptor) object;
            return otherFieldDescriptor.getName().equals(this.getName());
        }
        return false;
    }

    /**
     * My hashcode.
     */
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /**
     * Returns a formatted version of me as a String.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Field: ").append(super.getName()).append("@").append(super.getDescription());
    }

    /**
     * <p>
     * Check if a field name is of an acceptable value (start with a lowercase
     * letter)
     * </p>
     * @param fieldName is the field name to test
     * @throws IllegalArgumentException if the name is unacceptable
     */
    public static void checkFieldName(String fieldName) throws IllegalArgumentException {

        if (fieldName == null || fieldName.length()==0) {
            throw new IllegalArgumentException("cmp-field or cmr-field name cannot be empty strings");
        }
        char firstChar = fieldName.charAt(0);
        if (!Character.isLetter(firstChar)) {
            throw new IllegalArgumentException("cmp-field or cmr-field name " + fieldName + " must begin with a letter ");
        }
        if (!Character.isLowerCase(firstChar)) {
            throw new IllegalArgumentException("cmp-field or cmr-field name " + fieldName + " must begin with a lowercase letter");
        }
    }
}

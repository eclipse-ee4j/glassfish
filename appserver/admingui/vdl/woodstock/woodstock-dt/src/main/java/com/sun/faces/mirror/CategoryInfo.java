/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.mirror;

import java.util.Map;

/**
 * Represents a category descriptor.
 */
public final class CategoryInfo implements Comparable {

    /**
     * Category name.
     */
    private static final String NAME = "name";

    /**
     * Category sort key.
     */
    private static final String SORT_KEY = "sortKey";

    /**
     * Annotation value map.
     */
    private final Map<String, Object> annotationValueMap;

    /**
     * Field name.
     */
    private String fieldName;

    /**
     * Create a new instance.
     * @param annotValueMap annotation value map
     */
    CategoryInfo(final Map<String, Object> annotValueMap) {
        this.annotationValueMap = annotValueMap;
    }

    /**
     * Get the category name.
     * @return String
     */
    public String getName() {
        return (String) this.annotationValueMap.get(NAME);
    }

    /**
     * Get the category sort key.
     * @return String
     */
    public String getSortKey() {
        if (this.annotationValueMap.containsKey(SORT_KEY)) {
            return (String) this.annotationValueMap.get(SORT_KEY);
        }
        return this.getName();
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash;
        if (this.annotationValueMap != null) {
            hash = hash + this.annotationValueMap.hashCode();
        }
        hash = 59 * hash;
        if (this.fieldName != null) {
            hash = hash + this.fieldName.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof CategoryInfo) {
            return ((CategoryInfo) obj).getName().equals(this.getName());
        }
        return false;
    }

    @Override
    public int compareTo(final Object obj) {
        if (obj instanceof CategoryInfo) {
            return this.getSortKey().compareTo(((CategoryInfo) obj)
                    .getSortKey());
        }
        return -1;
    }

    /**
     * Get the field name.
     * @return String
     */
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * Set the field name.
     * @param fName new field name
     */
    void setFieldName(final String fName) {
        this.fieldName = fName;
    }
}

/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.rave.designtime;

/**
 * Placeholder added to pass compilation since the {@code com.sun.rave.}
 * classes are not available.
 */
public final class CategoryDescriptor {

    /**
     * Category name.
     */
    private final String name;

    /**
     * Create a new instance.
     * @param catName category name
     */
    public CategoryDescriptor(final String catName) {
        this.name = catName;
    }

    /**
     * Create a new instance.
     * @param catName category name
     * @param catDesc category description
     * @param expandByDefault  expand flag
     */
    public CategoryDescriptor(final String catName, final String catDesc,
            final boolean expandByDefault) {
        this.name = catName;
    }

    /**
     * Get the name of this category.
     * @return String
     */
    public String getName() {
        return name;
    }
}

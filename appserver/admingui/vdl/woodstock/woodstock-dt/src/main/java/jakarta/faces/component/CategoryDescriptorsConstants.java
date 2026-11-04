/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package jakarta.faces.component;

import com.sun.rave.designtime.CategoryDescriptor;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Category descriptors constants.
 */
public final class CategoryDescriptorsConstants {

    /**
     * Cannot be instanciated.
     */
    private CategoryDescriptorsConstants() {
    }

    /**
     * The resource bundle.
     */
    private static final ResourceBundle BUNDLE = ResourceBundle
            .getBundle("jakarta.faces.component.bundle", Locale.getDefault());

    /**
     * Advanced category.
     */
    public static final CategoryDescriptor ADVANCED =
            new CategoryDescriptor(BUNDLE.getString("advanced_category"));

    /**
     * Appearance category.
     */
    public static final CategoryDescriptor APPEARANCE =
            new CategoryDescriptor(BUNDLE.getString("appearance_category"));

    /**
     * Data category.
     */
    public static final CategoryDescriptor DATA =
            new CategoryDescriptor(BUNDLE.getString("data_category"));

    /**
     * General category.
     */
    public static final CategoryDescriptor GENERAL = new CategoryDescriptor(
            BUNDLE.getString("general_category"));

}

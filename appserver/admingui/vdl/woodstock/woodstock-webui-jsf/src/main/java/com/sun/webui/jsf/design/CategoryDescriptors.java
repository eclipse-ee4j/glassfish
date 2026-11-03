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
package com.sun.webui.jsf.design;

import com.sun.faces.annotation.PropertyCategory;
import com.sun.rave.designtime.CategoryDescriptor;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Definition for category descriptors processed at build time by the annotation
 * processor.
 */
public final class CategoryDescriptors {

    /**
     * Cannot be instanciated.
     */
    private CategoryDescriptors() {
    }

    /**
     * The resource bundle to use for the category descriptors.
     */
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "com.sun.webui.jsf.design.Bundle",
            Locale.getDefault(),
            CategoryDescriptors.class.getClassLoader());

    /**
     * Create a new {@code CategoryDescriptor} instance.
     * @param nameKey bundle key for the category name
     * @param descKey bundle key for the category description
     * @param expand expand flag
     * @return CategoryDescriptor
     */
    private static CategoryDescriptor newCD(final String nameKey,
            final String descKey, final boolean expand) {

        return new CategoryDescriptor(BUNDLE.getString(nameKey),
                BUNDLE.getString(descKey), expand);
    }

    /**
     * Category descriptor for Accessibility.
     */
    @PropertyCategory(name = "Accessibility", sortKey = "h")
    public static final CategoryDescriptor ACCESSIBILITY = newCD(
            "accessibility", "accessibilityCatDesc", false);

    /**
     * Category descriptor for Advanced.
     */
    @PropertyCategory(name = "Advanced", sortKey = "j")
    public static final CategoryDescriptor ADVANCED = newCD(
            "adv", "advCatDesc", false);

    /**
     * Category descriptor for Appearance.
     */
    @PropertyCategory(name = "Appearance", sortKey = "b")
    public static final CategoryDescriptor APPEARANCE = newCD(
            "appear", "appearCatDesc", false);

    /**
     * Category descriptor for Behavior.
     */
    @PropertyCategory(name = "Behavior", sortKey = "g")
    public static final CategoryDescriptor BEHAVIOR = newCD(
            "behavior", "behaviorCatDesc", false);

    /**
     * Category descriptor for Data.
     */
    @PropertyCategory(name = "Data", sortKey = "d")
    public static final CategoryDescriptor DATA = newCD(
            "data", "dataCatDesc", false);

    /**
     * Category descriptor for Events.
     */
    @PropertyCategory(name = "Events", sortKey = "e")
    public static final CategoryDescriptor EVENTS =  newCD(
            "events", "eventsCatDesc", false);

    /**
     * Category descriptor for General.
     */
    @PropertyCategory(name = "General", sortKey = "a")
    public static final CategoryDescriptor GENERAL = newCD(
            "general", "generalCatDesc", false);

    /**
     * Category descriptor for Internal.
     */
    @PropertyCategory(name = "Internal", sortKey = "k")
    public static final CategoryDescriptor INTERNAL = newCD(
            "internal", "internalCatDesc", false);

    /**
     * Category descriptor for Javascript.
     */
    @PropertyCategory(name = "Javascript", sortKey = "i")
    public static final CategoryDescriptor JAVASCRIPT = newCD(
            "javascript", "javascriptCatDesc", false);

    /**
     * Category descriptor for Layout.
     */
    @PropertyCategory(name = "Layout", sortKey = "c")
    public static final CategoryDescriptor LAYOUT = newCD(
            "layout", "layoutCatDesc", false);

    /**
     * Category descriptor for Navigation.
     */
    @PropertyCategory(name = "Navigation", sortKey = "f")
    public static final CategoryDescriptor NAVIGATION = newCD(
            "navigation", "navigationCatDesc", false);

    /**
     * Default category descriptors.
     */
    private static final CategoryDescriptor[] DEFAULT_CATEGORY_DESCRIPTOR = {
        GENERAL,
        APPEARANCE,
        LAYOUT,
        DATA,
        EVENTS,
        NAVIGATION,
        BEHAVIOR,
        ACCESSIBILITY,
        JAVASCRIPT,
        ADVANCED,
        INTERNAL
    };
}

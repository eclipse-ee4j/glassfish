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

package com.sun.webui.jsf.model.list;

/**
 * List item.
 */
public final class ListItem {

    /**
     * Item value.
     */
    private Object valueObject;

    /**
     * Label.
     */
    private String label;

    /**
     * Value string.
     */
    private String value;

    /**
     * Description.
     */
    private String description = null;

    /**
     * Selected flag.
     */
    private boolean selected = false;

    /**
     * Disabled flag.
     */
    private boolean disabled = false;

    /**
     * Is title flag.
     */
    private boolean title = false;

    /**
     * Create a new instance.
     * @param newLabel label
     */
    public ListItem(final String newLabel) {
        this.label = newLabel;
        this.valueObject = newLabel;
    }

    /**
     * Create a new instance.
     * @param newValueObject value
     * @param newLabel label
     */
    public ListItem(final Object newValueObject, final String newLabel) {
        this.label = newLabel;
        this.valueObject = newValueObject;
    }

    /**
     * Create a new instance.
     * @param newValueObject value
     * @param newLabel label
     * @param newDisabled disabled flag
     */
    public ListItem(final Object newValueObject, final String newLabel,
            final boolean newDisabled) {

        this.label = newLabel;
        this.valueObject = newValueObject;
        this.disabled = newDisabled;
    }

    /**
     * Create a new instance.
     * @param newValueObject value
     * @param newLabel label
     * @param newDescription description
     * @param newDisabled disabled flag
     */
    public ListItem(final Object newValueObject, final String newLabel,
            final String newDescription, final boolean newDisabled) {

        this.label = newLabel;
        this.valueObject = newValueObject;
        this.description = newDescription;
        this.disabled = newDisabled;
    }

    /**
     * Get the label.
     * @return String
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the value as {@code String}.
     * @return String
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value as {@code String}.
     * @param newValue new value
     */
    public void setValue(final String newValue) {
        this.value = newValue;
    }

    /**
     * Get the description.
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the selected flag.
     * @return {@code boolean}
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Set the selected flag.
     * @param newSelected selected
     */
    public void setSelected(final boolean newSelected) {
        this.selected = newSelected;
    }

    /**
     * Get the value object.
     * @return Object
     */
    public Object getValueObject() {
        return valueObject;
    }

    /**
     * Get the disabled flag.
     * @return {@code boolean}
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Set the disabled flag.
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
    }

    /**
     * Set the title flag.
     * @param newTitle title flag
     */
    public void setTitle(final boolean newTitle) {
        this.title = newTitle;
    }

    /**
     * Get the title flag.
     * @return {@code boolean}
     */
    public boolean isTitle() {
        return title;
    }
}

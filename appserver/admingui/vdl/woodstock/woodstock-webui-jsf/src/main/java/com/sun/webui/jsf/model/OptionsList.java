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

package com.sun.webui.jsf.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents a list of selectable options, which can be used to
 * initialize the {@code items} property of all selector-based components
 * ({@code Listbox}, {@code Dropdown}, {@code JumpDropdown},
 * {@code CheckboxGroup}, {@code RadioButtonGroup}), and
 * {@code AddRemove}.
 */
public class OptionsList implements Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 6695656179045426419L;

    /**
     * The options list.
     */
    private final ArrayList<Option> options;

    /**
     * The selected values.
     */
    private final ArrayList<Object> selectedValues;

    /**
     * isMultiple flag.
     */
    private boolean isMultiple;

    /**
     * Create a new instance.
     */
    public OptionsList() {
        options = new ArrayList<Option>();
        selectedValues = new ArrayList<Object>();
        isMultiple = false;
    }

    /**
     * Get the isMultiple flag value.
     * @return {@code boolean}
     */
    public boolean isMultiple() {
        return isMultiple;
    }

    /**
     * Set the isMultiple flag value.
     * @param newIsMultiple new isMultiple flag value
     */
    public void setMultiple(final boolean newIsMultiple) {
        this.isMultiple = newIsMultiple;
    }

    /**
     * Set the options.
     * @param newOptions options
     */
    public void setOptions(final Option[] newOptions) {
        this.options.clear();
        if (newOptions == null) {
            return;
        }
        this.options.addAll(Arrays.asList(newOptions));
    }

    /**
     * Get the options.
     * @return Option[]
     */
    public Option[] getOptions() {
        Option[] opts = new Option[this.options.size()];
        this.options.toArray(opts);
        return opts;
    }

    /**
     * If this options list is in "multiple" mode, value specified may be an
     * array of objects or a singleton.Otherwise, the value is treated as a
     * singleton.
     *
     * @param value new value
     */
    public void setSelectedValue(final Object value) {
        selectedValues.clear();
        if (value == null) {
            return;
        }
        if (value instanceof Object[]) {
            Object[] values = (Object[]) value;
            for (Object value1 : values) {
                selectedValues.add(value1);
            }
        } else {
            selectedValues.add(value);
        }
    }

    /**
     * If this options list is in "multiple" mode, returns an array of objects;
     * otherwise, returns a singleton.
     * @return Object
     */
    public Object getSelectedValue() {
        if (isMultiple) {
            if (selectedValues.isEmpty()) {
                return new Object[0];
            }
            return selectedValues.toArray();
        } else {
            if (selectedValues.isEmpty()) {
                return null;
            }
            return selectedValues.get(0);
        }
    }
}

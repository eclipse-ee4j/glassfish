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

/**
 * This interface describes a selectable item in the file chooser list-box. The
 * item has 5 values which can be implemented based on the resource type in
 * question:
 * <ul>
 * <li>An Object representing the value of the item</li>
 * <li>A key that will be used as the value of the {@code <option>} tag in the
 * file chooser list-box></li>
 * <li>A label that will be the label of the {@code <option>} tag described
 * above.</li>
 * <li>A {@code boolean} flag indicating whether the option should be disabled
 * or not.</li>
 * <li>A {@code boolean} flag indicating if the resource being represented by
 * this item is a container resource or a child resource. In the realm of of
 * File systems this would translate to directory and file respectively.</li>
 * </ul>
 */
public interface ResourceItem {

    /**
     * Returns an object representing the value of this resource item.
     * For the default case of the FileChooser this would be a File
     * object.
     *
     * @return an object which is the value of the ResourceItem.
     */
    Object getItemValue();

    /**
     * Returns a String representing the item key.
     *
     * @return returns an object representing the resource item
     */
    String getItemKey();

    /**
     * Set the item key.
     *
     * @param key - the resource item key
     */
    void setItemKey(String key);

    /**
     * Returns an object representing the resource item.
     *
     * @return returns an object representing the resource item
     */
    String getItemLabel();

    /**
     * Returns an object representing the resource item.
     *
     * @param label new value
     */
    void setItemLabel(String label);

    /**
     * Returns an boolean value indicating if the item should be selectable
     * in the file chooser list-box.
     *
     * @return true if the item in the list-box should be disabled.
     */
    boolean isItemDisabled();

    /**
     * Sets the item disabled flag.If set to true the item should not be
     * selectable.
     *
     * @param disabled flag when set to true indicates item is not selectable.
     */
    void setItemDisabled(boolean disabled);

    /**
     * Returns a flag indicating if the resource item is a container. If true
     * the item is a container item.
     *
     * @return true if the item is a container, false otherwise.
     */
    boolean isContainerItem();

    @Override
    boolean equals(Object resourceItem);
}

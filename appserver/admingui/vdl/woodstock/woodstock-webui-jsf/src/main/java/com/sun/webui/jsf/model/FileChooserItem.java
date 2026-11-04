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

import java.io.File;
import java.io.Serializable;

/**
 * File chooser item.
 */
public final class FileChooserItem implements ResourceItem, Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -6760386335678324488L;

    /**
     * File item.
     */
    private File item = null;

    /**
     * Item key.
     */
    private String itemKey = null;

    /**
     * Item label.
     */
    private String itemLabel = null;

    /**
     * Item disabled.
     */
    private boolean itemDisabled = true;

    /**
     * Creates a new instance of FileChooserItem.
     * @param file file
     */
    public FileChooserItem(final File file) {
        this.item = file;
        StringBuilder buffer = new StringBuilder();
        if (item.isDirectory()) {
            buffer.append("folder=");
        } else {
            buffer.append("file=");
        }
        buffer.append(item.getAbsolutePath());
        this.itemKey = buffer.toString();
    }

    /**
     * Returns an object representing the value of this resource item. For the
     * default case of the FileChooser this would be a File object.
     *
     * @return an object which is the value of the ResourceItem.
     */
    @Override
    public Object getItemValue() {
        return item;
    }

    /**
     * Returns a String representing the item key.
     *
     * @return returns an object representing the resource item
     */
    @Override
    public String getItemKey() {
        return this.itemKey;
    }

    /**
     * Set the item key.
     *
     * @param key - the resource item key
     */
    @Override
    public void setItemKey(final String key) {
        if (key != null) {
            this.itemKey = key;
        }
    }

    /**
     * Returns an object representing the resource item.
     *
     * @return returns an object representing the resource item
     */
    @Override
    public String getItemLabel() {
        return this.itemLabel;
    }

    /**
     * Returns an object representing the resource item.
     * @param label new value
     */
    @Override
    public void setItemLabel(final String label) {

        if (label != null) {
            this.itemLabel = label;
        }
    }

    /**
     * Returns an boolean value indicating if the item should be selectable in
     * the file chooser's list box.
     * @return true if the item in the list box should be disabled.
     */
    @Override
    public boolean isItemDisabled() {
        return this.itemDisabled;
    }

    /**
     * Sets the item disabled flag. If set to true the item should not be
     * selectable.
     * @param disabled flag when set to true indicates item is not selectable.
     */
    @Override
    public void setItemDisabled(final boolean disabled) {
        this.itemDisabled = disabled;
    }

    /**
     * Returns a flag indicating if the resource item is a container. If true
     * the item is a container item.
     *
     * @return true if the item is a container, false otherwise.
     */
    @Override
    public boolean isContainerItem() {
        return item.isDirectory();
    }

    @Override
    public boolean equals(final Object object) {

        if (!(object instanceof ResourceItem)) {
            return false;
        }
        ResourceItem resourceItem = (ResourceItem) object;

        boolean flags = isContainerItem() == resourceItem.isContainerItem()
                && isItemDisabled() == resourceItem.isItemDisabled();

        if (!flags) {
            return false;
        }
        if (!objectEquals(getItemValue(), resourceItem.getItemValue())) {
            return false;
        }
        if (!objectEquals(getItemKey(), resourceItem.getItemKey())) {
            return false;
        }
        return objectEquals(getItemLabel(), resourceItem.getItemLabel());
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash;
        if (this.item != null) {
            hash = hash + this.item.hashCode();
        }
        hash = 79 * hash;
        if (this.itemKey != null) {
            hash = hash + this.itemKey.hashCode();
        }
        hash = 79 * hash;
        if (this.itemLabel != null) {
            hash = hash + this.itemLabel.hashCode();
        }
        hash = 79 * hash;
        if (this.itemDisabled) {
            hash = hash + 1;
        }
        return hash;
    }

    /**
     * Shallow equal testing.
     * @param obj0 object to test equality
     * @param obj1 object to test equality
     * @return boolean
     */
    private static boolean objectEquals(final Object obj0, final Object obj1) {
        if (obj0 == null) {
            return obj1 == null;
        } else {
            // Should handle null
            return !obj0.equals(obj1);
        }
    }
}

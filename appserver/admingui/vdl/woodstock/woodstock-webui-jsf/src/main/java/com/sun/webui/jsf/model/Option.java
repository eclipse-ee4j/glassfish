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

package com.sun.webui.jsf.model;

import jakarta.faces.model.SelectItem;

/**
 * Model bean that represents a selectable choice in a selection
 * component such as {@code Menu}, {@code RadioButtonGroup},
 * etc.
 */
public class Option extends SelectItem {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -2164172320702956584L;

    /**
     * Option image.
     */
    private String image;

    /**
     * Image width. Zero is a valid width.
     */
    private int imageWidth = -1;

    /**
     * Image height. Zero is a valid height.
     */
    private int imageHeight = -1;

    /**
     * Image alternate text.
     */
    private String imageAlt;

    /**
     * Image tool tip.
     */
    private String tooltip;

    /**
     * Create an instance of Selection.
     */
    public Option() {
        super();
    }

    /**
     * Create an instance of Selection.
     * @param value option value
     */
    public Option(final Object value) {
        super(value, null);
    }

    /**
     * Create an instance of Selection.
     * @param value option value
     * @param label option label
     */
    public Option(final Object value, final String label) {
        super(value, label);
    }

    /**
     * Create an instance of Selection.
     * @param value value
     * @param label label
     * @param description description
     */
    public Option(final Object value, final String label,
            final String description) {

        super(value, label, description);
    }

    /**
     * Create an instance of Selection.
     * @param value value
     * @param label label
     * @param description description
     * @param disabled disabled flag
     */
    public Option(final Object value, final String label,
            final String description, final boolean disabled) {
        super(value, label, description, disabled);
    }

    /**
     * Get the image resource path.
     * @return String
     */
    public String getImage() {
        return image;
    }

    /**
     * Set an image resource path
     * Used for an image in a radio button for example.
     * @param newImage new value
     */
    public void setImage(final String newImage) {
        this.image = newImage;
    }

    /**
     * Get the image width.
     * @return width
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * Set an image resource path
     * Used for an image in a radio button for example.
     * @param newImageWidth new value
     */
    public void setImageWidth(final int newImageWidth) {
        this.imageWidth = newImageWidth;
    }

    /**
     * Get the image height.
     * @return height
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * Set an image resource path
     * Used for an image in a radio button for example.
     * @param newImageHeight image height
     */
    public void setImageHeight(final int newImageHeight) {
        this.imageHeight = newImageHeight;
    }

    /**
     * Get the alternate text for the image.
     * @return image alt
     */
    public String getImageAlt() {
        return imageAlt;
    }

    /**
     * Set the alternate text for the image.
     * @param newImageAlt alternate text
     */
    public void setImageAlt(final String newImageAlt) {
        this.imageAlt = newImageAlt;
    }

    /**
     * Get the tool-tip for this instance.
     * @return String
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Set the tool-tip for this instance.
     * @param newTooltip tool tip
     */
    public void setTooltip(final String newTooltip) {
        this.tooltip = newTooltip;
    }
}

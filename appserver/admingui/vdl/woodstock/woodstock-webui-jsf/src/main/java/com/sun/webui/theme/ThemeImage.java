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

package com.sun.webui.theme;

/**
 * {@code ThemeImage} encapsulates the properties of a theme image
 * resource. If a theme provides an image resource it is desirable that the
 * theme also be configured with the details of that image in addition
 * to the path location of the resource, such as the
 * height, width, the units that the dimensions are expressed in,
 * alt text if the browser cannot load the image, and a title
 * that acts as a tool tip in a browser environment.
 */
public final class ThemeImage {

    /**
     * The suffix applied to the theme property key that defines an image
     * for the height of the image expressed in the units defined for the units
     * member.
     */
    public static final String HEIGHT_SUFFIX = "_HEIGHT";

    /**
     * The suffix applied to the theme property key that defines an image
     * for the width of the image expressed in the units defined for the units
     * member.
     */
    public static final String WIDTH_SUFFIX = "_WIDTH";

    /**
     * The suffix applied to the theme property key that defines an image
     * for the units to expressed by the width and height properties.
     */
    public static final String UNITS_SUFFIX = "_UNITS";

    /**
     * The suffix applied to the theme property key that defines an image
     * for the alt text for the image if the resource cannot be loaded
     * in the client.
     */
    public static final String ALT_SUFFIX = "_ALT";

    /**
     * The suffix applied to the theme property key that defines an image
     * for the title attribute for the image and used for a tool-tip
     * in the client.
     */
    public static final String TITLE_SUFFIX = "_TITLE";

    /**
     * Defines the recognized units that the height and width can be
     * expressed in.
     */
    public enum UNITS {
        /**
         * Pixels.
         */
        px,

        /**
         * Scalable.
         */
        em,

        /**
         * Percentage.
         */
        percent,

        /**
         * None.
         */
        none
    };

    /**
     * Image width.
     */
    private int width;

    /**
     * Image height.
     */
    private int height;

    /**
     * Image size units.
     */
    private UNITS units;

    /**
     * Image alternative description.
     */
    private String alt;

    /**
     * Image title.
     */
    private String title;

    /**
     * Image path.
     */
    private String path;

    /**
     * Construct a {@code ThemeImage} from the passed arguments.
     * @param newWidth image width
     * @param newHeight image height
     * @param newUnit size units
     * @param newAlt alt attribute value
     * @param newTitle image title
     * @param newPath image path
     */
    public ThemeImage(final int newWidth, final int newHeight,
            final ThemeImage.UNITS newUnit, final String newAlt,
            final String newTitle, final String newPath) {

        this.width = newWidth;
        this.height = newHeight;
        this.units = newUnit;
        this.alt = newAlt;
        this.title = newTitle;
        this.path = newPath;
    }

    /**
     * Returns the width of the image in units defined in the
     * {@code units} member.
     * @return width
     * @see #getUnits()
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the image in units defined in the
     * {@code units} member.
     * @return height
     * @see #getUnits()
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the {@code UNITS} expressed in the {@code height}
     * and {@code width}  members.{@code units} member.
     * @return size units
     * @see #getHeight()
     * @see #setHeight(int)
     * @see #getWidth()
     * @see #setWidth(int)
     */
    public ThemeImage.UNITS getUnits() {
        return units;
    }

    /**
     * Returns the {@code alt} member or alternate text to display if the
     * client cannot render the image.
     * @return alt attribute value
     */
    public String getAlt() {
        return alt;
    }

    /**
     * Returns the {@code title} member sometimes used to display a
     * tool tip on the client.
     * @return image title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the application or jar relative path of this image resource.The
     * path is relative to location that can be found on the application class
     * path.
     *
     * @return image path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the width of the image in units defined by the {@code units}
     * member.
     * @param newWidth new width
     */
    public void setWidth(final int newWidth) {
        this.width = newWidth;
    }

    /**
     * Set the height of the image in units defined by the {@code units}
     * member.
     * @param newHeight new height
     */
    public void setHeight(final int newHeight) {
        this.height = newHeight;
    }

    /**
     * Set the units used by the {@code height} and {@code width}
     * members.
     * @param newUnits new units
     */
    public void setUnits(final ThemeImage.UNITS newUnits) {
        this.units = newUnits;
    }

    /**
     * Set the text that will appear on the client if the image cannot be
     * loaded.
     * @param newAlt new alt
     */
    public void setAlt(final String newAlt) {
        this.alt = newAlt;
    }

    /**
     * Set the text that may appear as a tool tip on the client.
     * @param newTitle new title
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
    }

    /**
     * Set the application or jar relative path of the image.
     * The path is relative to location that can be found on the application
     * class path. This path must begin with a "/".
     * @param newPath new path
     */
    public void setPath(final String newPath) {
        this.path = newPath;
    }
}

/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: ChartItem.java,v 1.3 2004/11/14 07:33:13 tcfujii Exp $
 */

package components.model;

/**
 * This class represents an individual graphable item for the chart conmponent.
 */
public class ChartItem {

    public ChartItem() {
       super();
    }

    public ChartItem(String label, int value, String color) {
        setLabel(label);
        setValue(value);
        setColor(color);
    }

    /**
     * <p>The label for this item.</p>
     */
    private String label = null;
    /**
     *<p>Return the label for this item.</p>
     */
    public String getLabel() {
        return label;
    }
    /**
     * <p>Set the label for this item.</p>
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * <p>The value for this item.</p>
     */
    private int value = 0;
    /**
     *<p>Return the value for this item.</p>
     */
    public int getValue() {
        return value;
    }
    /**
     * <p>Set the value for this item.</p>
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * <p>The color for this item.</p>
     */
    private String color = null;
    /**
     *<p>Return the color for this item.</p>
     */
    public String getColor() {
        return color;
    }
    /**
     * <p>Set the color for this item.</p>
     */
    public void setColor(String color) {
        this.color = color;
    }
}

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
 * $Id: ChartBean.java,v 1.3 2004/11/14 07:33:17 tcfujii Exp $
 */

package demo.model;

import java.util.ArrayList;
import java.util.Collection;

import components.model.ChartItem;
public class ChartBean {

    // Bar Chart Properties -------------------------

    public static final int    VERTICAL = 0;
    public static final int     HORIZONTAL = 1;

    private int orientation = VERTICAL;
    public int getOrientation() {
        return orientation;
    }
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    // ----------------------------------------------

    private int columns = 0;
    public int getColumns() {
        return columns;
    }
    public void setColumns(int columns) {
        this.columns = columns;
    }

    private ArrayList chartItems = null;
    public Collection getChartItems() {
        return chartItems;
    }

    private String title = null;
    public String getTitle() {
        return title;
    }
    public void setTitle() {
        this.title = title;
    }

    private int scale = 10;
    public int getScale() {
        return scale;
    }
    public void setScale(int scale) {
        this.scale = scale;
    }

    private int width = 400;
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    private int height = 300;
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height= height;
    }

    public ChartBean() {

         setWidth(400);
         setHeight(300);
         setColumns(2);
         setOrientation(ChartBean.HORIZONTAL);

        chartItems = new ArrayList(columns);
        chartItems.add(new ChartItem("one", 10, "red"));
        chartItems.add(new ChartItem("two", 20, "blue"));

    }
}

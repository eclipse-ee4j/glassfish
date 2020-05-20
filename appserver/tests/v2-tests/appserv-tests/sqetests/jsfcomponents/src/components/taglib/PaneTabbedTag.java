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
 * $Id: PaneTabbedTag.java,v 1.1 2005/11/03 03:00:13 SherryShen Exp $
 */

package components.taglib;


import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import jakarta.faces.component.UIComponent;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.webapp.UIComponentTag;


/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents a the overall tabbed pane control.
 */
public class PaneTabbedTag extends UIComponentTag {


    private static Log log = LogFactory.getLog(PaneTabbedTag.class);


    private String contentClass = null;


    public void setContentClass(String contentClass) {
        this.contentClass = contentClass;
    }


    private String paneClass = null;


    public void setPaneClass(String paneClass) {
        this.paneClass = paneClass;
    }


    private String selectedClass = null;


    public void setSelectedClass(String selectedClass) {
        this.selectedClass = selectedClass;
    }


    private String unselectedClass = null;


    public void setUnselectedClass(String unselectedClass) {
        this.unselectedClass = unselectedClass;
    }


    public String getComponentType() {
        return ("Pane");
    }


    public String getRendererType() {
        return ("Tabbed");
    }


    public void release() {
        super.release();
        contentClass = null;
        paneClass = null;
        selectedClass = null;
        unselectedClass = null;
    }


    protected void setProperties(UIComponent component) {

        super.setProperties(component);

        if (contentClass != null) {
            if (isValueReference(contentClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(contentClass);
                component.setValueBinding("contentClass", vb);
            } else {
                component.getAttributes().put("contentClass", contentClass);
            }
        }

        if (paneClass != null) {
            if (isValueReference(paneClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(paneClass);
                component.setValueBinding("paneClass", vb);
            } else {
                component.getAttributes().put("paneClass", paneClass);
            }
        }

        if (selectedClass != null) {
            if (isValueReference(selectedClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(selectedClass);
                component.setValueBinding("selectedClass", vb);
            } else {
                component.getAttributes().put("selectedClass", selectedClass);
            }
        }

        if (unselectedClass != null) {
            if (isValueReference(unselectedClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(unselectedClass);
                component.setValueBinding("unselectedClass", vb);
            } else {
                component.getAttributes().put("unselectedClass",
                                              unselectedClass);
            }
        }
    }


}

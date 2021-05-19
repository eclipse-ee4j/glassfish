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
 * $Id: PaneTabTag.java,v 1.1 2005/11/03 03:00:12 SherryShen Exp $
 */

package components.taglib;


import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import jakarta.faces.component.UIComponent;
import jakarta.faces.webapp.UIComponentTag;
import jakarta.faces.el.ValueBinding;


/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents an individual tab on the overall control.
 */
public class PaneTabTag extends UIComponentTag {


    private static Log log = LogFactory.getLog(PaneTabTag.class);


    public String getComponentType() {
        return ("Pane");
    }


    public String getRendererType() {
        return ("Tab");
    }


    public void release() {
        super.release();
    }

    protected String paneClass;
    public String getPaneClass() {
        return paneClass;
    }

    public void setPaneClass(String newPaneClass) {
        paneClass = newPaneClass;
    }



    protected void setProperties(UIComponent component) {
        super.setProperties(component);

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

    }
}

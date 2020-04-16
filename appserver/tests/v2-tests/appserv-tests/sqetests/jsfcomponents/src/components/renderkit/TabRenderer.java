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
 * $Id: TabRenderer.java,v 1.1 2005/11/03 03:00:02 SherryShen Exp $
 */

package components.renderkit;


import components.components.PaneComponent;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.util.Iterator;


/**
 * <p>Render the individual {@link PaneComponent} and its children, but
 * <strong>only</strong> if this {@link PaneComponent} is currently
 * selected.  Otherwise, no output at all is sent.</p>
 *
 * <p><strong>IMPLEMENTATION NOTE</strong> - Because of the fact that we
 * want standard JSP text (not nested inside Faces components) to be usable
 * on a pane, this Renderer needs to know whether it is being used in a JSP
 * environment (where the rendering of the child components writes to the
 * local value of our Pane component) or not (where we must do it ourselves).
 * This is resolved by having the <code>Pane_Tab</code> tag set a
 * render dependent attribute named "demo.renderer.TabRenderer.JSP" when it
 * creates the corresponding component, so that we can tell what is going on.
 * </p>
 */

public class TabRenderer extends BaseRenderer {


    private static Log log = LogFactory.getLog(TabRenderer.class);


    public void decode(FacesContext context, UIComponent component) {
    }


    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {

        if (log.isDebugEnabled()) {
            log.debug("encodeBegin(" + component.getId() + ")");
        }

    }


    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {

        if (log.isDebugEnabled()) {
            log.debug("encodeChildren(" + component.getId() + ")");
        }

    }


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if (log.isDebugEnabled()) {
            log.debug("encodeEnd(" + component.getId() + ")");
        }

        // Render our children only -- our parent has rendered ourself
        Iterator kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            encodeRecursive(context, kid);
        }

    }


}

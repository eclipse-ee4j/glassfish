/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package template;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.util.*;


public class DefinitionTag extends SimpleTagSupport {
    private String definitionName = null;
    private String screenId;
    private HashMap screens = null;

    public DefinitionTag() {
        super();
    }

    public HashMap getScreens() {
        return screens;
    }

    public void setName(String name) {
        this.definitionName = name;
    }

    public void setScreen(String screenId) {
        this.screenId = screenId;
    }

    public void doTag() {
        try {
            screens = new HashMap();

            getJspBody()
                .invoke(null);

            Definition definition = new Definition();
            PageContext context = (PageContext) getJspContext();
            ArrayList params = (ArrayList) screens.get(screenId);
            Iterator ir = null;

            if (params != null) {
                ir = params.iterator();

                while (ir.hasNext())
                    definition.setParam((Parameter) ir.next());

                // put the definition in the page context
                context.setAttribute(definitionName, definition,
                    context.APPLICATION_SCOPE);
            } else {
                Debug.println("DefinitionTag: params are not defined.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

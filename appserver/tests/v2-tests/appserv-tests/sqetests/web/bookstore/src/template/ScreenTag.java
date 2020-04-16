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

import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import jakarta.servlet.jsp.PageContext;
import java.util.*;


public class ScreenTag extends SimpleTagSupport {
    private String screenId;
    private ArrayList parameters = null;

    public ScreenTag() {
        super();
    }

    public ArrayList getParameters() {
        return parameters;
    }

    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    public void doTag() {
        parameters = new ArrayList();

        HashMap screens = (HashMap) ((DefinitionTag) getParent()).getScreens();

        if (screens != null) {
            try {
                if (!screens.containsKey(screenId)) {
                    screens.put(screenId, parameters);
                }

                getJspBody()
                    .invoke(null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Debug.println("ScreenTag: Unable to get screens object.");
        }
    }
}

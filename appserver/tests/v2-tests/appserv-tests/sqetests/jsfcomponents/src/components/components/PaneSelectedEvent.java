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
 * $Id: PaneSelectedEvent.java,v 1.3 2004/11/14 07:33:13 tcfujii Exp $
 */

package components.components;


import jakarta.faces.component.UIComponent;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;


/**
 * A custom event which indicates the currently selected pane
 * in a tabbed pane control.
 */
public class PaneSelectedEvent extends FacesEvent {


    public PaneSelectedEvent(UIComponent component, String id) {
        super(component);
        this.id = id;
    }


    // The component id of the newly selected child pane
    private String id = null;


    public String getId() {
        return (this.id);
    }


    public String toString() {
        StringBuffer sb = new StringBuffer("PaneSelectedEvent[id=");
        sb.append(id);
        sb.append("]");
        return (sb.toString());
    }


    public boolean isAppropriateListener(FacesListener listener) {
        return (listener instanceof PaneComponent.PaneSelectedListener);
    }


    public void processListener(FacesListener listener) {
        ((PaneComponent.PaneSelectedListener) listener).processPaneSelectedEvent(
            this);
    }

}

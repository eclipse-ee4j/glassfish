/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.component;

/**
 * <p>
 * This <code>UIComponent</code> exists so that custom events may be queued and triggered at appropriate times. This
 * will allow "beforeEncode", "afterEncode", and even "command" events to be associated with components by wrapping 1 or
 * more components with this component.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class EventComponent extends TemplateComponentBase {

    /**
     * <p>
     * This is the location of the XML file that declares the layout for the EventComponent. (/jsftemplating/event.xml)
     * </p>
     */
    public static final String LAYOUT_KEY = "/jsftemplating/event.xml";

    /**
     * <p>
     * Constructor for <code>EventComponent</code>.
     * </p>
     */
    public EventComponent() {
        super();
        setRendererType("com.sun.jsftemplating.EventComponent");
        setLayoutDefinitionKey(LAYOUT_KEY);
    }

    /**
     * <p>
     * Return the family for this component.
     * </p>
     */
    @Override
    public String getFamily() {
        return "com.sun.jsftemplating.EventComponent";
    }

    /**
     * <p>
     * Restore the state of this component.
     * </p>
     * public void restoreState(FacesContext _context,Object _state) { Object _values[] = (Object[]) _state;
     * super.restoreState(_context, _values[0]); }
     */

    /**
     * <p>
     * Save the state of this component.
     * </p>
     * public Object saveState(FacesContext _context) { Object _values[] = new Object[1]; _values[0] =
     * super.saveState(_context); return _values; }
     */

}

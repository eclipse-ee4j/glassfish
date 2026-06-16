/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.event;

import jakarta.faces.component.UIComponent;

/**
 * <p>
 * This event is typically invoked when a factory not only creates a component, but creates children under that
 * component. This event may be invoked to allow a page author to have greater control over what happens during the
 * child creation. See individual factory JavaDocs to see which factories support this and what may be done during this
 * event.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class CreateChildEvent extends EventObjectBase implements UIComponentHolder {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param component The <code>UIComponent</code> associated with this <code>EventObject</code>.
     */
    public CreateChildEvent(UIComponent component) {
        super(component);
    }

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param component The <code>UIComponent</code> associated with this <code>EventObject</code>.
     */
    public CreateChildEvent(UIComponent component, Object data) {
        super(component);
        setData(data);
    }

    /**
     * <p>
     * This method provides access to extra data that is set by the creator of this Event. See documentation of the code
     * that fires this event to learn what (if anything) is stored here.
     * </p>
     */
    public Object getData() {
        return _data;
    }

    /**
     * <p>
     * This setter allows extending classes to set this value via this setter. Normally this value is passed into the
     * constructor.
     * <p>
     */
    protected void setData(Object data) {
        _data = data;
    }

    /**
     * <p>
     * The "createChild" event type. ("createChild")
     * </p>
     */
    public static final String EVENT_TYPE = "createChild";

    /**
     * <p>
     * This value provides extra information that can be associated with this event. See code that uses this event to learn
     * more about what (if anything) this information is used for.
     * </p>
     */
    private Object _data = null;
}

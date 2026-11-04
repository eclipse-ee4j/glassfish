/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.mirror;

/**
 * A base class that defines the basic metadata available for an event set,
 * whether it belongs to a class declared in the current compilation unit, or to
 * a class in a dependent library.
 */
public abstract class EventInfo extends FeatureInfo {

    /**
     * Property info.
     */
    private PropertyInfo propertyInfo;

    /**
     * Original event info.
     */
    private EventInfo original = null;

    /**
     * Get the simple name of the method used to add event listeners.
     * @return String
     */
    public abstract String getAddListenerMethodName();

    /**
     * Get the simple name of the method used to remove event listeners.
     * @return String
     */
    public abstract String getRemoveListenerMethodName();

    /**
     * Get the simple name of the method used to get event listeners. May be
     * null if there is no such method.
     * @return String, may be {@code null} if there is no such method.
     */
    public abstract String getGetListenersMethodName();

    /**
     * Returns the signature of the singleton method defined by this event's
     * listener class or interface.
     * @return String
     */
    public abstract String getListenerMethodSignature();

    /**
     * Returns the simple name of the event listener class's singleton method.
     * @return String
     */
    public abstract String getListenerMethodName();

    /**
     * Returns an array of the fully qualified names of the singleton listener
     * method's parameters.
     * @return String[]
     */
    public abstract String[] getListenerMethodParameterClassNames();

    /**
     * Returns the fully qualified name of the event listener class.
     * @return String
     */
    public abstract String getListenerClassName();

    /**
     * Get the {@code PropertyInfo} instance corresponding to the binding
     * property.
     * This method walks the copy chain and returns the first non {@code null}
     * binding property.
     * @return PropertyInfo or {@code null} if no property is used to bind this
     * event.
     */
    public PropertyInfo getPropertyInfo() {
        PropertyInfo propInfo = null;
        EventInfo eventInfo = this;
        while (propInfo == null && eventInfo != null) {
            propInfo = eventInfo.propertyInfo;
            eventInfo = eventInfo.original;
        }
        return propInfo;
    }

    /**
     * Set the property info if not already set.
     * @param propInfo new prop info
     */
    void setPropertyInfo(final PropertyInfo propInfo) {
        if (this.propertyInfo != null) {
            throw new IllegalStateException("propertyInfo is already set");
        }
        this.propertyInfo = propInfo;
    }

    /**
     * Create a copy of this event. Copies have a reference to the original
     * in order to delegate the call to {@link #getPropertyInfo()}.
     * @return EventInfo
     */
    abstract EventInfo copy();

    /**
     * Get the original event.
     * @return EventInfo
     */
    EventInfo getOriginal() {
        return original;
    }

    /**
     * Set the original event.
     * @param newOriginal new original event
     */
    public void setOriginal(final EventInfo newOriginal) {
        this.original = newOriginal;
    }
}

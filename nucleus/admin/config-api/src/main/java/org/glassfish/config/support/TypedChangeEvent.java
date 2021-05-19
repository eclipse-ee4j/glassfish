/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.config.support;

import java.beans.PropertyChangeEvent;

/**
 * Simple extension to java beans events to support notification for adding/removing indexed properties.
 *
 */
public class TypedChangeEvent extends PropertyChangeEvent {

    public enum Type {
        ADD, REMOVE, CHANGE
    };

    final Type type;

    public TypedChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
        type = Type.CHANGE;
    }

    public TypedChangeEvent(Object source, String propertyName, Object oldValue, Object newValue, Type type) {
        super(source, propertyName, oldValue, newValue);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

}

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

import java.beans.EventSetDescriptor;
import java.lang.reflect.Method;

/**
 * Introspected event info.
 */
public final class IntrospectedEventInfo extends EventInfo {

    /**
     * Event descriptor.
     */
    private final EventSetDescriptor eventDescriptor;

    /**
     * Listener method signature.
     */
    private String listenerMethodSignature;

    /**
     * Listener method parameter class names.
     */
    private String[] listenerMethodParameterClassNames;

    /**
     * Create a new instance.
     * @param evtDescriptor event descriptor
     */
    IntrospectedEventInfo(final EventSetDescriptor evtDescriptor) {
        this.eventDescriptor = evtDescriptor;
    }

    @Override
    public String getName() {
        return this.eventDescriptor.getName();
    }

    @Override
    public String getDisplayName() {
        return this.eventDescriptor.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return this.eventDescriptor.getShortDescription();
    }

    @Override
    public String getAddListenerMethodName() {
        if (this.eventDescriptor.getAddListenerMethod() != null) {
            return this.eventDescriptor.getAddListenerMethod().getName();
        }
        return null;
    }

    @Override
    public String getRemoveListenerMethodName() {
        if (this.eventDescriptor.getRemoveListenerMethod() != null) {
            return this.eventDescriptor.getRemoveListenerMethod().getName();
        }
        return null;
    }

    @Override
    public String getGetListenersMethodName() {
        if (this.eventDescriptor.getGetListenerMethod() != null) {
            return this.eventDescriptor.getGetListenerMethod().getName();
        }
        return null;
    }

    @Override
    public String getListenerMethodSignature() {
        if (this.listenerMethodSignature == null) {
            StringBuilder buffer = new StringBuilder();
            Method listenerMethod = this.eventDescriptor
                    .getListenerMethods()[0];
            buffer.append(listenerMethod.getReturnType().toString());
            buffer.append(" ");
            buffer.append(listenerMethod.getName());
            buffer.append("(");
            for (Class paramClass : listenerMethod.getParameterTypes()) {
                buffer.append(paramClass.getName());
                buffer.append(",");
            }
            buffer.setLength(buffer.length() - 1);
            buffer.append(")");
            this.listenerMethodSignature = buffer.toString();
        }
        return this.listenerMethodSignature;
    }

    @Override
    public String getListenerClassName() {
        return this.eventDescriptor.getListenerType().getName();
    }

    @Override
    public String[] getListenerMethodParameterClassNames() {
        if (listenerMethodParameterClassNames == null) {
            Class[] paramClasses = this.eventDescriptor
                            .getListenerMethods()[0]
                            .getParameterTypes();
            listenerMethodParameterClassNames =
                    new String[paramClasses.length];
            for (int i = 0; i < paramClasses.length; i++) {
                listenerMethodParameterClassNames[i] =
                        paramClasses[i].getName();
            }
        }
        return listenerMethodParameterClassNames;
    }

    @Override
    public String getListenerMethodName() {
        return this.eventDescriptor.getListenerMethods()[0].getName();
    }

    @Override
    public boolean isHidden() {
        return this.eventDescriptor.isHidden();
    }

    @Override
    EventInfo copy() {
        IntrospectedEventInfo copy = new IntrospectedEventInfo(eventDescriptor);
        copy.setOriginal(this);
        copy.setDeclaringClassInfo(getDeclaringClassInfo());
        copy.setPropertyInfo(getPropertyInfo());
        copy.listenerMethodParameterClassNames =
                this.listenerMethodParameterClassNames;
        copy.listenerMethodSignature = this.listenerMethodSignature;
        return copy;
    }
}

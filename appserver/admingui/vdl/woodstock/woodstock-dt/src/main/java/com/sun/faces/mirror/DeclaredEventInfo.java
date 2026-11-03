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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Declared event information.
 */
public final class DeclaredEventInfo extends EventInfo {

    /**
     * Event name key.
     */
    static final String NAME = "name";

    /**
     * Event display name key.
     */
    static final String DISPLAY_NAME = "displayName";

    /**
     * Event short description key.
     */
    static final String SHORT_DESCRIPTION = "shortDescription";

    /**
     * Event add listener method name key.
     */
    static final String ADD_LISTENER_METHOD_NAME = "addListenerMethodName";

    /**
     * Event remove listener method name key.
     */
    static final String REMOVE_LISTENER_METHOD_NAME =
            "removeListenerMethodName";

    /**
     * Event get listeners method name key.
     */
    static final String GET_LISTENERS_METHOD_NAME = "getListenersMethodName";

    /**
     * Event isDefault flag key.
     */
    static final String IS_DEFAULT = "isDefault";

    /**
     * Declared element representing the event.
     */
    private final Element decl;

    /**
     * Annotation value map.
     */
    private final Map<String, Object> annotationValueMap;

    /**
     * Listener method name.
     */
    private String listenerMethodName;

    /**
     * Listener class.
     */
    private Class listenerClass;

    /**
     * Listener method parameter class names.
     */
    private String[] listenerMethodParameterClassNames;

    /**
     * Listener method.
     */
    private ExecutableElement listenerDeclaration;

    /**
     * Add listener method name.
     */
    private String addListenerMethodName;

    /**
     * Remove listener method name.
     */
    private String removeListenerMethodName;

    /**
     * Get listener method name.
     */
    private String getListenersMethodName;

    /**
     * Event name.
     */
    private String name;

    /**
     * Create a new instance.
     * @param annotValueMap annotation value map
     * @param eltDecl element declaration representing the event
     */
    DeclaredEventInfo(final Map<String, Object> annotValueMap,
            final Element eltDecl) {

        this.annotationValueMap = annotValueMap;
        this.decl = eltDecl;
    }

    /**
     * Get the annotation value map.
     * @return {@code Map<String, Object>}
     */
    Map<String, Object> getAnnotationValueMap() {
        return annotationValueMap;
    }

    /**
     * Get the declaration representing this event.
     * @return Element
     */
    public Element getDeclaration() {
        return this.decl;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Set the event name.
     * @param eventName new event name
     */
    void setName(final String eventName) {
        this.name = eventName;
    }

    @Override
    public String getDisplayName() {
        return this.getName();
    }

    @Override
    public String getShortDescription() {
        return this.getDisplayName();
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public String getAddListenerMethodName() {
        if (this.addListenerMethodName == null) {
            this.addListenerMethodName = (String) this.annotationValueMap
                    .get(ADD_LISTENER_METHOD_NAME);
        }
        return this.addListenerMethodName;
    }

    /**
     * Set the add listener method name.
     * @param addMethodName new add listener method name
     */
    void setAddListenerMethodName(final String addMethodName) {
        this.addListenerMethodName = addMethodName;
    }

    @Override
    public String getRemoveListenerMethodName() {
        if (this.removeListenerMethodName == null) {
            this.removeListenerMethodName = (String) this.annotationValueMap
                    .get(REMOVE_LISTENER_METHOD_NAME);
        }
        return this.removeListenerMethodName;
    }

    /**
     * Set the remove listener method name.
     * @param removeMethodName new remove listener method name
     */
    void setRemoveListenerMethodName(final String removeMethodName) {
        this.removeListenerMethodName = removeMethodName;
    }

    @Override
    public String getGetListenersMethodName() {
        if (this.getListenersMethodName == null) {
            this.getListenersMethodName = (String) this.annotationValueMap
                    .get(GET_LISTENERS_METHOD_NAME);
        }
        return this.getListenersMethodName;
    }

    /**
     * Set the get listener method name.
     * @param getMethodName new get listener method name
     */
    void setGetListenersMethodName(final String getMethodName) {
        this.getListenersMethodName = getMethodName;
    }

    @Override
    public String getListenerMethodSignature() {
        StringBuilder buffer = new StringBuilder();
        if (this.getListenerDeclaration() != null) {
            ExecutableElement methodDecl = this.getListenerDeclaration();
            buffer.append(methodDecl.getReturnType().toString());
            buffer.append(" ");
            buffer.append(methodDecl.getSimpleName());
            buffer.append("(");
            for (VariableElement paramDecl : methodDecl.getParameters()) {
                buffer.append(paramDecl.asType().toString());
                buffer.append(",");
            }
            buffer.setLength(buffer.length() - 1);
            buffer.append(")");
        } else if (this.getListenerClass() != null) {
            Method listenerMethod = this.getListenerClass().getMethods()[0];
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
        }
        return buffer.toString();
    }

    /**
     * Get the listener method declaration.
     * @return ExecutableElement
     */
    public ExecutableElement getListenerDeclaration() {
        return this.listenerDeclaration;
    }

    /**
     * Set the listener method declaration.
     * @param listenerMethod new listener method declaration
     */
    void setListenerDeclaration(final ExecutableElement listenerMethod) {
        this.listenerDeclaration = listenerMethod;
    }

    /**
     * Get the listener class.
     * @return Class
     */
    public Class getListenerClass() {
        return this.listenerClass;
    }

    /**
     * Set the listener class.
     * @param clazz new listener class
     */
    void setListenerClass(final Class clazz) {
        this.listenerClass = clazz;
    }

    @Override
    public String getListenerClassName() {
        if (this.getListenerDeclaration() != null) {
            return this.getListenerDeclaration().getSimpleName().toString();
        } else if (this.getListenerClass() != null) {
            return this.getListenerClass().getName();
        }
        return null;
    }

    @Override
    public String getListenerMethodName() {
        if (listenerMethodName == null) {
            if (this.getListenerDeclaration() != null) {
                ExecutableElement methodDecl = this.getListenerDeclaration();
                listenerMethodName = methodDecl.getSimpleName().toString();
            } else if (this.getListenerClass() != null) {
                listenerMethodName = this.getListenerClass().getMethods()[0]
                        .getName();
            }
        }
        return listenerMethodName;
    }

    @Override
    public String[] getListenerMethodParameterClassNames() {
        if (listenerMethodParameterClassNames == null) {
            ArrayList<String> paramNameList = new ArrayList<String>();
            if (this.getListenerDeclaration() != null) {
                for (VariableElement paramDecl : this.getListenerDeclaration()
                        .getParameters()) {
                    paramNameList.add(paramDecl.asType().toString());
                }
            } else if (this.getListenerClass() != null) {
                for (Class paramClass : this.getListenerClass().getMethods()[0]
                        .getParameterTypes()) {
                    paramNameList.add(paramClass.getName());
                }
            }
            listenerMethodParameterClassNames = paramNameList.toArray(
                    new String[paramNameList.size()]);
        }
        return listenerMethodParameterClassNames;
    }

    @Override
    EventInfo copy() {
        DeclaredEventInfo copy =
                new DeclaredEventInfo(annotationValueMap, decl);
        copy.setOriginal(this);
        copy.setDeclaringClassInfo(getDeclaringClassInfo());
        copy.setPropertyInfo(getPropertyInfo());
        copy.name = this.name;
        copy.addListenerMethodName = this.addListenerMethodName;
        copy.getListenersMethodName = this.getListenersMethodName;
        copy.listenerClass = this.listenerClass;
        copy.listenerDeclaration = this.listenerDeclaration;
        copy.listenerMethodName = this.listenerMethodName;
        copy.listenerMethodParameterClassNames =
                this.listenerMethodParameterClassNames;
        copy.removeListenerMethodName = this.removeListenerMethodName;
        return copy;
    }
}

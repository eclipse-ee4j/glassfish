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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor6;

/**
 * Represents a class or interface declared in the current compilation unit.
 */
public abstract class DeclaredTypeInfo extends ClassInfo {

    /**
     * Annotation processing environment.
     */
    private final ProcessingEnvironment env;

    /**
     * Type element representing the type.
     */
    private final TypeElement decl;

    /**
     * Method names.
     */
    private Set<String> methodNames;

    /**
     * Properties.
     */
    private Map<String, PropertyInfo> propertyInfos;

    /**
     * Events.
     */
    private Map<String, EventInfo> eventInfos;

    /**
     * Class name.
     */
    private final String className;

    /**
     * Package name.
     */
    private final String packageName;

    /**
     * Super class info.
     */
    private ClassInfo superClassInfo;

    /**
     * Default property info.
     */
    private PropertyInfo defaultPropertyInfo;

    /**
     * Default event info.
     */
    private EventInfo defaultEventInfo;

    /**
     * Create a new instance.
     * @param processingEnv annotation processing environment
     * @param typeDecl type element representing the class
     */
    DeclaredTypeInfo(final ProcessingEnvironment processingEnv,
            final TypeElement typeDecl) {

        this.env = processingEnv;
        this.decl = typeDecl;
        String qualifiedName = this.decl.getQualifiedName().toString();
        int index = qualifiedName.lastIndexOf('.');
        if (index >= 0) {
            this.className = qualifiedName.substring(index + 1);
            this.packageName = qualifiedName.substring(0, index);
        } else {
            this.className = qualifiedName;
            this.packageName = null;
        }
    }

    /**
     * Get the annotation processing environment.
     * @return ProcessingEnvironment
     */
    ProcessingEnvironment getEnv() {
        return env;
    }

    /**
     * Get the type element representing the declared class.
     * @return TypeElement
     */
    public final TypeElement getDeclaration() {
        return this.decl;
    }

    @Override
    public final String getClassName() {
        return this.className;
    }

    @Override
    public final String getPackageName() {
        return this.packageName;
    }

    @Override
    public final ClassInfo getSuperClassInfo() {
        return this.superClassInfo;
    }

    /**
     * Set the super class info.
     * @param cInfo new super class info
     */
    final void setSuperClassInfo(final ClassInfo cInfo) {
        this.superClassInfo = cInfo;
    }

    @Override
    public final boolean isAssignableTo(final String qualifiedName) {
        TypeElement t2 = env.getElementUtils().getTypeElement(qualifiedName);
        return env.getTypeUtils().isAssignable(decl.asType(), t2.asType());
    }

    /**
     * Returns a map of all properties declared explicitly in this class or
     * interface.
     * @return {@code Map<String, PropertyInfo>}
     */
    @Override
    public final Map<String, PropertyInfo> getPropertyInfos() {
        return this.propertyInfos;
    }

    /**
     * Set the properties for this class.
     * @param propInfos new propertyInfos
     */
    final void setPropertyInfos(final Map<String, PropertyInfo> propInfos) {
        this.propertyInfos = propInfos;
        for (PropertyInfo propertyInfo : propInfos.values()) {
            DeclaredPropertyInfo declaredPropertyInfo =
                    (DeclaredPropertyInfo) propertyInfo;
            declaredPropertyInfo.setDeclaringClassInfo(this);
            if (Boolean.TRUE.equals(declaredPropertyInfo
                    .getAnnotationValueMap()
                    .get(DeclaredPropertyInfo.IS_DEFAULT))) {
                this.setDefaultPropertyInfo(declaredPropertyInfo);
            }
        }
    }

    /**
     * Returns a map of all events declared explicitly in this class or
     * interface.
     * @return {@code Map<String, EventInfo>}
     */
    @Override
    public final Map<String, EventInfo> getEventInfos() {
        return this.eventInfos;
    }

    /**
     * Set the events for this class.
     * @param evtInfos new eventInfos
     */
    final void setEventInfos(final Map<String, EventInfo> evtInfos) {
        this.eventInfos = evtInfos;
        for (EventInfo eventInfo : evtInfos.values()) {
            DeclaredEventInfo declaredEventInfo = (DeclaredEventInfo) eventInfo;
            declaredEventInfo.setDeclaringClassInfo(this);
            if (Boolean.TRUE.equals(declaredEventInfo
                    .getAnnotationValueMap()
                    .get(DeclaredEventInfo.IS_DEFAULT))) {
                this.setDefaultEventInfo(declaredEventInfo);
            }
        }
    }

    /**
     * This implementation returns the current value set on this instance.
     * @return PropertyInfo
     */
    @Override
    public PropertyInfo getDefaultPropertyInfo() {
        return this.defaultPropertyInfo;
    }

    /**
     * Set the default property info.
     * @param defaultPropInfo new default property info
     */
    final void setDefaultPropertyInfo(final PropertyInfo defaultPropInfo) {
        this.defaultPropertyInfo = defaultPropInfo;
    }

    /**
     * This implementation returns the current value set on this instance.
     * @return EventInfo
     */
    @Override
    public EventInfo getDefaultEventInfo() {
        return this.defaultEventInfo;
    }

    /**
     * Set the default event info.
     * @param defaultEvtInfo new default event info
     */
    final void setDefaultEventInfo(final EventInfo defaultEvtInfo) {
        this.defaultEventInfo = defaultEvtInfo;
    }

    @Override
    final Set<String> getMethodNames() {
        if (this.methodNames == null) {
            this.methodNames = new HashSet<String>();
            MethodVistitor visitor = new MethodVistitor();
            this.decl.accept(visitor, null);
            this.methodNames.addAll(visitor.getMethodNames());
        }
        return this.methodNames;
    }

    /**
     * Returns the JavaDoc comments associated with the type declaration.
     * @return String
     */
    public final String getDocComment() {
        return env.getElementUtils().getDocComment(decl);
    }

    /**
     * Visitor to get the methods and method names in a class.
     */
    private final class MethodVistitor
            extends SimpleElementVisitor6<Object, Object> {

        /**
         * Method names.
         */
        private final Set<String> methodNames = new HashSet<String>();

        /**
         * Methods.
         */
        private final Set<ExecutableElement> methods =
                new HashSet<ExecutableElement>();

        @Override
        public Object visitExecutable(final ExecutableElement elt,
                final Object p) {

            if (elt.getKind().equals(ElementKind.METHOD)
                    && elt.getModifiers().contains(Modifier.PUBLIC)
                    && !elt.getModifiers().contains(Modifier.STATIC)) {
                methods.add(elt);
                methodNames.add(elt.getSimpleName().toString());
            }
            return elt;
        }

        @Override
        public Object visitType(final TypeElement elt,
                final Object p) {

            for (Element e : env.getElementUtils().getAllMembers(elt)) {
                e.accept(this, p);
            }
            return elt;
        }

        /**
         * Get the visited method names.
         * @return Set of String
         */
        public Set<String> getMethodNames() {
            return methodNames;
        }

        /**
         * Get the visited methods.
         * @return Set of ExecutableElement
         */
        public Set<ExecutableElement> getMethods() {
            return methods;
        }
    }

    /**
     * Visit the type for the class and get the visited methods.
     * @return Set of ExecutableElement
     */
    final Set<ExecutableElement> getMethods() {
        MethodVistitor visitor = new MethodVistitor();
        getDeclaration().accept(visitor, null);
        return visitor.getMethods();
    }
}

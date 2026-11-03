/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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

package com.sun.webui.jsf.faces;

import com.sun.faces.annotation.Resolver;
import com.sun.webui.jsf.util.LogUtil;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.faces.component.UIComponent;

import java.util.List;

/**
 * Custom {@code ELResolver} that, when the {@code base}
 * object is a {@code UIComponent}, scans for a child with the
 * {@code id} specified by the property name.
 */
@Resolver
public final class UIComponentELResolver extends ELResolver {

    /**
     * When the base object is a {@code UIComponent}, treat the property name as
     * the {@code id} of a child component to be returned.If there is no such
     * child, return without calling {@code context.setPropertyResolved(true)}.
     *
     * @param context the ELContext
     * @param base Base object
     * @param property Property name
     * @return Object
     */
    @Override
    public Object getValue(final ELContext context, final Object base,
            final Object property) {

//        log("getValue(ctx, " + base + "," + property + ")");
        if (context == null) {
            throw new NullPointerException();
        }
        if (!(base instanceof UIComponent) || property == null) {
            log("argument is null or not of applicable type. returning");
            return null;
        }

        // Try to resolve to facet or child UIComponent
        UIComponent component = (UIComponent) base;
        String id = property.toString();

        // First check for a facet w/ that name
        UIComponent kid = component.getFacets().get(id);
        if (kid != null) {
            context.setPropertyResolved(true);
            log("returning facet " + kid);
            return kid;
        }

        // Now check for child component w/ that id
        if (component.getChildCount() < 1) {
            log("child count less than 1. returning");
            return null;
        }
        List kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            kid = (UIComponent) kids.get(i);
            if (id.equals(kid.getId())) {
                context.setPropertyResolved(true);
                log("returning child " + kid);
                return kid;
            }
        }

        // Not found
        log("can't resolve. returning");
        return null;
    }

    /**
     * When the base object is a {@code UIComponent}, treat the
     * property name as the {@code id} of a child component to be
     * replaced.  If there is no such child, return without calling
     * {@code context.setPropertyResolved(true)}.
     *
     * @param context the ELContext
     * @param base Base object
     * @param property Property name
     * @param value Replacement component
     */

    @Override
    public void setValue(final ELContext context, final Object base,
            final Object property, final Object value) {

//        log("setValue(ctx, " + base + "," + property + "," + value + ")");

        if (context == null) {
            throw new NullPointerException();
        }
        if (!(base instanceof UIComponent) || (property == null) || !(value instanceof UIComponent)) {
            log("argument is null or not of applicable type. returning");
            return;
        }

        UIComponent component = (UIComponent) base;
        String id = property.toString();
        // First check to for facet w/ this name
        if (component.getFacets().get(id) != null) {
            component.getFacets().put(id, (UIComponent) value);
            context.setPropertyResolved(true);
            log("set facet");
            return;
        }
        // Not a facet, see if it's a child
        if (component.getChildCount() < 1) {
            log("child count less than 1. returning");
            return;
        }
        List<UIComponent> kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            UIComponent kid = (UIComponent) kids.get(i);
            if (id.equals(kid.getId())) {
                kids.set(i, (UIComponent) value);
                context.setPropertyResolved(true);
                log("set child");
                return;
            }
        }

        log("can't resolve. returning");
    }

    /**
     * When the base object is a {@code UIComponent}, treat the property name as
     * the {@code id} of a child component to be retrieved.If the specified
     * child actually exists, return {@code false} (because replacement is
     * allowed). If there is no such child, , return without calling
     * {@code context.setPropertyResolved(true)}.
     *
     * @param context the ELContext
     * @param base Base object
     * @param property Property name
     * @return {@code true} if read-only, {@code false} otherwise
     */
    @Override
    public boolean isReadOnly(final ELContext context, final Object base,
            final Object property) {

//        log("isReadOnly(ctx, " + base + "," + property + ")");
        if (context == null) {
            throw new NullPointerException();
        }
        if (!(base instanceof UIComponent) || property == null) {
            log("argument is null or not of applicable type. returning");
            return false;
        }

        UIComponent component = (UIComponent) base;
        String id = property.toString();
        if (component.getFacets().get(id) != null) {
            context.setPropertyResolved(true);
            log("not read-only. found facet");
            return false;
        }
        if (component.getChildCount() < 1) {
            log("child count less than 1. returning");
            return false;
        }
        List kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            UIComponent kid = (UIComponent) kids.get(i);
            if (id.equals(kid.getId())) {
                context.setPropertyResolved(true);
                log("not read-only. found child");
                return false;
            }
        }
        log("can't resolve. returning");
        return false;

    }

    /**
     * When the base object is a {@code UIComponent}, treat the property name as
     * the {@code id} of a child component to be retrieved.If the specified
     * child actually exists, return {@code jakarta.faces.component.UIComponent}.
     * If there is no such child, return without calling
     * {@code context.setPropertyResolved(true)}.
     *
     * @param context the ELContext
     * @param base Base object
     * @param property Property name
     * @return Class
     */
    @Override
    public Class getType(final ELContext context, final Object base,
            final Object property) {

//        log("getType(ctx, " + base + "," + property + ")");
        if (context == null) {
            throw new NullPointerException();
        }
        if (!(base instanceof UIComponent) || property == null) {
            log("argument is null or not of applicable type. returning");
            return null;
        }

        UIComponent component = (UIComponent) base;
        String id = property.toString();
        if (component.getFacets().get(id) != null) {
            context.setPropertyResolved(true);
            log("found facet. returning UIComponent.class");
            return UIComponent.class;
        }
        if (component.getChildCount() < 1) {
            log("child count less than 1. returning");
            return null;
        }
        List kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            UIComponent kid = (UIComponent) kids.get(i);
            if (id.equals(kid.getId())) {
                context.setPropertyResolved(true);
                log("found child. returning UIComponent.class");
                return UIComponent.class;
            }
        }
        log("can't resolve. returning");
        return null;
    }

    /**
     * Log a message.
     * @param message message to log.
     */
    private static void log(final String message) {
        if (LogUtil.finestEnabled(UIComponentELResolver.class)) {
            LogUtil.finest(UIComponentELResolver.class, message);
        }
    }

    /**
     * When the base object is a {@code UIComponent}, return
     * {@code String.class}.If the base is {@code null} or not a
     * {@code UIComponent}, return {@code null}.
     *
     * @param context the ELContext
     * @param base Base object
     * @return Class
     */
    @Override
    public Class getCommonPropertyType(final ELContext context,
            final Object base) {

//        log("getCommonPropertyType(ctx, " + base + ")");

        if (context == null) {
            throw new NullPointerException();
        }
        if (!(base instanceof UIComponent)) {
            return null;
        }
        return String.class;
    }
}

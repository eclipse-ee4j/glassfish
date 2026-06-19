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

/*
 * NavigationHandlers.java
 *
 * Created on December 6, 2004, 11:06 PM
 */
package com.sun.jsftemplating.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import java.io.IOException;

/**
 * <p>
 * This class contains {@link com.sun.jsftemplating.layout.descriptors.handler.Handler} methods that perform navigation
 * oriented actions.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class NavigationHandlers {

    /**
     * <p>
     * Default Constructor.
     * </p>
     */
    public NavigationHandlers() {
    }

    /**
     * <p>
     * This handler returns a <code>UIViewRoot</code>. If the <code>id</code> parameter is supplied it will return the
     * requested <code>UIViewRoot</code> (this may fail and cause an exception). If the <code>id</code> is <em>not</em>
     * supplied, it will return the current <code>UIViewRoot</code>. The result will be returned in an output parameter
     * named <code>viewRoot</code>.
     * </p>
     */
    @Handler(id = "getUIViewRoot", input = { @HandlerInput(name = "id", type = String.class) }, output = {
            @HandlerOutput(name = "viewRoot", type = UIViewRoot.class) })
    public void getUIViewRoot(HandlerContext context) {
        String pageName = (String) context.getInputValue("id");
        FacesContext ctx = context.getFacesContext();
        UIViewRoot root = null;
        if (pageName == null) {
            root = ctx.getViewRoot();
        } else {
            if (pageName.charAt(0) != '/') {
                // Ensure we start w/ a '/'
                pageName = "/" + pageName;
            }
            root = ctx.getApplication().getViewHandler().createView(ctx, pageName);
        }
        context.setOutputValue("viewRoot", root);
    }

    /**
     * <p>
     * This method gives you a "resource URL" as defined by the <code>ViewHandler</code>'s <code>getActionURL(String
     *        url)</code> method.
     * </p>
     *
     * @param handlerCtx The {@link HandlerContext}.
     */
    @Handler(id = "getActionURL", input = { @HandlerInput(name = "url", type = String.class, required = true) }, output = {
            @HandlerOutput(name = "result", type = String.class) })
    public static void getActionURL(HandlerContext handlerCtx) {
        String url = (String) handlerCtx.getInputValue("url");
        FacesContext ctx = handlerCtx.getFacesContext();
        handlerCtx.setOutputValue("result", ctx.getApplication().getViewHandler().getActionURL(ctx, url));
    }

    /**
     * <p>
     * This method gives you a "resource URL" as defined by the <code>ViewHandler</code>'s <code>getResourceURL(String
     *        url)</code> method.
     * </p>
     *
     * @param handlerCtx The {@link HandlerContext}.
     */
    @Handler(id = "getResourceURL", input = { @HandlerInput(name = "url", type = String.class, required = true) }, output = {
            @HandlerOutput(name = "result", type = String.class) })
    public static void getResourceURL(HandlerContext handlerCtx) {
        String url = (String) handlerCtx.getInputValue("url");
        FacesContext ctx = handlerCtx.getFacesContext();
        handlerCtx.setOutputValue("result", ctx.getApplication().getViewHandler().getResourceURL(ctx, url));
    }

    /**
     * <p>
     * This handler navigates to the given page. <code>page</code> may either be a <code>UIViewRoot</code> or a
     * <code>String</code> representing a <code>UIViewRoot</code>. Passing in a <code>String</code> name of a
     * <code>UIViewRoot</code> will always create a new <code>UIViewRoot</code>. Passing in the <code>UIViewRoot</code>
     * provides an opportunity to customize the <code>UIComponent</code> tree that will be displayed.
     * </p>
     *
     * <p>
     * The {@link #getUIViewRoot(HandlerContext)} handler provides a way to obtain a <code>UIViewRoot</code>.
     * </p>
     *
     * <p>
     * Input value: "page" -- Type: <code>Object</code> (should be a <code>String</code> or a <code>UIViewRoot</code>).
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "navigate", input = { @HandlerInput(name = "page", type = Object.class, required = true) })
    public static void navigate(HandlerContext context) {
        Object page = context.getInputValue("page");
        UIViewRoot root = null;
        FacesContext ctx = context.getFacesContext();
        if (page instanceof String) {
            // Create a new UIViewRoot with the given id
            String strPage = (String) page;
            if (strPage.charAt(0) != '/') {
                // Ensure we start w/ a '/'
                strPage = "/" + strPage;
            }
            root = ctx.getApplication().getViewHandler().createView(ctx, strPage);
        } else if (page instanceof UIViewRoot) {
            // We recieved a UIViewRoot, use it...
            root = (UIViewRoot) page;
        } else {
            throw new IllegalArgumentException(
                    "Type '" + page.getClass().getName() + "' is not valid.  It must be a String or UIViewRoot.");
        }

        // Set the UIViewRoot so that it will be displayed
        ctx.setViewRoot(root);
    }

    /**
     * <p>
     * This handler redirects to the given page.
     * </p>
     *
     * <p>
     * Input value: "page" -- Type: <code>String</code>
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "redirect", input = { @HandlerInput(name = "page", type = String.class, required = true) })
    public static void redirect(HandlerContext context) {
        String page = (String) context.getInputValue("page");
        FacesContext ctx = context.getFacesContext();
        try {
            ctx.getExternalContext().redirect(page);
            ctx.responseComplete();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to navigate to page '" + page + "'!", ex);
        }
    }

    /**
     * <p>
     * This handler forwards to the given page. Normally you will want to do {@link #navigate} as that follows JSF patterns.
     * This uses the raw dispatcher forward mechanism (via the ExternalContext).
     * </p>
     *
     * <p>
     * Input value: "url" -- Type: <code>String</code>
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "dispatch", input = { @HandlerInput(name = "path", type = String.class, required = true) })
    public static void dispatch(HandlerContext context) {
        String path = (String) context.getInputValue("path");
        FacesContext ctx = context.getFacesContext();
        try {
            ctx.getExternalContext().dispatch(path);
            ctx.responseComplete();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to navigate to path '" + path + "'!", ex);
        }
    }
}

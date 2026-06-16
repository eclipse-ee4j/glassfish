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

package com.sun.jsftemplating.layout.descriptors;

import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.event.AfterLoopEvent;
import com.sun.jsftemplating.layout.event.BeforeLoopEvent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class defines a LayoutForEach {@link LayoutElement}. The LayoutForEach provides the functionality necessary to
 * iteratively display a portion of the layout tree. The list property contains the <code>List</code> of items to
 * iterate over.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutForEach extends LayoutComponent {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param parent The parent {@link LayoutElement}
     * @param listBinding The <code>List</code> to iterate over
     * @param key The <code>ServletRequest</code> attribute key used to store the object being processed
     */
    public LayoutForEach(LayoutElement parent, String listBinding, String key) {
        super(parent, null, LayoutDefinitionManager.getGlobalComponentType(null, "foreach"));
        if (listBinding == null || listBinding.equals("")) {
            throw new IllegalArgumentException("'listBinding' is required!");
        }
        if (key == null || key.equals("")) {
            throw new IllegalArgumentException("'key' is required!");
        }
        addOption("list", listBinding);
        addOption("key", key);
        if (listBinding.equals("$property{list}")) {
            _doubleEval = true;
        }
    }

    /**
     * <p>
     * This method always returns true. The condition is based on an <code>Iterator.hasNext()</code> call instead of here
     * because the {@link #encode(FacesContext, UIComponent)} method evaluates this and then calls the super. Performing the
     * check here would cause the condition to be evaluated twice.
     * </p>
     *
     * @param context The <code>FacesContext</code>.
     * @param component The <code>UIComponent</code>.
     *
     * @return true
     */
    @Override
    public boolean encodeThis(FacesContext context, UIComponent component) {
        return true;
    }

    /**
     * <p>
     * This method evaluates the list binding for this <code>LayoutForEach</code>. This is expected to evaulate to a
     * <code>List</code> object. If it doesn't, this method will throw a <code>NullPointerException</code> (if it evaulates
     * to <code>null</code>), or an <code>IllegalArgumentException</code> if it doesn't evaluate to a <code>List</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     *
     * return The <code>List</code> of objects to iterate over
     */
    protected List<Object> getList(FacesContext context, UIComponent comp) {
        Object value = resolveValue(context, comp, getOption("list"));
        if (_doubleEval) {
// FIXME: Generalize double evaluation... all $property{} calls from inside a component??
            value = resolveValue(context, comp, value);
        }

        // Make sure we found something...
        if (value == null) {
            throw new NullPointerException("List not found via expression: '" + getOption("list") + "'.");
        }

        // Make sure we have a List...
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("Expression '" + getOption("list") + "' did not resolve to a List! Found: '" + value.getClass().getName() + "'");
        }

        // Return the List
        return (List<Object>) value;
    }

    /**
     * <p>
     * This method sets the <code>Object</code> that is currently being processed by this <code>LayoutForEach</code>. This
     * implementation stores this value in the request attribute map undert the key provided to this
     * <code>LayoutForEach</code>.
     * </p>
     *
     * <p>
     * As an added convenience, this method will also set an attribute that contains the current index number. The attribute
     * key will be the same key the <code>Object</code> is stored under plus "-index". The index is stored as a String.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param value The <code>Object</code> to store
     * @param index The current index number of the <code>Object</code>
     */
    private void setCurrentForEachValue(FacesContext context, Object value, int index, String key) {
        Map<String, Object> map = context.getExternalContext().getRequestMap();
        map.put(key, value);
        map.put(key + "-index", "" + index);
    }

    /**
     * <p>
     * This implementation overrides the parent <code>encode</code> method. It does this to cause the encode process to loop
     * as long as there are more <code>List</code> entries to process.
     * </p>
     *
     * @param context The FacesContext
     * @param component The UIComponent
     */
    @Override
    public void encode(FacesContext context, UIComponent component) throws IOException {
        // Before events..
        dispatchHandlers(context, BEFORE_LOOP, new BeforeLoopEvent(component));

        String key = resolveValue(context, component, getOption("key")).toString();

        // Get the List
        List<Object> list = getList(context, component);

        // Save the list size in case it is needed.
        context.getExternalContext().getRequestMap().put(key + "-size", list.size());

        // Iterate over the values in the list and perform the requested
        // action(s) per the body of the LayoutForEach
        Iterator<Object> it = list.iterator();
        for (int index = 1; it.hasNext(); index++) {
            setCurrentForEachValue(context, it.next(), index, key);
            super.encode(context, component);
        }

        // Invoke any "after" handlers
        dispatchHandlers(context, AFTER_LOOP, new AfterLoopEvent(component));
    }

    /**
     * <p>
     * This method retrieves the Handlers for the requested type. But does *NOT* includes any handlers that are associated
     * with the instance (i.e. the UIComponent). This is desired behavior when this is *not* a component. I am not sure if
     * this is correct if we support a foreach() component. FIXME: think about this.
     * </p>
     */
    @Override
    public List<Handler> getHandlers(String type, UIComponent comp) {
        return super.getHandlers(type, null);
    }

    /**
     * <p>
     * This is the event "type" for {@link com.sun.jsftemplating.layout.descriptors.handler.Handler} elements to be invoked
     * after this LayoutForEach is processed (outside loop).
     * </p>
     */
    public static final String AFTER_LOOP = "afterLoop";

    /**
     * <p>
     * This is the event "type" for {@link com.sun.jsftemplating.layout.descriptors.handler.Handler} elements to be invoked
     * before this LayoutForEach is processed (outside loop).
     * </p>
     */
    public static final String BEFORE_LOOP = "beforeLoop";

    /**
     * <p>
     * This flag is set to true when the condition equals "$property{condition}". This is a special case where the value to
     * be evaluated is not $property{condition}, but rather the value of this expression. This requires double evaluation to
     * correct interpret the expression. For now this is a hack for this case only. In the future we may want to support an
     * $eval{} or something more general syntax for doing this declaratively.
     * </p>
     *
     * See LayoutIf also.
     */
    private boolean _doubleEval = false;
}

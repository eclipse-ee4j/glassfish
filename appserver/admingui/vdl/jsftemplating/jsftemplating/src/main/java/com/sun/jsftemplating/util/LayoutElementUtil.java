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

package com.sun.jsftemplating.util;

import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.layout.descriptors.LayoutFacet;

import jakarta.faces.context.FacesContext;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * This class is a utility class for misc {@link LayoutElement} related tasks.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutElementUtil {

    /**
     * <p>
     * This method determines if the given {@link LayoutElement} is inside a {@link LayoutComponent}. It will look at all
     * the parents, if any of them are {@link LayoutComponent} instances, this method will return <code>true</code>.
     * </p>
     *
     * @param elt The {@link LayoutElement} to check.
     *
     * @return true If a {@link LayoutComponent} exists as an ancestor.
     */
    public static boolean isNestedLayoutComponent(LayoutElement elt) {
        return getParentLayoutComponent(elt) != null;
    }

    /**
     * <p>
     * This method returns the nearest {@link LayoutComponent} that contains the given {@link LayoutElement}, null if none.
     * </p>
     *
     * @param elt The {@link LayoutElement} to start with.
     *
     * @return The containing {@link LayoutComponent} if one exists.
     */
    public static LayoutComponent getParentLayoutComponent(LayoutElement elt) {
        elt = elt.getParent();
        while (elt != null) {
            if (elt instanceof LayoutComponent) {
                return (LayoutComponent) elt;
            }
            elt = elt.getParent();
        }
        return null;
    }

    /**
     * <p>
     * This method returns true if any of the parent {@link LayoutElement}s are {@link LayoutComponent}s. If a
     * {@link LayoutFacet} is encountered first, false is automatically returned. This method is specific to processing
     * needed for creating a {@link LayoutComponent}. Do not use this for general cases when you need to find if a
     * {@link LayoutElement} is embedded within a {@link LayoutComponent} (which should ignore {@link LayoutFacet}
     * elements).
     * </p>
     *
     * @param elt The {@link LayoutElement} to check.
     *
     * @return true if it has a {@link LayoutComponent} ancestor.
     */
    public static boolean isLayoutComponentChild(LayoutElement elt) {
        elt = elt.getParent();
        while (elt != null) {
            if (elt instanceof LayoutComponent) {
                // Make sure we are in a LayoutComponent and not an "if",
                // "while", or something like that
                if (elt.getClass().getName().equals(LayoutComponent.CLASS_NAME)) {
                    // This is a real LayoutComponent, return true
                    return true;
                }

                // This isn't a LC, however, we need to count it as such if
                // there is any LC parent (even if we encounter a LayoutFacet
                // first)
                return isNestedLayoutComponent(elt);
            } else if (elt instanceof LayoutFacet) {
                // Don't consider it a child if it is a facet
                return false;
            }
            elt = elt.getParent();
        }

        // Not found
        return false;
    }

    /**
     * <p>
     * This method produces a generated ID. It optionally uses the given base as a prefix to the generated ID
     * ({@link #DEFAULT_ID_BASE} is used otherwise). Do not depend on this implementation, it may change in the future.
     * </p>
     *
     * <p>
     * Since this implementation increments the number each call, it may not produce reproducible results. You should pass
     * in your own number to use, see {@link #getGeneratedId(String, int)}. JSFT makes an effort to cause generated ids to
     * be reproducible accross requests, but it is not guarenteed (particularly in highly dynamic pages in development
     * mode).
     * </p>
     */
    public static String getGeneratedId(String base) {
        return getGeneratedId(base, incHighestId(_highId));
    }

    /**
     * <p>
     * This method produces a generated ID. It optionally uses the given base as a prefix to the generated ID
     * ({@link #DEFAULT_ID_BASE} is used otherwise). This implementation will generate an id that contains the given number.
     * Do not depend result of this this implementation, it may change in the future.
     * </p>
     *
     * <p>
     * This method replaces illegal characters (all non alpha characters) with an '_'.
     * </p>
     *
     * @param base Prefix to use in the id.
     * @param num Number to use in the id.
     */
    public static String getGeneratedId(String base, int num) {
        if (base == null) {
            base = DEFAULT_ID_BASE;
        } else {
            base = base.trim();
            if (base.equals("")) {
                base = DEFAULT_ID_BASE;
            } else {
                StringBuffer buf = new StringBuffer();
                int lowch;
                for (int ch : base.toCharArray()) {
                    lowch = ch | 0x20;
                    if (lowch >= 'a' && lowch <= 'z') {
                        buf.append((char) ch);
                    } else {
                        buf.append('_');
                    }
                }
                base = buf.toString();
            }
        }
        return base + num;
    }

    /**
     * <p>
     * This method returns the next id that has not been used.
     * </p>
     */
    public static int getStartingIdNumber(FacesContext ctx, String key) {
        if (ctx == null) {
            // Make sure we have the FacesContext...
            ctx = FacesContext.getCurrentInstance();
        }
        Map<String, Integer> startMap = null;
        if (ctx != null) {
            // Check Application Scope for the Map...
            startMap = (Map<String, Integer>) ctx.getExternalContext().getApplicationMap().get(ID_IDX_MAP);
        }
        if (startMap == null) {
            // Not found, 1st time... create / initialize it
            startMap = new ConcurrentHashMap<>(50, 0.75f, 3);
            if (ctx != null) {
                // Save for later
                ctx.getExternalContext().getApplicationMap().put(ID_IDX_MAP, startMap);
            }
        }

        // Now look for the desired start key...
        Integer start = startMap.get(key);
        if (start == null) {
            // Save for later
// FIXME: Why do I need _highId?  Isn't key+0 good enough?
            start = incHighestId(_highId);
            startMap.put(key, start);
        }

        // Return the answer...
        return start;
    }

    /**
     * <p>
     * This method ensures the highest id is higher than the given int.
     * </p>
     */
    public static synchronized int incHighestId(int num) {
        if (num >= _highId) {
            _highId = num + 1;
        }
        return _highId;
    }

    /**
     * <p>
     * This method checks to see if the given <code>component</code> is sitting inside a facet or not. If it is, it will use
     * the facet name for its id so that it will be found correctly. However, if the facet tag exists outside a component,
     * then it is not a facet -- its a place holder for a facet. In this case it will not use the id of the place holder.
     * </p>
     */
    public static void checkForFacetChild(LayoutElement parent, LayoutComponent component) {
        // Figure out if this should be stored as a facet, if so under what id
        if (!LayoutElementUtil.isLayoutComponentChild(component)) {
            // Need to add this so that it has the correct facet name
            // Check to see if this LayoutComponent is inside a LayoutFacet
            String id = component.getUnevaluatedId();
            while (parent != null) {
                if (parent instanceof LayoutFacet) {
                    // Inside a LayoutFacet, use its id... only if this facet
                    // is a child of a LayoutComponent (otherwise, it is a
                    // layout facet used for layout, not for defining a facet
                    // of a UIComponent)
                    if (LayoutElementUtil.isLayoutComponentChild(parent)) {
                        id = parent.getUnevaluatedId();
                    }
                    break;
                }
                parent = parent.getParent();
            }

            // Set the facet name
            component.addOption(LayoutComponent.FACET_NAME, id);
        }
    }

    /**
     * <p>
     * This method recurses through the {@link LayoutElement} tree to generate a String representation of its structure.
     * </p>
     */
    public static void dumpTree(LayoutElement elt, StringBuffer buf, String indent) {
        // First add the current LayoutElement
        String compInfo = "";
        if (elt instanceof LayoutComponent) {
            LayoutComponent comp = (LayoutComponent) elt;
            compInfo = " nested=" + comp.isNested();
        } else if (elt instanceof LayoutFacet) {
            compInfo = " isRendered=" + ((LayoutFacet) elt).isRendered();
        }
        buf.append(indent + elt.getUnevaluatedId() + " (" + elt.getClass().getName() + ")" + compInfo + "\n");

        // Children...
        Iterator<LayoutElement> it = elt.getChildLayoutElements().iterator();
        if (it.hasNext()) {
            while (it.hasNext()) {
                dumpTree(it.next(), buf, indent + "    ");
            }
        }
    }

    /**
     * <p>
     * Application scope key for the id index <code>Map</code>.
     * </p>
     */
    private static final String ID_IDX_MAP = "__jsft_ID_Index_Map";

    // May need to make this application-scoped...
    private static int _highId = 0;

    public static final String DEFAULT_ID_BASE = "id";
}

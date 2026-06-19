/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2011 Ken Paulsen
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

package com.sun.jsft.component;

import com.sun.jsft.tasks.TaskManager;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
public class FragmentRenderer extends UIComponentBase implements ComponentSystemEventListener {

    /**
     * <p>
     * Default constructor.
     * </p>
     */
    public FragmentRenderer() {
    }

    /**
     *
     */
    @Override
    public String getFamily() {
        return FAMILY;
    }

    /**
     *
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        System.out.println("Starting FragmentRenderer...");
        // Start processing the Tasks...
        TaskManager.getInstance().start();
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        // It should have no children... do nothing...
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        // Render fragments as they become ready.
        fragsToRender = getFragmentCount();
        DeferredFragment comp = null;
        while (fragsToRender > 0) {
            synchronized (renderQueue) {
                if (renderQueue.isEmpty()) {
                    try {
                        // Wait at most 30 seconds...
                        renderQueue.wait(30 * 1000);
                        if (renderQueue.isEmpty()) {
                            System.out.println("EMPTY QUEUE!");
                            return;
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("Interrupted!");
                        return;
                    }
                }
                comp = renderQueue.poll();
            }
            if (comp != null) {
                fragsToRender--;
                try {
                    System.out.println("Encoding: " + comp.getId());
                    comp.encodeAll(FacesContext.getCurrentInstance());
                } catch (Exception ex) {
                    // FIXME: cleanup
                    ex.printStackTrace();
                }
            }
        }

        System.out.println("Ending FragmentRenderer..." + fragsToRender);
    }

    /**
     * <p>
     * This method returns the number of tasks this DeferredFragment is waiting for.
     * </p>
     */
    public int getFragmentCount() {
        return fragments.size();
    }

    /**
     * <p>
     * This method adds the given {@link DeferredFragment} to the <code>List</code> of fragments that are to be processed by
     * this <code>FragmentRenderer</code>.
     * </p>
     */
    public void addDeferredFragment(DeferredFragment fragment) {
        fragments.add(fragment);
    }

    /**
     * <p>
     * This method gets invoked whenever a DeferredFragment associated with this component becomes ready to be rendered.
     * </p>
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        // Get the component
        processDeferredFragment((DeferredFragment) event.getComponent());
    }

    /**
     *
     */
    private void processDeferredFragment(DeferredFragment comp) {
        // Find the "place-holder" component...
        String key = ":" + comp.getPlaceHolderId();
        UIComponent placeHolder = comp.findComponent(key);
        if (placeHolder != null) {
            // This "should" always be the case... swap it back.
            List<UIComponent> peers = placeHolder.getParent().getChildren();
            int index = peers.indexOf(placeHolder);
            peers.set(index, comp);
        }

        // Queue it up...
        synchronized (renderQueue) {
            renderQueue.add(comp);
            renderQueue.notifyAll();
        }
    }

    /**
     * <p>
     * A count of remaining fragments to render.
     * </p>
     */
    private transient int fragsToRender = 1;

    private transient List<DeferredFragment> fragments = new ArrayList<>();

    private transient Queue<DeferredFragment> renderQueue = new ConcurrentLinkedQueue<>();

    /**
     * <p>
     * The component family.
     * </p>
     */
    public static final String FAMILY = FragmentRenderer.class.getName();
}

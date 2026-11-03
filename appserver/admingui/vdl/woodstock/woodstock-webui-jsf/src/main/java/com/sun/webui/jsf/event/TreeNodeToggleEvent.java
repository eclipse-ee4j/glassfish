/*
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
package com.sun.webui.jsf.event;

import com.sun.webui.jsf.component.TreeNode;

import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.FacesListener;

/**
 * This is an event instance that queued by a TreeNode object's associated
 * toggle listener. Queuing this even causes TreeNode's broadcast method to fire
 * during which time actionListenerExpressions, if associated with the TreeNode,
 * can be handled appropriately. The default phaseId of "ANY_PHASE" is being
 * used.
 */
public final class TreeNodeToggleEvent extends ActionEvent {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 6421867968165307677L;

    /**
     * Create a new instance.
     *
     * @param newNode tree node
     */
    public TreeNodeToggleEvent(final TreeNode newNode) {
        super(newNode);
    }

    @Override
    public boolean isAppropriateListener(final FacesListener listener) {
        return listener instanceof ToggleActionListener;
    }

    @Override
    public void processListener(final FacesListener listener) {
        // This should be a no op as all we want to do
        // is invoke the actionlistenerExpressions associated
        // with the node.
    }
}

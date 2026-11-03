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

import com.sun.webui.jsf.component.Tree;
import com.sun.webui.jsf.component.TreeNode;

import jakarta.faces.component.UIComponent;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

import java.io.Serializable;

/**
 * Toggle action listener.
 */
public final class ToggleActionListener
        implements ActionListener, Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -6635913171312578091L;

    @Override
    public void processAction(final ActionEvent event) {

        UIComponent comp = event.getComponent();
        boolean flag = false;
        if (!comp.getId().endsWith("turner")) {
            flag = true;
        }
        while (comp != null && !(comp instanceof TreeNode)) {
            comp = comp.getParent();
        }

        if (comp != null) {
            TreeNode node = (TreeNode) comp;
            Tree root = TreeNode.getAbsoluteRoot(comp);
            if (flag) {
                root.setSelected(node.getId());
            }
            // Queue the TreeNodeToggleEvent. This will
            // enable control to flow thru tree nodes's
            // broadcast method where it can be checked
            // if the tree node has an actionListenerExpression
            // associated with it and if so invoke it.
            // This also gives the correct impression of the
            // actionEvent being fired from the treeNode as
            // opposed to the hyperlink representing the
            // toggle icon.
            node.queueEvent(new TreeNodeToggleEvent(node));
        }
    }
}

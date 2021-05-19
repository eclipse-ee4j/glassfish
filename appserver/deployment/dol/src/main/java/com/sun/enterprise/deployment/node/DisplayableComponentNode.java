/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node;


import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;


/**
 * This node class is responsible for handling all the information
 * related to displayable elements like display-name or icons.
 *
 * @author  Jerome Dochez
 * @version
 */
public abstract class DisplayableComponentNode<T extends Descriptor> extends DeploymentDescriptorNode<T> {

    public DisplayableComponentNode() {
        super();
        registerElementHandler(new XMLElement(TagNames.NAME), LocalizedInfoNode.class);
        registerElementHandler(new XMLElement(TagNames.ICON), IconNode.class);
        registerElementHandler(new XMLElement(TagNames.SMALL_ICON), IconNode.class);
        registerElementHandler(new XMLElement(TagNames.LARGE_ICON), IconNode.class);
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, T descriptor) {
        Node node = super.writeDescriptor(parent, descriptor);

        // description, display-name, icons...
        writeDisplayableComponentInfo(node, descriptor);
        return node;
    }

    /**
     * write the localized descriptions, display-names and icons info
     *
     * @param the node to write the info to
     * @param the descriptor containing the displayable information
     */
    public void writeDisplayableComponentInfo(Node node, T descriptor) {
        LocalizedNode localizedNode = new LocalizedNode();
        localizedNode.writeLocalizedMap(node, TagNames.DESCRIPTION, descriptor.getLocalizedDescriptions());
        localizedNode.writeLocalizedMap(node, TagNames.NAME, descriptor.getLocalizedDisplayNames());
        IconNode iconNode = new IconNode();
        iconNode.writeLocalizedInfo(node, descriptor);

    }
}

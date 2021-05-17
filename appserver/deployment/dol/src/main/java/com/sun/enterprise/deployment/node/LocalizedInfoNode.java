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

/*
 * DescriptionNode.java
 *
 * Created on August 16, 2002, 2:46 PM
 */

package com.sun.enterprise.deployment.node;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.xml.TagNames;

/**
 * This node handles the descriptionType xml fragment
 *
 * @author Jerome Dochez
 */
public class LocalizedInfoNode extends LocalizedNode {

    /**
     * we do not create descriptors in this node
     */
     public Object getDescriptor() {
     return null;
     }

    /**
     * notification of the end of XML parsing for this node
     */
    public void postParsing() {
        Object o = getParentNode().getDescriptor();
        if (o instanceof Descriptor) {
            Descriptor descriptor = (Descriptor) o;
            if (getXMLRootTag().getQName().equals(TagNames.DESCRIPTION)) {
                descriptor.setLocalizedDescription(lang, localizedValue);
            } else
            if (getXMLRootTag().getQName().equals(TagNames.NAME)) {
                descriptor.setLocalizedDisplayName(lang, localizedValue);
            }
        }
    }
}

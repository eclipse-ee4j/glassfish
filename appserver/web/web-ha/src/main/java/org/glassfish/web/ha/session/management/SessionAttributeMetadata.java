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

package org.glassfish.web.ha.session.management;

import java.io.Serializable;

/**
 * AttributeMetadata contains the metadata of an attribute that is part of an
 * Http Session. When a container decides to save a session it passes an
 * instance of CompositeMetaData which contains a collection of
 * AttributeMetadata.
 *
 * <p>
 * The attribute in question could have been deleted, or modified or could be a
 * new attribute inside the HttpSession. getOperation() tells exactly what
 * operation needs to be performed for this attribute
 *
 * <p>
 * The attribute state/data itself can be obtained with getState(). Since an
 * attribute is part of a session, the attributes must be deleted when the
 * session is removed. The CompositeMetadata contains the last access time and
 * inactive timeout for the session.
 *
 * @see CompositeMetadata
 */
public final class SessionAttributeMetadata implements Serializable {

    private String attributeName;

    private Operation opcode;

    private byte[] data;

    /**
     * Operation to be performed on this attribute
     */
    public enum Operation {
        ADD, DELETE, UPDATE
    };

    /**
     * Construct an AtributeMetadata
     *
     * @param attributeName
     *            the attribute name
     * @param opcode
     *            The operation to be performed on the AttrbuteMetadata
     * @param data
     *            The attribute data
     */
    public SessionAttributeMetadata(String attributeName, Operation opcode, byte[] data) {
        this.attributeName = attributeName;
        this.opcode = opcode;
        this.data = data;
    }

    /**
     * Returns name of the attribute
     *
     * @return attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Get the operation to be performed on the attribute.
     *
     * @return the operation to be performed on this attribute
     */
    public Operation getOperation() {
        return opcode;
    }

    /**
     * Get the attribute data
     *
     * @return the data
     */
    public byte[] getState() {
        return data;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && obj instanceof SessionAttributeMetadata) {
            SessionAttributeMetadata otherAttributeMetadata = (SessionAttributeMetadata) obj;
            return (getAttributeName() == null)
                ? otherAttributeMetadata.getAttributeName() == null
                : getAttributeName().equals(otherAttributeMetadata.getAttributeName());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 31 + (attributeName == null ? 0 : attributeName.hashCode());
    }

}

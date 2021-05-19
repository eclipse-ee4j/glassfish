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

package org.glassfish.resources.naming;

import javax.naming.RefAddr;

/**
 *
 * @author Qingqing Ouyang
 *
 * @see RefAddr
 * @see javax.naming.StringRefAddr
 * @since 1.3
 */
public class SerializableObjectRefAddr extends RefAddr {

    /**
     * Contains the contents of this address.
     * Can be null.
     *
     * @serial
     */
    private Object contents;

    /**
     * Constructs a new instance of SerializableObjectRefAddr
     * using its address type and contents.
     *
     * @param addrType A non-null string describing the type of the address.
     * @param contents The possibly null contents of the address in the
     *            form of a string.
     */
    public SerializableObjectRefAddr(String addrType, Object contents) {
        super(addrType);
        this.contents = contents;
    }


    /**
     * Retrieves the contents of this address. The result is a string.
     *
     * @return The possibly null address contents.
     */
    @Override
    public Object getContent() {
        return contents;
    }
}

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

package com.sun.ejb.spi.sfsb.util;

import java.io.Serializable;

/**
 *
 * An instance of this class will be used by the Stateful SessionBean
 *  Container to create sessionIDs whenever home.create(..) is called
 *
 * @author Mahesh Kannan
 */
public interface SFSBUUIDUtil<T> {

   /**
    * Create and return the sessionKey.
    * @return the sessionKey object
    */
    public T createSessionKey();

   /**
    * Called from the Container before publishing an IOR. The method must convert the sessionKey into a byte[]
    * @return A byte[] representation of the key. The byte[] could be created using serialization.
    */
    public byte[] keyToByteArray(T sessionKey);

    /**
     * Return the sessionKey that represents the sessionKey. This has to be super efficient as the container
     *    calls this method on every invocation. Two objects obtained from identical byte[] must
     *    satisfy both o1.equals(o2) and o1.hashCode() == o2.hashCode()
     * @return the sessionKey object
     */
     public T byteArrayToKey(byte[] array, int startIndex, int len);

}

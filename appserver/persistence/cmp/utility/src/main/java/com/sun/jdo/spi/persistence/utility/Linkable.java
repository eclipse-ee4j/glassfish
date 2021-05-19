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
 * ConnectionImpl.java
 *
 * Create on March 3, 2000
 */

package com.sun.jdo.spi.persistence.utility;


/**
 * This file defines a linkable interface.  Any object that needs
 * to be a member of com.forte.util.DoubleLinkedList should implement
 * this interface.
 */
public interface Linkable
{
    public Linkable getNext();
    public Linkable getPrevious();
    public void setNext(Linkable node);
    public void setPrevious(Linkable node);
}


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
 * MappingMemberElement.java
 *
 * Created on May 23, 2000, 12:00 AM
 */

package com.sun.jdo.api.persistence.model.mapping;

/**
 *
 * @author raccah
 * @version %I%
 */
public interface MappingMemberElement extends MappingElement
{
    /** Get the declaring class.
     * @return the class that owns this member element, or <code>null</code>
     * if the element is not attached to any class
     */
    public MappingClassElement getDeclaringClass ();
}


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

package org.glassfish.ejb.deployment.descriptor;

import java.util.Iterator;

/**
 * This interface is to define the functionality of a query Parser.
 */
public interface QueryParser
{
    /**
     * This method returns an iterator over the parameter types of the
     * specified parameter declaration. The types are represented by their name, thus
     * the Iterator's next method returns Strings.
     * @param parameterDeclarations parameter declaration string.
     * @returns an iterator over parameter types.
     */
    Iterator parameterTypeIterator(String parameterDeclarations);
}

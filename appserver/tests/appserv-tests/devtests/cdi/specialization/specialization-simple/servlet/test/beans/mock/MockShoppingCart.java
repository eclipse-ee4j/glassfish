/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.beans.mock;

import jakarta.enterprise.inject.Specializes;

import test.beans.artifacts.MockStereotype;
import test.beans.artifacts.Transactional;
import test.beans.nonmock.ShoppingCart;


@MockStereotype
@Specializes
@Transactional(requiresNew=true)
//This alternative (via MockStereotype) does not specify Preferred
//qualifier, but since it specializes ShoppingCart, it
//gets ShoppingCart's qualifers etc
//Interceptors are not inherited and so we need to specify Transactional
//explicitly
public class MockShoppingCart  extends ShoppingCart{
    public static boolean mockShoppingCartInvoked = false;

    public void addItem(String s) {
        mockShoppingCartInvoked = true;
        System.out.println("MockShoppingCart::addItem called");

    }

}

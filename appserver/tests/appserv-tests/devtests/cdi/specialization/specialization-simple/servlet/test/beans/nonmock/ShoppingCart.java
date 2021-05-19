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

package test.beans.nonmock;

import test.beans.artifacts.Preferred;
import test.beans.artifacts.Transactional;


@Preferred //This qualifier would be inherited by a @Specializes sub-class.
@Transactional(requiresNew=true)
public class ShoppingCart  {
    public static boolean shoppingCartInvoked = false;

    public void addItem(String s) {
        shoppingCartInvoked = true;
        System.out.println("ShoppingCart::addItem called");

    }

}

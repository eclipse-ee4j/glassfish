/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package org.example;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;

@Component()
public class Component16 extends Component15 {

    @Property(isHidden = false, isAttribute = true, displayName = "The First", shortDescription = "The First")
    private String one;

    @Property(isHidden = false, isAttribute = true, displayName = "The Second", shortDescription = "The Second")
    @Override
    public String getTwo() {
        return super.getTwo();
    }

    @Property(isHidden = false, isAttribute = true, displayName = "The Third", shortDescription = "The Third")
    @Override
    public void setThree(String three) {
        super.setThree(three);
    }

}

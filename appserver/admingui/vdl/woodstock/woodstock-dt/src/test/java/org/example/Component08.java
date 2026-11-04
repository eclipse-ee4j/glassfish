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

@Component()
public class Component08 implements Interface01 {

    private String one;

    @Override
    public String getOne() {
        return this.one;
    }

    @Override
    public void setOne(String one) {
        this.one = one;
    }

    private String two;

    @Override
    public String getTwo() {
        return this.two;
    }

    @Override
    public void setTwo(String two) {
        this.two = two;
    }

    private String three;

    @Override
    public String getThree() {
        return this.three;
    }

    @Override
    public void setThree(String three) {
        this.three = three;
    }
}

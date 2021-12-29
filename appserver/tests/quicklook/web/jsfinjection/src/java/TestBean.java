/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package jsfinjection;

import jakarta.enterprise.context.RequestScoped;
import jakarta.annotation.*;
import javax.sql.DataSource;
import jakarta.inject.Named;

@RequestScoped
@Named
public class TestBean {

    @Resource(name = "entry")
    private String entry;

    @Resource(name = "jdbc/__default")
    private DataSource ds;
    
    private boolean initCalled;
    
    @PostConstruct
    void init() {
        initCalled = true;
    }

    public String getEntry() {
        return entry;
    }

    public int getNumber() {
        int number = -3000;
        if (ds != null) {
            try {
                number = ds.getLoginTimeout();
            } catch (Exception ex) {
                ex.printStackTrace();
                number = -1000;
            }
        }
        
        return number;
    }

    public boolean getInit() {
        return initCalled;
    }

}

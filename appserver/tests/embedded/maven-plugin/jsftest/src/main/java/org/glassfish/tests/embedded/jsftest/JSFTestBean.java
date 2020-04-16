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

package org.glassfish.tests.embedded.jsftest;

import jakarta.faces.bean.ManagedBean;

/**
 * @author bhavanishankar@java.net
 */

@ManagedBean(name = "testbean")
public class JSFTestBean {

    public TestTable[] getTestTable() {
        return testTable;
    }

    private TestTable[] testTable = new TestTable[]{
            new TestTable("BHAVANI", "+91999000000", "INDIA"),
            new TestTable("SHANKAR", "+199999999999", "USA"),
            new TestTable("Mr. X", "+122222222", "SFO"),
    };

    
    public class TestTable {
        String name;
        String number;
        String country;

        public TestTable(String name, String phone, String country) {
            this.name = name;
            this.number = phone;
            this.country = country;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        public String getCountry() {
            return country;
        }
    }

}

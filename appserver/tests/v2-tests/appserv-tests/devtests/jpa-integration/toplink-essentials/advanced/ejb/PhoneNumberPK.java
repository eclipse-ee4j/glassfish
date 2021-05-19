/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package oracle.toplink.essentials.testing.models.cmp3.advanced;

public class PhoneNumberPK  {
    public Integer id;
    public String type;

    public PhoneNumberPK() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * equals: Answer true if the ids are equal
     */
    public boolean equals(Object anotherPhoneNumber) {
        if (anotherPhoneNumber.getClass() != PhoneNumberPK.class) {
            return false;
        }
        return (getId().equals(((PhoneNumberPK)anotherPhoneNumber).getId()));
    }
}

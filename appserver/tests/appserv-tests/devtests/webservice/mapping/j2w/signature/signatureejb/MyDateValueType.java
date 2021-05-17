/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package signatureejb;

import java.util.Date;

public class MyDateValueType {
    protected java.util.Date date;
    protected String whine;
    protected MySecondDateValueType[] dates;

    public MyDateValueType() {}

    public MyDateValueType(Date date, java.lang.String whine,
                            MySecondDateValueType[] dates) {
        this.date = date;
        this.whine = whine;
        this.dates = dates;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public java.lang.String getWhine() {
        return whine;
    }

    public void setWhine(java.lang.String whine) {
        this.whine = whine;
    }

    public MySecondDateValueType[] getMySecondDateValueTypes() {
        return dates;
    }

    public void setMySecondDateValueTypes(MySecondDateValueType[] dates) {
        this.dates = dates;
    }
}

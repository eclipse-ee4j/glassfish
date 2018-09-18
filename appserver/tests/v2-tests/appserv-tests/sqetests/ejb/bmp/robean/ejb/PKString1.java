/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.bmp.robean.ejb;

public class PKString1 implements java.io.Serializable {
    public String pkString1 = null;

    public PKString1() {
    }

    public PKString1(String pkString1) {
        this.pkString1 = pkString1;
    }

    public String getPK() {
        return pkString1;
    }

    public int hashCode() {
        return pkString1.hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof PKString1) {
            return ((PKString1) other).pkString1.equals(pkString1);
        }
        return false;
    }
}

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

package dataregistry;


public final class VendorKey implements java.io.Serializable {

    public int vendorId;

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object otherOb) {

        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof VendorKey)) {
            return false;
        }
        VendorKey other = (VendorKey) otherOb;
        return (vendorId == other.vendorId);
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        return vendorId;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "" + vendorId;
    }
}

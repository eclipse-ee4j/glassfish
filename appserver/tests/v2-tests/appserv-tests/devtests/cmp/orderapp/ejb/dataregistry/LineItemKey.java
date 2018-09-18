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


public final class LineItemKey implements java.io.Serializable {

    public Integer orderId;
    public int itemId;

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object otherOb) {

        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof LineItemKey)) {
            return false;
        }
        LineItemKey other = (LineItemKey) otherOb;
        return (

        (orderId==null?other.orderId==null:orderId.equals(other.orderId))
        &&
        (itemId == other.itemId)

        );
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        return (

        (orderId==null?0:orderId.hashCode())
        ^
        ((int) itemId)

        );
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "" + orderId + "-" + itemId;
    }

}

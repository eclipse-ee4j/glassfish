/*
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

/*
 * SCO.java
 *
 * created May 9, 2000
 *
 * @author Marina Vatkina
 * @version 1.0
 */

package com.sun.jdo.spi.persistence.support.sqlstore;

public interface SCO
{
    /**
         * Returns the field name
     *
         * @return field name as java.lang.String
         */
    String getFieldName();

    /**
         * Returns the owner object of the SCO instance
     *
         * @return owner object
         */
    Object getOwner();

    /**
         * Nullifies references to the owner Object and Field
         */
        void unsetOwner();

    /**
         * Apply changes (can be a no-op)
     */
    void applyUpdates(StateManager sm, boolean modified);

    /**
         * Marks object dirty
     *
     * @return StateManager associated with the owner
     */
    StateManager makeDirty();

        /**
         * Creates clone with the same owner and field value.
         * Used for the beforeImage.
         */
        Object cloneInternal();
}

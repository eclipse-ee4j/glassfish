/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.api.common;


import java.util.Set;

/**
 * The Attribute interface defines an interface for interacting with individual Attributes.  It is a read-only interface.
 * <br>
 * Note that, because the collection used to hold attribute values is a Set, the attribute can be multi-valued.  Each value
 * must be distinct, however -- it is not a bag that can contain multiple instances of the same value.
 */
public interface Attribute {

    /**
     * Get the name of this attribute.
     *
     * @return The name.
     */
    String getName();

    /**
     * Get a count of the number of values this attribute has (0-n).
     *
     * @return The value count.
     */
    int getValueCount();

    /**
     * Get the first value from the Set of attribute values, or null if the attribute has no values.
     * This is a shorthand method that should be useful for single-valued attributes, but note that
     * there are no guarantees about which value is returned in the case that there are multiple values.
     * The value returned will be whichever value the underlying Set implementation returns first.
     *
     * @return The attribute value.
     */
    String getValue();

    /**
     * Get the Set of values for this attribute.  The Set returned is a copy of the original; changes
     * to this Set will not affect the original.
     *
     * @return The attribute values Set.
     */
    Set<String> getValues();

    /**
     * Return the attributes values as a String array.  Note that this array can be zero-length
     * in the case that there are no values.
     *
     * @return The attribute values array.
     */
    String[] getValuesAsArray();

    void addValue(String value);
    void addValues(Set<String> values);
    void addValues(String[] values);

    void removeValue(String value);
    void removeValues(Set<String> values);
    void removeValues(String[] values);

    void clear();

}

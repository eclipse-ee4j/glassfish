/*
 * Copyright (c) 2000, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jndi.ldap.ctl;

/**
 * This class implements a sort key which is used by the LDAPv3
 * Control for server side sorting of search results as defined in
 * <a href="http://www.ietf.org/rfc/rfc2891.txt">RFC-2891</a>.
 *
 * @author Vincent Ryan
 */
public class SortKey {

    /**
     * The ID of the attribute to sort by.
     */
    private String attrID;

    /**
     * The sort order. Ascending order, by default.
     */
    private boolean reverseOrder = false;

    /**
     * The ID of the matching rule to use for ordering attribute values.
     */
    private String matchingRuleID = null;

    /**
     * Constructs a new instance of SortKey.
     *
     * @param    attrID    The ID of the attribute to be used as a sort key.
     */
    public SortKey(String attrID) {
        this.attrID = attrID;
    }

    /**
     * Constructs a new instance of SortKey.
     *
     * @param    attrID        The ID of the attribute to be used as a sort
     *                key.
     * @param    ascendingOrder    If true then entries are arranged in ascending
     *                order. Otherwise there are in descending order.
     * @param    matchingRule    The possibly null ID of the matching rule to
     *                use to order the attribute values. If not
     *                specified then the ordering matching rule
     *                defined for the sort key attribute, is used.
     */
    public SortKey(String attrID, boolean ascendingOrder,
        String matchingRuleID) {

        this.attrID = attrID;
        reverseOrder = (! ascendingOrder);
        if (matchingRuleID != null) {
            this.matchingRuleID = matchingRuleID;
        }
    }

    /**
     * Retrieves the attribute ID of the sort key.
     *
     * @return    Attribute ID of the sort key.
     */
    public String getAttributeID() {
        return attrID;
    }

    /**
     * Determines the sort order.
     *
     * @return    true if the sort order is ascending, false if descending.
     */
    public boolean isAscending() {
        return (! reverseOrder);
    }

    /**
     * Retrieves the matching rule ID used to order the attribute values.
     *
     * @return    The possibly null matching rule ID. If null then the
     *            ordering matching rule defined for the sort key attribute,
     *            is used.
     */
    public String getMatchingRuleID() {
        return matchingRuleID;
    }
}

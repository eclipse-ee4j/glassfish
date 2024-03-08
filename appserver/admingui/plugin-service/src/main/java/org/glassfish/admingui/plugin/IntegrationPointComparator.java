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

package org.glassfish.admingui.plugin;

import java.io.Serializable;
import java.util.Comparator;

import org.glassfish.admingui.connector.IntegrationPoint;

/**
 *  <p>        This class compares two {@link IntegrationPoint} Objects.  See
 * {@link #compare} for more details.</p>
 */
public class IntegrationPointComparator implements Comparator<IntegrationPoint>, Serializable {
    /**
     * <p> Protected constructor.  Use {@link #getInstance()} instead.</p>
     */
    protected IntegrationPointComparator() {
    }

    /**
     * <p> Accessor for this <code>Comparator</code>.</p>
     */
    public static IntegrationPointComparator getInstance() {
        return _instance;
    }

    /**
     * <p> This method compares two {@link IntegrationPoint}s.  It will first
     *     check the <code>parentId</code>, then the <code>priority</code> if
     *     the <code>parentId</code>s are equal.  If the priorities happen to
     *     be equal as well, it will compare the <code>id</code>s.</p>
     */
    public int compare(IntegrationPoint ip1, IntegrationPoint ip2) {
        // First check parentIds
        String left = "" + ip1.getParentId();
        int result = left.compareTo("" + ip2.getParentId());
        if (result == 0) {
            // parentIds are the same, check the priorities
            result = ip1.getPriority() - ip2.getPriority();
            if (result == 0) {
                // priorities are the same, check the ids
                left = "" + ip1.getId();
                result = left.compareTo("" + ip2.getId());
                if (result == 0) {
                    // Equal
                    return 0;
                }
            }
        }

        // Return the answer
        return (result < 0) ? -1 : 1;
    }

    private static IntegrationPointComparator _instance =
            new IntegrationPointComparator();
}

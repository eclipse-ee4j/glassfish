/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.api.naming;

import java.util.List;

import org.jvnet.hk2.annotations.Contract;

/**
 * Identifies a class that implements a default resource. Typically, this class is also annotated with NamespacePrefixes
 * which allows a caller to get the logical name for the resource.
 *
 * @author Tom Mueller
 */
@Contract
public interface DefaultResourceProxy {
    String getPhysicalName();

    String getLogicalName();

    static class Util {
        /*
         * This utility method is useful for obtaining the logical name for the given physical name and a list of
         * DefaultResourceProxies. If the physicalName is not found, then null is returned.
         */
        public static String getLogicalName(List<DefaultResourceProxy> drps, String physicalName) {
            for (DefaultResourceProxy drp : drps) {
                if (drp.getPhysicalName().equals(physicalName)) {
                    return drp.getLogicalName();
                }
            }
            return null;
        }
    }
}

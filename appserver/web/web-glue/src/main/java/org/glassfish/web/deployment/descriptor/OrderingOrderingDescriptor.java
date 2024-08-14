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

package org.glassfish.web.deployment.descriptor;

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.Set;
import java.util.TreeSet;

import org.glassfish.deployment.common.Descriptor;

/**
 * This represents the ordering-ordering in web-fragment.xml.
 *
 * @author Shing Wai Chan
 */
public class OrderingOrderingDescriptor extends Descriptor {
    private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(OrderingOrderingDescriptor.class);

    private Set<String> names = new TreeSet<String>();
    private boolean others = false;

    public void addName(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalStateException(localStrings.getLocalString(
                    "web.deployment.exceptioninvalidnameinrelativeordering",
                    "The empty name is invalid for relative ordering element."));
        }
        names.add(name);
    }

    public void addOthers() {
        others = true;
    }

    public Set<String> getNames() {
        return names;
    }

    public boolean containsOthers() {
        return others;
    }

    public boolean containsName(String name) {
        return names.contains(name);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (String n : names) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(n);
            first = false;
        }

        if (others) {
            if (!first) {
                builder.append(", ");
            }
            builder.append("<others/>");
        }
        builder.append("]");
        return builder.toString();
    }
}

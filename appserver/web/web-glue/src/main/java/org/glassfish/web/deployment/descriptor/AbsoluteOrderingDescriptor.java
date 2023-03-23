/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.deployment.common.Descriptor;

/**
 * Deployment object representing the absolute-ordering of web-fragment.
 *
 * @author Shing Wai Chan
 */
public class AbsoluteOrderingDescriptor extends Descriptor {

    private static final Object OTHERS = new Object();

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(
        AbsoluteOrderingDescriptor.class);

    private final List<Object> absOrder = new ArrayList<>();

    private boolean hasOthers;

    public void addName(String name) {
        if (!absOrder.add(name)) {
            throw new IllegalStateException(localStrings.getLocalString(
                    "web.deployment.exceptionalreadydefinedinabsoluteordering",
                    "[{0}] has already been defined in the absolute-ordering.",
                    new Object[] { name }));
        }
    }

    public void addOthers() {
        if (!absOrder.add(OTHERS)) {
            throw new IllegalStateException(localStrings.getLocalString(
                    "web.deployment.exceptionalreadydefinedinabsoluteordering",
                    "[{0}] is already defined in the absolute-ordering.",
                    new Object[] { "<others/>" }));
        }
        hasOthers = true;
    }

    public List<Object> getOrdering() {
        return Collections.unmodifiableList(absOrder);
    }

    /**
     * @return true if this AbsoluteOrderingDescriptor contains an
     * others element, false otherwise
     */
    public boolean hasOthers() {
        return hasOthers;
    }

    /**
     * This method return the WebFragmentDescriptor in absolute order.
     * Note that the number of element return may be less than that of the original list.
     */
    public List<WebFragmentDescriptor> order(List<WebFragmentDescriptor> wfs) {
        List<WebFragmentDescriptor> wfList = new ArrayList<>();
        if (wfs != null && !wfs.isEmpty()) {
            Map<String, WebFragmentDescriptor> map = new HashMap<>();
            List<WebFragmentDescriptor> othersList = new ArrayList<>();
            for (WebFragmentDescriptor wf : wfs) {
                String name = wf.getName();
                if (name != null && name.length() > 0 && absOrder.contains(name)) {
                    map.put(name, wf);
                } else {
                    othersList.add(wf);
                }
            }

            for (Object obj : absOrder) {
                if (obj instanceof String) {
                    WebFragmentDescriptor wf = map.get(obj);
                    if (wf != null) {
                        wfList.add(wf);
                    }
                } else {
                    // others
                    for (WebFragmentDescriptor wf : othersList) {
                        wfList.add(wf);
                    }
                }
            }
        }
        return wfList;
    }
}

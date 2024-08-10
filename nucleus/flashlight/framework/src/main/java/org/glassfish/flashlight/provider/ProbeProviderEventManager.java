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

package org.glassfish.flashlight.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 */
@Service
public class ProbeProviderEventManager {

    private Collection<ProbeProviderListener> listeners = new ArrayList<ProbeProviderListener>();

    private Set<String> registeredTuples = new HashSet<String>();

    public synchronized void registerProbeProviderListener(ProbeProviderListener listener) {
        listeners.add(listener);
        for (String str : registeredTuples) {
            String[] names = str.split(":");
            String m1 = null;
            String p1 = null;
            String a1 = null;
            if (names.length >= 1) {
                m1 = names[0].length() == 0 ? null : names[0];
            }
            if (names.length >= 2) {
                p1 = names[1].length() == 0 ? null : names[1];
            }
            if (names.length >= 3) {
                a1 = names[2].length() == 0 ? null : names[2];
            }
            listener.providerRegistered(m1, p1, a1);
            //System.out.println("Notifying listener");
        }
    }

    public synchronized void unregisterProbeProviderListener(ProbeProviderListener listener) {
        listeners.remove(listener);
    }

    public synchronized void notifyListenersOnRegister(String moduleName, String providerName, String appName) {
        String moduleName1 = moduleName == null ? "" : moduleName;
        String providerName1 = providerName == null ? "" : providerName;
        String appName1 = appName == null ? "" : appName;
        for (ProbeProviderListener listener : listeners) {
            listener.providerRegistered(moduleName, providerName, appName);
        }
        registeredTuples.add(moduleName1 + ":" + providerName1 + ":" + appName1);
    }

    public synchronized void notifyListenersOnUnregister(String moduleName, String providerName, String appName) {
        String moduleName1 = moduleName == null ? "" : moduleName;
        String providerName1 = providerName == null ? "" : providerName;
        String appName1 = appName == null ? "" : appName;
        for (ProbeProviderListener listener : listeners) {
            listener.providerUnregistered(moduleName, providerName, appName);
        }
        registeredTuples.remove(moduleName1 + ":" + providerName1 + ":" + appName1);
    }

}

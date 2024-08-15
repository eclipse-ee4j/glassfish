/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices;

import com.sun.enterprise.deployment.WebServiceEndpoint;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * {@link WebServiceDeploymentNotifier} notifier implementation.
 *
 * @author Alexey Stashok
 */
public final class WebServiceDeploymentNotifierImpl implements WebServiceDeploymentNotifier {
    private final Collection<WebServiceDeploymentListener> listeners =
            new CopyOnWriteArraySet<WebServiceDeploymentListener>();

    public void addListener(WebServiceDeploymentListener listener) {
        listeners.add(listener);
    }

    public void removeListener(WebServiceDeploymentListener listener) {
        listeners.remove(listener);
    }

    public void notifyDeployed(WebServiceEndpoint nextEndpoint) {
        for(WebServiceDeploymentListener listener : listeners) {
            listener.onDeployed(nextEndpoint);
        }
    }

    public void notifyUndeployed(WebServiceEndpoint nextEndpoint) {
        for(WebServiceDeploymentListener listener : listeners) {
            listener.onUndeployed(nextEndpoint);
        }
    }

}

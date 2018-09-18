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

import org.jvnet.hk2.annotations.Contract;

/**
 * @author Mahesh Kannan
 *         Date: May 22, 2008
 */
@Contract
public interface ProbeProviderFactory {
    void dtraceEnabledChanged(boolean newValue);

    void monitoringEnabledChanged(boolean newValue);

    public <T> T getProbeProvider(Class<T> providerClazz)
            throws InstantiationException, IllegalAccessException;

    public <T> T getProbeProvider(Class<T> providerClazz, String invokerId)
            throws InstantiationException, IllegalAccessException;

    public <T> T getProbeProvider(String moduleName, String providerName, String appName, Class<T> clazz)
            throws InstantiationException, IllegalAccessException;

    public void unregisterProbeProvider(Object probeProvider);

    public void processXMLProbeProviders(ClassLoader cl, String xml, boolean inBundle);

    public void addProbeProviderEventListener(ProbeProviderEventListener listener);

    public void removeProbeProviderEventListener(ProbeProviderEventListener listener);
}

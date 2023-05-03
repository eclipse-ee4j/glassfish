/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.lang.reflect.InvocationHandler;

/**
 * All views of @Configured interface implementation must implement this
 * interface
 *
 * @author Jerome Dochez
 */
public interface ConfigView extends InvocationHandler {

    public ConfigView getMasterView();

    public void setMasterView(ConfigView view);

    public <T extends ConfigBeanProxy> Class<T> getProxyType();

    public <T extends ConfigBeanProxy> T getProxy(Class<T> proxyType);
}

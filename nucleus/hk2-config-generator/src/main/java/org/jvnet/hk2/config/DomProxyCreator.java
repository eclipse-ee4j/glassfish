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

import org.jvnet.hk2.config.provider.internal.Creator;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * {@link org.jvnet.hk2.component.Creator} that returns a typed proxy to {@link Dom}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("unchecked")
final class DomProxyCreator<T extends ConfigBeanProxy> implements Creator<T> {
    private final Class<? extends T> type;

    private final Dom dom;

    private volatile T proxyInstance;

    public DomProxyCreator(Class<T> type, Dom dom) {
        this.type = type;
        this.dom = dom;
    }

    public T create() {
        if (proxyInstance == null) {
            synchronized (this) {
                if (proxyInstance == null) {
                    proxyInstance = (T) dom.createProxy((Class<ConfigBeanProxy>) type);
                }
            }
        }

        return proxyInstance;
    }
}


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

package com.sun.enterprise.config.serverbeans;

import java.util.List;

import org.glassfish.grizzly.config.dom.ThreadPool;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

@Configured
public interface ThreadPools extends ConfigBeanProxy {
    /**
     * Gets the value of the {@code threadPool} property.
     *
     * <p>>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for the
     * {@code threadPool} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getThreadPool().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link ThreadPool}
     */
    @Element(required = true)
    List<ThreadPool> getThreadPool();
}

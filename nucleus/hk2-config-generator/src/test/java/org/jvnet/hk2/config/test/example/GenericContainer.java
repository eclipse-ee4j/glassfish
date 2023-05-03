/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.test.example;

import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import jakarta.validation.constraints.NotNull;

@Configured
public interface GenericContainer extends ConfigBeanProxy {
    int DEFAULT_THREAD_CORE_POOL_SIZE = 16;
    int DEFAULT_THREAD_MAX_POOL_SIZE = 32;
    long DEFAULT_THREAD_KEEP_ALIVE_SECONDS = 60;
    int DEFAULT_THREAD_QUEUE_CAPACITY = Integer.MAX_VALUE;
    boolean DEFAULT_ALLOW_CORE_THREAD_TIMEOUT = false;
    boolean DEFAULT_PRESTART_ALL_CORE_THREADS = false;

    @Attribute (defaultValue="32")
    String getMaxPoolSize();

    @Attribute (defaultValue="1234")
    long getStartupTime();

    @Attribute (defaultValue="1234")
    int getIntValue();

    @NotNull
    @Element WebContainerAvailability getWebContainerAvailability();
    void setWebContainerAvailability(WebContainerAvailability v);

    @Element("*")
    List<GenericConfig> getExtensions();

}

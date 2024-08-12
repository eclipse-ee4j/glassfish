/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.impl.util;

import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import com.sun.enterprise.container.common.spi.util.EntityManagerMethod;
import com.sun.enterprise.container.common.spi.util.EntityManagerQueryMethod;

import jakarta.inject.Singleton;

import org.jvnet.hk2.annotations.Service;

@Service
@Singleton
public class DummyCallFlowAgentImpl
    implements CallFlowAgent {

    public boolean isEnabled() {return false;}

    public void entityManagerMethodStart(EntityManagerMethod val) {}

    public void entityManagerMethodEnd() {}

    public void entityManagerQueryStart(EntityManagerQueryMethod val) {}

    public void entityManagerQueryEnd() {}
}

/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.web.ContextParameter;

/**
 * I am an object representing a dependency on a resource environment.
 *
 * @author Kenneth Saks
 */
public interface ResourceEnvReference extends ContextParameter {

    /**
     * @return the logical name of the destination reference
     */
    @Override
    String getName();

    /**
     * @param refName the logical name of the des+tination reference
     */
    @Override
    void setName(String refName);

    /**
     * @return the type(jakarta.jms.Queue, jakarta.jms.Topic) of the destination
     */
    String getType();

    /**
     * @param refType the type(jakarta.jms.Queue, jakarta.jms.Topic) of the destination
     */
    void setType(String refType);
}

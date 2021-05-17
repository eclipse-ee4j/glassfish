/*
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


/**
 * I am an object representing a dependency on a resource environment.
 * @author Kenneth Saks
 */

import com.sun.enterprise.deployment.web.ContextParameter;

public interface ResourceEnvReference extends ContextParameter {

    /* Gets the logical name of the destination reference */
    public String getName();
    public void setName(String refName);

    /* Gets the type(jakarta.jms.Queue, jakarta.jms.Topic) of the destination */
    public String getType();
    public void setType(String refType);

}

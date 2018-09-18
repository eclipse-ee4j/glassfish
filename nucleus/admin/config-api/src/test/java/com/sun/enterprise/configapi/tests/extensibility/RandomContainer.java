/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests.extensibility;

import org.glassfish.api.admin.config.Container;
import org.glassfish.api.admin.config.Named;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Configured;

/**
 * @author Jerome Dochez
 */
@Configured
public interface RandomContainer extends Container, Named {

    @Attribute
    public String getNumberOfRuntime();

    public void setNumberOfRuntime();

    @Element
    public RandomElement getRandomElement();

    public void setRandomElement(RandomElement randomElement);
}

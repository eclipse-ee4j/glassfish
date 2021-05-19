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

package com.sun.enterprise.deployment.types;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;

import java.util.Set;

/**
 * This interface defines the behaviour of a Java EE component containaing
 * web service references
 * Note from the author, I hate being so verbose with method names but
 * I follow the current design pattern. should be fixed one day
 *
 * @author Jerome Dochez
 */
public interface ServiceReferenceContainer {

    /**
     * get a particular service reference by name
     */
    ServiceReferenceDescriptor getServiceReferenceByName(String name);

    /**
     * return the list of service references
     */
    Set getServiceReferenceDescriptors();

    /**
     * Add a new service reference
     */
    void addServiceReferenceDescriptor(ServiceReferenceDescriptor descriptor);

    /**
     * remove a service reference
     */
    void removeServiceReferenceDescriptor(ServiceReferenceDescriptor descriptor);
}

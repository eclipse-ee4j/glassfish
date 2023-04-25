/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.serverbeans;

import java.util.List;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import static java.util.stream.Collectors.toList;

/**
 * An {@link Application} or {@link Resource} reference container object.
 *
 * @author Jerome Dochez
 */
@Configured
public interface RefContainer extends ConfigBeanProxy {

    /**
     * List of all the resources that this instance is referencing.
     *
     * @return the list of resources references
     */
    @Element
    List<ResourceRef> getResourceRef();

    /**
     * List of all the applications that this instance is referencing.
     *
     * @return the list of applications references.
     */
    @Element
    List<ApplicationRef> getApplicationRef();

    /**
     * Note: This method uses stream to process names, it is not just a getter.
     *
     * @return list of {@link ResourceRef#getRef()} retrieved from {@link #getResourceRef()}
     */
    default List<String> getResourceRefNames() {
        return getResourceRef().stream().map(ResourceRef::getRef).collect(toList());
    }

    /**
     * Note: This method uses stream to process names, it is not just a getter.
     *
     * @return list of {@link ApplicationRef#getRef()} retrieved from {@link #getApplicationRef()}
     */
    default List<String> getApplicationRefNames() {
        return getApplicationRef().stream().map(ApplicationRef::getRef).collect(toList());
    }
}

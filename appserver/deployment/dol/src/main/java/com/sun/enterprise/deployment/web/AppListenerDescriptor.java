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

package com.sun.enterprise.deployment.web;

/**
 * Objects exhibiting this interface represent a lifecycle listener.
 * This represents the <listener-class> XML element in the servlet spec.
 *
 * @author Vivek Nagar
 */
public interface AppListenerDescriptor {

    /**
     * @return the listener class name.
     */
    String getListener();

    /**
     * Set the listener class.
     *
     * @param listener class name.
     */
    void setListener(String listener);

    // add get/set for descriptionGroup
    String getDescription();
    void setDescription(String description);

    void setDisplayName(String name);
    String getDisplayName();

    void setLargeIconUri(String largeIconUri);
    String getLargeIconUri();

    void setSmallIconUri(String smallIconUri);
    String getSmallIconUri();
}

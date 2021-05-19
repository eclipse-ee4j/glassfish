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

package com.sun.enterprise.deployment.web;

/** I am the type representing a named descriptor that may be shown in a UI tool. */
public interface WebDescriptor {

    /** Return the relative URI to the large icon for this descriptor. */
    String getLargeIconUri();

    void setLargeIconUri(String largeIconUri);

    /** Return the relative URI to the small icon for this descriptor. */
    String getSmallIconUri();

    void setSmallIconUri(String smallIconUri);

    /** Return the human readable display name of this descriptor. */
    String getName();

    void setName(String name);

    /** Return a human readable description of this entity. */
    String getDescription();

    void setDescription(String description);

}

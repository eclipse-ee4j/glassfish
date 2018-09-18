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

import java.util.Collection;
import java.util.Vector;

/** Servlet filter objects */
public interface ServletFilter {

    public void setName(String name);
    public String getName();

    public void setDisplayName(String name);
    public String getDisplayName();

    public void setDescription(String description);
    public String getDescription();

    public void setClassName(String name);
    public String getClassName();

    public void setInitializationParameters(Collection<InitializationParameter> c);
    public Vector getInitializationParameters();
    public void addInitializationParameter(InitializationParameter ref);
    public void removeInitializationParameter(InitializationParameter ref);

    public void setLargeIconUri(String largeIconUri);
    public String getLargeIconUri();

    public void setSmallIconUri(String smallIconUri);
    public String getSmallIconUri();

    public void setAsyncSupported(Boolean asyncSupported);
    public Boolean isAsyncSupported();
}

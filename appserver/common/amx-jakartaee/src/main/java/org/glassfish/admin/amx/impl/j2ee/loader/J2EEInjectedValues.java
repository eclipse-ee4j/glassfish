/*
 * Copyright (c) 2006, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.j2ee.loader;

import com.sun.enterprise.deployment.archivist.ArchivistFactory;

import jakarta.inject.Inject;

import org.glassfish.admin.amx.impl.util.InjectedValues;
import org.glassfish.internal.data.ApplicationRegistry;
import org.jvnet.hk2.annotations.Service;

/**
   Supplies the needed values for other classes such as MBeans that do not have access to
   the injection facilities.
 */
@Service
public final class J2EEInjectedValues extends InjectedValues
{
    @Inject
    private ApplicationRegistry mAppsRegistry;
    public ApplicationRegistry getApplicationRegistry() { return mAppsRegistry; }

    @Inject
    ArchivistFactory mArchivistFactory;
    public ArchivistFactory getArchivistFactory() { return mArchivistFactory; }

    public static synchronized J2EEInjectedValues getInstance()
    {
        return getDefaultServices().getService(J2EEInjectedValues.class);
    }

    public J2EEInjectedValues()
    {
    }
}
















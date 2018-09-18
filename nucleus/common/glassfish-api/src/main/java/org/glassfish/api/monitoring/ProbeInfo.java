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

package org.glassfish.api.monitoring;

import java.lang.reflect.Method;

/**
 * The bare minimum info contained in a FlashlightProbe that a value-add module needs
 * The names look weird because they match pre-existing methods.  Those methods were
 * already declared so they can not change...
 * @author bnevins
 */
public interface ProbeInfo {
    Class[]     getParamTypes();
    String      getProviderJavaMethodName();
    String      getProbeName();
    int         getId();
    String      getModuleName();
    String      getModuleProviderName();
    String      getProbeProviderName();
    String[]    getProbeParamNames();
    Method      getDTraceMethod();
}

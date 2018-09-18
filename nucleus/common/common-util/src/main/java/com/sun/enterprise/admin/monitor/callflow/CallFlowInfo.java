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

/*
 * CallFlowInfo.java
 * $Id: CallFlowInfo.java,v 1.3 2005/12/25 04:10:35 tcfujii Exp $
 * $Date: 2005/12/25 04:10:35 $
 * $Revision: 1.3 $
 */

package com.sun.enterprise.admin.monitor.callflow;

public interface CallFlowInfo {
    public String getApplicationName();
    public String getModuleName();
    public String getComponentName();
    public ComponentType getComponentType();
    public java.lang.reflect.Method getMethod();
    public String getTransactionId();
    public String getCallerPrincipal();
    public Throwable getException();
}

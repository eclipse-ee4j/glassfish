/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
 * ASProbeMBean.java
 *
 * Created on March 29, 2006, 4:44 PM
 */
package testmbeans;

/**
 * Interface ASProbeMBean
 * ASProbe Description
 * @author bnevins
 */
public interface ASProbeMBean
{
    /**
     * Get Amount of memory allocated in JVM
     */
    public long     getHeapUsage();
    public int      getNumApps();
    public int      getNumEJBModules();
    public int      getNumWebModules();
    public int      getNumComponents();
    public int      getThreadCount();
    public int      getPeakThreadCount();
    public String[] getAppNames();
    public String[] getWebModuleNames();
}



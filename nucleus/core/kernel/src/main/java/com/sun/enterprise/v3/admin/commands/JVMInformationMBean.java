/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.commands;

/** An interface to get the information about the JVM which the appserver is running.
 * This interface is intended to replace the traditional techniques to get thread
 * dump from a JVM. This is the interface of the MBean that will implement the
 * JMX based techniques in JDK 1.5+ platform to get interesting information about
 * the JVM itself.
 */
public interface JVMInformationMBean {

    public String getThreadDump(String processName);

    public String getClassInformation(String processName);

    public String getMemoryInformation(String processName);

    public String getSummary(String processName);

    public String getLogInformation(String processName);
}

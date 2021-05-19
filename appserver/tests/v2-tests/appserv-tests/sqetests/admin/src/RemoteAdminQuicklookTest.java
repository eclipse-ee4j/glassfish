/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.management.MBeanServerConnection;
public interface RemoteAdminQuicklookTest {
    /** Performs an arbitrary test and returns the result of the same as a String.
     * The returned result must be something that is available as status string in
     * com.sun.ejte.ccl.reporter.SimpleReporterAdapter.PASS and FAIL.
     */
    public String test() throws RuntimeException;

    /** Sets the MBeanServerConnection for testing the stuff remotely. The parameter may
     * not be null.
     */
    public void setMBeanServerConnection(final MBeanServerConnection c);

    /** Returns the name of the test.
     */
    public String getName();

    /** Returns the time taken by the test to execute in milliseconds
     * @return long denoting the time taken for this test to execute
     */
    public long getExecutionTime();
}

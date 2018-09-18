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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/base/AMXDebugTest.java,v 1.5 2007/05/05 05:23:53 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:23:53 $
*/
package org.glassfish.admin.amxtest.base;

import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.util.misc.Output;

import java.io.File;

/**
 */
public final class AMXDebugTest
        extends junit.framework.TestCase {
    public AMXDebugTest() {
        getAMXDebug().setDefaultDebug(true);
        getAMXDebug().setAll(true);
    }

    private String
    getID(final String uniquifier) {
        return this.getClass().getName() + "." + uniquifier;
    }

    private Output
    getOutput(final String id) {
        return getAMXDebug().getOutput(id);
    }

    private AMXDebug
    getAMXDebug() {
        return AMXDebug.getInstance();
    }

    public synchronized void
    testCreateFile() {
        // multiple iterations require that we choose a new file each time
        final String id = getID("testCreateFile" + System.currentTimeMillis());
        final Output output = getOutput(id);

        final File outputFile = getAMXDebug().getOutputFile(id);
        outputFile.delete();
        assert (!outputFile.exists());

        output.printDebug("test");
        assert (outputFile.exists());
    }

    public synchronized void
    testToggleDebug() {
        final String id = getID("testToggleDebug");
        final Output output = getOutput(id);

        getAMXDebug().setDebug(id, false);
        assert (!getAMXDebug().getDebug(id));
        getAMXDebug().setDebug(id, true);
        assert (getAMXDebug().getDebug(id));
    }

    public synchronized void
    testReset() {
        final String id = getID("testReset");
        final Output output = getOutput(id);

        getAMXDebug().reset(id);
        final File outputFile = getAMXDebug().getOutputFile(id);
        outputFile.delete();
        assert (!outputFile.exists());
        output.printDebug("test");
        assert (outputFile.exists());

        // make sure we can call it repeatedly
        getAMXDebug().reset(id);
        getAMXDebug().reset(id);
        getAMXDebug().reset(id);
    }


    public synchronized void
    testPrint() {
        final String id = getID("testPrint");
        final Output output = getOutput(id);

        output.printDebug("printDebug");
        output.printError("printError");
        output.println("println");
        output.print("print");
        output.print("...");
        output.print("END");
    }


    public synchronized void
    testClose() {
        final String id = getID("testClose");
        final Output output = getOutput(id);
        final File outputFile = getAMXDebug().getOutputFile(id);

        output.println("hello");
        assert (outputFile.exists());

        output.close();
        outputFile.delete();
        assert (!outputFile.exists());

        output.println("hello");
        assert (outputFile.exists());
    }

    public synchronized void
    testToggleDefaultDebug() {
        final String id = getID("testToggleDefaultDebug");
        final Output output = getOutput(id);

        getAMXDebug().setDefaultDebug(false);
        assert (!getAMXDebug().getDefaultDebug());

        getAMXDebug().setDefaultDebug(true);
        assert (getAMXDebug().getDefaultDebug());
    }


    public synchronized void
    testSetAll() {
        final String id = getID("testSetAll");
        final Output output = getOutput(id);

        getAMXDebug().setAll(false);
        getAMXDebug().setAll(false);
        getAMXDebug().setAll(true);
        getAMXDebug().setAll(true);
        getAMXDebug().setAll(false);
        getAMXDebug().setAll(true);
        getAMXDebug().setAll(true);
    }


    public synchronized void
    testMark() {
        final String id = getID("testMark");
        final Output output = getOutput(id);

        getAMXDebug().mark(id);
        getAMXDebug().mark(id, null);
        getAMXDebug().mark(id, "marker 1");
        getAMXDebug().mark(id, "marker 2");
        getAMXDebug().mark(output, null);
        getAMXDebug().mark(output, "marker 3");
    }
}


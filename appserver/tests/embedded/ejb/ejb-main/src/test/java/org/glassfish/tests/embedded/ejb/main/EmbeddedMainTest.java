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

package org.glassfish.tests.embedded.ejb.main;

import org.glassfish.tests.embedded.ejb.test.*;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Nov 2, 2009
 * Time: 10:20:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmbeddedMainTest extends EmbeddedTest {

    public static void main(String[] args) {
        EmbeddedMainTest test = new EmbeddedMainTest();
        System.setProperty("basedir", System.getProperty("user.dir"));
        test.test();
    }

    @Override
    public File getDeployableArtifact() {
        File f = new File(System.getProperty("basedir"));
        f = f.getParentFile();
        f = new File(f, "ejb-api");
        f = new File(f, "target");
        f = new File(f, "classes");
        System.out.println("Using file " + f.getAbsolutePath());
        return f;
    }
}

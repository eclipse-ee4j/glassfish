/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.nucleus.admin;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.tests.utils.NucleusTestUtils;
import static org.glassfish.tests.utils.NucleusTestUtils.*;
import static org.testng.AssertJUnit.*;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class NucleusStartStopTest {

    private static final String TEST_LIBS_KEY = "TEST_LIBS";
    private static final Map<String, String> COPY_LIB;
    static {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("modules", "modules");
        COPY_LIB = Collections.unmodifiableMap(map);
    }

    @BeforeSuite
    public void setUp(ITestContext context) throws IOException {
        //Copy testing libraries into Nucleus distribution
        Collection<File> testLibs = new ArrayList<File>();
        context.setAttribute(TEST_LIBS_KEY, testLibs);
        String basedir = System.getProperty("basedir");
        assertNotNull(basedir);
        File addondir = new File(basedir, "target/addon");
        for (Map.Entry<String, String> entry : COPY_LIB.entrySet()) {
            copyLibraries(new File(addondir, entry.getKey()), 
                    new File(NucleusTestUtils.getNucleusRoot(), 
                    entry.getValue()), testLibs);
        }
        //Start
        assertTrue(nadmin("start-domain"));
    }

    @AfterSuite(alwaysRun = true)
    public void tearDown(ITestContext context) {
        try {
            assertTrue(nadmin("stop-domain", "--kill=true"));
        } finally {
            Collection<File> libs = (Collection<File>) context.getAttribute(TEST_LIBS_KEY);
            if (libs != null) {
                for (File lib : libs) {
                    if (lib.exists()) {
                        try {
                            lib.delete();
                        } catch (Exception ex) {
                            System.out.println("Can not delete " + lib.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
    
    private void copyLibraries(File src, File dest, Collection<File> copiedLibs) throws IOException {
        if (src.exists() && src.isDirectory()) {
            File[] dirs = src.listFiles(new FileFilter() {
                                       @Override
                                       public boolean accept(File f) {
                                           return f.isDirectory();
                                       }
                                   });
            for (File dir : dirs) {
                File target = new File(dir, "target");
                if (target.exists() && target.isDirectory()) {
                    File[] jars = target.listFiles(new FileFilter() {
                                                   @Override
                                                   public boolean accept(File f) {
                                                       return f.isFile() && f.getName().toLowerCase().endsWith(".jar");
                                                   }
                                               });
                    for (File jar : jars) {
                        File df = new File(dest, jar.getName());
                        copiedLibs.add(df);
                        copy(jar, df);
                        System.out.println("TESTING LIBRARY: " + df.getAbsolutePath());
                    }
                }
            }
        }
    }

    private static void copy(File src, File dest) throws IOException {
        if (!dest.exists()) {
            dest.createNewFile();
        }
        FileChannel sch = null;
        FileChannel dch = null;
        try {
            sch = new FileInputStream(src).getChannel();
            dch = new FileOutputStream(dest).getChannel();
            dch.transferFrom(sch, 0, sch.size());
        } finally {
            try { sch.close(); } catch (Exception ex) {}
            try { dch.close(); } catch (Exception ex) {}
        }
    }

    
}

/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.verifier;

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.tools.verifier.gui.MainFrame;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class VerifierMain {

    private static volatile GlassFishRuntime gfr;

    public static void main(String[] args) throws GlassFishException, IOException {
        VerifierFrameworkContext verifierFrameworkContext =
                new Initializer(args).getVerificationContext();

        addShutdownHook(); // Since in gui mode, we don't get a chance to clean up, we need to install a shutdown hook
        gfr = GlassFishRuntime.bootstrap();
        GlassFishProperties gfp = new GlassFishProperties();
        gfp.setProperty(StartupContext.TIME_ZERO_NAME, (new Long(System.currentTimeMillis())).toString());
        final String VERIFIER_MODULE = "org.glassfish.verifier";
        gfp.setProperty(StartupContext.STARTUP_MODULE_NAME, VERIFIER_MODULE);
//        gfp.setConfigFileURI("file:/tmp/domain.xml");
        GlassFish gf = gfr.newGlassFish(gfp);
        gf.start();
        int failedCount = -1;
        Verifier verifier = gf.getService(Verifier.class);
        if (verifierFrameworkContext.isUsingGui()) {
            MainFrame mf = new MainFrame(
                    verifierFrameworkContext.getJarFileName(), true, verifier);
            mf.setSize(800, 600);
            mf.setVisible(true);
        } else {
            LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
            try {
                verifier.init(verifierFrameworkContext);
                verifier.verify();
            } catch (Exception e) {
                LogRecord logRecord = new LogRecord(Level.SEVERE,
                        smh.getLocalString(
                                verifier.getClass().getName() +
                                ".verifyFailed", // NOI18N
                                "Could not verify successfully.")); // NOI18N
                logRecord.setThrown(e);
                verifierFrameworkContext.getResultManager().log(logRecord);
            }
            verifier.generateReports();
            failedCount = verifierFrameworkContext.getResultManager()
                    .getFailedCount() +
                    verifierFrameworkContext.getResultManager().getErrorCount();
            System.exit(failedCount);
        }
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("Verifier Shutdown Hook") {
            public void run() {
                if (gfr == null) return;
                try {
                    gfr.shutdown();
                    gfr = null;
                }
                catch (Exception ex) {
                    System.err.println("Error shutting down glassfish runtime: " + ex);
                    ex.printStackTrace();
                }
            }
        });

    }
}

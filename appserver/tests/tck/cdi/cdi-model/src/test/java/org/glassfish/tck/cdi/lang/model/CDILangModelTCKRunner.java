/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation. All rights reserved.
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
package org.glassfish.tck.cdi.lang.model;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.cdi.lang.model.tck.LangModelVerifier;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.tck.cdi.lang.model.LangModelVerifierBuildCompatibleExtension.langModelVerifierBuildCompatibleExtensionPassed;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class CDILangModelTCKRunner {

    private static final System.Logger LOG = System.getLogger(CDILangModelTCKRunner.class.getName());

    @Deployment
    public static WebArchive deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class)

                // The package we are testing
                .addPackage(LangModelVerifier.class.getPackage())

                // The build compatible extension that starts the test
                .addClass(LangModelVerifierBuildCompatibleExtension.class)
                .addAsServiceProvider(BuildCompatibleExtension.class, LangModelVerifierBuildCompatibleExtension.class)

                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        LOG.log(INFO, archive.toString(true));

        return archive;
    }

    @Test
    public void testCDILangModel() {
        // If the extension has been called, and no exception was thrown
        assertTrue(langModelVerifierBuildCompatibleExtensionPassed);
    }
}
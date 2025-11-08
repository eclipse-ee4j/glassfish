/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.tck.data.arquillian;

import com.sun.tdk.signaturetest.SignatureTest;
import com.sun.tdk.signaturetest.plugin.PluginAPI;
import com.sun.tdk.signaturetest.util.CommandLineParserException;

import org.glassfish.tck.data.junit5.TransactionExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.data.standalone.entity.EntityTests;

/**
 *
 * @author Ondro Mihalyi
 */
public class JakartaNoSqlProcessor implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (archive instanceof WebArchive webArchive) {
            webArchive.addPackages(false,
                          TransactionExtension.class.getPackage(),
                          EntityTests.class.getPackage())
                      .addPackages(true,
                          SignatureTest.class.getPackage(),
                          PluginAPI.class.getPackage(),
                          com.sun.tdk.signaturetest.core.Log.class.getPackage(),
                          CommandLineParserException.class.getPackage());
            if (Boolean.getBoolean("org.glassfish.data.tck.global-transaction")) {
                 webArchive.add(new ClassLoaderAsset("META-INF/services/org.junit.jupiter.api.extension.Extension"),
                          "WEB-INF/classes/" + "META-INF/services/org.junit.jupiter.api.extension.Extension");

            }
        }
    }


}

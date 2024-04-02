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
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;

import org.jboss.cdi.lang.model.tck.LangModelVerifier;

public class LangModelVerifierBuildCompatibleExtension implements BuildCompatibleExtension {

    public static boolean langModelVerifierBuildCompatibleExtensionCalled;

    public static boolean langModelVerifierBuildCompatibleExtensionPassed;

    @Discovery
    public void discovery(ScannedClasses scannedClasses) {
        scannedClasses.add(LangModelVerifier.class.getName());
    }

    @Enhancement(types = LangModelVerifier.class)
    public void configure(ClassConfig classConfig) {
        langModelVerifierBuildCompatibleExtensionCalled = true;
        LangModelVerifier.verify(classConfig.info());
        // If there's an error, the verify() method will throw an exception
        langModelVerifierBuildCompatibleExtensionPassed = true;
    }
}
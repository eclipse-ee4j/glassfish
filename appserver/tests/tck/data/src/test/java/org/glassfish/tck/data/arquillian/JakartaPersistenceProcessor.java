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

import org.glassfish.tck.data.junit5.TransactionExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.data.core.cdi.CDITests;
import ee.jakarta.tck.data.framework.read.only.NaturalNumber;
import ee.jakarta.tck.data.standalone.entity.EntityTests;
import ee.jakarta.tck.data.standalone.persistence.PersistenceEntityTests;
import ee.jakarta.tck.data.standalone.signature.SignatureTests;
import ee.jakarta.tck.data.web.validation.ValidationTests;

/**
 *
 * @author Ondro Mihalyi
 */
public class JakartaPersistenceProcessor implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if(archive instanceof WebArchive webArchive) {
            webArchive.addAsWebInfResource(getClass().getClassLoader().getResource("persistence.xml"), "classes/META-INF/persistence.xml");
            webArchive.addPackages(false,
                    TransactionExtension.class.getPackage(),
                    NaturalNumber.class.getPackage(),
                    ValidationTests.class.getPackage(),
                    PersistenceEntityTests.class.getPackage(),
                    CDITests.class.getPackage(),
                    EntityTests.class.getPackage(),
                    SignatureTests.class.getPackage());
        }
    }

}

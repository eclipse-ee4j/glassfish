/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.tck.runner;

import java.net.URL;
import java.util.HashSet;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import tck.arquillian.porting.lib.spi.AbstractTestArchiveProcessor;


public class GlassfishTestResourceProcessor extends AbstractTestArchiveProcessor {

    static HashSet<String> sunXmlFiles = new HashSet<String>();
    static {
        sunXmlFiles.add("META-INF/sun-application-client.xml");
        sunXmlFiles.add("META-INF/sun-application.xml");
        sunXmlFiles.add("META-INF/sun-ra.xml");
        sunXmlFiles.add("META-INF/sun-ejb-jar.xml");
        sunXmlFiles.add("WEB-INF/sun-web.xml");
    }

    /**
     * Called on completion of the Arquillian configuration.
     */
    @Override
    public void initalize(@Observes ArquillianDescriptor descriptor) {
        super.initalize(descriptor);
    }


    @Override
    public void processClientArchive(JavaArchive clientArchive, Class<?> testClass, URL sunXmlURL) {
    }


    @Override
    public void processEjbArchive(JavaArchive ejbArchive, Class<?> testClass, URL sunXmlURL) {
    }


    @Override
    public void processWebArchive(WebArchive webArchive, Class<?> testClass, URL sunXmlURL) {
    }


    @Override
    public void processEarArchive(EnterpriseArchive earArchive, Class<?> testClass, URL sunXmlURL) {
    }


    @Override
    public void processRarArchive(JavaArchive rarArchive, Class<?> testClass, URL sunXmlUrl) {
    }

    @Override
    public void processParArchive(JavaArchive parArchive, Class<?> testClass, URL persistenceXmlUrl) {
    }
}

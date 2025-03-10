/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.messaging.tck;

import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import tck.arquillian.porting.lib.spi.AbstractTestArchiveProcessor;

public class GlassfishTestArchiveProcessor extends AbstractTestArchiveProcessor {

    static Logger log = Logger.getLogger(GlassfishTestArchiveProcessor.class.getName());

    /**
     * Called on completion of the Arquillian configuration.
     */
    @Override
    public void initalize(@Observes ArquillianDescriptor descriptor) {
        super.initalize(descriptor);
    }

    @Override
    public void processClientArchive(JavaArchive clientArchive, Class<?> testClass, URL sunXmlURL) {
        String name = clientArchive.getName();
    }

    @Override
    public void processWebArchive(WebArchive webArchive, Class<?> testClass, URL sunXmlURL) {
        String name = webArchive.getName();
    }

    @Override
    public void processRarArchive(JavaArchive warArchive, Class<?> testClass, URL sunXmlURL) {

    }

    @Override
    public void processParArchive(JavaArchive javaArchive, Class<?> aClass, URL url) {

    }

    @Override
    public void processEarArchive(EnterpriseArchive earArchive, Class<?> testClass, URL sunXmlURL) {
        String name = earArchive.getName();
    }

    @Override
    public void processEjbArchive(JavaArchive ejbArchive, Class<?> testClass, URL sunXmlURL) {
        String name = ejbArchive.getName();
    }

}

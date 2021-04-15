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

package com.sun.enterprise.admin.servermgmt.cli;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.bootstrap.HK2Populator;
import org.glassfish.hk2.bootstrap.impl.ClasspathDescriptorFileFinder;
import org.glassfish.hk2.bootstrap.impl.Hk2LoaderPopulatorPostProcessor;
import org.glassfish.internal.api.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;

/**
 * Implementation for the CLI command verify-domain-xml Verifies the content of the domain.xml file
 *
 * verify-domain-xml [--domaindir install_dir/domains] [domain_name]
 * 
 * @author Nandini Ektare
 */
@Service(name = "verify-domain-xml")
@org.glassfish.hk2.api.PerLookup
public final class VerifyDomainXmlCommand extends LocalDomainCommand {

    @Param(name = "domain_name", primary = true, optional = true)
    private String domainName0;

    private static final LocalStringsImpl strings = new LocalStringsImpl(VerifyDomainXmlCommand.class);

    @Override
    protected void validate() throws CommandException, CommandValidationException {
        setDomainName(domainName0);
        super.validate();
    }

    /**
     */
    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {

        File domainXMLFile = getDomainXml();
        logger.log(Level.FINER, "Domain XML file = {0}", domainXMLFile);
        try {
            // get the list of JAR files from the modules directory
            ArrayList<URL> urls = new ArrayList<URL>();
            File idir = new File(System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
            File mdir = new File(idir, "modules");
            File[] files = mdir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.toString().endsWith(".jar")) {
                        urls.add(f.toURI().toURL());
                    }
                }
            }

            final URL[] urlsA = urls.toArray(new URL[urls.size()]);

            ClassLoader cl = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public Object run() {
                    return new URLClassLoader(urlsA, Globals.class.getClassLoader());
                }
            });

            ModulesRegistry registry = new StaticModulesRegistry(cl);
            ServiceLocator serviceLocator = registry.createServiceLocator("default");

            ConfigParser parser = new ConfigParser(serviceLocator);
            URL domainURL = domainXMLFile.toURI().toURL();
            DomDocument doc = parser.parse(domainURL);
            Dom domDomain = doc.getRoot();
            Domain domain = domDomain.createProxy(Domain.class);
            DomainXmlVerifier validator = new DomainXmlVerifier(domain);

            if (validator.invokeConfigValidator())
                return 1;
        } catch (Exception e) {
            throw new CommandException(e);
        }
        return 0;
    }
}

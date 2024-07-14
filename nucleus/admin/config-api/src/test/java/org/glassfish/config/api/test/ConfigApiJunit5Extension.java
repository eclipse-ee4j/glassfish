/*
 * Copyright (c) 2021, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.config.api.test;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Server;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.GlassFishDocument;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.tests.utils.junit.DomainXml;
import org.glassfish.tests.utils.junit.BaseHK2JUnit5Extension;
import org.glassfish.tests.utils.mock.TestDocument;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.jvnet.hk2.config.DomDocument;

import static org.glassfish.hk2.utilities.BuilderHelper.createConstantDescriptor;

/**
 * @author David Matejcek
 */
public class ConfigApiJunit5Extension extends BaseHK2JUnit5Extension {

    @Override
    protected String getDomainXml(Class<?> testClass) {
        final DomainXml annotation = testClass.getAnnotation(DomainXml.class);
        if (annotation == null || annotation.value() == null) {
            return "DomainTest.xml";
        }
        return annotation.value();
    }


    @Override
    protected Class<? extends DomDocument<?>> getDomainXmlDomClass(final Class<?> testClass) {
        final DomainXml annotation = testClass.getAnnotation(DomainXml.class);
        if (annotation == null || annotation.domDocumentClass() == TestDocument.class) {
            return GlassFishTestDocument.class;
        }
        return annotation.domDocumentClass();
    }


    /**
     * We don't need to parse locator files. In fact it breaks some tests.
     */
    @Override
    protected Set<String> getLocatorFilePaths(final ExtensionContext context) {
        return Set.of();
    }


    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        Logger.getLogger("").setLevel(Level.ALL);
        super.beforeAll(context);
        Server server = getLocator().getService(Server.class, "server");
        if (server == null) {
            return;
        }
        ActiveDescriptor<Server> serverDescriptor = createConstantDescriptor(server,
            ServerEnvironment.DEFAULT_INSTANCE_NAME, Server.class);
        ServiceLocatorUtilities.addOneDescriptor(getLocator(), serverDescriptor);

        server.getConfig().addIndex(getLocator(), ServerEnvironment.DEFAULT_INSTANCE_NAME);

        Cluster cluster = server.getCluster();
        if (cluster != null) {
            ActiveDescriptor<Cluster> clusterDescriptor = createConstantDescriptor(cluster,
                ServerEnvironment.DEFAULT_INSTANCE_NAME, Cluster.class);
            ServiceLocatorUtilities.addOneDescriptor(getLocator(), clusterDescriptor);
        }
    }

    public static class GlassFishTestDocument extends GlassFishDocument {

        public GlassFishTestDocument(ServiceLocator locator) {
            super(locator, Executors.newCachedThreadPool(new ThreadFactory() {

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                }

            }));
        }
    }
}

/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.io.runtime.GFAppClientRuntimeDDFile;
import com.sun.enterprise.deployment.test.DolJunit5Extension;
import com.sun.enterprise.deployment.util.DOLUtils;

import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.main.jul.handler.LogCollectorHandler;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Java Web Start is not supported, but existing runtime descriptors may still contain
 * the java-web-start-access element. They must keep deploying.
 */
@ExtendWith(DolJunit5Extension.class)
class AppClientRuntimeNodeTest {

    private static final String DESCRIPTOR = """
        <glassfish-application-client>
          <java-web-start-access>
            <context-root>/custom</context-root>
            <eligible>true</eligible>
            <vendor>Acme</vendor>
            <jnlp-doc href="custom.jnlp"/>
          </java-web-start-access>
          <resource-ref>
            <res-ref-name>jdbc/app</res-ref-name>
            <jndi-name>jdbc/__default</jndi-name>
          </resource-ref>
        </glassfish-application-client>
        """;

    @Inject
    private ServiceLocator locator;

    private LogCollectorHandler logCollector;

    @BeforeEach
    void init() {
        Globals.setDefaultHabitat(locator);
        logCollector = new LogCollectorHandler(DOLUtils.getDefaultLogger());
        logCollector.setLevel(Level.WARNING);
    }


    @AfterEach
    void reset() {
        logCollector.close();
        Globals.setDefaultHabitat(null);
    }


    @Test
    void javaWebStartAccessIsIgnored() throws Exception {
        ApplicationClientDescriptor descriptor = new ApplicationClientDescriptor();
        ResourceReferenceDescriptor resourceRef = new ResourceReferenceDescriptor("jdbc/app", null, "javax.sql.DataSource");
        descriptor.addResourceReferenceDescriptor(resourceRef);

        GFAppClientRuntimeDDFile ddFile = new GFAppClientRuntimeDDFile();
        ddFile.setXMLValidation(false);
        try (InputStream is = new ByteArrayInputStream(DESCRIPTOR.getBytes(UTF_8))) {
            ddFile.read(descriptor, is);
        }

        assertThat("Elements after java-web-start-access are processed", resourceRef.getJndiName().toString(),
            equalTo("jdbc/__default"));
        List<GlassFishLogRecord> logs = logCollector.getAll();
        assertThat("Logs: " + logs, logs, hasSize(1));
        assertThat(logs.get(0).getMessage(), containsString("java-web-start-access element is ignored"));
    }
}

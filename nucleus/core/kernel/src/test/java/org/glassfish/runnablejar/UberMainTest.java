/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.runnablejar;

import com.sun.enterprise.config.serverbeans.AppTenants;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationExtension;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resources;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.embeddable.GlassFishException;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.HttpRedirect;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.PortUnification;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.ProtocolChainInstanceHandler;
import org.glassfish.grizzly.config.dom.Ssl;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.types.Property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasLength;

/**
 *
 * @author Ondro Mihalyi
 */
public class UberMainTest {

    @Test
    public void getInfoAfterStartup() throws GlassFishException {
        String info = new InfoPrinter().getInfoAfterStartup(List.of(app("/app1"), app("application")),
            List.of(listener(8080, false), listener(8181, true)));
        System.out.println(info);
        String[] lines = info.split("\n");
        assertThat("Number of lines", lines, arrayWithSize(6));
        assertThatLinesAreEquallyLong(lines);
    }

    @Test
    public void getInfoAfterStartup_noListeners() throws GlassFishException {
        String info = new InfoPrinter().getInfoAfterStartup(List.of(app("/app1"), app("application")),
            List.of());
        System.out.println(info);
        String[] lines = info.split("\n");
        assertThat("Number of lines", lines, arrayWithSize(6));
        assertThatLinesAreEquallyLong(lines);
    }


    @Test
    public void getInfoAfterStartup_noApps() {
        String info = new InfoPrinter().getInfoAfterStartup(List.of(),
            List.of(listener(8080, false), listener(8181, true)));
        System.out.println(info);
        String[] lines = info.split("\n");
        assertThat("Number of lines", lines, arrayWithSize(5));
        assertThatLinesAreEquallyLong(lines);
    }

    @Test
    public void getInfoAfterStartup_noApps_noListeners() {
        String info = new InfoPrinter().getInfoAfterStartup(List.of(), List.of());
        System.out.println(info);
        String[] lines = info.split("\n");
        assertThat("Number of lines", lines, arrayWithSize(5));
        assertThatLinesAreEquallyLong(lines);
    }

    private void assertThatLinesAreEquallyLong(String[] lines) {
        int lineLength = lines[0].length();
        for (int i = 1; i < lines.length; i++) {
            assertThat("All lines should as long as the first line. Line with number " + i + " broke the rule.",
                lines[i], hasLength(lineLength));
        }
    }

    private MockListener listener(int port, boolean secure) {
        return new MockListener(port, secure);
    }

    private MockApplication app(String app) {
        return new MockApplication(app);
    }

    private class MockApplication implements Application {

        private final String name;

        MockApplication(String name) {
            this.name = name;
        }

        @Override
        public String getContextRoot() {
            return name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEnabled() {
            return Boolean.TRUE.toString();
        }

        @Override
        public void setContextRoot(String contextRoot) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLocation(String location) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getObjectType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setObjectType(String objectType) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setEnabled(String enabled) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLibraries() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLibraries(String libraries) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getAvailabilityEnabled() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAvailabilityEnabled(String availabilityEnabled) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getAsyncReplication() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAsyncReplication(String asyncReplication) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDirectoryDeployed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDirectoryDeployed(String directoryDeployed) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDescription(String description) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDeploymentOrder() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDeploymentOrder(String deploymentOrder) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Module> getModule() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Engine> getEngine() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Resources getResources() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setResources(Resources resources) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public AppTenants getAppTenants() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAppTenants(AppTenants appTenants) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<ApplicationExtension> getExtensions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Property> getProperty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setName(String name) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property addProperty(Property property) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property lookupProperty(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property removeProperty(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property removeProperty(Property property) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class MockListener implements NetworkListener {

        int port;
        boolean secure;

        MockListener(int port, boolean secure) {
            this.port = port;
            this.secure = secure;
        }

        @Override
        public String getEnabled() {
            return Boolean.TRUE.toString();
        }

        @Override
        public String getPort() {
            return String.valueOf(port);
        }

        @Override
        public Protocol findProtocol() {
            return new Protocol() {
                @Override
                public String getSecurityEnabled() {
                    return Boolean.toString(secure);
                }

                @Override
                public Http getHttp() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setHttp(Http http) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public String getName() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setName(String name) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public PortUnification getPortUnification() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setPortUnification(PortUnification portUnification) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public HttpRedirect getHttpRedirect() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setHttpRedirect(HttpRedirect httpRedirect) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public ProtocolChainInstanceHandler getProtocolChainInstanceHandler() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setProtocolChainInstanceHandler(ProtocolChainInstanceHandler protocolChainHandler) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setSecurityEnabled(String securityEnabled) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Ssl getSsl() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setSsl(Ssl ssl) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public List<Property> getProperty() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Property addProperty(Property property) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Property lookupProperty(String name) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Property removeProperty(String name) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Property removeProperty(Property property) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }

        @Override
        public String getAddress() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAddress(String address) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setEnabled(String enabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getJkConfigurationFile() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJkConfigurationFile(String configFile) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getJkEnabled() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJkEnabled(String jkEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setType(String type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setPort(String port) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setProtocol(String protocol) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getThreadPool() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setThreadPool(String threadPool) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getTransport() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setTransport(String transport) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Property> getProperty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property addProperty(Property property) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property lookupProperty(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property removeProperty(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property removeProperty(Property property) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

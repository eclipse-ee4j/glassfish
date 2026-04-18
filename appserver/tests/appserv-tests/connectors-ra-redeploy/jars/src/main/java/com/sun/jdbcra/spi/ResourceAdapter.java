/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdbcra.spi;

import jakarta.resource.NotSupportedException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.AuthenticationMechanism;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.Connector;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.TransactionSupport;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

import javax.transaction.xa.XAResource;

/**
 * {@link jakarta.resource.spi.ResourceAdapter} implementation for Generic JDBC Connector.
 *
 * @author        Evani Sai Surya Kiran
 */
@Connector(
    description = "Resource adapter wrapping Datasource implementation of driver",
    displayName = "DataSource Resource Adapter",
    vendorName = "Sun Microsystems",
    eisType = "Database",
    version = "1.0",
    licenseRequired = false,
    transactionSupport = TransactionSupport.TransactionSupportLevel.LocalTransaction,
    authMechanisms = {
        @AuthenticationMechanism(
            authMechanism = "BasicPassword",
            credentialInterface = AuthenticationMechanism.CredentialInterface.PasswordCredential
        )
    },
    reauthenticationSupport = false
)
public class ResourceAdapter implements jakarta.resource.spi.ResourceAdapter {

    private static int loadVersion() {
        ClassLoader cl = ResourceAdapter.class.getClassLoader();
        try {
            Enumeration<URL> urls = cl.getResources("META-INF/MANIFEST.MF");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStream stream = url.openStream()) {
                    Manifest manifest = new Manifest(stream);
                    String value = manifest.getMainAttributes().getValue("ra-redeploy-version");
                    if (value != null) {
                        return Integer.parseInt(value);
                    }
                }
            }
            return -1;
        } catch (IOException e) {
            throw new Error("Cannot detect version value!", e);
        }
    }

    public static final int VERSION = loadVersion();

    public String raProp;

    /**
     * Empty method implementation for endpointActivation
     * which just throws <code>NotSupportedException</code>
     *
     * @param        mef        <code>MessageEndpointFactory</code>
     * @param        as        <code>ActivationSpec</code>
     * @throws        NotSupportedException
     */
    @Override
    public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }

    /**
     * Empty method implementation for endpointDeactivation
     *
     * @param        mef        <code>MessageEndpointFactory</code>
     * @param        as        <code>ActivationSpec</code>
     */
    @Override
    public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) {

    }

    /**
     * Empty method implementation for getXAResources
     * which just throws <code>NotSupportedException</code>
     *
     * @param        specs        <code>ActivationSpec</code> array
     * @throws        NotSupportedException
     */
    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }

    /**
     * Empty implementation of start method
     *
     * @param        ctx        <code>BootstrapContext</code>
     */
    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {

    }

    /**
     * Empty implementation of stop method
     */
    @Override
    public void stop() {

    }

    public void setRAProperty(String s) {
        raProp = s;
    }

    public String getRAProperty() {
        return raProp;
    }

}

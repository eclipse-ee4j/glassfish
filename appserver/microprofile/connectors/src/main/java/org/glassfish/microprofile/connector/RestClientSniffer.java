/*
 * Copyright (c) 2022 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.connector;

import java.lang.annotation.Annotation;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.deployment.GenericSniffer;
import org.jvnet.hk2.annotations.Service;

@Service
public class RestClientSniffer extends GenericSniffer {

    private static final String[] containers = { "org.glassfish.microprofile.config.ConfigContainer" };

    public RestClientSniffer() {
        super("mp-rest-client", null, null);
    }

    @Override
    public boolean handles(DeploymentContext context) {

        // Check if REST Client builder is used statically
        final var types = context.getTransientAppMetaData(Types.class.getName(), Types.class);
        final boolean mpRestClientUsed = types != null && types.getBy(RestClientBuilder.class.getName()) != null;

        return mpRestClientUsed || super.handles(context);
    }

    @Override
    public boolean handles(ReadableArchive location) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Annotation>[] getAnnotationTypes() {
        return new Class[]{
                RegisterRestClient.class
        };
    }

    @Override
    public String[] getContainersNames() {
        return containers;
    }

    @Override
    public boolean supportsArchiveType(ArchiveType archiveType) {
        return archiveType != null && archiveType.toString().equals("war");
    }
}

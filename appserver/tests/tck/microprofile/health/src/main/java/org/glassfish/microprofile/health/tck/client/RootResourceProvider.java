/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.health.tck.client;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.arquillian.container.test.impl.enricher.resource.URIResourceProvider;
import org.jboss.arquillian.test.api.ArquillianResource;

public class RootResourceProvider extends URIResourceProvider {

    @Override
    public Object lookup(ArquillianResource arquillianResource, Annotation... annotations) {
        Object lookup = super.lookup(arquillianResource, annotations);
        // remove the context path from the URI
        if (lookup instanceof URI) {
            URI uri = (URI) lookup;
            try {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), "", uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}

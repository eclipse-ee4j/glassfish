/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jboss.weld.tck.glassfish;

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Registers the exception transformer to properly identify deployment failures.
 *
 * @author J J Snyder (j.j.snyder@oracle.com)
 */
public class GlassFishExtension implements LoadableExtension {

    private static final String GLASSFISH_CLIENTUTILS_CLASS = "org.omnifaces.arquillian.container.glassfish.clientutils.GlassFishClientUtil";

    @Override
    public void register(ExtensionBuilder builder) {
        if (Validate.classExists(GLASSFISH_CLIENTUTILS_CLASS)) {
            builder.service(DeploymentExceptionTransformer.class, GlassFishDeploymentExceptionTransformer.class);
        }
    }

}

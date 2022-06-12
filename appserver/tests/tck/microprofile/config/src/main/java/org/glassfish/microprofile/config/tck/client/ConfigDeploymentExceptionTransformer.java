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
package org.glassfish.microprofile.config.tck.client;

import jakarta.enterprise.inject.spi.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;

/**
 * The common GlassFishClientCommon handler will always throw a GlassfishClientException,
 * with a message from the response. Deployment errors can safely be converted to the expected
 * exception type.
 */
public class ConfigDeploymentExceptionTransformer implements DeploymentExceptionTransformer {

    @Override
    public Throwable transform(Throwable throwable) {
        if (throwable != null && throwable.getMessage().contains("Error occurred during deployment")) {
            return new DeploymentException(throwable);
        }
        return throwable;
    }

}

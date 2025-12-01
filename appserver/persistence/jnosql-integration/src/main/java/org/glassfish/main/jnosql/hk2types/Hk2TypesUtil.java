/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.main.jnosql.hk2types;

import jakarta.enterprise.inject.spi.CDI;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.classmodel.reflect.ExtensibleType;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.main.jnosql.jakartapersistence.JakartaDataApplicationContext;

/**
 *
 * @author Ondro Mihalyi
 */
public final class Hk2TypesUtil {

    private Hk2TypesUtil() {
    }

    /* TODO: Optimization - cache all sets returned from methods into the ApplicationContext or deployment context */
    public static Types getTypes() {
        DeploymentContext deploymentContext = getDeploymentContext();
        if (deploymentContext != null) {
            // During deployment, we can retrieve Types from the context.
            // We can't access CDI context at this point as this class is not in a bean archive.
            return deploymentContext.getTransientAppMetaData(Types.class.getName(), Types.class);
        } else {
            // After deployment, we retrieve types stored in the app context defined in the CDI extension
            final JakartaDataApplicationContext appContext = CDI.current().select(JakartaDataApplicationContext.class).get();
            return appContext.getTypes();
        }
    }

    public static DeploymentContext getDeploymentContext() {
        final ServiceLocator locator = Globals.getDefaultHabitat();
        return locator != null
                ? locator
                        .getService(Deployment.class)
                        .getCurrentDeploymentContext()
                : null;
    }

    public static Class<?> typeModelToClass(ExtensibleType<?> type) throws RuntimeException {
        return classForName(type.getName());
    }

    public static Class<?> classForName(String name) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}

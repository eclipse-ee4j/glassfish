/*
 * Copyright (c) 2025, 2026 Contributors to the Eclipse Foundation
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
package org.glassfish.microprofile.jwt;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.interceptor.Interceptor;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;

import org.eclipse.microprofile.auth.LoginConfig;
import org.glassfish.api.security.jwt.MicroProfileJwtAuthenticationMechanism;
import org.omnifaces.jwt.cdi.CdiExtension;
import org.omnifaces.jwt.eesecurity.JWTAuthenticationMechanism;

/**
 *
 * @author Ondro Mihalyi
 */
public class MpJwtCdiExtension implements Extension {

    public void addQualifiedBean(@Observes @Priority(1000) AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        CdiExtension omnifacesExtension = beanManager.getExtension(CdiExtension.class);

        if (omnifacesExtension.isAddJWTAuthenticationMechanism()) {
            afterBeanDiscovery.addBean()
                    .scope(ApplicationScoped.class)
                    .beanClass(HttpAuthenticationMechanism.class)
                    .qualifiers(MicroProfileJwtAuthenticationMechanism.Literal.INSTANCE, Default.Literal.INSTANCE)
                    .priority(Interceptor.Priority.PLATFORM_BEFORE)
                    .alternative(true)
                    .types(Object.class, HttpAuthenticationMechanism.class, JWTAuthenticationMechanism.class)
                    .id("mechanism " + LoginConfig.class)
                    .createWith(e -> new JWTAuthenticationMechanism());
        }
    }
}

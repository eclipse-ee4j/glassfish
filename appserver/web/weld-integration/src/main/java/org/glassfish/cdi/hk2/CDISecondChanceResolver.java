/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.hk2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * @author jwells
 *
 */
@Singleton
public class CDISecondChanceResolver implements JustInTimeInjectionResolver {
    private final ServiceLocator locator;
    
    @Inject
    private CDISecondChanceResolver(ServiceLocator locator) {
        this.locator = locator;
    }
    
    /**
     * Gets the currently scoped BeanManager
     * @return The currently scoped BeanManager, or null if a bean manager cannot be found
     */
    private BeanManager getCurrentBeanManager() {
        try {
            Context jndiContext = new InitialContext();
            return (BeanManager) jndiContext.lookup("java:comp/BeanManager");
        }
        catch (NamingException ne) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.JustInTimeInjectionResolver#justInTimeResolution(org.glassfish.hk2.api.Injectee)
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public boolean justInTimeResolution(Injectee failedInjectionPoint) {
        Type requiredType = failedInjectionPoint.getRequiredType();
        
        Set<Annotation> setQualifiers = failedInjectionPoint.getRequiredQualifiers();
        
        Annotation qualifiers[] = setQualifiers.toArray(new Annotation[setQualifiers.size()]);
        
        BeanManager manager = getCurrentBeanManager();
        if (manager == null) return false;
        
        Set<Bean<?>> beans = manager.getBeans(requiredType, qualifiers);
        if (beans == null || beans.isEmpty()) {
            return false;
        }
        
        DynamicConfiguration config = ServiceLocatorUtilities.createDynamicConfiguration(locator);
        for (Bean<?> bean : beans) {
            // Add a bean to the service locator
            CDIHK2Descriptor<Object> descriptor = new CDIHK2Descriptor<Object>(manager, (Bean<Object>) bean, requiredType);
            config.addActiveDescriptor(descriptor);
        }
        
        config.commit();
        
        return true;
    }

}

/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;

import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

/**
 * Wrapper for application references to entity manager factories.
 * A new instance of this class will be created for each injected
 * EntityManagerFactory reference or each lookup of an EntityManagerFactory
 * reference within the component jndi environment.
 *
 * @author Kenneth Saks
 */
public class EntityManagerFactoryWrapper implements EntityManagerFactory, Serializable {

    private static final long serialVersionUID = 4719469920862714502L;

    private final String unitName;

    private transient InvocationManager invMgr;

    private transient EntityManagerFactory entityManagerFactory;

    private transient ComponentEnvManager compEnvMgr;

    public EntityManagerFactoryWrapper(String unitName, InvocationManager invMgr,
                                       ComponentEnvManager compEnvMgr) {

        this.unitName = unitName;
        this.invMgr = invMgr;
        this.compEnvMgr = compEnvMgr;
    }

    private EntityManagerFactory getDelegate() {

        if( entityManagerFactory == null ) {
            entityManagerFactory = lookupEntityManagerFactory(invMgr, compEnvMgr, unitName);

            if( entityManagerFactory == null ) {
                throw new IllegalStateException
                    ("Unable to retrieve EntityManagerFactory for unitName "
                     + unitName);
            }
        }

        return entityManagerFactory;
    }

    @Override
    public EntityManager createEntityManager() {
        return getDelegate().createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return getDelegate().createEntityManager(map);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return getDelegate().createEntityManager(synchronizationType);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return getDelegate().createEntityManager(synchronizationType, map);
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        getDelegate().addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return getDelegate().unwrap(cls);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getDelegate().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return getDelegate().getMetamodel();
    }

    @Override
    public Map<java.lang.String, java.lang.Object> getProperties() {
        return getDelegate().getProperties();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public Cache getCache() {
        return getDelegate().getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return getDelegate().getPersistenceUnitUtil();
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        getDelegate().addNamedEntityGraph(graphName, entityGraph);
    }


    /**
     * Lookup physical EntityManagerFactory based on current component
     * invocation.
     * @param invMgr invocationmanager
     * @param emfUnitName unit name of entity manager factory or null if not
     *                    specified.
     * @return EntityManagerFactory or null if no matching factory could be
     *         found.
     **/
    static EntityManagerFactory lookupEntityManagerFactory(InvocationManager invMgr,
            ComponentEnvManager compEnvMgr, String emfUnitName)
    {

        ComponentInvocation inv  =  invMgr.getCurrentInvocation();

        EntityManagerFactory emf = null;

        if( inv != null ) {
            Object desc = compEnvMgr.getCurrentJndiNameEnvironment();
            if (desc != null) {
                emf = lookupEntityManagerFactory(inv.getInvocationType(),
                    emfUnitName, desc);
            }
        }

        return emf;
    }

    public static EntityManagerFactory lookupEntityManagerFactory(ComponentInvocationType invType,
            String emfUnitName, Object descriptor) {

        Application app = null;
        BundleDescriptor module = null;

        EntityManagerFactory emf = null;

        switch (invType) {

        case EJB_INVOCATION:

            if (descriptor instanceof EjbDescriptor) {
                EjbDescriptor ejbDesc = (EjbDescriptor) descriptor;
                module = (BundleDescriptor) ejbDesc.getEjbBundleDescriptor().getModuleDescriptor().getDescriptor();
                app = module.getApplication();
                break;
            }
            // EJB invocation in web bundle?
            // fall through into web case...

        case SERVLET_INVOCATION:

            module = (WebBundleDescriptor) descriptor;
            app = module.getApplication();

            break;

        case APP_CLIENT_INVOCATION:

            module = (ApplicationClientDescriptor) descriptor;
            app = module.getApplication();

            break;

        default:

            break;
        }

        // First check module-level for a match.
        if (module != null) {
            if (emfUnitName != null) {
                emf = module.getEntityManagerFactory(emfUnitName);
            } else {
                Set<EntityManagerFactory> emFactories = module
                        .getEntityManagerFactories();
                if (emFactories.size() == 1) {
                    emf = emFactories.iterator().next();
                }
            }
        }

        // If we're in an .ear and no module-level persistence unit was
        // found, check for an application-level match.
        if ((app != null) && (emf == null)) {
            if (emfUnitName != null) {

                emf = app.getEntityManagerFactory(emfUnitName, module);

            } else {
                Set<EntityManagerFactory> emFactories = app
                        .getEntityManagerFactories();
                if (emFactories.size() == 1) {
                    emf = emFactories.iterator().next();
                }
            }
        }

        return emf;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        //Initialize the transients that were passed at ctor.
        ServiceLocator defaultServiceLocator = Globals.getDefaultHabitat();
        invMgr        = defaultServiceLocator.getService(InvocationManager.class);
        compEnvMgr    = defaultServiceLocator.getService(ComponentEnvManager.class);
    }

}

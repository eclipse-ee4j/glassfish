/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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



package oracle.toplink.essentials.testing.framework;

import oracle.toplink.essentials.internal.databaseaccess.Platform;
import oracle.toplink.essentials.threetier.ServerSession;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class holds creates an EntityManagerFactory and provides
 * convenience methods to access TopLink specific artifacts.  The
 * EntityManagerFactory is created by referencing the PersistenceUnit
 * "default", which is associated to the JavaDB bundled with the
 * application server.
 */
public abstract class TestNGTestCase {

    private Map propertiesMap = null;

    private EntityManagerFactory emf = null;

    public void clearCache() {
         try {
            getServerSession().getIdentityMapAccessor().initializeAllIdentityMaps();
         } catch (Exception ex) {
            throw new  RuntimeException("An exception occurred trying clear the cache.", ex);
        }
    }

    /**
     * Create an entity manager.
     */
    public EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public Map getDatabaseProperties(){
        if (propertiesMap == null){
            propertiesMap = new HashMap();
             propertiesMap.put("toplink.session.name", "default");
        }
        return propertiesMap;
    }

    public ServerSession getServerSession(){
        return ((oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl)createEntityManager()).getServerSession();
    }

    public EntityManagerFactory getEntityManagerFactory(){
        if (emf == null){
            emf = Persistence.createEntityManagerFactory("default", getDatabaseProperties());
        }
        return emf;
    }

    public Platform getDbPlatform() {
        return getServerSession().getDatasourcePlatform();
    }

    @Configuration(beforeTestClass = true)
    public void setUp(){
        // Tables are created by Java2DB. Please see the option in persistence.xml!
    }

    @Configuration(afterTestClass = true)
    public void tearDown() {
        clearCache();
    }

}

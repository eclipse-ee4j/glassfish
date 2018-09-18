/*
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

package com.sun.enterprise.deployment;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This descriptor represents contents for one persistence.xml file.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitsDescriptor extends RootDeploymentDescriptor {

    /** the parent descriptor that contains this descriptor */
    private RootDeploymentDescriptor parent;

    /**
     * The relative path from the parent {@link RootDeploymentDescriptor}
     * to the root of this persistence unit. e.g.
     * WEB-INF/classes -- if persistence.xml is in WEB-INF/classes/META-INF,
     * WEB-INF/lib/foo.jar -- if persistence.xml is in WEB-INF/lib/foo.jar/META-INF,
     * "" -- if persistence.xml is in some ejb.jar, or
     * util/bar.jar -- if persistence.xml is in a.ear/util/bar.jar
     */
    private String puRoot;

    List<PersistenceUnitDescriptor> persistenceUnitDescriptors =
            new ArrayList<PersistenceUnitDescriptor>();

    private static final String JPA_1_0 = "1.0";

    public PersistenceUnitsDescriptor() {
    }

    public RootDeploymentDescriptor getParent() {
        return parent;
    }

    public void setParent(RootDeploymentDescriptor parent) {
        this.parent = parent;
    }

    public String getPuRoot() {
        return puRoot;
    }

    public void setPuRoot(String puRoot) {
        this.puRoot = puRoot;
    }

    public String getDefaultSpecVersion() {
        return JPA_1_0;
    }

    public String getModuleID() {
        throw new RuntimeException();
    }

    public ArchiveType getModuleType() {
        throw new RuntimeException();
    }

    public ClassLoader getClassLoader() {
        return parent.getClassLoader();
    }

    public boolean isApplication() {
        return false;
    }

    /**
     * This method does not do any validation like checking for unique names
     * of PersistenceUnits.
     * @param pud the PersistenceUnitDescriptor to be added.
     */
    public void addPersistenceUnitDescriptor(PersistenceUnitDescriptor pud){
        persistenceUnitDescriptors.add(pud);
        pud.setParent(this);
    }

    /**
     * @return an unmodifiable list.
     */
    public List<PersistenceUnitDescriptor> getPersistenceUnitDescriptors() {
        return Collections.unmodifiableList(persistenceUnitDescriptors);
    }

    /**
     * This is a utility method which calculates the absolute path of the
     * root of a PU. Absolute path is not the path with regards to
     * root of file system. It is the path from the root of the Java EE
     * application this persistence unit belongs to.
     * Like {@link #getPuRoot()} returned path always uses '/' as path separator.
     * @return the absolute path of the root of this persistence unit
     * @see #getPuRoot()
     */
    public String getAbsolutePuRoot() {
        RootDeploymentDescriptor rootDD = getParent();
        if(rootDD.isApplication()){
            return getPuRoot();
        } else {
            ModuleDescriptor module = BundleDescriptor.class.cast(rootDD).
                    getModuleDescriptor();
            if(module.isStandalone()) {
                return getPuRoot();
            } else {
                final String moduleLocation = module.getArchiveUri();
                return moduleLocation + '/' + getPuRoot(); // see we always '/'
            }
        }
    }


    public boolean isEmpty() {
        return persistenceUnitDescriptors.isEmpty();
    }
}

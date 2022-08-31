/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

/**
 * Holds namespace-to-package mapping information from a
 * "non-exhaustive" jaxrpc mapping file.
 *
 * @author Kenneth Saks
 */
public class JaxrpcMappingDescriptor extends RootDeploymentDescriptor {

    private static final long serialVersionUID = 1L;
    private final Map<String, String> packageToNamespaceUriMap = new HashMap<>();
    private final Map<String, String> namespaceUriToPackageMap = new HashMap<>();

    private boolean simpleMapping = true;

    @Override
    public String getModuleID() {
        return "";
    }


    @Override
    public String getDefaultSpecVersion() {
        return "1.0";
    }


    @Override
    public boolean isEmpty() {
        return namespaceUriToPackageMap.isEmpty();
    }


    @Override
    public ArchiveType getModuleType() {
        return null;
    }


    @Override
    public ClassLoader getClassLoader() {
        return null;
    }


    @Override
    public boolean isApplication() {
        return false;
    }


    public void setIsSimpleMapping(boolean flag) {
        simpleMapping = flag;
    }


    /**
     * @return true if only mapping info only contains package->namespace mapping.
     */
    public boolean isSimpleMapping() {
        return simpleMapping;
    }


    public void addMapping(String javaPackage, String namespaceUri) {
        packageToNamespaceUriMap.put(javaPackage, namespaceUri);
        namespaceUriToPackageMap.put(namespaceUri, javaPackage);
    }


    /**
     * @return Collection of Mapping elements
     */
    public Collection<Mapping> getMappings() {
        Collection<Mapping> mappings = new HashSet<>();
        Iterator<Entry<String, String>> nIter = namespaceUriToPackageMap.entrySet().iterator();
        while (nIter.hasNext()) {
            Entry<String, String> entry = nIter.next();
            String namespaceUri = entry.getKey();
            String javaPackage = entry.getValue();
            Mapping mapping = new Mapping(namespaceUri, javaPackage);
            mappings.add(mapping);
        }
        return mappings;
    }

    public static class Mapping {
        private final String namespaceUri;
        private final String javaPackage;

        public Mapping(String namespace, String thePackage) {
            namespaceUri = namespace;
            javaPackage  = thePackage;
        }

        public String getNamespaceUri() {
            return namespaceUri;
        }

        public String getPackage() {
            return javaPackage;
        }
    }
}

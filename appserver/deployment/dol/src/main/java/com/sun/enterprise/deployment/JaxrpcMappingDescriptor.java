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

import org.glassfish.deployment.common.Descriptor;

import java.util.*;

/**
 * Holds namespace-to-package mapping information from a
 * "non-exhaustive" jaxrpc mapping file.
 *
 * @author Kenneth Saks
 */

public class JaxrpcMappingDescriptor extends Descriptor {

    private Map packageToNamespaceUriMap = new HashMap();
    private Map namespaceUriToPackageMap = new HashMap();

    private boolean simpleMapping = true;

    public JaxrpcMappingDescriptor() {
    }

    public void setSpecVersion(String version) {
        // ignore
    }

    public void setIsSimpleMapping(boolean flag) {
        simpleMapping = flag;
    }

    /**
     * @return true if only mapping info only contains package->namespace
     * mapping.
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
    public Collection getMappings() {
        Collection mappings = new HashSet();
        Iterator nIter = namespaceUriToPackageMap.entrySet().iterator();
        while(nIter.hasNext()){
            Map.Entry entry = (Map.Entry) nIter.next();
            String namespaceUri = (String) entry.getKey();
            String javaPackage = (String) entry.getValue();
            Mapping mapping = new Mapping(namespaceUri, javaPackage);
            mappings.add(mapping);
        }
        return mappings;
    }

    public static class Mapping {
        private String namespaceUri;
        private String javaPackage;

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

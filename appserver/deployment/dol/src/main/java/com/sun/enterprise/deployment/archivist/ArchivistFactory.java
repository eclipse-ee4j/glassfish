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

package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.BundleDescriptor;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * This factory class is responsible for creating Archivists
 *
 * @author Jerome Dochez
 */
@Service
@Singleton
public class ArchivistFactory {

    public final static String ARCHIVE_TYPE = "archiveType";
    public final static String EXTENSION_ARCHIVE_TYPE = "extensionArchiveType";

    @Inject
    private ServiceLocator habitat;

    public <A extends Archivist<D>, D extends BundleDescriptor> A getArchivist(String archiveType, ClassLoader cl) {
        A result = getArchivist(archiveType);
        if (result != null) {
            result.setClassLoader(cl);
        }
        return result;
    }


    public <A extends Archivist<D>, D extends BundleDescriptor> A getArchivist(String archiveType) {
        ActiveDescriptor<A> best = (ActiveDescriptor<A>) habitat
            .getBestDescriptor(new ArchivistFilter(archiveType, ARCHIVE_TYPE, Archivist.class));
        if (best == null) {
            return null;
        }
        return habitat.getServiceHandle(best).getService();
    }


    public <A extends Archivist<D>, D extends BundleDescriptor> A getArchivist(ArchiveType moduleType) {
        return (A) getArchivist(String.valueOf(moduleType));
    }


    public List<ExtensionsArchivist<?>> getExtensionsArchivists(Collection<Sniffer> sniffers, ArchiveType moduleType) {
        Set<String> containerTypes = new HashSet<>();
        for (Sniffer sniffer : sniffers) {
            containerTypes.add(sniffer.getModuleType());
        }
        List<ExtensionsArchivist<?>> archivists = new ArrayList<>();
        for (String containerType : containerTypes) {
            List<ActiveDescriptor<?>> descriptors = habitat
                .getDescriptors(new ArchivistFilter(containerType, EXTENSION_ARCHIVE_TYPE, ExtensionsArchivist.class));

            for (ActiveDescriptor<?> item : descriptors) {
                ActiveDescriptor<ExtensionsArchivist<?>> descriptor = (ActiveDescriptor<ExtensionsArchivist<?>>) item;
                ServiceHandle<ExtensionsArchivist<?>> handle = habitat.getServiceHandle(descriptor);
                ExtensionsArchivist<?> ea = handle.getService();
                if (ea.supportsModuleType(moduleType)) {
                    archivists.add(ea);
                }
            }
        }
        return archivists;
    }

    private static class ArchivistFilter implements IndexedFilter {

        private final String archiveType;
        private final String metadataKey;
        private final Class<?> index;

        private ArchivistFilter(String archiveType, String metadataKey, Class<?> index) {
            this.archiveType = archiveType;
            this.metadataKey = metadataKey;
            this.index = index;
        }


        @Override
        public boolean matches(Descriptor d) {
            Map<String, List<String>> metadata = d.getMetadata();

            List<String> values = metadata.get(metadataKey);
            if (values == null) {
                return false;
            }

            return values.contains(archiveType);
        }


        @Override
        public String getAdvertisedContract() {
            return index.getName();
        }


        @Override
        public String getName() {
            return null;
        }

    }

}

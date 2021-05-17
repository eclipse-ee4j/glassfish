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

package com.sun.enterprise.v3.server;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.classmodel.reflect.*;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.internal.deployment.SnifferManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Provide convenience methods to deal with {@link Sniffer}s in the system.
 *
 * @author Kohsuke Kawaguchi
 */
@Service
public class SnifferManagerImpl implements SnifferManager {
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(SnifferManagerImpl.class);

    @Inject
    protected ServiceLocator habitat;

    /**
     * Returns all the presently registered sniffers
     *
     * @return Collection (possibly empty but never null) of Sniffer
     */
    public Collection<Sniffer> getSniffers() {
        // this is a little bit of a hack, sniffers are now ordered by their names
        // which is useful since connector is before ejb which is before web so if
        // a standalone module happens to implement the three types of components,
        // they will be naturally ordered correctly. We might want to revisit this
        // later and formalize the ordering of sniffers. The hard thing as usual
        // is that sniffers are highly pluggable so you never know which sniffers
        // set you are working with depending on the distribution
        List<Sniffer> sniffers = new ArrayList<Sniffer>();
        sniffers.addAll(habitat.<Sniffer>getAllServices(Sniffer.class));
        Collections.sort(sniffers, new Comparator<Sniffer>() {
            public int compare(Sniffer o1, Sniffer o2) {
                return o1.getModuleType().compareTo(o2.getModuleType());
            }
        });

        return sniffers;
    }

    /**
     * Check if there's any {@link Sniffer} installed at all.
     */
    public final boolean hasNoSniffers() {
        return getSniffers().isEmpty();
    }

    public Sniffer getSniffer(String appType) {
        assert appType!=null;
        for (Sniffer sniffer :  getSniffers()) {
            if (appType.equalsIgnoreCase(sniffer.getModuleType())) {
                return sniffer;
            }
        }
        return null;
    }

    /**
     * Returns a collection of sniffers that recognized some parts of the
     * passed archive as components their container handle.
     *
     * If no sniffer recognize the passed archive, an empty collection is
     * returned.
     *
     * @param context the deployment context
     * @return possibly empty collection of sniffers that handle the passed
     * archive.
     */
    public Collection<Sniffer> getSniffers(DeploymentContext context) {
        ReadableArchive archive = context.getSource();
        ArchiveHandler handler = context.getArchiveHandler();
        List<URI> uris = handler.getClassPathURIs(archive);
        Types types = context.getTransientAppMetaData(Types.class.getName(), Types.class);
        return getSniffers(context, uris, types);
    }

    public Collection<Sniffer> getSniffers(DeploymentContext context, List<URI> uris, Types types) {
        // it is important to keep an ordered sequence here to keep sniffers
        Collection<Sniffer> regularSniffers = getSniffers();

        // in their natural order.
        // scan for registered annotations and retrieve applicable sniffers
        List<Sniffer> appSniffers = this.getApplicableSniffers(context, uris, types, regularSniffers, true);

        // call handles method of the sniffers
        for (Sniffer sniffer : regularSniffers) {
            if ( !appSniffers.contains(sniffer) && sniffer.handles(context)) {
                appSniffers.add(sniffer);
            }
        }
        return appSniffers;
    }

    private <T extends Sniffer> List<T> getApplicableSniffers(DeploymentContext context, List<URI> uris, Types types, Collection<T> sniffers, boolean checkPath) {
        ArchiveType archiveType = habitat.getService(ArchiveType.class, context.getArchiveHandler().getArchiveType());

        if (sniffers==null || sniffers.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<T>();
        for (T sniffer : sniffers) {
            if (archiveType != null &&
                !sniffer.supportsArchiveType(archiveType)) {
                continue;
            }
            String[] annotationNames = sniffer.getAnnotationNames(context);
            if (annotationNames==null) continue;
            for (String annotationName : annotationNames)  {
              if (types != null) {
                Type type = types.getBy(annotationName);
                if (type instanceof AnnotationType) {
                    Collection<AnnotatedElement> elements = ((AnnotationType) type).allAnnotatedTypes();
                    for (AnnotatedElement element : elements) {
                        if (checkPath) {
                            Type t = (element instanceof Member?((Member) element).getDeclaringType():(Type) element);
                            if (t.wasDefinedIn(uris)) {
                                result.add(sniffer);
                                break;
                            }
                        } else {
                            result.add(sniffer);
                            break;
                        }
                    }
                }
              }
            }
        }
        return result;
    }

    public void validateSniffers(Collection<? extends Sniffer> snifferCol, DeploymentContext context) {
        for (Sniffer sniffer : snifferCol) {
            String[] incompatTypes = sniffer.getIncompatibleSnifferTypes();
            if (incompatTypes==null)
                return;
            for (String type : incompatTypes) {
                for (Sniffer sniffer2 : snifferCol) {
                    if (sniffer2.getModuleType().equals(type)) {
                        throw new IllegalArgumentException(
                            localStrings.getLocalString(
                            "invalidarchivepackaging",
                            "Invalid archive packaging {2}",
                            sniffer.getModuleType(), type,
                            context.getSourceDir().getPath()));
                    }
                }
            }
        }
    }
}

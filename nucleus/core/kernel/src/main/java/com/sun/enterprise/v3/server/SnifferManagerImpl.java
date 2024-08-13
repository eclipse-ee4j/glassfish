/*
 * Copyright (c) 2022 Contributors to Eclipse Foundation.
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

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.classmodel.reflect.AnnotatedElement;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.Member;
import org.glassfish.hk2.classmodel.reflect.Parameter;
import org.glassfish.hk2.classmodel.reflect.ParameterizedType;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.deployment.SnifferManager;
import org.jvnet.hk2.annotations.Service;

/**
 * Provide convenience methods to deal with {@link Sniffer}s in the system.
 *
 * @author Kohsuke Kawaguchi
 */
@Service
public class SnifferManagerImpl implements SnifferManager {
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(SnifferManagerImpl.class);

    @Inject
    protected ServiceLocator serviceLocator;

    /**
     * Returns all the presently registered sniffers
     *
     * @return Collection (possibly empty but never null) of Sniffer
     */
    @Override
    public Collection<Sniffer> getSniffers() {
        // This is a little bit of a hack, sniffers are now ordered by their names
        // which is useful since connector is before ejb which is before web so if
        // a standalone module happens to implement the three types of components,
        // they will be naturally ordered correctly. We might want to revisit this
        // later and formalize the ordering of sniffers.

        // The hard thing as usual
        // is that sniffers are highly pluggable so you never know which sniffers
        // set you are working with depending on the distribution

        List<Sniffer> sniffers = new ArrayList<>(serviceLocator.getAllServices(Sniffer.class));
        Collections.sort(sniffers, (o1, o2) -> o1.getModuleType().compareTo(o2.getModuleType()));

        return sniffers;
    }

    /**
     * Check if there's any {@link Sniffer} installed at all.
     */
    @Override
    public final boolean hasNoSniffers() {
        return getSniffers().isEmpty();
    }

    @Override
    public Sniffer getSniffer(String appType) {
        assert appType != null;
        for (Sniffer sniffer : getSniffers()) {
            if (appType.equalsIgnoreCase(sniffer.getModuleType())) {
                return sniffer;
            }
        }

        return null;
    }

    /**
     * Returns a collection of sniffers that recognized some parts of the passed archive as components their container
     * handle.
     *
     * If no sniffer recognize the passed archive, an empty collection is returned.
     *
     * @param context the deployment context
     * @return possibly empty collection of sniffers that handle the passed archive.
     */
    @Override
    public Collection<Sniffer> getSniffers(DeploymentContext context) {
        return getSniffers(
            context,
            context.getArchiveHandler().getClassPathURIs(context.getSource()),
            context.getTransientAppMetaData(Types.class.getName(), Types.class));
    }

    @Override
    public Collection<Sniffer> getSniffers(DeploymentContext context, List<URI> uris, Types types) {
        // It is important to keep an ordered sequence here to keep sniffers
        Collection<Sniffer> regularSniffers = getSniffers();

        // In their natural order.
        // Scan for registered annotations and retrieve applicable sniffers
        List<Sniffer> appSniffers = getApplicableSniffers(context, uris, types, regularSniffers, true);

        // Call handles method of the sniffers
        for (Sniffer sniffer : regularSniffers) {
            if (!appSniffers.contains(sniffer) && sniffer.handles(context)) {
                appSniffers.add(sniffer);
            }
        }

        return appSniffers;
    }

    private <T extends Sniffer> List<T> getApplicableSniffers(DeploymentContext context, List<URI> uris, Types types, Collection<T> sniffers, boolean checkPath) {
        if (sniffers == null || sniffers.isEmpty() || types == null || types.getAllTypes().isEmpty()) {
            return new ArrayList<>();
        }

        ArchiveType archiveType = serviceLocator.getService(ArchiveType.class, context.getArchiveHandler().getArchiveType());
        List<T> applicableSniffers = new ArrayList<>();

        for (T sniffer : sniffers) {
            if (archiveType != null && !sniffer.supportsArchiveType(archiveType)) {
                continue;
            }

            String[] annotationNames = sniffer.getAnnotationNames(context);
            if (annotationNames == null) {
                continue;
            }

            for (String annotationName : annotationNames) {
                Type type = types.getBy(annotationName);
                if (type instanceof AnnotationType) {
                    Collection<AnnotatedElement> elements = ((AnnotationType) type).allAnnotatedTypes();
                    for (AnnotatedElement element : elements) {
                        if (checkPath) {
                            final Type t = getDeclaringType(element);
                            if (t != null && t.wasDefinedIn(uris)) {
                                applicableSniffers.add(sniffer);
                                break;
                            }
                        } else {
                            applicableSniffers.add(sniffer);
                            break;
                        }
                    }
                }
            }
        }

        return applicableSniffers;
    }

    public void validateSniffers(Collection<? extends Sniffer> sniffers, DeploymentContext context) {
        for (Sniffer sniffer : sniffers) {
            String[] incompatibleTypes = sniffer.getIncompatibleSnifferTypes();
            if (incompatibleTypes == null) {
                return;
            }

            for (String type : incompatibleTypes) {
                for (Sniffer sniffer2 : sniffers) {
                    if (sniffer2.getModuleType().equals(type)) {
                        throw new IllegalArgumentException(localStrings.getLocalString("invalidarchivepackaging",
                                "Invalid archive packaging {2}", sniffer.getModuleType(), type, context.getSourceDir().getPath()));
                    }
                }
            }
        }
    }

    /**
     * @param element an annotated element in the archive
     * @return the type of the class containing the element
     */
    private static Type getDeclaringType(AnnotatedElement element) {
        if (element instanceof Type) {
            return (Type) element;
        }
        if (element instanceof Member) {
            return ((Member) element).getDeclaringType();
        }
        if (element instanceof Parameter) {
            return getDeclaringType(((Parameter) element).getMethod());
        }
        if (element instanceof ParameterizedType) {
            return ((ParameterizedType) element).getType();
        }
        throw new IllegalStateException("Unable to recognise declaring type: " + element.getName());
    }
}

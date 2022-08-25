/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.annotation.impl;

import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import com.sun.enterprise.deployment.deploy.shared.MultiReadableArchive;
import com.sun.enterprise.deployment.util.DOLUtils;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.apf.impl.AnnotationUtils;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.classmodel.reflect.AnnotatedElement;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.Member;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.internal.deployment.AnnotationTypesProvider;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * Implementation of the Scanner interface for AppClient
 * <p>
 * This scanner overrides process(ReadableArchive...) so that when used in the
 * ACC it will work correctly with InputJarArchive readable archives, not just the
 * expanded directory archives during deployment on the server.
 *
 * @author Shing Wai Chan
 * @author tjquinn
 */
@Service(name = "car")
@PerLookup
public class AppClientScanner extends ModuleScanner<ApplicationClientDescriptor> {

    private static final Logger LOG = DOLUtils.getDefaultLogger();

    @Inject
    @Named("EJB")
    @Optional
    protected AnnotationTypesProvider ejbProvider;

    @Override
    public void process(ReadableArchive archive, ApplicationClientDescriptor bundleDesc, ClassLoader classLoader, Parser parser) throws IOException {
        setParser(parser);
        doProcess(archive, bundleDesc, classLoader);
        completeProcess(bundleDesc, archive);
        calculateResults(bundleDesc);
    }


    @Override
    protected void process(File archiveFile, ApplicationClientDescriptor bundleDesc, ClassLoader classLoader)
        throws IOException {
        /*
         * This variant should not be invoked, but we need to have it here to
         * satisfy the interface contract. For this app client scanner, its
         * own process(ReadableArchive...) method will be invoked rather than
         * the one implemented at ModuleScanner. This is to allow the app
         * client one to support InputJarArchives as well as FileArchives. This
         * is important because the ACC deals with JARs directly rather than
         * expanding them into directories.
         */
        throw new UnsupportedOperationException("Not supported.");
    }


    /**
     * This scanner will scan the given main class for annotation processing.
     * The archiveFile and libJarFiles correspond to classpath.
     *
     * @param archiveFile
     * @param descriptor
     * @param classLoader
     */
    private void doProcess(ReadableArchive archive, ApplicationClientDescriptor descriptor, ClassLoader classLoader)
        throws IOException {
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Processing file={0}, descriptor={1}, classLoader={2}",
                new Object[] {archiveFile, descriptor, classLoader});
        }

        // always add main class
        String mainClassName = descriptor.getMainClassName();
        addScanClassName(mainClassName);

        // add callback handle if it exist in appclient-client.xml
        String callbackHandler = descriptor.getCallbackHandler();
        if (callbackHandler != null && !callbackHandler.trim().equals("")) {
            addScanClassName(descriptor.getCallbackHandler());
        }

        if (archive instanceof FileArchive) {
            addScanDirectory(new File(archive.getURI()));
        } else if (archive instanceof InputJarArchive) {
            /*
             * This is during deployment, so use the faster code path using
             * the File object.
             */
            URI uriToAdd = archive.getURI();
            addScanJar(scanJar(uriToAdd));
        } else if (archive instanceof MultiReadableArchive) {
            /*
             * During app client launches, scan the developer's archive
             * which is in slot #1, not the facade archive which is in
             * slot #0. Also, use URIs instead of File objects because
             * during Java Web Start launches we don't have access to
             * File objects.
             */
            addScanURI(scanURI(((MultiReadableArchive) archive).getURI(1)));
        }

        this.classLoader = classLoader;
        this.archiveFile = null;
    }


    private File scanJar(URI uriToAdd) {
        return new File(uriToAdd);
    }


    private URI scanURI(URI uriToAdd) throws IOException {
        if (uriToAdd.getScheme().equals("jar")) {
            try {
                uriToAdd = new URI("file", uriToAdd.getSchemeSpecificPart(), null);
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
        return uriToAdd;
    }


    /**
     * Overriding to handle the case where EJB class is mistakenly packaged inside an appclient jar.
     * Instead of throwing an error which might raise backward compatiability issues, a cleaner way
     * is to just skip the annotation processing for them.
     */
    @Override
    protected void calculateResults(ApplicationClientDescriptor bundleDesc) {
        super.calculateResults(bundleDesc);

        Class<?>[] ejbAnnotations;
        if (ejbProvider != null) {
            ejbAnnotations = ejbProvider.getAnnotationTypes();
        } else {
            ejbAnnotations = new Class[] {jakarta.ejb.Stateful.class, jakarta.ejb.Stateless.class,
                jakarta.ejb.MessageDriven.class, jakarta.ejb.Singleton.class};
        }
        Set<String> toBeRemoved = new HashSet<>();
        ParsingContext context = classParser.getContext();
        for (Class<?> ejbAnnotation : ejbAnnotations) {
            Type type = context.getTypes().getBy(ejbAnnotation.getName());
            if (type != null && type instanceof AnnotationType) {
                AnnotationType at = (AnnotationType) type;
                for (AnnotatedElement ae : at.allAnnotatedTypes()) {
                    Type t = (ae instanceof Member ? ((Member) ae).getDeclaringType() : (Type) ae);
                    if (t.wasDefinedIn(scannedURI)) {
                        toBeRemoved.add(t.getName());
                    }
                }
            }
        }

        for (String element : toBeRemoved) {
            entries.remove(element);
        }
    }
}

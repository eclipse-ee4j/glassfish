/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.annotation.impl;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.annotation.introspection.DefaultAnnotationScanner;
import com.sun.enterprise.deployment.util.DOLUtils;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import org.glassfish.apf.impl.JavaEEScanner;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.hk2.classmodel.reflect.AnnotatedElement;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.Member;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * This is an abstract class of the Scanner interface for J2EE module.
 *
 * @author Shing Wai Chan
 */
// FIXME: broken hierarchy, inheritance
public abstract class ModuleScanner<T extends Descriptor> extends JavaEEScanner<T> {

    private static final Logger LOG = DOLUtils.getDefaultLogger();

    @LogMessageInfo(
        message = "Exception caught during annotation scanning.",
        cause = "An exception was caught that indicates that the annotation is incorrect.",
        action = "Correct the annotation.",
        level = "SEVERE")
    private static final String ANNOTATION_SCANNING_EXCEPTION = "AS-DEPLOYMENT-00005";

    @LogMessageInfo(message = "Adding {0} since {1} is annotated with {2}.", level = "CONFIG")
    private static final String ANNOTATION_ADDED = "AS-DEPLOYMENT-00006";

    @LogMessageInfo(message = "Adding {0} since it is implementing {1}.", level = "CONFIG")
    private static final String INTERFACE_ADDED = "AS-DEPLOYMENT-00007";

    @LogMessageInfo(
        message = "Inconsistent type definition.  {0} is neither an annotation nor an interface.",
        cause = "The annotation is incorrect.",
        action = "Correct the annotation.",
        level = "SEVERE")
    private static final String INCORRECT_ANNOTATION = "AS-DEPLOYMENT-00008";

    @LogMessageInfo(
        message = "The exception {0} occurred while examining the jar at file path:  {1}.",
        level = "WARNING")
    private static final String JAR_EXCEPTION = "AS-DEPLOYMENT-00009";

    @LogMessageInfo(
        message = "No classloader can be found to use",
        cause = "The archive being processed is not correct.",
        action = "Examine the archive to determine what is incorrect.",
        level = "SEVERE")
    private static final String NO_CLASSLOADER = "AS-DEPLOYMENT-00010";

    @LogMessageInfo(message = "Error in annotation processing:", level = "WARNING")
    private static final String ANNOTATION_ERROR = "AS-DEPLOYMENT-00011";

    @LogMessageInfo(message = "Cannot load {0}  reason : {1}.", level = "WARNING")
    private static final String CLASSLOADING_ERROR = "AS-DEPLOYMENT-00012";

    @LogMessageInfo(message = "An exception was caught during library jar processing:  {0}.", level = "WARNING")
    private static final String LIBRARY_JAR_ERROR = "AS-DEPLOYMENT-00013";

    private static ExecutorService executorService;

    @Inject
    private DefaultAnnotationScanner defaultScanner;

    protected File archiveFile;
    protected ClassLoader classLoader;
    protected Parser classParser;

    protected Set<URI> scannedURI = new HashSet<>();
    protected Set<String> entries = new HashSet<>();

    private boolean needScanAnnotation;

    /**
     * This scanner will scan the archiveFile for annotation processing.
     * @param archiveFile the archive to process
     * @param descriptor existing bundle descriptor to add to
     * @param classLoader classloader to load archive classes with.
     */
    public void process(ReadableArchive archiveFile, T descriptor, ClassLoader classLoader, Parser parser)
        throws IOException {
        File file = new File(archiveFile.getURI());
        setParser(parser);
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Processing file={0}, descriptor={1}, classLoader={2}",
                new Object[] {archiveFile, descriptor, classLoader});
        }
        process(file, descriptor, classLoader);
        completeProcess(descriptor, archiveFile);
        calculateResults(descriptor);
    }



    protected void setParser(Parser parser) {
        if (parser == null) {
            // if the passed in parser is null, it means no annotation scanning
            // has been done yet, we need to construct a new parser
            // and do the annotation scanning here
            ParsingContext pc = new ParsingContext.Builder().executorService(getExecutorService()).build();
            parser = new Parser(pc);
            needScanAnnotation = true;
        }
        classParser = parser;
    }

    /**
     * Performs all additional work after the "process" method has finished.
     * <p>
     * This is a separate method from "process" so that the app client scanner can invoke
     * it from its overriding process method. All post-processing logic needs to be
     * collected in this one place.
     *
     * @param descriptor
     * @param archive
     * @throws IOException
     */
    protected void completeProcess(T descriptor, ReadableArchive archive) throws IOException {
        addLibraryJars(descriptor, archive);
    }


    protected void calculateResults(T descriptor) {
        try {
            classParser.awaitTermination();
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, ANNOTATION_SCANNING_EXCEPTION, e);
            return;
        }
        ParsingContext context = classParser.getContext();
        final boolean isFullAttribute;
        if (descriptor instanceof BundleDescriptor) {
            isFullAttribute = ((BundleDescriptor) descriptor).isFullAttribute();
        } else {
            isFullAttribute = false;
        }
        Set<String> annotationsToProcess = defaultScanner.getAnnotations(isFullAttribute);
        for (String annotation : annotationsToProcess) {
            Type type = context.getTypes().getBy(annotation);

            // we never found anyone using that type
            if (type == null) {
                continue;
            }

            // is it an annotation
            if (type instanceof AnnotationType) {
                AnnotationType at = (AnnotationType) type;
                for (AnnotatedElement ae : at.allAnnotatedTypes()) {
                    // if it is a member (field, method), let's retrieve the declaring type
                    // otherwise, use the annotated type directly.
                    Type t = (ae instanceof Member ? ((Member) ae).getDeclaringType() : (Type) ae);
                    if (t.wasDefinedIn(scannedURI)) {
                        if (LOG.isLoggable(Level.CONFIG)) {
                            LOG.log(Level.CONFIG, ANNOTATION_ADDED, new Object[] {t.getName(), ae.getName(), at.getName()});
                        }
                        entries.add(t.getName());
                    }
                }

            } else if (type instanceof InterfaceModel) {
                // or is it an interface ?
                InterfaceModel im = (InterfaceModel) type;
                for (ClassModel cm : im.allImplementations()) {
                    if (LOG.isLoggable(Level.CONFIG)) {
                        LOG.log(Level.CONFIG, INTERFACE_ADDED, new Object[] {cm.getName(), im.getName()});
                    }
                    entries.add(cm.getName());
                }
            } else {
                LOG.log(Level.SEVERE, INCORRECT_ANNOTATION, annotation);
            }
        }
        LOG.log(Level.FINE, "Done with results");
    }


    /**
     * This add extra className to be scanned.
     *
     * @param className
     */
    protected void addScanClassName(String className) {
        if (className != null && !className.isEmpty()) {
            entries.add(className);
        }
    }


    /**
     * This add all classes in given jarFile to be scanned.
     *
     * @param jarFile
     */
    protected void addScanJar(File jarFile) throws IOException {
        try {
            /*
             * An app might refer to a non-existent JAR in its Class-Path. Java
             * SE accepts that silently, and so will GlassFish.
             */
            if (!jarFile.exists()) {
                return;
            }
            scannedURI.add(jarFile.toURI());
            if (needScanAnnotation) {
                classParser.parse(jarFile, null);
            }
        } catch (ZipException ze) {
            LOG.log(Level.WARNING, JAR_EXCEPTION, new Object[] {ze.getMessage(), jarFile.getPath()});
        }
    }


    /**
     * This add all classes in given jarFile to be scanned.
     *
     * @param jarURI
     */
    protected void addScanURI(final URI jarURI) throws IOException {
        addScanJar(new File(jarURI));
    }


    /**
     * This will include all class in directory to be scanned.
     * param directory
     */
    protected void addScanDirectory(File directory) throws IOException {
        scannedURI.add(directory.toURI());
        if (needScanAnnotation) {
            classParser.parse(directory, null);
        }
    }


    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }


    @Override
    public Set<Class<?>> getElements() {
        Set<Class<?>> elements = new HashSet<>();
        if (getClassLoader() == null) {
            LOG.log(Level.SEVERE, NO_CLASSLOADER);
            return elements;
        }

        for (String className : entries) {
            LOG.log(Level.FINEST, "Getting {0}", className);
            try {
                elements.add(classLoader.loadClass(className));
            } catch (NoClassDefFoundError err) {
                LOG.log(Level.WARNING, ANNOTATION_ERROR, err);
            } catch (ClassNotFoundException cnfe) {
                LogRecord lr = new LogRecord(Level.WARNING, CLASSLOADING_ERROR);
                Object args[] = {className, cnfe.getMessage()};
                lr.setParameters(args);
                lr.setThrown(cnfe);
                LOG.log(lr);
            }
        }
        return elements;
    }


    protected void addLibraryJars(T bundleDesc, ReadableArchive moduleArchive) {
        List<URI> libraryURIs = new ArrayList<>();
        try {
            if (bundleDesc instanceof BundleDescriptor) {
                libraryURIs = DOLUtils.getLibraryJarURIs((BundleDescriptor) bundleDesc, moduleArchive);
            }

            for (URI uri : libraryURIs) {
                File libFile = new File(uri);
                if (libFile.isFile()) {
                    addScanJar(libFile);
                } else if (libFile.isDirectory()) {
                    addScanDirectory(libFile);
                }
            }
        } catch (Exception ex) {
            // we log a warning and proceed for any problems in
            // adding library jars to the scan list
            LOG.log(Level.WARNING, LIBRARY_JAR_ERROR, ex.getMessage());
        }
    }


    protected synchronized ExecutorService getExecutorService() {
        if (executorService != null) {
            return executorService;
        }
        Runtime runtime = Runtime.getRuntime();
        int nrOfProcessors = runtime.availableProcessors();
        executorService = Executors.newFixedThreadPool(nrOfProcessors, r -> {
            Thread t = new Thread(r);
            t.setName("dol-jar-scanner");
            t.setDaemon(true);
            t.setContextClassLoader(getClass().getClassLoader());
            return t;
        });
        return executorService;
    }
}

/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.glassfish.common.util.GlassfishUrlClassLoader;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.glassfish.internal.api.DelegatingClassLoader.ClassFinder;
import org.jvnet.hk2.annotations.Service;

/**
 * This class is responsible for constructing class loader that has visibility
 * to deploy time libraries ({@code --libraries} and {@code EXTENSION_LIST} of
 * {@code MANIFEST.MF} for an application.
 * <p>
 * It is different from common class loader in a sense that the libraries that are part of
 * common class loader are shared by all applications, whereas this class loader adds
 * a scope to a library.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
@Singleton
public class AppLibClassLoaderServiceImpl {

    /**
     * Class finders' registry.
     * <p>
     * If multiple applications or modules refer to the same libraries,
     * share this libraries by reusing the same class loaders.
     */
    private final Map<Library, ClassFinder> classFinderRegistry = new HashMap<>();

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private CommonClassLoaderServiceImpl commonClassLoaderService;


    /**
     * Returns the application libraries class loader.
     * <p>
     * This class loader has visibility to deploy time libraries for an application.
     * <p>
     * This class loader is different from common class loader in a sense that the libraries
     * that are part of common class loader are shared by all applications, whereas this
     * class loader adds a scope to a libraries.
     *
     * @param application the application for which class loader is created
     * @param libURIs the URIs from which to load classes and resources
     * @return the class loader that has visibility to appropriate application specific libraries
     * @throws MalformedURLException if some error occurred while constructing the URL
     * @see org.glassfish.internal.api.ClassLoaderHierarchy#getAppLibClassLoader(String, List)
     */
    public ClassLoader getAppLibClassLoader(String application, List<URI> libURIs) throws MalformedURLException {
        ClassLoaderHierarchy classLoaderHierarchy = serviceLocator.getService(ClassLoaderHierarchy.class);
        DelegatingClassLoader connectorClassLoader = classLoaderHierarchy.getConnectorClassLoader(application);

        if (libURIs == null || libURIs.isEmpty()) {
            // Optimization: when there are no libraries, why create an empty
            // class loader in the hierarchy? Instead, return the parent.
            return connectorClassLoader;
        }

        final ClassLoader commonClassLoader = commonClassLoaderService.getCommonClassLoader();
        PrivilegedAction<DelegatingClassLoader> action = () -> new DelegatingClassLoader(commonClassLoader);
        DelegatingClassLoader appLibClassLoader = AccessController.doPrivileged(action);

        // Order of class finders is important here.
        // Connector's class finders should be added before libraries' class finders
        // as the delegation hierarchy is appCL->app-libsCL->connectorCL->commonCL->API-CL
        // since we are merging connector and applib class finders to be at same level,
        // connector class finders need to be before applib class finders in the horizontal
        // search path
        for (ClassFinder classFinder : connectorClassLoader.getDelegates()) {
            appLibClassLoader.addDelegate(classFinder);
        }
        addDelegates(libURIs, appLibClassLoader);

        return appLibClassLoader;
    }

    /**
     * Returns the application libraries class loader.
     * <p>
     * This class loader has visibility to deploy time libraries for an application.
     * <p>
     * This class loader adds a scope to a libraries and will be used only by the
     * connector class loader.
     *
     * @param libURIs the URIs from which to load classes and resources
     * @return the class loader that has visibility to appropriate application specific libraries
     * @throws MalformedURLException if some error occurred while constructing the URL
     * @see org.glassfish.internal.api.ClassLoaderHierarchy#getAppLibClassFinder(List)
     */
    public ClassFinder getAppLibClassFinder(Collection<URI> libURIs) throws MalformedURLException {
        final ClassLoader commonClassLoader = commonClassLoaderService.getCommonClassLoader();
        DelegatingClassFinder appLibClassFinder = AccessController.doPrivileged(
                (PrivilegedAction<DelegatingClassFinder>) () -> new DelegatingClassFinder(commonClassLoader));
        addDelegates(libURIs, appLibClassFinder);
        return appLibClassFinder;
    }

    /**
     * Adds application libraries class loaders to the delegating class loader.
     */
    private void addDelegates(Collection<URI> libURIs, DelegatingClassLoader holder) throws MalformedURLException {
        ClassLoader commonClassLoader = commonClassLoaderService.getCommonClassLoader();
        for (URI libURI : libURIs) {
            synchronized (this) {
                Library library = new Library(libURI);
                ClassFinder classFinder = classFinderRegistry.get(library);
                if (classFinder == null) {
                    classFinder = new URLClassFinder(library.getURI().toURL(), commonClassLoader);
                    classFinderRegistry.put(library, classFinder);
                } else {
                    library.close();
                }
                holder.addDelegate(classFinder);
            }
        }
    }

    /**
     * This class loader is used to load classes and resources from the URL
     * referring to a JAR file.
     */
    private static class URLClassFinder extends GlassfishUrlClassLoader implements ClassFinder {

        URLClassFinder(URL url, ClassLoader parent) {
            super(new URL[] {url}, parent);
        }

        /**
         * Finds the class with the specified binary name.
         *
         * @param name the binary name of the class
         * @return the resulting {@code Class} object
         * @throws ClassNotFoundException if class could not be found
         */
        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                return c;
            }
            return super.findClass(name);
        }

        /**
         * Returns the loaded class with the given binary name.
         *
         * @param name the binary name of the class
         * @return the {@code Class} object, or {@code null} if the class has not been loaded
         */
        @Override
        public Class<?> findExistingClass(String name) {
            return findLoadedClass(name);
        }
    }

    /**
     * This class loader has a list of class loaders called as delegates
     * that it uses to find resources and classes.
     */
    private static class DelegatingClassFinder extends DelegatingClassLoader implements ClassFinder {

        DelegatingClassFinder(ClassLoader parent) {
            super(parent);
        }

        /**
         * Always returns {@code null} because delegating class loader will
         * never be a defining class loader.
         */
        @Override
        public Class<?> findExistingClass(String name) {
            return null;
        }

        /**
         * Finds the resource with the given name.
         *
         * @param name the resource name
         * @return a URL object for reading the resource, or {@code null} if the resource
         * could not be found
         */
        @Override
        public URL findResource(String name) {
            return super.findResource(name);
        }

        /**
         * Returns an enumeration of URL objects representing all resources with the given name.
         *
         * @param name the resource name
         * @return an enumeration of URL objects for the resources, or empty enumeration
         * if the resources not found
         * @throws IOException if an I/O error occurs
         */
        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            return super.findResources(name);
        }
    }

    /**
     * Represents a deployment time library.
     */
    private static class Library {

        /**
         * Represents a method to read the attributes of an open file.
         */
        private static final Method readAttributesMethod;

        /**
         * Represents a field to access to the native descriptor for an open file.
         */
        private static final Field nativeDescriptorField;

        static {
            Method method;
            Field field;
            try {
                if (OS.isWindows()) {
                    Class<?> attributesClass = Class.forName("sun.nio.fs.WindowsFileAttributes");
                    method = attributesClass.getDeclaredMethod("readAttributes", Long.class);
                    field = FileDescriptor.class.getDeclaredField("handle");
                } else {
                    Class<?> attributesClass = Class.forName("sun.nio.fs.UnixFileAttributes");
                    method = attributesClass.getDeclaredMethod("get", Integer.TYPE);
                    field = FileDescriptor.class.getDeclaredField("fd");
                }
                method.setAccessible(true);
                field.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                method = null;
                field = null;
            }
            readAttributesMethod = method;
            nativeDescriptorField = field;
        }

        /**
         * Original library URI.
         */
        private final URI originalSource;

        /**
         * Library basic file attributes.
         */
        private final BasicFileAttributes attributes;

        /**
         * Library file input stream. Used to create library snapshot.
         */
        private FileInputStream fileInputStream;

        /**
         * Actual library URI.
         */
        private URI source;

        Library(URI libURI) {
            this.originalSource = libURI;

            try {
                this.fileInputStream = new FileInputStream(new File(libURI));
            } catch (IOException e) {
                // do nothing
            }

            BasicFileAttributes attributes = null;
            // Try to read file attributes of an open library file
            if (fileInputStream != null) {
                Object nativeDescriptor = getNativeDescriptor(fileInputStream);
                if (nativeDescriptor != null) {
                    attributes = readAttributes(nativeDescriptor);
                }
            }
            // Fallback to the standard NIO.2 method
            if (attributes == null) {
                attributes = readAttributes(libURI);
            }
            this.attributes = attributes;
        }

        /**
         * Gets a {@code file:} URI that represents this library.
         * <p>
         * Creates a library snapshot in the default temporary-file directory.
         *
         * @return the deployment time library URI
         */
        public URI getURI() {
            if (source == null) {
                File snapshot = createSnapshot();
                if (snapshot != null) {
                    // Snapshot successfully created.
                    // Use it URI as a library source.
                    source = snapshot.toURI();
                } else {
                    // Snapshot creation failed.
                    // Use original library URI.
                    source = originalSource;
                }
            }
            return source;
        }

        /**
         * Closes associated file input stream and releases any system resources
         * associated with the stream.
         */
        public void close() {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }

        /**
         * Tests this library for equality with the given object.
         * <p>
         * If the given object is not a {@code Library}, then this method returns {@code false}.
         *
         * @param obj the object to which this object to be compared
         * @return {@code true} if, and only if, the given object is a {@code Library} that is
         * identical to this {@code Library}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Library)) {
                return false;
            }

            Library library = (Library) obj;
            if (!originalSource.equals(library.originalSource)) {
                return false;
            }
            if (attributes == null || library.attributes == null) {
                return attributes == library.attributes;
            }
            if (attributes.size() != library.attributes.size()) {
                return false;
            }
            if (!Objects.equals(attributes.lastModifiedTime(), library.attributes.lastModifiedTime())) {
                return false;
            }
            return Objects.equals(attributes.fileKey(), library.attributes.fileKey());
        }

        /**
         * Computes a hash code for this library.
         *
         * @return the hash code value for this library
         */
        @Override
        public int hashCode() {
            int hash = originalSource.hashCode();
            if (attributes != null) {
                hash = 31 * hash + Long.hashCode(attributes.size());
                hash = 31 + hash + Objects.hashCode(attributes.lastModifiedTime());
                hash = 31 + hash + Objects.hashCode(attributes.fileKey());
            }
            return hash;
        }

        /**
         * Gets OS-dependent native file descriptor for an open file related to
         * this application library {@code fileInputStream}.
         *
         * @param fileInputStream the application library file input stream
         * @return the native file descriptor
         */
        private Object getNativeDescriptor(FileInputStream fileInputStream) {
            Object nativeDescriptor = null;
            if (nativeDescriptorField != null) {
                try {
                    FileDescriptor fileDescriptor = fileInputStream.getFD();
                    if (fileDescriptor.valid()) {
                        nativeDescriptor = nativeDescriptorField.get(fileDescriptor);
                    }
                } catch (IllegalAccessException | IOException e) {
                    // do nothing
                }
            }
            return nativeDescriptor;
        }

        /**
         * Gets the {@link BasicFileAttributes} for an open application library.
         *
         * @param nativeDescriptor the open library native descriptor
         * @return the file attributes or {@code null} if an error occurs
         */
        private BasicFileAttributes readAttributes(Object nativeDescriptor) {
            BasicFileAttributes attributes = null;
            if (readAttributesMethod != null) {
                try {
                    attributes = (BasicFileAttributes) readAttributesMethod.invoke(null, nativeDescriptor);
                } catch (ReflectiveOperationException e) {
                    // do nothing
                }
            }
            return attributes;
        }

        /**
         * Reads a file's basic attributes as a bulk operation.
         *
         * @param libURI an absolute, hierarchical URI with scheme equal to {@code file:},
         * a non-empty path component, and undefined authority, query and fragment components
         * @return the file attributes or {@code null} if an error occurs
         */
        private BasicFileAttributes readAttributes(URI libURI) {
            try {
                return Files.readAttributes(Path.of(libURI), BasicFileAttributes.class);
            } catch (IOException e) {
                return null;
            }
        }

        /**
         * Creates a library snapshot in the default temporary-file directory.
         * <p>
         * Closes a file input stream associated with library file.
         *
         * @return an abstract pathname denoting a newly-created snapshot
         */
        private File createSnapshot() {
            File snapshot = null;
            try {
                snapshot = File.createTempFile("applib", ".jar");
                if (!copy(fileInputStream, snapshot)) {
                    FileUtils.copy(new File(originalSource), snapshot);
                }
                snapshot.deleteOnExit();
            } catch (IOException e) {
                FileUtils.deleteFileMaybe(snapshot);
            } finally {
                fileInputStream = null;
            }
            return snapshot;
        }

        /**
         * Copies all bytes from an input stream to a file.
         * <p>
         * Closes input stream after completion.
         *
         * @param inputStream the input stream to read from
         * @param file the target output file. If the file already exists, it will be overwritten
         * @return {@code true} if all bytes copied, {@code false} otherwise
         */
        private boolean copy(InputStream inputStream, File file) {
            if (inputStream == null) {
                return false;
            }
            try (inputStream) {
                FileUtils.copy(inputStream, file);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}

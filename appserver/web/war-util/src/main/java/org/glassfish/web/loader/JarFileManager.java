/*
 * Copyright (c) 2023, 2025 Eclipse Foundation and/or its affiliates.
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

package org.glassfish.web.loader;

import com.sun.enterprise.util.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.Runtime.version;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.zip.ZipFile.OPEN_READ;
import static org.glassfish.web.loader.LogFacade.ILLEGAL_JAR_PATH;
import static org.glassfish.web.loader.LogFacade.UNABLE_TO_CREATE;
import static org.glassfish.web.loader.LogFacade.VALIDATION_ERROR_JAR_PATH;
import static org.glassfish.web.loader.LogFacade.getString;

/**
 * @author David Matejcek
 */
class JarFileManager implements Closeable {

    private static final int SECONDS_TO_CLOSE_UNUSED_JARS = Integer
        .getInteger("org.glassfish.web.loader.unusedJars.secondsToClose", 60);
    private static final int SECONDS_TO_CHECK_UNUSED_JARS = Integer
        .getInteger("org.glassfish.web.loader.unusedJars.secondsToRunCheck", 15);

    private static final Logger LOG = System.getLogger(JarFileManager.class.getName());

    /** The list of JARs, in the order they should be searched for locally loaded classes or resources. */
    private final List<JarResource> files = new ArrayList<>();

    private final ScheduledExecutorService scheduler = newScheduledThreadPool(1, new JarFileManagerThreadFactory());
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private volatile long lastJarFileAccess;
    private ScheduledFuture<?> unusedJarsCheck;

    private volatile boolean resourcesExtracted;


    void addJarFile(File file) {
        writeLock.lock();
        try {
            files.add(new JarResource(file));
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * @return array of {@link JarFile}s. Note that they can be closed at any time. Can be null.
     */
    JarFile[] getJarFiles() {
        if (!isJarsOpen() && !openJARs()) {
            return null;
        }
        readLock.lock();
        try {
            lastJarFileAccess = System.currentTimeMillis();
            return files.stream().map(r -> r.jarFile).toArray(JarFile[]::new);
        } finally {
            readLock.unlock();
        }
    }


    File[] getJarRealFiles() {
        readLock.lock();
        try {
            return files.stream().map(r -> r.file).toArray(File[]::new);
        } finally {
            readLock.unlock();
        }
    }


    /**
     * Attempts to load the requested resource from this classloader's JAR files.
     *
     * @return The requested resource, or null if not found
     */
    ResourceEntry findResource(String name, String path, File loaderDir, boolean antiJARLocking) {
        LOG.log(TRACE, "findResource(name={0}, path={1}, loaderDir={2}, antiJARLocking={3})",
            name, path, loaderDir, antiJARLocking);
        if (!isJarsOpen() && !openJARs()) {
            return null;
        }
        readLock.lock();
        try {
            lastJarFileAccess = System.currentTimeMillis();
            for (JarResource jarResource : files) {
                final JarFile jarFile = jarResource.jarFile;
                final JarEntry jarEntry = jarFile.getJarEntry(path);
                if (jarEntry == null) {
                    continue;
                }
                final ResourceEntry entry = createResourceEntry(name, jarResource.file, jarFile, jarEntry, path);
                if (entry == null) {
                    // We have found the entry, but we cannot load it.
                    return null;
                }

                // Extract resources contained in JAR to the workdir
                if (antiJARLocking && !path.endsWith(".class")) {
                    final File resourceFile = new File(loaderDir, jarEntry.getName());
                    if (!resourcesExtracted && !resourceFile.exists()) {
                        extractResources(loaderDir, path);
                    }
                }
                return entry;
            }
        } finally {
            readLock.unlock();
        }
        return null;
    }


    void extractResources(File loaderDir, String canonicalLoaderDir) {
        LOG.log(DEBUG, "extractResources(loaderDir={0}, canonicalLoaderDir={1})", loaderDir, canonicalLoaderDir);
        if (resourcesExtracted) {
            return;
        }
        readLock.lock();
        try {
            for (JarResource jarResource : files) {
                extractResource(jarResource.jarFile, loaderDir, canonicalLoaderDir);
            }
        } finally {
            readLock.unlock();
        }
        resourcesExtracted = true;
    }


    /**
     * Closes jar files. Can be executed multiple times.
     */
    void closeJarFiles() {
        LOG.log(DEBUG, "closeJarFiles()");
        writeLock.lock();
        try {
            lastJarFileAccess = 0L;
            closeJarFiles(files);
        } finally {
            // No need to interrupt, just cancel next executions
            if (this.unusedJarsCheck != null) {
                this.unusedJarsCheck.cancel(false);
            }
            writeLock.unlock();
        }
    }


    @Override
    public void close() throws IOException {
        closeJarFiles();
        scheduler.shutdown();
    }


    /**
     * @return true if opening succeeded
     */
    private boolean openJARs() {
        LOG.log(DEBUG, "openJARs()");
        writeLock.lock();
        try {
            if (isJarsOpen()) {
                return true;
            }
            lastJarFileAccess = System.currentTimeMillis();
            for (JarResource jarResource : files) {
                if (jarResource.jarFile != null) {
                    continue;
                }
                try {
                    jarResource.jarFile = new JarFile(jarResource.file, true, OPEN_READ, version());
                } catch (IOException e) {
                    LOG.log(DEBUG, "Failed to open JAR", e);
                    lastJarFileAccess = 0L;
                    closeJarFiles(files);
                    return false;
                }
            }
            LOG.log(DEBUG, "JAR files are open. If unused, will be closed after {0} s", SECONDS_TO_CLOSE_UNUSED_JARS);
            this.unusedJarsCheck = scheduler.scheduleAtFixedRate(this::closeJarFilesIfNotUsed, SECONDS_TO_CHECK_UNUSED_JARS,
                SECONDS_TO_CHECK_UNUSED_JARS, TimeUnit.SECONDS);
            return true;
        } finally {
            writeLock.unlock();
        }
    }


    private boolean isJarsOpen() {
        return lastJarFileAccess > 0L;
    }


    private ResourceEntry createResourceEntry(String name, File file, JarFile jarFile, JarEntry jarEntry, String entryPath) {
        final URL codeBase;
        try {
            codeBase = file.getCanonicalFile().toURI().toURL();
        } catch (IOException e) {
            LOG.log(WARNING, "Invalid file: " + file, e);
            return null;
        }

        final URL source;
        try {
            source = URI.create("jar:" + codeBase + "!/" + entryPath).toURL();
        } catch (MalformedURLException e) {
            LOG.log(WARNING, "Cannot create valid URL of file " + file + " and entry path " + entryPath, e);
            return null;
        }

        final ResourceEntry entry = new ResourceEntry(codeBase, source);
        try {
            entry.manifest = jarFile.getManifest();
        } catch (IOException e) {
            LOG.log(WARNING, "Failed to get manifest from " + jarFile.getName(), e);
            return null;
        }

        entry.lastModified = file.lastModified();
        final int contentLength = (int) jarEntry.getSize();
        try (InputStream binaryStream = jarFile.getInputStream(jarEntry)) {
            if (binaryStream != null) {
                entry.readEntryData(name, binaryStream, contentLength, jarEntry);
            }
        } catch (IOException e) {
            LOG.log(WARNING, "Failed to read entry data for " + name, e);
            return null;
        }

        return entry;
    }


    private static void extractResource(JarFile jarFile, File loaderDir, String pathPrefix) {
        LOG.log(DEBUG, "extractResource(jarFile={0}, loaderDir={1}, pathPrefix={2})", jarFile, loaderDir, pathPrefix);

        Iterator<JarEntry> jarEntries =
            jarFile.versionedStream()
                   .filter(
                       jarEntry -> !jarEntry.isDirectory() &&
                       !jarEntry.getName().endsWith(".class")).iterator();

        while (jarEntries.hasNext()) {
            JarEntry jarEntry = jarEntries.next();
            File resourceFile = new File(loaderDir, jarEntry.getName());
            try {
                if (!resourceFile.getCanonicalPath().startsWith(pathPrefix)) {
                    throw new IllegalArgumentException(getString(ILLEGAL_JAR_PATH, jarEntry.getName()));
                }
            } catch (IOException ioe) {
                throw new IllegalArgumentException(
                    getString(VALIDATION_ERROR_JAR_PATH, jarEntry.getName()), ioe);
            }
            if (!FileUtils.mkdirsMaybe(resourceFile.getParentFile())) {
                LOG.log(WARNING, UNABLE_TO_CREATE, resourceFile.getParentFile());
            }

            try (InputStream is = jarFile.getInputStream(jarEntry)) {
                FileUtils.copy(is, resourceFile, jarEntry.getSize());
            } catch (IOException e) {
                LOG.log(WARNING, "Failed to copy entry " + jarEntry, e);
            }
        }
    }

    private void closeJarFilesIfNotUsed() {
        if (!isJarsOpen()) {
            return;
        }

        final long unusedFor = (System.currentTimeMillis() - lastJarFileAccess) / 1000L;
        if (unusedFor <= SECONDS_TO_CLOSE_UNUSED_JARS) {
            return;
        }
        LOG.log(DEBUG, "Closing jar files, because they were not used for {0} s.", unusedFor);
        closeJarFiles();
    }


    private static void closeJarFiles(List<JarResource> files) {
        for (JarResource jarResource : files) {
            if (jarResource.jarFile == null) {
                continue;
            }

            final JarResource toClose = jarResource.copy();
            jarResource.jarFile = null;

            closeJarResource(toClose);
        }

        LOG.log(DEBUG, "JAR files were closed.");
    }

    private static void closeJarFileViaURL(final File file) {
        try {
            JarURLConnection jarURLConnection = openJarRoot(file.toPath());
            jarURLConnection.setUseCaches(true);

            jarURLConnection.getJarFile()
                            .close();
        } catch (IOException e) {
            LOG.log(WARNING, "Could not close the jarFile " + file, e);
        }
    }

    public static JarURLConnection openJarRoot(Path jarPath) throws IOException {
        // Make URL of the form: jar:file:/.../x.jar!/
        return (JarURLConnection)
            URI.create("jar:" + jarPath.toUri() + "!/")
               .toURL()
               .openConnection();
    }

    private static void closeJarResource(final JarResource jarResource) {
        // We need to close the Jar file via its URL, so we get hold
        // of the potentially cached version. Caching of jarFiles in the
        // OpenJDK is done by sun.net.www.protocol.jar.JarFileFactory
        // Of course this is an implementation detail, but in the Open JDK
        // it has worked like this for a long time and will likely stay for
        // some time to come.
        closeJarFileViaURL(jarResource.file);

        try {
            // Also close the jar file in the conventional way. Noop if
            // closeJarFileViaURL already did its work.
            jarResource.jarFile.close();
        } catch (IOException e) {
            LOG.log(WARNING, "Could not close the jarFile " + jarResource.jarFile, e);
        }
    }

    private static class JarResource {

        final File file;
        JarFile jarFile;

        JarResource(File file) {
            this.file = file;
        }

        JarResource copy() {
            JarResource copy = new JarResource(file);
            copy.jarFile = jarFile;
            return copy;
        }
    }

    private static class JarFileManagerThreadFactory implements ThreadFactory {
        private int counter = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "JarFileManager-" + counter++);
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        }
    }
}

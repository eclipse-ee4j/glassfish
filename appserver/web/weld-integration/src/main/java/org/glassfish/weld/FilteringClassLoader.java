/*
 * Copyright (c) 2022, 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Runtime.Version;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.enumeration;
import static java.util.stream.StreamSupport.stream;

/**
 * This classloader filters out extensions which are incompatible with current environment.
 *
 * @author David Matejcek
 */
class FilteringClassLoader extends ClassLoader {
    private static final Logger LOG = Logger.getLogger(FilteringClassLoader.class.getName());
    private static final String PATH_WELD_EXTENTSION = "META-INF/services/jakarta.enterprise.inject.spi.Extension";
    private static final String PATH_MANIFEST = "META-INF/MANIFEST.MF";
    private static final Pattern PATTERN_OSGI_EE_JAVA = Pattern.compile("\\(&\\(osgi.ee=JavaSE\\)\\(version=(.+)\\)\\)");
    private static final Version JDK_VERSION_MIN = Version.parse("21");
    private static final Version JDK_VERSION = Runtime.version();

    FilteringClassLoader(ClassLoader parent) {
        super(parent);
        LOG.log(Level.FINEST, "Parent: {0}", parent);
    }


    @Override
    public URL getResource(final String name) {
        LOG.log(Level.FINE, "getResource(): {0}", name);
        return getParent().getResource(name);
    }


    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        LOG.log(Level.FINE, "getResources(): {0}", name);
        final Enumeration<URL> resources = getParent().getResources(name);
        if (JDK_VERSION_MIN.compareTo(JDK_VERSION) < 0) {
            return resources;
        }
        if (!name.endsWith(PATH_WELD_EXTENTSION)) {
            return resources;
        }
        final Predicate<URL> filter = url -> {
            if (isCompatible(url)) {
                LOG.log(Level.FINEST, "Resource OK: {0}", url);
                return true;
            }
            LOG.log(Level.WARNING, "Removed extension {0} incompatible with the current JDK {1}.",
                new Object[] {url, JDK_VERSION});
            return false;
        };
        final Iterable<URL> iterable = () -> resources.asIterator();
        final List<URL> list = stream(iterable.spliterator(), false).filter(filter).collect(Collectors.toList());
        return enumeration(list);
    }


    /**
     * Resolves compatibility based on extension's manifest file.
     * <p>
     * If the extension's module doesn't declare any usable attribute to resolve compatibility,
     * we expect the extension is compatible.
     */
    private boolean isCompatible(final URL extensionUrl) {
        final URL manifestURL = toManifestURL(extensionUrl);
        final Manifest manifest = loadManifest(manifestURL);
        if (manifest == null) {
            return true;
        }
        final Version requiredMinVersion = getRequiredMinimalJavaVersion(manifest.getMainAttributes());
        if (requiredMinVersion == null) {
            return true;
        }
        return requiredMinVersion.compareTo(JDK_VERSION) <= 0;
    }


    private URL toManifestURL(final URL extensionUrl) {
        try {
            return new URL(extensionUrl.toExternalForm().replaceFirst(PATH_WELD_EXTENTSION, PATH_MANIFEST));
        } catch (final MalformedURLException e) {
            // ISE: because it should be always possible to use the constructor here.
            throw new IllegalStateException("Unprocessable URL: " + extensionUrl, e);
        }
    }


    /**
     * @param manifestURL
     * @return {@link Manifest} or null if there's no such file or cannot be read.
     */
    private Manifest loadManifest(final URL manifestURL) {
        try (InputStream stream = openStream(manifestURL)) {
            if (stream == null) {
                return null;
            }
            return new Manifest(stream);
        } catch (final IOException e) {
            LOG.log(Level.WARNING, "Could not read manifest at " + manifestURL, e);
            return null;
        }
    }


    private InputStream openStream(final URL manifestURL) throws IOException {
        try {
            return manifestURL.openStream();
        } catch (final FileNotFoundException e) {
            LOG.log(Level.FINEST, "The manifest is not present at " + manifestURL, e);
            return null;
        }
    }


    private Version getRequiredMinimalJavaVersion(final Attributes attributes) {
        final String requireCapability = attributes.getValue("Require-Capability");
        if (requireCapability == null) {
            return null;
        }
        final Matcher matcher = PATTERN_OSGI_EE_JAVA.matcher(requireCapability);
        if (matcher.find()) {
            return Version.parse(matcher.group(1));
        }
        return null;
    }
}
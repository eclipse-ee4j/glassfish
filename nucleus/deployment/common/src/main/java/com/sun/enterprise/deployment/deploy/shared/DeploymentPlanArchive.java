/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.deploy.shared;

import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

/**
 * This Archive offers an abstraction for jsr88
 * deployment plan as defined for the SJES Application
 * Server.
 *
 * @author Jerome Dochez
 */
public class DeploymentPlanArchive extends JarArchive implements ReadableArchive {

    private final static ServiceLocator locator = Globals.getDefaultHabitat();

    /** the deployment plan jar file */
    JarFile jarFile;

    /** original archive uri */
    URI uri;

    /** cached list of elements */
    Vector<String> elements;

    String subArchiveUri;


    /**
     * Creates a new instance of DeploymentPlanArchive
     * package private
     */
    public DeploymentPlanArchive() {
    }


    /**
     * Open an existing DeploymentPlan archive and return
     * a abstraction for reading from it.
     *
     * @param uri the path to the archive
     */
    @Override
    public void open(URI uri) throws IOException {
        this.uri = uri;
        File f = new File(uri);
        if (f.exists()) {
            jarFile = new JarFile(f);
        }
    }


    /**
     * Get the size of the archive
     *
     * @return tje the size of this archive or -1 on error
     */
    @Override
    public long getArchiveSize() throws NullPointerException, SecurityException {
        if (uri == null) {
            return -1;
        }
        File tmpFile = new File(uri);
        return tmpFile.length();
    }


    /**
     * Closes the current jar file
     */
    @Override
    public void close() throws IOException {
        if (jarFile != null) {
            jarFile.close();
            jarFile = null;
        }
    }


    /**
     * Closes the output jar file entry
     */
    public void closeEntry() throws java.io.IOException {
        // nothing to do
    }


    /**
     * Closes the output sub archive entry
     */
    public void closeEntry(ReadableArchive sub) throws java.io.IOException {
        // nothing to do...
    }


    /**
     * Deletes the underlying jar file
     */
    @Override
    public boolean delete() {
        File f = new File(uri);
        if (f.exists()) {
            return FileUtils.deleteFile(f);
        }
        return false;
    }


    @Override
    public JarEntry getJarEntry(String name) {
        if (jarFile != null) {
            return jarFile.getJarEntry(name);
        }
        return null;
    }


    @Override
    public Collection<String> getDirectories() throws IOException {
        return new Vector<>();
    }


    /**
     * @return an Enumeration of entries for this archive
     */
    @Override
    public Enumeration<String> entries() {
        // Deployment Plan are organized flatly,

        if (elements == null) {
            synchronized (this) {
                elements = new Vector<>();
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
                    JarEntry ze = e.nextElement();
                    if (!ze.isDirectory() && !ze.getName().startsWith("META-INF/")) {
                        elements.add(ze.getName());
                    }
                }
            }
        }

        List<String> entries = new ArrayList<>();
        for (String entryName : elements) {
            String mangledName = entryName;
            String prefix = "META-INF/";
            ArchiveType warType = locator.getService(ArchiveType.class, "war");
            boolean isWar = DeploymentUtils.isArchiveOfType(getParentArchive(), warType, locator);
            if (isWar) {
                prefix = "WEB-INF/classes/" + prefix;
            }
            if (entryName.equals("web.xml") ||
                entryName.indexOf("sun-web.xml")!=-1 ||
                entryName.indexOf("glassfish-web.xml")!=-1) {
                prefix = "WEB-INF/";
            } else if (entryName.indexOf("glassfish-resources.xml")!=-1 && isWar) {
                prefix = "WEB-INF/";
            } else if (entryName.indexOf("glassfish-services.xml")!=-1 && isWar) {
                prefix = "WEB-INF/";
            } else if (entryName.indexOf("faces-config.xml")!=-1 && isWar) {
                prefix = "WEB-INF/";
            } else if (entryName.indexOf("beans.xml")!=-1 && isWar) {
                prefix = "WEB-INF/";
            }
            if (subArchiveUri != null && entryName.startsWith(subArchiveUri)) {
                mangledName = mangledName.substring(subArchiveUri.length()+1);
            }
            if (entryName.endsWith(".dbschema")) {
                mangledName = mangledName.replaceAll("#", "/");
            } else {
                mangledName = prefix + mangledName;
            }

            if (subArchiveUri == null) {
                // top level archive
                if (entryName.indexOf(".jar.") != -1
                    || entryName.indexOf(".war.") != -1
                    || entryName.indexOf(".rar.") != -1) {

                    // this element is in a sub archive
                    continue;
                }
                entries.add(mangledName);
            } else {
                // this is a sub archive
                if (entryName.startsWith(subArchiveUri)) {
                    entries.add(mangledName);
                }
            }
        }
        return Collections.enumeration(entries);
    }

    /**
     * @return an Enumeration of entries not including entries
     * from the subarchives
     */
    public Enumeration entries(java.util.Enumeration embeddedArchives) {
        return entries();
    }

    /**
     * @return true if the underlying archive exists
     */
    @Override
    public boolean exists() {
        File f = new File(uri);
        return f.exists();
    }

    /**
     * @return a sub archive giving the name
     */
    @Override
    public ReadableArchive getSubArchive(String name) throws java.io.IOException {
        if (jarFile == null) {
            return null;
        }
        DeploymentPlanArchive dpArchive = new DeploymentPlanArchive();
        dpArchive.jarFile = new JarFile(new File(uri));
        try {
            if (uri != null) {
                dpArchive.uri = new URI("file", uri.getSchemeSpecificPart() + File.separator + name, null);
            }
        } catch (URISyntaxException e) {
            //
        }
        dpArchive.subArchiveUri = name;
        dpArchive.elements = elements;
        return dpArchive;
    }


    /**
     * Returns the existence of the given entry name
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.
     * @return the existence the given entry name.
     */
    @Override
    public boolean exists(String name) throws IOException {
        return getEntry(name) != null;
    }

    /**
     * @return an input stream giving its entry name
     */
    @Override
    public InputStream getEntry(String name) throws IOException {
        // we are just interested in the file name, not the
        // relative path
        if (name.endsWith(".dbschema")) {
            name = name.replaceAll("/", "#");
        } else {
            name = name.substring(name.lastIndexOf('/')+1);
        }

        if (subArchiveUri == null) {
            // we are at the "top level"
            return getElement(name);
        }
        // we are in a sub archive...
        // now let's mangle the name...
        String mangledName = subArchiveUri + "." + name;
        return getElement(mangledName);
    }

    /**
     * Returns the entry size for a given entry name or 0 if not known
     *
     * @param name the entry name
     * @return the entry size
     */
    @Override
    public long getEntrySize(String name) {
        if (elements.contains(name)) {
            ZipEntry je = jarFile.getEntry(name);
            return je.getSize();
        }
        return 0;
    }

    private InputStream getElement(String name) throws IOException {
        if (elements.contains(name)) {
            return jarFile.getInputStream(jarFile.getEntry(name));
        }
        return null;
    }

    /**
     * @return the manifest
     */
    @Override
    public java.util.jar.Manifest getManifest() throws java.io.IOException {
        // no manifest in DeploymentPlan
        return new Manifest();
    }

    /**
     * Returns the path used to create or open the underlying archive
     *
     * @return the path for this archive.
     */
    @Override
    public URI getURI() {
        return uri;
    }

    /**
     * rename the underlying archive
     */
    @Override
    public boolean renameTo(String name) {
        File f = new File(uri);
        File to  = new File(name);
        boolean result = FileUtils.renameFile(f, to);
        if (result) {
            uri = to.toURI();
        }
        return result;

    }

}

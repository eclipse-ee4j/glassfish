/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.io;

import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * A class for keeping track of the directories that a domain lives in and under.
 *
 * @author Byron Nevins
 * @since 3.1
 * Created: April 19, 2010
 */
public final class DomainDirs {
    /**
     * This convenience constructor is used when nothing is known about the
     * domain-dir
     */
    public DomainDirs() throws IOException {
        this(null, null);
    }

    /**
     * This constructor is used when both the name of the domain is known and
     * the domains-dir is known.
     */
    public DomainDirs(File domainsDir, String domainName) throws IOException {

        if (domainsDir == null) {
            domainsDir = getDefaultDomainsDir();
        }

        if (!domainsDir.isDirectory()) {
            throw new IOException(strings.get("Domain.badDomainsDir", domainsDir));
        }

        File domainDir;

        if (domainName != null) {
            domainDir = new File(domainsDir, domainName);
        }
        else {
            domainDir = getTheOneAndOnlyDir(domainsDir);
        }

        if (!domainDir.isDirectory()) {
            throw new IOException(strings.get("Domain.badDomainDir", domainDir));
        }

        dirs = new ServerDirs(domainDir);
    }

    /**
     * This constructor is used when the path of the domain-directory is known.
     * @param domainsDir
     * @param domainName
     * @throws IOException
     */
    public DomainDirs(File domainDir) throws IOException {
        dirs = new ServerDirs(domainDir);
    }

    /**
     * Create a DomainDir from the more general ServerDirs instance.
     * along with getServerDirs() you can convert freely back and forth
     *
     * @param aServerDir
     */
    public DomainDirs(ServerDirs sd) {
        dirs = sd;
    }

    @Override
    public String toString() {
        return dirs.toString();
    }

    public final String getDomainName() {
        return dirs.getServerName();
    }

    public final File getDomainDir() {
        return dirs.getServerDir();
    }

    public final File getDomainsDir() {
        return dirs.getServerParentDir();
    }

    public final ServerDirs getServerDirs() {
        return dirs;
    }

    public final boolean isValid() {
        try {
            return dirs.isValid();
        }
        catch(Exception e) {
            return false;
        }
    }

    public static File getDefaultDomainsDir() throws IOException {
        Map<String, String> systemProps = new ASenvPropertyReader().getProps();
        String defDomains =
                systemProps.get(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);

        if (defDomains == null)
            throw new IOException(strings.get("Domain.noDomainsDir",
                    SystemPropertyConstants.DOMAINS_ROOT_PROPERTY));

        return new File(defDomains);

    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////           All Private Below           /////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    private File getTheOneAndOnlyDir(File parent) throws IOException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = parent.listFiles(new FileFilter() {
            public boolean accept(File f) {
                File config = new File(f, "config");
                File dxml = new File(config, "domain.xml");
                return f.isDirectory() && config.isDirectory() &&
                        dxml.isFile();
            }
        });

        if (files == null || files.length == 0)
            throw new IOException(strings.get("Domain.noDomainDirs", parent));

        if(files.length > 1) {
            StringBuilder names = new StringBuilder();

            for(int i = 0 ; i < files.length; i++) {
                if(i > 0)
                    names.append(", ");
                names.append(files[i].getName());
            }

            throw new IOException(strings.get("Domain.tooManyDomainDirs", parent, names.toString()));
        }

        return files[0];
    }

    private final ServerDirs dirs;
    private final static LocalStringsImpl strings = new LocalStringsImpl(DomainDirs.class);
}

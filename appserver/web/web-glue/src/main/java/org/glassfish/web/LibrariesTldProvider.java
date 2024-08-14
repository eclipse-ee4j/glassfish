/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web;

import com.sun.enterprise.util.net.JarURIPattern;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.web.TldProvider;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

/**
 * Implementation of TldProvider for libraries in lib and DOMAIN_ROOT/lib/applibs.
 * @author Shing Wai Chan
 */

@Service(name="librariesTld")
@Singleton
public class LibrariesTldProvider implements TldProvider, PostConstruct {

    @Inject
    private ServerEnvironment serverEnvironment;

    private Map<URI, List<String>> tldMap = new HashMap<URI, List<String>>();

    /**
     * Gets the name of this TldProvider
     */
    public String getName() {
        return "librariesTld";
    }

    /**
     * Gets a mapping from JAR files to their TLD resources.
     */
    public Map<URI, List<String>> getTldMap() {
        return cloneTldMap();
    }

    /**
     * Gets a mapping from JAR files to their TLD resources
     * that are known to contain listener declarations.
     */
    public Map<URI, List<String>> getTldListenerMap() {
        // return the whole map as the content for tld files is not known
        return cloneTldMap();
    }

    @SuppressWarnings("unchecked")
    private Map<URI, List<String>> cloneTldMap() {
        return (Map<URI, List<String>>)((HashMap<URI, List<String>>)tldMap).clone();
    }

    public void postConstruct() {
        File[] domainLibJars = serverEnvironment.getLibPath().listFiles(
                new FileFilter() {
                    public boolean accept(File path) {
                        return (path.isFile() && path.getName().endsWith(".jar"));
                    }
                });

        if (domainLibJars != null && domainLibJars.length > 0) {
            List <URI> uris = new ArrayList<URI>();
            for (File f : domainLibJars) {
               uris.add(f.toURI());
            }

            Pattern pattern = Pattern.compile("META-INF/.*\\.tld");
            for (URI uri : uris) {
                List<String> entries =  JarURIPattern.getJarEntries(uri, pattern);
                if (entries != null && entries.size() > 0) {
                    tldMap.put(uri, entries);
                }
            }
        }
    }
}

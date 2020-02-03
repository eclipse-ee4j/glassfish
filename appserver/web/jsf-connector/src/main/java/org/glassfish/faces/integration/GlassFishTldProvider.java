/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.faces.integration;

import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.util.net.JarURIPattern;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.web.TldProvider;
import org.glassfish.hk2.api.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.web.loader.LogFacade;

/**
 * Implementation of TldProvider for JSF.
 * @author Shing Wai Chan
 * @author Sahoo
 */

@Service(name="jsfTld")
@Singleton
public class GlassFishTldProvider implements TldProvider, PostConstruct {

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    @Inject
    ModulesRegistry registry;

    private Map<URI, List<String>> tldMap =
        new HashMap<URI, List<String>>();

    private Map<URI, List<String>> tldListenerMap = null;

    /**
     * Gets the name of this TldProvider
     */
    public String getName() {
        return "jsfTld";
    }
 
    /**
     * Gets a mapping from JAR files to their TLD resources.
     */
    @SuppressWarnings("unchecked")
    public Map<URI, List<String>> getTldMap() {
        return (Map<URI, List<String>>)((HashMap)tldMap).clone();
    }

    /**
     * Gets a mapping from JAR files to their TLD resources
     * that are known to contain listener declarations.
     */
    public synchronized Map<URI, List<String>> getTldListenerMap() {
        if (tldListenerMap == null) {
            tldListenerMap = new HashMap<URI, List<String>>();
            Set<Map.Entry<URI, List<String>>> entrySet = tldMap.entrySet();
            for (Map.Entry<URI, List<String>> entry : entrySet) {
                /*
                 * In the case of JSF, the only TLD that declares any
                 * listener is META-INF/jsf_core.tld 
                 */
                if (entry.getValue().contains("META-INF/jsf_core.tld")) {
                    tldListenerMap.put(entry.getKey(), entry.getValue());
                    break;
                }
            }
            tldListenerMap = Collections.unmodifiableMap(tldListenerMap);
        }

        return tldListenerMap;
    }

    public void postConstruct() {

        Class jsfImplClass = null;
        try {
            jsfImplClass = getClass().getClassLoader().loadClass(
                "com.sun.faces.spi.InjectionProvider");
        } catch (ClassNotFoundException ignored) {
        }

        URI[] uris = null;
        HK2Module m = null;
        if (jsfImplClass != null) {
            m = registry.find(jsfImplClass);
        }
        if (m != null) {
            uris = m.getModuleDefinition().getLocations();
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            if (classLoader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader)classLoader).getURLs();
                if (urls != null && urls.length > 0) {
                    uris = new URI[urls.length];
                    for (int i = 0; i < urls.length; i++) {
                        try {
                            uris[i] = urls[i].toURI();
                        } catch(URISyntaxException e) {
                            String msg = rb.getString(LogFacade.TLD_PROVIDER_IGNORE_URL);
                            msg = MessageFormat.format(msg, urls[i]);
                            logger.log(Level.WARNING, msg, e);
                        }
                    }
                }
            } else {
                logger.log(Level.WARNING,
                    LogFacade.UNABLE_TO_DETERMINE_TLD_RESOURCES,
                    new Object[] {"JSF", classLoader,
                        GlassFishTldProvider.class.getName()});
            }
        }

        if (uris != null && uris.length > 0) {
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

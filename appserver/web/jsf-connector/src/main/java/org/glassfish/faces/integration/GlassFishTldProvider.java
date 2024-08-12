/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.glassfish.api.web.TldProvider;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.web.loader.LogFacade;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.Utility.isEmpty;
import static com.sun.enterprise.util.net.JarURIPattern.getJarEntries;
import static java.text.MessageFormat.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.logging.Level.WARNING;
import static java.util.regex.Pattern.compile;
import static org.glassfish.web.loader.LogFacade.TLD_PROVIDER_IGNORE_URL;
import static org.glassfish.web.loader.LogFacade.UNABLE_TO_DETERMINE_TLD_RESOURCES;

/**
 * Implementation of TldProvider for JSF.
 *
 * @author Shing Wai Chan
 * @author Sahoo
 */
@Service(name = "jsfTld")
@Singleton
public class GlassFishTldProvider implements TldProvider, PostConstruct {

    private static final Logger logger = LogFacade.getLogger();
    private static final ResourceBundle rb = logger.getResourceBundle();

    @Inject
    ModulesRegistry registry;

    private Map<URI, List<String>> tldMap = new HashMap<>();
    private Map<URI, List<String>> tldListenerMap;

    /**
     * Gets the name of this TldProvider
     */
    @Override
    public String getName() {
        return "jsfTld";
    }

    /**
     * Gets a mapping from JAR files to their TLD resources.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<URI, List<String>> getTldMap() {
        return (Map<URI, List<String>>) ((HashMap) tldMap).clone();
    }

    /**
     * Gets a mapping from JAR files to their TLD resources that are known to contain listener declarations.
     */
    @Override
    public synchronized Map<URI, List<String>> getTldListenerMap() {
        if (tldListenerMap == null) {
            tldListenerMap = new HashMap<URI, List<String>>();
            for (Map.Entry<URI, List<String>> entry : tldMap.entrySet()) {
                // In the case of Faces, the only TLD that declares any listener is META-INF/jsf_core.tld
                if (entry.getValue().contains("META-INF/jsf_core.tld")) {
                    tldListenerMap.put(entry.getKey(), entry.getValue());
                    break;
                }
            }

            tldListenerMap = unmodifiableMap(tldListenerMap);
        }

        return tldListenerMap;
    }

    @Override
    public void postConstruct() {
        Class<?> jsfImplClass = null;
        try {
            jsfImplClass = getClass().getClassLoader().loadClass("com.sun.faces.spi.InjectionProvider");
        } catch (ClassNotFoundException ignored) {
        }

        URI[] uris = null;
        HK2Module hk2Module = null;
        if (jsfImplClass != null) {
            hk2Module = registry.find(jsfImplClass);
        }

        if (hk2Module != null) {
            uris = hk2Module.getModuleDefinition().getLocations();
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            if (classLoader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) classLoader).getURLs();
                if (!isEmpty(urls)) {
                    uris = new URI[urls.length];
                    for (int i = 0; i < urls.length; i++) {
                        try {
                            uris[i] = urls[i].toURI();
                        } catch (URISyntaxException e) {
                            logger.log(WARNING, format(rb.getString(TLD_PROVIDER_IGNORE_URL), urls[i]), e);
                        }
                    }
                }
            } else {
                logger.log(WARNING, UNABLE_TO_DETERMINE_TLD_RESOURCES,
                        new Object[] { "JSF", classLoader, GlassFishTldProvider.class.getName() });
            }
        }

        if (!isEmpty(uris)) {
            Pattern pattern = compile("META-INF/.*\\.tld");
            for (URI uri : uris) {
                List<String> entries = getJarEntries(uri, pattern);
                if (!isEmpty(entries)) {
                    tldMap.put(uri, entries);
                }
            }
        }
    }
}

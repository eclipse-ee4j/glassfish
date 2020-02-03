/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.web.jersey.server.mvc.jsp.integration;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.PostConstruct;

import org.jvnet.hk2.annotations.Service;

import org.glassfish.api.web.TldProvider;
import org.glassfish.web.loader.LogFacade;

import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.util.net.JarURIPattern;

/**
 * {@link TldProvider TLD Provider} suited for Jersey MVC *.tld.
 * <br/>
 * Note: Copied from {@code jstl-connector}.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
@Service(name = "jerseyMvcTld")
@Singleton
public class JerseyMvcTldProvider implements TldProvider, PostConstruct {

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    @Inject
    private ModulesRegistry registry;

    private Map<URI, List<String>> tldMap = new HashMap<URI, List<String>>();

    public String getName() {
        return "jerseyMvcTld";
    }

    @SuppressWarnings("unchecked")
    public Map<URI, List<String>> getTldMap() {
        return (Map<URI, List<String>>) ((HashMap) tldMap).clone();
    }

    public Map<URI, List<String>> getTldListenerMap() {
        return null;
    }

    public void postConstruct() {
        final Class jerseyIncludeClass = org.glassfish.jersey.server.mvc.jsp.Include.class;

        URI[] uris = null;
        HK2Module m = null;
        if (jerseyIncludeClass != null) {
            m = registry.find(jerseyIncludeClass);
        }
        if (m != null) {
            uris = m.getModuleDefinition().getLocations();
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            if (classLoader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) classLoader).getURLs();
                if (urls != null && urls.length > 0) {
                    uris = new URI[urls.length];
                    for (int i = 0; i < urls.length; i++) {
                        try {
                            uris[i] = urls[i].toURI();
                        } catch (URISyntaxException e) {
                            String msg = rb.getString(LogFacade.TLD_PROVIDER_IGNORE_URL);
                            msg = MessageFormat.format(msg, urls[i]);
                            logger.log(Level.WARNING, msg, e);
                        }
                    }
                }
            } else {
                logger.log(Level.WARNING, LogFacade.UNABLE_TO_DETERMINE_TLD_RESOURCES, new Object[]{
                        JerseyMvcTldProvider.class.getSimpleName(), classLoader, JerseyMvcTldProvider.class.getName()
                });
            }
        }

        if (uris != null && uris.length > 0) {
            Pattern pattern = Pattern.compile("META-INF/.*\\.tld");
            for (URI uri : uris) {
                List<String> entries = JarURIPattern.getJarEntries(uri, pattern);
                if (entries != null && entries.size() > 0) {
                    tldMap.put(uri, entries);
                }
            }
        }
    }
}

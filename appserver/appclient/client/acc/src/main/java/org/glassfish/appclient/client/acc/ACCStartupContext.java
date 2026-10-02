/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.module.bootstrap.StartupContext;

import jakarta.inject.Singleton;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import org.glassfish.main.jdke.props.EnvToPropsConverter;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.embeddable.GlassFishVariable.CONFIG_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.DERBY_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.DOMAINS_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.IMQ_BIN;
import static org.glassfish.embeddable.GlassFishVariable.IMQ_LIB;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.JAVA_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.NODES_ROOT;

/**
 * Start-up context for the ACC.
 *
 * @author tjquinn
 */
@Service
@Singleton
public class ACCStartupContext extends StartupContext {

    public ACCStartupContext() {
        super(accEnvironment());
    }

    /**
     * Creates a Properties object containing setting for the definitions
     * in the asenv[.bat|.conf] file.
     *
     * @return
     */
    private static Properties accEnvironment() {
        final File rootDirectory = getRootDirectory();
        final Map<String, String> pairs = Map.of(
            DERBY_ROOT.getEnvName(), DERBY_ROOT.getPropertyName(),
            IMQ_LIB.getEnvName(), IMQ_LIB.getPropertyName(),
            IMQ_BIN.getEnvName(), IMQ_BIN.getPropertyName(),
            CONFIG_ROOT.getEnvName(), CONFIG_ROOT.getPropertyName(),
            INSTALL_ROOT.getEnvName(), INSTALL_ROOT.getPropertyName(),
            JAVA_ROOT.getEnvName(), JAVA_ROOT.getPropertyName(),
            DOMAINS_ROOT.getEnvName(), DOMAINS_ROOT.getPropertyName(),
            NODES_ROOT.getEnvName(), NODES_ROOT.getEnvName());
        Map<String, File> files = new EnvToPropsConverter(rootDirectory.toPath()).convert(pairs);
        Properties env = new Properties();
        files.entrySet().forEach(e -> env.put(e.getKey(), e.getValue().getPath()));
        return env;
    }

    private static File getRootDirectory() {
        final URI jarURI;
        try {
            jarURI = ACCStartupContext.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not resolve URI of the current JAR!", e);
        }
        return new File(jarURI).getParentFile().getParentFile();
    }
}

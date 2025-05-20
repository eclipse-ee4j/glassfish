/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Singleton;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import org.glassfish.main.jdke.props.EnvToPropsConverter;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.SystemPropertyConstants.AGENT_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.CONFIG_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.DOMAINS_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.IMQ_BIN_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.IMQ_LIB_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.JAVA_ROOT_PROPERTY_ASENV;
import static org.glassfish.embeddable.GlassFishVariable.DERBY_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * Start-up context for the ACC.  Note that this context is used also for
 * Java Web Start launches.
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
            "AS_IMQ_LIB", IMQ_LIB_PROPERTY,
            "AS_IMQ_BIN", IMQ_BIN_PROPERTY,
            "AS_CONFIG", CONFIG_ROOT_PROPERTY,
            "AS_INSTALL", INSTALL_ROOT.getPropertyName(),
            "AS_JAVA", JAVA_ROOT_PROPERTY_ASENV,
            "AS_DEF_DOMAINS_PATH", DOMAINS_ROOT_PROPERTY,
            "AS_DEF_NODES_PATH", AGENT_ROOT_PROPERTY);
        Map<String, File> files = new EnvToPropsConverter(rootDirectory.toPath()).convert(pairs);
        Properties env = new Properties();
        files.entrySet().forEach(e -> env.put(e.getKey(), e.getValue().getPath()));
        return env;
    }

    private static File getRootDirectory() {
        /*
         * During launches not using Java Web Start the root directory
         * is important; it is used in setting some system properties.
         */
        URI jarURI = null;
        try {
            jarURI = ACCStartupContext.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        if (jarURI.getScheme().startsWith("http")) {
            // We do not really rely on the root directory during Java
            // Web Start launches but we must return something.
            return FileUtils.USER_HOME;
        }
        File jarFile = new File(jarURI);
        File dirFile = jarFile.getParentFile().getParentFile();
        return dirFile;
    }
}

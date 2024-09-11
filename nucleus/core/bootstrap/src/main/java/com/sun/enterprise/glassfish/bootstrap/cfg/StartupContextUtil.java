/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.glassfish.bootstrap.cfg;

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.Which;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.ARG_SEP;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTALL_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTANCE_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.ORIGINAL_ARGS;

/**
 * This encapsulates the behavior of the properties object that's part of
 * {@link com.sun.enterprise.module.bootstrap.StartupContext}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public final class StartupContextUtil {

    // this contains utility methods only
    private StartupContextUtil() {
    }


    /**
     * @return autodetected glassfish directory based on where usually is this class.
     */
    public static File detectInstallRoot() {
        // glassfish/modules/glassfish.jar
        File bootstrapFile = findBootstrapFile();
        // glassfish/
        return bootstrapFile.getParentFile().getParentFile();
    }


    /**
     * @return uses this class to locate its jar file.
     */
    private static File findBootstrapFile() {
        try {
            return Which.jarFile(StartupContextUtil.class);
        } catch (IOException e) {
            throw new Error("Cannot get bootstrap path from " + StartupContextUtil.class + " class location, aborting",
                e);
        }
    }


    /**
     * @param context
     * @return absolute glassfish directory aka install root.
     */
    public static File getInstallRoot(StartupContext context) {
        return getInstallRoot(context.getArguments());
    }


    /**
     * @param properties
     * @return absolute glassfish directory aka install root.
     */
    public static File getInstallRoot(Properties properties) {
        return absolutize(new File(properties.getProperty(INSTALL_ROOT_PROP_NAME)));

    }


    /**
     * @param context
     * @return absolute domain or instance directory aka instance root.
     */
    public static File getInstanceRoot(StartupContext context) {
        return getInstanceRoot(context.getArguments());
    }


    /**
     * @param properties
     * @return absolute domain or instance directory aka instance root.
     */
    public static File getInstanceRoot(Properties properties) {
        return absolutize(new File(properties.getProperty(INSTANCE_ROOT_PROP_NAME)));
    }


    /**
     * @param context
     * @return parsed array of arguments saved as {@link #ORIGINAL_ARGS}
     */
    public static String[] getOriginalArguments(StartupContext context) {
        Properties args = context.getArguments();
        // See how ASMain packages the arguments
        String s = args.getProperty(ORIGINAL_ARGS);
        if (s == null) {
            return new String[0];
        }
        StringTokenizer st = new StringTokenizer(s, ARG_SEP, false);
        List<String> result = new ArrayList<>();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result.toArray(new String[result.size()]);
    }


    private static File absolutize(File f) {
        try {
            return f.getCanonicalFile();
        } catch (IOException e) {
            return f.getAbsoluteFile();
        }
    }
}

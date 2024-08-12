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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.bootstrap.StartupContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

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

    public static File getInstallRoot(Properties p) {
        return absolutize(new File(p.getProperty(Constants.INSTALL_ROOT_PROP_NAME)));

    }

    public static File getInstallRoot(StartupContext sc) {
        return getInstallRoot(sc.getArguments());
    }

    public static File getInstanceRoot(Properties p) {
        return absolutize(new File(p.getProperty(Constants.INSTANCE_ROOT_PROP_NAME)));
    }

    public static File getInstanceRoot(StartupContext sc) {
        return getInstanceRoot(sc.getArguments());
    }

    public static String[] getOriginalArguments(StartupContext sc) {
        Properties args = sc.getArguments();
        String s = args.getProperty(Constants.ORIGINAL_ARGS); // See how ASMain packages the arguments
        if (s == null) return new String[0];
        StringTokenizer st = new StringTokenizer(s, Constants.ARG_SEP, false);
        List<String> result = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result.toArray(new String[result.size()]);

    }

    private static File absolutize(File f)
    {
        try
        {
            return f.getCanonicalFile();
        }
        catch(Exception e)
        {
            return f.getAbsoluteFile();
        }
    }

}

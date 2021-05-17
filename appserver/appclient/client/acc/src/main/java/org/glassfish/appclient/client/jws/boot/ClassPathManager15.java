/*
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

package org.glassfish.appclient.client.jws.boot;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Class Path manager for Java Web Start-aware ACC running under Java runtime 1.5.x.
 *
 * @author tjquinn
 */
public class ClassPathManager15 extends ClassPathManager {

    /**
     *Return a new instance of the manager
     *@param loader the Java Web Start-provided class loader
     */
    protected ClassPathManager15(ClassLoader loader, boolean keepJWSClassLoader) {
        super(loader, keepJWSClassLoader);
    }

    public ClassLoader getParentClassLoader() {
        return (keepJWSClassLoader() ? getJnlpClassLoader() : null);
    }

    public File findContainingJar(URL resourceURL) throws IllegalArgumentException, URISyntaxException {
        File result = null;
        if (resourceURL != null) {
            URI uri = resourceURL.toURI();
            String scheme = uri.getScheme();
            String ssp = uri.getSchemeSpecificPart();
            if (scheme.equals("jar")) {
                /*
                 *The scheme-specific part will look like "file:<file-spec>!/<path-to-class>.class"
                 *so we need to isolate the scheme and the <file-spec> part.
                 *The subscheme (the scheme within the jar) precedes the colon
                 *and the file spec appears after it and before the exclamation point.
                 */
                int colon = ssp.indexOf(':');
                int excl = ssp.indexOf('!');
                String containingJarPath = ssp.substring(colon + 1, excl);
                result = new File(containingJarPath);
            } else {
                throw new IllegalArgumentException(resourceURL.toExternalForm());
            }
        }
        return result;
    }


}

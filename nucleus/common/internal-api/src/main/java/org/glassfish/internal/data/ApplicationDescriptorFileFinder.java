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

package org.glassfish.internal.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.hk2.bootstrap.DescriptorFileFinder;

/**
 * This DescriptorFileFinder is used to find all of the META-INF/hk2-locator/application files
 * in the application
 *
 * @author jwells
 *
 */
public class ApplicationDescriptorFileFinder implements DescriptorFileFinder {

    private final String resourceName;
    private final ClassLoader loaderToUse;

    /* package */
    ApplicationDescriptorFileFinder(ClassLoader loaderToUse, String resourceName) {
        this.resourceName = resourceName;
        this.loaderToUse = loaderToUse;
    }

    @Override
    public List<InputStream> findDescriptorFiles() throws IOException {
        Enumeration<URL> urls = loaderToUse.getResources(resourceName);

        LinkedList<InputStream> retVal = new LinkedList<InputStream>();

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();

            retVal.add(url.openStream());
        }

        return retVal;
    }

}

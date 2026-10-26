/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.web.embed.impl;

import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;
import com.sun.enterprise.deployment.archivist.ArchivistFor;

import java.io.IOException;
import java.net.URL;

import org.glassfish.apf.Scanner;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ScatteredWarArchiveType;
import org.glassfish.api.deployment.archive.WarArchiveType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Globals;
import org.glassfish.web.deployment.annotation.impl.WarScanner;
import org.glassfish.web.deployment.archivist.WebArchivist;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jerome Dochez
 * @author David Matejcek
 */
@Service
@PerLookup
@ArchivistFor(ScatteredWarArchiveType.ARCHIVE_TYPE)
public class ScatteredWebArchivist extends WebArchivist {

    private static URL defaultWebXmlLocation;

    static void setDefaultWebXml(URL defaultWebXml) {
        defaultWebXmlLocation = defaultWebXml;
    }


    @Override
    public URL getDefaultWebXML() throws IOException {
        if (defaultWebXmlLocation != null) {
            return defaultWebXmlLocation;
        }
        URL defaultWebXml = super.getDefaultWebXML();
        return defaultWebXml == null
            ? getClass().getClassLoader().getResource("org/glassfish/web/embed/default-web.xml")
            : defaultWebXml;
    }


    @Override
    public ArchiveType getModuleType() {
        return Globals.getDefaultHabitat().getService(ArchiveType.class, ScatteredWarArchiveType.ARCHIVE_TYPE);
    }


    /**
     * Embedded GlassFish deploys regular WAR files (a {@code ScatteredArchive} is assembled
     * into one as well), so the annotations must be scanned the same way as on the server,
     * including the JAR files in {@code WEB-INF/lib}. The module type of this archivist has
     * no scanner of its own, so the one registered for the war module type is used.
     *
     * @return the {@link WarScanner}
     */
    @Override
    @SuppressWarnings("unchecked")
    public ModuleScanner<WebBundleDescriptorImpl> getScanner() {
        return (ModuleScanner<WebBundleDescriptorImpl>) habitat.getService(Scanner.class, WarArchiveType.ARCHIVE_TYPE);
    }
}

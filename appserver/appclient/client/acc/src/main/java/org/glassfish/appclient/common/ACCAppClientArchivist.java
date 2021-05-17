/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.common;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.ExtensionsArchivist;
import java.io.IOException;
import java.util.ArrayList;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Optional;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;
import org.glassfish.hk2.api.PerLookup;
import org.xml.sax.SAXException;

/**
 * AppClientArchivist that does not warn if both the GlassFish and the
 * legacy Sun runtime descriptors are present.
 * <p>
 * The ACC uses a MultiReadableArchive to essentially merge the contents of
 * the generated app client JAR with the developer's original app client JAR.
 * The generated file contains a generated GlassFish runtime descriptor.
 * If the developer's app client contains a legacy sun-application-client.xml
 * descriptor, then the normal archivist logic would detect that both the
 * GlassFish DD and the developer's legacy sun-application-client.xml were
 * present in the merged contents and it would log a warning.
 * <p>
 * We prevent such warnings by overriding the method which reads the runtime
 * deployment descriptor.
 *
 * @author Tim Quinn
 */
@Service
@PerLookup
public class ACCAppClientArchivist extends AppClientArchivist implements PostConstruct {

    @Inject @Optional
    IterableProvider<ExtensionsArchivist> allExtensionArchivists;

    @Override
    public void readRuntimeDeploymentDescriptor(ReadableArchive archive, ApplicationClientDescriptor descriptor) throws IOException, SAXException {
        super.readRuntimeDeploymentDescriptor(archive, descriptor, false);
    }

    public void postConstruct() {
        extensionsArchivists = new ArrayList<ExtensionsArchivist>();
        for (ExtensionsArchivist extensionArchivist : allExtensionArchivists) {
            if (extensionArchivist.supportsModuleType(getModuleType())) {
                extensionsArchivists.add(extensionArchivist);
            }
        }
    }
}

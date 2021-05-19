/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.extras.grizzly;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.internal.deployment.GenericSniffer;
import org.jvnet.hk2.annotations.Service;

/**
 * Sniffs raw grizzly adapters in jar files
 *
 * @author Jerome Dochez
 */
@Service(name="grizzly")
public class GrizzlyAdapterSniffer extends GenericSniffer {

    final static private String[] containerNames = { "grizzly" };

    public GrizzlyAdapterSniffer() {
        super("grizzly", GrizzlyModuleDescriptor.DescriptorPath ,null);
    }

    @Override
    public String[] getContainersNames() {
        return containerNames;
    }

    /**
     *
     * This API is used to help determine if the sniffer should recognize
     * the current archive.
     * If the sniffer does not support the archive type associated with
     * the current deployment, the sniffer should not recognize the archive.
     *
     * @param archiveType the archive type to check
     * @return whether the sniffer supports the archive type
     *
     */
    @Override
    public boolean supportsArchiveType(ArchiveType archiveType) {
        if (archiveType.toString().equals("war") ||
            archiveType.toString().equals("ejb")) {
            return true;
        }
        return false;
    }
}

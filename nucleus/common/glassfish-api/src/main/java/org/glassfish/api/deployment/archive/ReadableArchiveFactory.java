/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.deployment.archive;

import java.net.URI;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author Vivek Pandey
 *
 * ReadableArchiveFactory implementation should be packaged as a HK2 service, This factory implementation, when present
 * should be asked for a ReadableArchive during the deployment phase. For example, the DeployCommand can ask for
 * ReadableArchive from this factory from each of the v3 modules.
 *
 */
@Contract
public interface ReadableArchiveFactory {
    /**
     * Gives a ReadableArchive.
     *
     * @param archivePath Path to the application
     * @return returns null if it can not create archive
     */
    ReadableArchive open(URI archivePath, DeployCommandParameters commandProperties);
}

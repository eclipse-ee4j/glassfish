/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.extras.osgicontainer;

import org.glassfish.api.deployment.archive.ArchiveType;

import org.jvnet.hk2.annotations.Service;

/**
 * {@link ArchiveType} corresponding to OSGi archives deployed to GlassFish.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = OSGiArchiveType.ARCHIVE_TYPE)
@jakarta.inject.Singleton
public class OSGiArchiveType extends ArchiveType {
    /**
     * String value of this module type. This is what is accepted in --type argument of deploy command.
     */
    public static final String ARCHIVE_TYPE = "osgi";

    public OSGiArchiveType() {
        super(ARCHIVE_TYPE); // there is no definite extension for OSGi bundles.
    }
}

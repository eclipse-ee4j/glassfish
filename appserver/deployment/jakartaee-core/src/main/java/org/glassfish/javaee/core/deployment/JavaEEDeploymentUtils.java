/*
 * Copyright (c) 2012, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee.core.deployment;

import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.ServiceLocator;

import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;

/**
 * Deloyment utility class for JavaEE related things
 *
 */

public class JavaEEDeploymentUtils {

    /**
     * check whether the archive is a JavaEE archive
     * @param archive archive to be tested
     * @param habitat
     * @return whether the archive is a JavaEE archive
     */
    public static boolean isJavaEE(ReadableArchive archive, ServiceLocator habitat) {
        return isJavaEE(archive, null, habitat);
    }
    /**
     * check whether the archive is a JavaEE archive
     * @param archive archive to be tested
     * @param context deployment context
     * @param habitat
     * @return whether the archive is a JavaEE archive
     */
    public static boolean isJavaEE(ReadableArchive archive, DeploymentContext context, ServiceLocator habitat) {
        if (DeploymentUtils.isArchiveOfType(archive,DOLUtils.earType(), context, habitat) || DeploymentUtils.isArchiveOfType(archive, DOLUtils.warType(), context, habitat) || DeploymentUtils.isArchiveOfType(archive, DOLUtils.carType(), context, habitat) || DeploymentUtils.isArchiveOfType(archive, DOLUtils.rarType(), context, habitat) || DeploymentUtils.isArchiveOfType(archive, DOLUtils.ejbType(), context, habitat)) {
            return true;
        }
        return false;
    }
}

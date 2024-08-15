/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.resources.util;

import com.sun.enterprise.deployment.util.DOLUtils;

import java.io.IOException;
import java.util.Enumeration;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author Jagadish Ramu
 */
public class ResourceUtil {

    private static final String RESOURCES_XML_META_INF = "META-INF/glassfish-resources.xml";
    private static final String RESOURCES_XML_WEB_INF = "WEB-INF/glassfish-resources.xml";


    public static boolean hasResourcesXML(ReadableArchive archive, ServiceLocator locator) {
        try {
            if (!DeploymentUtils.isArchiveOfType(archive, DOLUtils.earType(), locator)) {
                if (DeploymentUtils.isArchiveOfType(archive, DOLUtils.warType(), locator)) {
                    return archive.exists(RESOURCES_XML_WEB_INF);
                }
                return archive.exists(RESOURCES_XML_META_INF);
            }

            // handle top-level META-INF/glassfish-resources.xml
            if (archive.exists(RESOURCES_XML_META_INF)) {
                return true;
            }

            // check sub-module level META-INF/glassfish-resources.xml and
            // WEB-INF/glassfish-resources.xml
            Enumeration<String> entries = archive.entries();
            while (entries.hasMoreElements()) {
                String element = entries.nextElement();
                if (element.endsWith(".jar") || element.endsWith(".war") || element.endsWith(".rar")
                    || element.endsWith("_jar") || element.endsWith("_war") || element.endsWith("_rar")) {
                    try (ReadableArchive subArchive = archive.getSubArchive(element)) {
                        boolean answer = subArchive != null && hasResourcesXML(subArchive, locator);
                        if (answer) {
                            return true;
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            //ignore
        }
        return false;
    }
}

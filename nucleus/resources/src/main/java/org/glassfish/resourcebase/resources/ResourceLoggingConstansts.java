/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.resourcebase.resources;

import org.glassfish.logging.annotation.LogMessageInfo;


/**
 * @author naman 18/3/13
 */
public class ResourceLoggingConstansts {
    @LogMessageInfo(
            message = "Unexpected exception in loading class [{0}] by classloader.",
            comment = "This is a comment about this log message.",
            cause = "Classpath is not properly set in the domain.xml or application server process does not have " +
                    "read permissions on the directory that holds the classes/jar.",
            action = "Check that the classpath attribute in the java-config includes a reference to the jar/package " +
                    "directory for the class or you do not have read permissions on the directory that holds the classes/jar.",
            level = "SEVERE")
    public static final String LOAD_CLASS_FAIL = "NCLS-RESOURCE-00001";

    @LogMessageInfo(
            message = "Unexpected exception in loading class by classloader [{0}].",
            comment = "This is a comment about this log message.",
            cause = "Classpath is not properly set in the domain.xml or you do not have read permissions on the directory " +
                    "that holds the classes/jar.",
            action = "Check that the classpath attribute in the java-config includes a reference to the jar/package " +
                    "directory for the class or check that the directory where the classes/jars reside have read " +
                    "permission for the application server process",
            level = "SEVERE")
    public static final String LOAD_CLASS_FAIL_EXCEP = "NCLS-RESOURCE-00002";

    @LogMessageInfo(
            message = "Cannot bind resource [{0}] to naming manager. Following exception occurred [{1}].",
            comment = "This is a comment about this log message.",
            cause = "Please check the exception to get more details.",
            action = "Please check the exception to resolve the same.",
            level = "SEVERE")
    public static final String BIND_RESOURCE_FAILED = "NCLS-RESOURCE-00003";

    @LogMessageInfo(
            message = "Unable to deploy resource [{0}] due to following exception: [{1}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String UNABLE_TO_DEPLOY = "NCLS-RESOURCE-00004";

    @LogMessageInfo(
            message = "Unable to undeploy resource, no Resource Deployer for [{0}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String UNABLE_TO_UNDEPLOY = "NCLS-RESOURCE-00005";

    @LogMessageInfo(
            message = "Unable to undeploy resource [{0}] due to following exception: [{1}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String UNABLE_TO_UNDEPLOY_EXCEPTION = "NCLS-RESOURCE-00006";

    @LogMessageInfo(
            message = "Error while handling Change event due to following exception: [{0}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String ERROR_HANDLE_CHANGE_EVENT = "NCLS-RESOURCE-00007";

    @LogMessageInfo(
            message = "Error while handling Remove event due to following exception: [{0}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String ERROR_HANDLE_REMOVE_EVENT = "NCLS-RESOURCE-00008";

    @LogMessageInfo(
            message = "Unable to find ResourceDeployer for [{0}].",
            comment = "This is a comment about this log message.",
            level = "WARNING")
    public static final String UNABLE_TO_FIND_RESOURCEDEPLOYER = "NCLS-RESOURCE-00009";

}

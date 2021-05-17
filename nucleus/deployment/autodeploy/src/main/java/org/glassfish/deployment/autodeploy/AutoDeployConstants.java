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

/*
 * AutoDeployConstants.java
 *
 * Created on February 27, 2003, 12:00 AM
 */

package org.glassfish.deployment.autodeploy;

import java.util.ArrayList;
import java.util.List;

/**
 *constants detail
 *
 * @author  vikas
 */
public class AutoDeployConstants {

    /**
     * Starting delay between AutoDeployTask activation and actual deployment.
     */
    public static  final long STARTING_DELAY=30; //sec
    /**
     * Max tardiness between schedule and actual execution of task.
     */
    public static  final long MAX_TARDINESS= 10; //sec
    /**
     * Max tardiness between schedule and actual execution of task.
     */
    public static  final long MIN_POOLING_INTERVAL= 2; //sec

    /**
     * Default autodeploy dir set to "autodeploy" relative to server root
     */
    public static final String DEFAULT_AUTODEPLOY_DIR = "autodeploy";

    /**
     * Default polling interval set to 2sec
     */
    public static final long DEFAULT_POLLING_INTERVAL = 2; //sec

    /**
     * Extension of file, after successful deployment
     */
    public static  final String DEPLOYED="_deployed";
    /**
     * Extension of file, if deployment fails
     */
    public static  final String NOTDEPLOYED="_notdeployed";

    /**
     * File type if it is being monitored due to slow growth
     */
    public static final String PENDING = "_pending";

/**
     * common deploy action
     */
    public static final String DEPLOY_METHOD       = "deploy";
    /**
     * common undeploy action
     */
    public static final String UNDEPLOY_METHOD       = "undeploy";

    public static final String DEPLOY_FAILED       = "_deployFailed";
    public static final String UNDEPLOYED       = "_undeployed";
    public static final String UNDEPLOY_FAILED       = "_undeployFailed";

    public static final String UNDEPLOY_REQUESTED = "_undeployRequested";

    public static final List<String> MARKER_FILE_SUFFIXES = initMarkerFileSuffixes();

    private static List<String> initMarkerFileSuffixes() {
        List<String> result = new ArrayList<String>();
        result.add(DEPLOYED);
        result.add(NOTDEPLOYED);
        result.add(PENDING);
        result.add(DEPLOY_FAILED);
        result.add(UNDEPLOYED);
        result.add(UNDEPLOY_FAILED);
        result.add(UNDEPLOY_REQUESTED);

        return result;
    }
}

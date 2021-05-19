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

package org.glassfish.appclient.server.core;

/**
 * Constants used in different parts of the deployment implementation
 *
 * @author  Jerome ochez
 */
public interface DeploymentImplConstants {

    public static final String ClientJarSuffix = "Client.jar";
    /*
     * Use this property to select the desired client jar genration senario.
     * Possible values are:
     * "transition" : current default. use the new simple applient jar maker
     *                when appropriate.  otherwise, defer to the original one.
     * "combo"      : near future default. use the new simple appclient jar
     *                maker when appropriate.  otherwise, defer to new ear
     *                appclient maker
     * "ear"        : use only the new ear appclient make
     * "original"   : use the original implementation (to verify regression)
     */
    public static final String
        CLIENT_JAR_MAKER_CHOICE = "client.jar.maker.choice";

    /*
     * Internal implementation constant.  NOT a supported system property.
     */
    public static final String
        USE_MODULE_CLIENT_JAR_MAKER = "use.module.client.jar.maker";
}

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

package org.glassfish.deployment.client;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ClientConfiguration;

import java.io.IOException;

/**
 * Represents any type of owner of a Target.
 * <p>
 * Each Target object needs to know what object created it so it can
 * delegate certain task to that object.  Different classes that connect to the
 * admin back-end in different ways can create Target objects, so this interface
 * prescribes the behavior that each such "owner" of Targets must provide.
 * <p>
 * Fully-formed Target objects will have links back to their respective TargetOwner
 * objects.
 * 
 * @author tjquinn
 */
public interface TargetOwner {

    /**
     * Creates a single {@link Target} with the given name.
     * @param name the name of the Target to be returned
     * @return a new Target
     */
    public Target createTarget(String name);
    
    /**
     * Creates several {@link Target} objects with the specified names.
     * @param names the names of the targets to be returned
     * @return new Targets, one for each name specified
     */
    public Target[] createTargets(String[] names);
    
    /**
     * Returns the Web URL for the specified module on the {@link Target}
     * implied by the TargetModuleID.
     * @param tmid
     * @return web url
     */
    public String getWebURL(TargetModuleID tmid);

    /**
     * Sets the Web URL for the specified module on the {@link Target} implied
     * by the TargetModuleID.
     * represents a Web module or submodule on a Target.
     * @param tmid
     * @param the URL
     */
    public void setWebURL(TargetModuleID tmid, String webURL);

    /**
     *  Exports the Client stub jars to the given location.
     *  @param appName The name of the application or module.
     *  @param destDir The directory into which the stub jar file
     *  should be exported.
     *  @return the absolute location to the main jar file.
     */
    public String exportClientStubs(String appName, String destDir) 
        throws IOException;
}

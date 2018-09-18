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

package org.glassfish.deployapi;

import javax.enterprise.deploy.spi.Target;
import org.glassfish.deployment.client.TargetOwner;

import java.io.IOException;

/**
 * Implements the Target interface as specified by JSR-88.
 * <p>
 * This implementation is independent of the concrete type of its owner.
 * 
 * @author tjquinn
 */
public class TargetImpl implements Target {

    private TargetOwner owner;
    
    private String name;
    
    private String description;
    
    /**
     * Creates a new TargetImpl object.
     * <p>
     * Note that this constructor should normally be used only by a TargetOwner.
     * Logic that needs to create {@link Target} instances should invoke {@link TargetOwner#createTarget} or 
     * {@link TargetOwner#createTargets} on the TargetOwner.
     * 
     * @param owner
     * @param name
     * @param description
     */ // XXX It would be nice to move classes around so this could be package-visible and not public
    public TargetImpl(TargetOwner owner, String name, String description) {
        this.owner = owner;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the name of the Target.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the Target.
     * @return
     */
    public String getDescription() {
        return description;
    }
    
    public TargetOwner getOwner() {
        return owner;
    }

    /**
     *  Exports the Client stub jars to the given location.
     *  @param appName The name of the application or module.
     *  @param destDir The directory into which the stub jar file
     *  should be exported.
     *  @return the absolute location to the main jar file.
     */
    public String exportClientStubs(String appName, String destDir) 
        throws IOException {
        return owner.exportClientStubs(appName, destDir);
    }
}

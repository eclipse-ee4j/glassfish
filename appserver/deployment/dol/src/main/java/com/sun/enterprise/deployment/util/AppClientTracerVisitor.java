/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.types.*;

import java.util.Iterator;
import java.util.Set;

public class AppClientTracerVisitor extends TracerVisitor implements AppClientVisitor {

    public AppClientTracerVisitor() {
    }

    public void accept (BundleDescriptor descriptor) {
        if (descriptor instanceof ApplicationClientDescriptor) {
            ApplicationClientDescriptor appClientDesc = (ApplicationClientDescriptor)descriptor;
            accept(appClientDesc);

            super.accept(descriptor);
        }
    }

     /**
     * visits an app client descriptor
     * @param app client descriptor
     */
    public void accept(ApplicationClientDescriptor appclientDesc) {
        DOLUtils.getDefaultLogger().info("==================");
        DOLUtils.getDefaultLogger().info("\tAppClient Description " + appclientDesc.getDescription());
        DOLUtils.getDefaultLogger().info("\tAppClient Name " + appclientDesc.getName());
        DOLUtils.getDefaultLogger().info("\tAppClient Small Icon " + appclientDesc.getSmallIconUri());
        DOLUtils.getDefaultLogger().info("\tAppClient Large Icon " + appclientDesc.getLargeIconUri());
        DOLUtils.getDefaultLogger().info("\tAppClient Callback Handler " + appclientDesc.getCallbackHandler());
        //add rest of the tags
    }
}


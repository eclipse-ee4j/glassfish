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
 * AutoDeployListner.java
 *
 *
 * Created on February 19, 2003, 10:23 AM
 */

package org.glassfish.deployment.autodeploy;

import org.glassfish.deployment.common.DeploymentException;

/**
 * AutoDeploymentException
 *
 * @author vikas
 */


public class AutoDeploymentException extends DeploymentException {
    /**
     *constructor with no argument
     */
    public AutoDeploymentException(){
        super();
    }
    /**
     *constructor with  argument String
     */
    public AutoDeploymentException(String mes){
        super(mes);
    }
    /**
     *constructor with  argument Throwable
     */
    public AutoDeploymentException(Throwable t) {
        super(t.toString());
    }
    /**
     *constructor with  argument String, Throwable
     */
    public AutoDeploymentException(String s, Throwable t) {
        super(s + " -- " + t.getMessage(), t);

    }


}

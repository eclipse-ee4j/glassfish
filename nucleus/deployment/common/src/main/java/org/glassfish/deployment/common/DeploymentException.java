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

package org.glassfish.deployment.common;

/**
 *
 * @author  bnevins
 * @version
 */
public class DeploymentException extends RuntimeException
{

        // declare SUID for class versioning compatibility
        // generated using pe build fcs-b52
        // this value should stay the same for all
        // 8.x releases
        static final long serialVersionUID = -7110600101249180249L;

    public DeploymentException()
    {
    }
    public DeploymentException(String s)
    {
        super(s);
    }
    public DeploymentException(Throwable t)
    {
            // we cannot just invoke the super(throwable) constructor because
            // the DeploymentException travels between processes and needs
            // to be serializable as well as all sub or chained exception.
            // Therefore, I use the setStackTrace to chain instead of initCause
            super(t.getMessage());
            setStackTrace(t.getStackTrace());
    }
    public DeploymentException(String s, Throwable t)
    {
            // we cannot just invoke the super(throwable) constructor because
            // the DeploymentException travels between processes and needs
            // to be serializable as well as all sub or chained exception.
            // Therefore, I use the setStackTrace to chain instead of initCause
        super(s + " -- " + t.getMessage());
            this.setStackTrace(t.getStackTrace());
    }
}

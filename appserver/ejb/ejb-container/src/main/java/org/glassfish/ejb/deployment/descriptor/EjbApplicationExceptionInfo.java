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

package org.glassfish.ejb.deployment.descriptor;

import java.io.Serializable;

/**
 * Contains information about an ejb application exception.
 */ 

public class EjbApplicationExceptionInfo implements Serializable 
{
    private String exceptionClassName;
    private boolean rollback;
    private boolean inherited = true;

    public void setExceptionClassName(String className) {
        exceptionClassName = className;
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    public boolean getRollback() {
        return rollback;
    }

    public void setRollback(boolean flag) {
        rollback = flag;
    }

    public void setInherited(boolean flag) {
        inherited = flag;
    }

    public boolean getInherited() {
        return inherited;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("exception class name = " + exceptionClassName + "\t");
        sb.append("rollback = " + rollback);
        sb.append("inherited = " + inherited);
        return sb.toString();
    }
}


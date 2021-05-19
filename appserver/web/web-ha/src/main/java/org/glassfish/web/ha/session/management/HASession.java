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

package org.glassfish.web.ha.session.management;

import org.apache.catalina.Session;

/**
 *
 * @author  lwhite
 * @author Rajiv Mordani
 */
public interface HASession extends Session {

    /**
     * this returns the dirty flag
     */
    public boolean isDirty();

    /**
     * this sets the dirty flag
     * @param value
     */
    public void setDirty(boolean value);

    /**
     * this sets the ssoId
     * @param ssoId
     */
    public void setSsoId(String ssoId);

    /**
     * this returns the ssoId
     */
    public String getSsoId();

    /**
     * this returns the user name
     */
    public String getUserName();

    /**
     * this sets the user name
     * @param userName
     */
    public void setUserName(String userName);

    public void sync();
    public long incrementVersion();
    public void setVersion(long value);
    public boolean isPersistent();
    public void setPersistent(boolean value);

}

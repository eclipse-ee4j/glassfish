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
 * JndiMBean.java
 *
 * Created on March 8, 2004, 1:42 PM
 */

package com.sun.enterprise.admin.monitor.jndi;

import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.naming.NamingException;

/**
 * The JndiMBean DynamicMBean interface.
 *
 * @author Rob Ruyak
 */
public interface JndiMBean extends DynamicMBean {

    /**
     * Gets the jndi naming entries given a particular context or subcontext.
     *
     * @param context The context name under which the entries live.
     * @return An array of serializable NameClassPair objects.
     * @throws MBeanException when an error occurs in retrieving the entries.
     */
    public java.util.ArrayList getNames(String context) throws NamingException;

}

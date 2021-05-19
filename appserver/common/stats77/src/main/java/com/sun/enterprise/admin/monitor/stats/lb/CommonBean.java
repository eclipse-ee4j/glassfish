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

package com.sun.enterprise.admin.monitor.stats.lb;

/**
 * This interface is the intersection of all generated methods.
 */
public interface CommonBean {
    void changePropertyByName(String name, Object value);

    com.sun.enterprise.admin.monitor.stats.lb.CommonBean[] childBeans(boolean recursive);

    void childBeans(boolean recursive, java.util.List beans);

    @Override boolean equals(Object o);

    Object fetchPropertyByName(String name);

    @Override int hashCode();

    void readNode(org.w3c.dom.Node node);

    @Override String toString();

    void validate() throws com.sun.enterprise.admin.monitor.stats.lb.LoadBalancerStats.ValidateException;

    void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException;

}

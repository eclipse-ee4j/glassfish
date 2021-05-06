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
 * ContainerTypeOrApplicationType.java
 * $Id: ContainerTypeOrApplicationType.java,v 1.3 2006/11/08 20:55:16 harpreet Exp $
 * $Date: 2006/11/08 20:55:16 $
 * $Revision: 1.3 $
 */

package        com.sun.enterprise.admin.monitor.callflow;

public enum ContainerTypeOrApplicationType {
    WEB_CONTAINER, EJB_CONTAINER, ORB_CONTAINER,
    WEB_APPLICATION, EJB_APPLICATION, JAVA_PERSISTENCE,        OTHER;
}

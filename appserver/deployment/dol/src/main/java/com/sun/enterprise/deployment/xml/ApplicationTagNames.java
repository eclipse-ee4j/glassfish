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

package com.sun.enterprise.deployment.xml;

/**
 * This interface holds all the XML tag names for a J2EE application
 * deployment descriptor.
 * @author Danny Coward
 */

public interface ApplicationTagNames extends TagNames {
     String APPLICATION = "application";
     String APPLICATION_NAME = "application-name";
     String INITIALIZE_IN_ORDER = "initialize-in-order";
     String MODULE = "module";
     String EJB = "ejb";
     String WEB = "web";
     String APPLICATION_CLIENT = "java";
     String CONNECTOR = "connector";
     String ALTERNATIVE_DD = "alt-dd";
     String SECUTIRY_ROLE = "security-role";
     String ROLE_NAME = "role-name";
     String CONTEXT_ROOT = "context-root";
     String WEB_URI = "web-uri";
     String LIBRARY_DIRECTORY = "library-directory";

}


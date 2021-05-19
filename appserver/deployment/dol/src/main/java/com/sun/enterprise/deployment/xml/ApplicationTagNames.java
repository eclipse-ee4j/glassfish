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
     public static String APPLICATION = "application";
     public static String APPLICATION_NAME = "application-name";
     public static String INITIALIZE_IN_ORDER = "initialize-in-order";
     public static String MODULE = "module";
     public static String EJB = "ejb";
     public static String WEB = "web";
     public static String APPLICATION_CLIENT = "java";
     public static String CONNECTOR = "connector";
     public static String ALTERNATIVE_DD = "alt-dd";
     public static String SECUTIRY_ROLE = "security-role";
     public static String ROLE_NAME = "role-name";
     public static String CONTEXT_ROOT = "context-root";
     public static String WEB_URI = "web-uri";
     public static String LIBRARY_DIRECTORY = "library-directory";

}


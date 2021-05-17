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

package com.sun.enterprise.server.logging.diagnostics;

/**
 * Constants for all Diagnostics Related stuff.
 *
 * @author Hemanth Puttaswamy
 * @AUTHOR Carla Mott
 */
public class DiagConstants{
     public static final int MAX_CAUSES_AND_CHECKS = 5;

     // The causes in the Resource Bundle will be in the format
     // <messageId>.diag.cause.<cause number>
     public static final String CAUSE_PREFIX = ".diag.cause.";

     // The causes in the Resource Bundle will be in the format
     // <messageId>.diag.check.<check number>
     public static final String CHECK_PREFIX = ".diag.check.";

     public static final String URI_PREFIX =
         "http://docs.sun.com/diagnostics/jes/appserver/";

     public static final String JDO_MESSAGE_PREFIX = "JDO";
}


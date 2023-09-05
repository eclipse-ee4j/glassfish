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

package org.glassfish.tests.webapi;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public final class TestListener
  implements ServletContextListener
{
  public static String msg = "Context not YET initialized";

  public void contextInitialized(ServletContextEvent event)
  {
    System.out.println("TestListener : contextInitialized called");
    try
    {
      System.out.println("TestListener : Trying to load TestCacaoList");

        Class c = Class.forName("org.glassfish.tests.embedded.web.TestCacaoList");

      msg = "Class TestCacaoList loaded successfully from listener";
      System.out.println(msg);
    }
    catch (Exception ex)
    {
      msg = "Exception while loading class TestCacaoList from listener : " + ex.toString();
      System.out.println(msg);
    }
    System.out.println("TestListener : contextInitialized DONE");
  }

  public void contextDestroyed(ServletContextEvent event) {}
}

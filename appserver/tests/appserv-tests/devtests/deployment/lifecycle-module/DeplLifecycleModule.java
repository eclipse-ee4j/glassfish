/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.appserv.server.*;

/**
 * This is a dummy implementation for the LifecycleListener interface.
 */

public class DeplLifecycleModule implements LifecycleListener {

  public void handleEvent(LifecycleEvent event) throws ServerLifecycleException {
    System.out.println("got event" + event.getEventType() + " event data: "
      + event.getData());

    if (LifecycleEvent.INIT_EVENT == event.getEventType()) {
      System.out.println("DeplLifecycleListener: INIT_EVENT");
      return;
    }

    if (LifecycleEvent.STARTUP_EVENT == event.getEventType()) {
      System.out.println("DeplLifecycleListener: STARTUP_EVENT");
      return;
    }

    if (LifecycleEvent.READY_EVENT == event.getEventType()) {
      System.out.println("DeplLifecycleListener: READY_EVENT");
      return;
    }

    if (LifecycleEvent.SHUTDOWN_EVENT== event.getEventType()) {
      System.out.println("DeplLifecycleListener: SHUTDOWN_EVENT");
      return;
    }

    if (LifecycleEvent.TERMINATION_EVENT == event.getEventType()) {
      System.out.println("DeplLifecycleListener: TERMINATE_EVENT");
      return;
    }
  }
}

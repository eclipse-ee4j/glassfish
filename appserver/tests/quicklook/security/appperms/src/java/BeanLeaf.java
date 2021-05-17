/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package myapp;


import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Singleton;

@Singleton
public class BeanLeaf {

    @EJB
    private BeanMessageInterface msg;

    String MESSAGE_POST = "PostBeanLeaf";
    String MESSAGE_HELLO = "HelloBeanLeaf";

    @PostConstruct
    public void afterConstruct() {
        if (msg != null && !msg.getMessage().contains(MESSAGE_POST)){
      msg.appendMessage(MESSAGE_POST);
    }
    }

    public String sayHello() {
        if (msg != null && !msg.getMessage().contains(MESSAGE_HELLO)){
      msg.appendMessage(MESSAGE_HELLO);
    }
        return "Hello from: " + this.getClass().getName() + "; " + System.identityHashCode(this);
    }

}

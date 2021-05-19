/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package connector;

import java.lang.reflect.Method;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkManager;

/**
 *
 * @author        Qingqing Ouyang
 */
public class MyWork implements Work {

    private String name;
    private boolean stop = false;
    private MessageEndpointFactory factory;
    private WorkManager wm;

    public MyWork(
            String name, MessageEndpointFactory factory, WorkManager wm) {
        this.factory = factory;
        this.name = name;
        this.wm = wm;
    }

    public void run() {

        debug("ENTER...");

        //try 3 times to create endpoint (in case of failure)
        for (int i = 0; i < 3; i++) {

            try {

                Method onMessage = getOnMessageMethod();
                System.out.println("isDeliveryTransacted = " +
                                      factory.isDeliveryTransacted(onMessage));

                /*
                  MessageEndpoint ep = factory.createEndpoint(null);
                  ep.beforeDelivery(onMessage);
                  ((MyMessageListener) ep).onMessage(name + ": TEST MSG ONE");
                  ((MyMessageListener) ep).onMessage(name + ": TEST MSG TWO");
                  ((MyMessageListener) ep).onMessage(name + ": TEST MSG THREE");
                  ep.afterDelivery();
                  break;
                */

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        debug("LEAVE...");
    }

    public void release() {}

    public void stop() {
        this.stop = true;
    }

    public String toString() {
       return name;
    }

    public Method getOnMessageMethod() {

        Method onMessageMethod = null;
        try {
            Class msgListenerClass = connector.MyMessageListener.class;
            Class[] paramTypes = { java.lang.String.class };
            onMessageMethod =
                msgListenerClass.getMethod("onMessage", paramTypes);

        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return onMessageMethod;
    }

    private void debug(String mesg) {
        System.out.println("MyWork[" + name + "] --> " + mesg);
    }
}

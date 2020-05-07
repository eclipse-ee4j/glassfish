/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.resource.spi.AdministeredObject;
import jakarta.resource.spi.ConfigProperty;

@AdministeredObject(
        adminObjectInterfaces = {Destination.class,Queue.class}
)
public class MyAdminObject extends AbstractAdminObject implements Destination, Queue {

    private static final long serialVersionUID = 1169995481259581782L;
    private Integer expectedResults;

    @ConfigProperty(
            type = java.lang.Integer.class,
            defaultValue = "88"
    )
    public void setExpectedResults (Integer value) {
        expectedResults = value;
    }

    public Integer getExpectedResults () {
        return expectedResults;
    }

    public void initialize() {
        System.out.println("[MyAdminObject] Initializing the Controls to false:"+getResetControl());
        if (getResetControl().equals("BEGINNING")) {
            synchronized (Controls.readyLock){
                Controls.done=false;
            }
            System.out.println("[MyAdminObject] Initialized the Controls to false");
        }
    }

    public boolean done() {
        synchronized (Controls.readyLock){
         return Controls.done;
        }
    }

    public int expectedResults(){
        synchronized (Controls.readyLock){

        return Controls.expectedResults;
        }
    }

    public Object getLockObject(){
        return Controls.readyLock;
    }

    @Override
    public String getQueueName() throws JMSException {
      return "testQueue";
    }

}


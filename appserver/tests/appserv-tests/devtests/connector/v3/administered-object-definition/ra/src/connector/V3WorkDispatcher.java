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

package connector;

import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.endpoint.MessageEndpoint;

public class V3WorkDispatcher extends WorkDispatcher {
    public V3WorkDispatcher(String id, BootstrapContext ctx, MessageEndpointFactory factory, ActivationSpec spec) {
        super(id, ctx, factory, spec);
    }

    public void run() {

        try {
            synchronized (Controls.readyLock) {
                debug("WAIT...");
                Controls.readyLock.wait();


                if (stop) {
                    return;
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            MessageEndpoint ep = factory.createEndpoint(new FakeXAResource());
            int numOfMessages = 1;
            debug("V3WorkDispatcher sleeping");
            //Thread.sleep(10000);
            debug("V3WorkDispatcher woke up");
            //importing transaction

            //write/commit
            ExecutionContext ec = startTx();
            debug("Start TX - " + ec.getXid());

            debug("V3WorkDispatcher about to submit work");
            DeliveryWork w =
                    new DeliveryWork(ep, numOfMessages, "WRITE");
            wm.doWork(w, 0, null, null);
                      xa.commit(ec.getXid(), true);

            debug("V3WorkDispatcher done work");
            debug("DONE WRITE TO DB");
            Controls.expectedResults = numOfMessages;
            notifyAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            debug("V3WorkDispatcher calling DONE()");
            done();
            debug("V3WorkDispatcher finished calling DONE()");

        }
    }
}

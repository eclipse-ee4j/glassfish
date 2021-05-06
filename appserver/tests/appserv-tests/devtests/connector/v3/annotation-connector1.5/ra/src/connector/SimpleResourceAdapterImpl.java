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

import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.*;
import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * This is a sample resource adapter
 *
 * @author        Qingqing Ouyang
 */
@Connector(
   /*moduleName="My Simple RAR",*/
   displayName = "Simple Resource Adapter",
   vendorName = "Java Software",
   eisType = "Generic Type",
   version = "1.0Alpha"
)
public class SimpleResourceAdapterImpl extends AbstractResourceAdapter
implements ResourceAdapter, java.io.Serializable {

    private BootstrapContext ctx;
    private WorkManager wm;

    private Work work;

    public SimpleResourceAdapterImpl () {
        debug ("constructor...");
    }

    public void
    start(BootstrapContext ctx) throws ResourceAdapterInternalException{

        debug("001. Simple RA start...");

        this.ctx = ctx;
        debug("002. Simple RA start...");
        this.wm  = ctx.getWorkManager();
        debug("003. Simple RA start...");

        //testing creat timer
        Timer timer = null;
          try{
              timer = ctx.createTimer();
          } catch(UnavailableException ue) {
              System.out.println("Error");
              throw new ResourceAdapterInternalException("Error form bootstrap");
          }
        debug("004. Simple RA start...");
        debug("005. Simple RA start...");
    }

    public void
    stop() {
        debug("999. Simple RA stop...");
        if (work != null) {
            ((WorkDispatcher) work).stop();

            synchronized (Controls.readyLock) {
                Controls.readyLock.notify();
            }

        }
    }

    public void
    endpointActivation ( MessageEndpointFactory factory, ActivationSpec spec)
        throws NotSupportedException {
        try {
            debug("B.000. Create and schedule Dispatcher");
            spec.validate();
            work = new WorkDispatcher("DISPATCHER", ctx, factory, spec);
            wm.scheduleWork(work, 4*1000, null, null);
            debug("B.001. Scheduled Dispatcher");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void
    endpointDeactivation (
            MessageEndpointFactory endpointFactory,
            ActivationSpec spec) {
        debug ("endpointDeactivation called...");

        ((WorkDispatcher) work).stop();
    }


    public XAResource[] getXAResources(ActivationSpec[] specs)
        throws ResourceException {
        throw new UnsupportedOperationException();
    }
}

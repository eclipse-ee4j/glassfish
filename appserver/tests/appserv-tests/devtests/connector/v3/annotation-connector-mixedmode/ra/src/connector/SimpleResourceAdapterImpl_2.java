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
import java.lang.IllegalStateException;
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

//@README : test : this @Connector should not be considered as component definition annotation.
// if there are multiple @Connector annotations,one that is a class as specified in ra.xml should be considered


/**
 * This is a sample resource adapter
 *
 * @author        Qingqing Ouyang
 */
@Connector(
   displayName = "Simple Resource Adapter",
   vendorName = "Java Software",
   eisType = "Generic Type",
   version = "1.0Alpha"
)
public class SimpleResourceAdapterImpl_2
implements ResourceAdapter, java.io.Serializable {

    private BootstrapContext ctx;
    private WorkManager wm;
    private String testName;
    @ConfigProperty(
            type = java.lang.String.class
    )
    private String testName1="testName1";

    private boolean debug = true;
    private Work work;

    public SimpleResourceAdapterImpl_2() {
        debug ("constructor...");
    }

    public void
    start(BootstrapContext ctx) throws ResourceAdapterInternalException{
        throw new IllegalStateException("This resource-adapter should not have been initialized");
    }

    public void
    stop() {
        throw new IllegalStateException("This resource-adapter should not have been initialized");
    }

    public void
    endpointActivation ( MessageEndpointFactory factory, ActivationSpec spec)
        throws NotSupportedException {
            throw new IllegalStateException("This resource-adapter should not have been initialized");
    }

    public void
    endpointDeactivation (
            MessageEndpointFactory endpointFactory,
            ActivationSpec spec) {
        throw new IllegalStateException("This resource-adapter should not have been initialized");
    }

    public String getTestName() {
        return testName;
    }

    @ConfigProperty(
            defaultValue = "ConfigPropertyForRA",
            type = java.lang.String.class
    )
    public void setTestName(String name) {
        debug("setTestName called... name = " + name);
        testName = name;
        throw new IllegalStateException("This resource-adapter should not have been initialized");
    }

  public String getTestName1() {
        return testName1;
    }

    public void setTestName1(String name) {
        debug("setTestName1 called... name = " + name);
        testName1 = name;
        throw new IllegalStateException("This resource-adapter should not have been initialized");
    }


    public void
    debug (String message) {
        if (debug)
            System.out.println("[SimpleResourceAdapterImpl] ==> " + message);
    }

    public XAResource[] getXAResources(ActivationSpec[] specs)
        throws ResourceException {
        throw new UnsupportedOperationException();
    }
}

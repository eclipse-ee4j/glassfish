/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.grizzly;

import java.nio.channels.SelectableChannel;

import org.jvnet.hk2.annotations.Contract;

/**
 * This interface is meant for all services that wish to be initialized lazily.
 * Such services are expected to implement this interface and those
 * implementatons should be available to HK2 for lookup.
 *
 *
 * @author Vijay Ramachandran
*/
@Contract
public interface LazyServiceInitializer {

    /**
     * Upon accepting the first request on the port (to which this listener is
     * bound), the listener will select the appropriate provider and call this
     * method to let the actual service initialize itself. All further accept
     * requests on this port will wait while the service is initialized.
     * Upon successful completion of service initialization, all pending
     * requests are passed to the service using the handleRequest method
     *
     * @return Return true if service initialization went through fine; false
     *         otherwise
     */
    public boolean initializeService();

    /**
     * Upon successful ACCEPT of every request on this port, the service
     * is called upon to handle the request. The service is provided the
     * channel itself. The service can setup connection, its characteristics,
     * decide on blocking/non-blocking modes etc. The service is expected to
     * return control back to the listener ASAP without consuming this thread
     * for processing the requst completely.
     *
     * @param channel where the incoming request was accepted.
     */
    public void handleRequest(SelectableChannel channel);
}

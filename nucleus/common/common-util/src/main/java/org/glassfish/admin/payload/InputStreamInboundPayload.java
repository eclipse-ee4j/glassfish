/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.payload;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of a Payload.Inbound that works with InputStreams
 *
 * Usage is like this:
 *
 *      CommandRunner.CommandInvocation ci =
 *              commandRunner.getCommandInvocation("some-command", areport, theSubject);
 *      InputStreamPayload in = new InputStreamInboundPayload();
 *      // get an InputStream is from somewhere
 *      in.addStream("someoption", is)
 *      // get another InputStream for the operand
 *      in.addStream("DEFAULT", is)
 *      ParameterMap map = new ParameterMap();
 *      // populate map with other options
 *      ci.inbound(in).parameters(map).execute();
 *
 * @author Tom Mueller
 */
public class InputStreamInboundPayload extends PayloadImpl.Inbound {
    private Map<String,InputStream> args = new HashMap<String,InputStream>();

    public void addStream(String name, InputStream is) {
        args.put(name, is);
    }

    @Override
    public Iterator<org.glassfish.api.admin.Payload.Part> parts() {
        return new Iterator<org.glassfish.api.admin.Payload.Part>() {

            private Iterator<Map.Entry<String,InputStream>> argiter = args.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return argiter.hasNext();
            }

            @Override
            public org.glassfish.api.admin.Payload.Part next() {
                Map.Entry<String,InputStream> e = argiter.next();
                Properties props = new Properties();
                props.setProperty("data-request-type", "file-xfer");
                props.setProperty("data-request-name", e.getKey());
                return PayloadImpl.Part.newInstance("application/binary", e.getKey(), props, e.getValue());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

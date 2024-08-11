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

package org.glassfish.webservices.transport.tcp;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.server.TCPAdapter;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.WSTCPException;

import java.io.IOException;

/**
 * General SOAP/TCP WebService Adapter for GFv3
 *
 * @author Alexey Stashok
 */
public abstract class TCP109Adapter extends TCPAdapter {

    /**
     * Currently 109 deployed WS's pipeline relies on Servlet request and response
     * attributes. So its temporary workaround to make 109 work with TCP
     */
    private final ServletFakeArtifactSet servletFakeArtifactSet;
    private final boolean isEJB;

    public TCP109Adapter(
            @NotNull final String name,
            @NotNull final String urlPattern,
            @NotNull final WSEndpoint endpoint,
            @NotNull final ServletFakeArtifactSet servletFakeArtifactSet,
            final boolean isEJB) {
        super(name, urlPattern, endpoint);
        this.servletFakeArtifactSet = servletFakeArtifactSet;
        this.isEJB = isEJB;
    }


    @Override
    public void handle(@NotNull final ChannelContext channelContext) throws IOException, WSTCPException {
        try {
            beforeHandle();
            super.handle(channelContext);
        } finally {
            postHandle();
        }
    }

    public boolean isEJB() {
        return isEJB;
    }

    protected abstract void beforeHandle();

    protected abstract void postHandle();

    @Override
    protected TCPAdapter.TCPToolkit createToolkit() {
        return new TCP109Toolkit();
    }

    final class TCP109Toolkit extends TCPAdapter.TCPToolkit {
        // if its Adapter from 109 deployed WS - add fake Servlet artifacts
        @Override
        public void addCustomPacketSattellites(@NotNull final Packet packet) {
            super.addCustomPacketSattellites(packet);
            packet.addSatellite(servletFakeArtifactSet);
        }
    }
}

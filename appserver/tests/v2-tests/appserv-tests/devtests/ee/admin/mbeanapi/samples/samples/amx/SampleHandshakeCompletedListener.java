/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package samples.amx;

import javax.net.ssl.HandshakeCompletedEvent;

import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;


/**
    Demonstrates how to write a HandshakeCompletedListener. Note that usually
    it is not necessary to write your own HandshakeCompletedListener since the
    event is available from {@link HandshakeCompletedListenerImpl#getLastEvent}.
    <p>
    You may wish to write a HandshakeCompletedListener if the data contained
    in the HandshakeCompletedEvent is of interest or you wish to exert more
    control over the TLS connection.
 */
public final class SampleHandshakeCompletedListener
    extends HandshakeCompletedListenerImpl
{
        public
    SampleHandshakeCompletedListener()
    {
    }

        public synchronized void
    handshakeCompleted( final HandshakeCompletedEvent event)
    {
        super.handshakeCompleted( event );

        System.out.println( "HandshakeCompleted:\n" + event + "\n" );
    }
}







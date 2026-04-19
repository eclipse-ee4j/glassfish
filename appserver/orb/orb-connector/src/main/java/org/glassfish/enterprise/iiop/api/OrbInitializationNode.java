/*
 * Copyright (c) 2025, 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.enterprise.iiop.api;

import jakarta.ejb.Singleton;

import java.lang.System.Logger;
import java.nio.channels.SelectableChannel;
import java.util.function.Consumer;

import org.jvnet.hk2.annotations.Service;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

/**
 * This class collects fields which are unsafely visited in ORB initialization.
 * That means objects are partially initialized, but still can be provided to some
 * other classes. The complete ORB initialization should change in the future so
 * this class would not be needed at all, however now it is not possible.
 * <p>
 * The less fields is here, the better, but it will need iterative refactoring including
 * glassfish-corba-orb dependency.
 */
@Service
@Singleton
public class OrbInitializationNode {

    private static final Logger LOG = System.getLogger(OrbInitializationNode.class.getName());

    private Consumer<SelectableChannel> acceptor;

    /**
     * Sequence:
     * <ol>
     * <li>Client sends CORBA request
     * <li>Server finds that the ORB service was not initialized yet
     * <li>ORBLazyService starts the initializeService method
     * <li>glassfish-corba-orb.jar calls PEORBConfigurator.configure
     * <li>{@link #setAcceptor(Consumer)} is called.
     * <li>Client's request is processed calling {@link #handleRequest(SelectableChannel)}
     * </ol>
     * Reason why it is not in the ORBLazyService:
     * HK2 would detect it as a cyclic dependency between ORBLazyService and PEORBConfigurator
     *
     * @param acceptorDelegate
     */
    public void setAcceptor(Consumer<SelectableChannel> acceptorDelegate) {
        LOG.log(DEBUG, "setAcceptor(acceptorDelegate={0})", acceptorDelegate);
        this.acceptor = acceptorDelegate;
    }

    /**
     * Sequence:
     * <ol>
     * <li>Client sends CORBA request
     * <li>Server finds that the ORB service was not initialized yet
     * <li>ORBLazyService starts the initializeService method
     * <li>glassfish-corba-orb.jar calls PEORBConfigurator.configure
     * <li>{@link #setAcceptor(Consumer)} is called.
     * <li>Client's request is processed calling {@link #handleRequest(SelectableChannel)}
     * </ol>
     * Reason why it is not in the ORBLazyService:
     * HK2 would detect it as a cyclic dependency between ORBLazyService and PEORBConfigurator
     *
     * @param channel
     */
    public void handleRequest(SelectableChannel channel) {
        LOG.log(TRACE, "handleRequest(channel={0})", channel);
        acceptor.accept(channel);
    }

    /**
     * @return true if the acceptor was already set by the ORB initialization.
     */
    public boolean canHandleRequest() {
        return acceptor != null;
    }
}

/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.dom;

import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.grizzly.IOStrategy;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Defines one specific transport and its properties.
 */
@Configured
public interface Transport extends ConfigBeanProxy, PropertyBag {

    String BYTE_BUFFER_TYPES = "heap|direct";

    boolean DISPLAY_CONFIGURATION = false;

    boolean ENABLE_SNOOP = false;

    boolean TCP_NO_DELAY = true;

    int ACCEPTOR_THREADS = 1;

    int BUFFER_SIZE = 8192;

    int IDLE_KEY_TIMEOUT = 30;

    int LINGER = -1;

    int MAX_CONNECTIONS_COUNT = 4096;

    int READ_TIMEOUT = 30000;

    int WRITE_TIMEOUT = 30000;

    int SELECTOR_POLL_TIMEOUT = 1000;

    int SOCKET_RCV_BUFFER_SIZE = -1;

    int SOCKET_SND_BUFFER_SIZE = -1;

    String BYTE_BUFFER_TYPE = "heap";

    String CLASSNAME = "org.glassfish.grizzly.nio.transport.TCPNIOTransport";

    boolean DEDICATED_ACCEPTOR_ENABLED = false;

    /**
     * The number of acceptor threads listening for the transport's events.
     */
    @Attribute(defaultValue = "" + ACCEPTOR_THREADS, dataType = Integer.class)
    String getAcceptorThreads();

    void setAcceptorThreads(String acceptorThreads);

    /**
     * The size, in bytes, of the socket send buffer size.  If the value is {@code 0} or less,
     * it defaults to the VM's default value.
     */
    @Attribute(defaultValue = "" + SOCKET_SND_BUFFER_SIZE, dataType = Integer.class)
    String getSocketWriteBufferSize();

    void setSocketWriteBufferSize(String writeBufferSize);

    /**
     * The size, in bytes, of the socket send buffer size.  If the value is {@code 0} or less,
     * it defaults to the VM's default value.
     */
    @Attribute(defaultValue = "" + SOCKET_RCV_BUFFER_SIZE, dataType = Integer.class)
    String getSocketReadBufferSize();

    void setSocketReadBufferSize(String readBufferSize);

    /**
     * @deprecated This attribute is now ignored. Use {@code socket-send-buffer-size}
     * and/or {@code socket-write-buffer-size instead}.
     */
    @Deprecated
    @Attribute(defaultValue = "" + BUFFER_SIZE, dataType = Integer.class)
    String getBufferSizeBytes();

    void setBufferSizeBytes(String bufferSize);

    /**
     * Type of {@link java.nio.ByteBuffer}, which will be used with transport. Possible values are:
     *
     * <ul>
     *     <li>HEAP</li>
     *     <li>DIRECT</li>
     * </ul>
     */
    @Attribute(defaultValue = BYTE_BUFFER_TYPE)
    @Pattern(
            regexp = BYTE_BUFFER_TYPES,
            message = "Valid values: " + BYTE_BUFFER_TYPES,
            flags = Pattern.Flag.CASE_INSENSITIVE
    )
    String getByteBufferType();

    void setByteBufferType(String byteBufferType);

    /**
     * Name of class, which implements transport logic
     */
    @Attribute(defaultValue = CLASSNAME)
    String getClassname();

    void setClassname(String classname);

    /**
     * {@link IOStrategy} to be used by {@link Transport}.
     */
    @Attribute
    String getIoStrategy();

    void setIoStrategy(String ioStrategy);

    /**
     * Flush Grizzly's internal configuration to the server logs (like number
     * of threads created, how many polled objects, etc.)
     */
    @Attribute(defaultValue = "" + DISPLAY_CONFIGURATION, dataType = Boolean.class)
    String getDisplayConfiguration();

    void setDisplayConfiguration(String displayConfiguration);

    /**
     * Dump the requests/response information in server.log. Useful for debugging purpose,
     * but significantly reduce performance as the request/response bytes are translated
     * to the {@link String}.
     *
     * @deprecated this option is ignored by the runtime.
     */
    @Deprecated
    @Attribute(defaultValue = "" + ENABLE_SNOOP, dataType = Boolean.class)
    String getEnableSnoop();

    void setEnableSnoop(String enableSnoop);

    /**
     * Timeout, after which idle key will be cancelled and channel closed.
     */
    @Attribute(defaultValue = "" + IDLE_KEY_TIMEOUT, dataType = Integer.class)
    String getIdleKeyTimeoutSeconds();

    void setIdleKeyTimeoutSeconds(String idleKeyTimeout);

    /**
     * The max number of connections the transport should handle at the same time.
     */
    @Attribute(defaultValue = "" + MAX_CONNECTIONS_COUNT, dataType = Integer.class)
    String getMaxConnectionsCount();

    void setMaxConnectionsCount(String maxConnectionsCount);

    /**
     * Transport's {@code name}, which could be used as reference.
     */
    @Attribute(required = true, key = true)
    String getName();

    void setName(String name);

    /**
     * Read operation timeout in ms.
     */
    @Attribute(defaultValue = "" + READ_TIMEOUT, dataType = Integer.class)
    String getReadTimeoutMillis();

    void setReadTimeoutMillis(String readTimeout);

    /**
     * Use public SelectionKey handler, which was defined earlier in the document.
     * @deprecated This attribute as well as the named selection-key-handler element
     * this attribute refers to has been deprecated and is effectively ignored
     * by the runtime. <strong><em>No equivalent functionality is available</em></strong>.
     */
    @Attribute
    @Deprecated
    String getSelectionKeyHandler();

    void setSelectionKeyHandler(String selectionKeyHandler);

    /**
     * The time, in milliseconds, a NIO Selector will block waiting for events (users requests).
     */
    @Attribute(defaultValue = "" + SELECTOR_POLL_TIMEOUT, dataType = Integer.class)
    String getSelectorPollTimeoutMillis();

    void setSelectorPollTimeoutMillis(String selectorPollTimeout);

    /**
     * Write operation timeout in ms.
     */
    @Attribute(defaultValue = "" + WRITE_TIMEOUT, dataType = Integer.class)
    String getWriteTimeoutMillis();

    void setWriteTimeoutMillis(String writeTimeout);

    @Attribute(defaultValue = "" + TCP_NO_DELAY, dataType = Boolean.class)
    String getTcpNoDelay();

    void setTcpNoDelay(String tcpNoDelay);

    @Attribute(defaultValue = "" + LINGER, dataType = Integer.class)
    String getLinger();

    void setLinger(String linger);

    @Attribute(defaultValue = "" + DEDICATED_ACCEPTOR_ENABLED, dataType = Boolean.class)
    String getDedicatedAcceptorEnabled();

    void setDedicatedAcceptorEnabled(String dedicatedAcceptorEnabled);

    default List<NetworkListener> findNetworkListeners() {
        List<NetworkListener> networkListeners = new ArrayList<>();
        for (NetworkListener listener : getParent().getParent().getNetworkListeners().getNetworkListener()) {
            if (listener.getTransport().equals(getName())) {
                networkListeners.add(listener);
            }
        }
        return networkListeners;
    }

    @Override
    default Transports getParent() {
        return getParent(Transports.class);
    }
}

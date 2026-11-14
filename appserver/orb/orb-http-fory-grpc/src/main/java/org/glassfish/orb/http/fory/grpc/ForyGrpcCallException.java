/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */


package org.glassfish.orb.http.fory.grpc;

/**
 * A call that failed in the machinery between the transport and the bean (an
 * adapter that could not convert a value, a target that does not offer the
 * method), as opposed to one that failed in the bean. The adapter reports it
 * in general terms, because what it says names classes and loaders.
 */
public final class ForyGrpcCallException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public ForyGrpcCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForyGrpcCallException(String message) {
        super(message);
    }
}

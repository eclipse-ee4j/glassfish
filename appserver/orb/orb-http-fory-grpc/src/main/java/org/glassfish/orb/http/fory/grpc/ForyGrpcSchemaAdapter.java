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
 * Bridges a Fory-generated request/response model and an EJB Java signature.
 *
 * <p>The Fory compiler owns serialization and generated model classes. This
 * interface only handles the application boundary after decoding and before
 * encoding, so GlassFish does not need to recreate compiler or wire logic.</p>
 */
public interface ForyGrpcSchemaAdapter {

    /** Converts a generated request model to the Java method argument. */
    Object request(String methodPath, Object generatedRequest, Class<?> javaType);

    /** Converts the Java return value to the generated response model. */
    Object response(String methodPath, Object javaValue, Class<?> javaType);

    /** Adapter for a contract whose generated model is already the EJB value. */
    ForyGrpcSchemaAdapter IDENTITY = new ForyGrpcSchemaAdapter() {
        @Override
        public Object request(String methodPath, Object generatedRequest, Class<?> javaType) {
            return generatedRequest;
        }

        @Override
        public Object response(String methodPath, Object javaValue, Class<?> javaType) {
            return javaValue;
        }
    };
}

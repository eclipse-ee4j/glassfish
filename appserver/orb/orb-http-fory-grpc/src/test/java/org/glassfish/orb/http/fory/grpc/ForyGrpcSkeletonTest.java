/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ForyGrpcSkeletonTest {

    public interface Greeter {
        String greet(String name);
    }

    static final class GreeterBean implements Greeter {
        @Override
        public String greet(String name) {
            return "Hello " + name;
        }
    }

    @Test
    void resolvesAndInvokesADeployTimeMethod() throws Throwable {
        ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of("demo.Greeter", Greeter.class);

        assertEquals("Hello Python", skeleton.invoke(
                "/demo.Greeter/greet", new GreeterBean(), "Python"));
    }

    @Test
    void adaptsGeneratedRequestAndResponseModelsAtTheEjbBoundary() throws Throwable {
        ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of("demo.Greeter", Greeter.class);
        ForyGrpcSchemaAdapter adapter = new ForyGrpcSchemaAdapter() {
            @Override
            public Object request(String path, Object request, Class<?> type) {
                return ((GeneratedRequest) request).value;
            }

            @Override
            public Object response(String path, Object value, Class<?> type) {
                return new GeneratedResponse((String) value);
            }
        };

        Object response = skeleton.invoke("/demo.Greeter/greet", new GreeterBean(),
                new GeneratedRequest("Ada"), adapter);

        assertEquals("Hello Ada", ((GeneratedResponse) response).value);
    }

    @Test
    void generatedAdapterDiscoversHandlesOnlyOnce() throws Throwable {
        ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of("demo.Greeter", Greeter.class);
        ForyGrpcSchemaAdapter adapter = ForyGeneratedSchemaAdapter.unary(
                GeneratedRequestModel.class, GeneratedResponseModel.class);

        Object response = skeleton.invoke("/demo.Greeter/greet", new GreeterBean(),
                new GeneratedRequestModel("Ada"), adapter);

        assertEquals("Hello Ada", ((GeneratedResponseModel) response).value());
    }

    static final class GeneratedRequest {
        final String value;

        GeneratedRequest(String value) {
            this.value = value;
        }
    }

    static final class GeneratedResponse {
        final String value;

        GeneratedResponse(String value) {
            this.value = value;
        }
    }

    public static final class GeneratedRequestModel {
        private final String value;

        public GeneratedRequestModel(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public static final class GeneratedResponseModel {
        private final String value;

        public GeneratedResponseModel(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    @Test
    void rejectsUnknownMethodsWithoutReflectiveScanning() {
        ForyGrpcSkeleton skeleton = new ForyGrpcSkeleton(List.of());

        assertThrows(NoSuchMethodException.class,
                () -> skeleton.invoke("/demo.Greeter/missing", new GreeterBean(), null));
    }


    @Test
    void generatesAnIdlContractFromTheRemoteView() {
        String idl = ForyIdlGenerator.generate("demo.greeter", "Greeter", Greeter.class);

        org.junit.jupiter.api.Assertions.assertTrue(idl.contains("message GreetRequest"));
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains("string value = 1;"));
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains(
                "rpc Greet(GreetRequest) returns (GreetResponse);"));
    }
}

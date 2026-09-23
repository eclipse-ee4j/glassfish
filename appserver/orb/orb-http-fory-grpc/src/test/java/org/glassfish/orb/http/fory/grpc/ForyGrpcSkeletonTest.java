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

    @Test
    void generatesDeployTimeModelsWithoutApplicationSourceChanges() {
        ForyRuntimeModelGenerator.GeneratedModels models = ForyRuntimeModelGenerator.unary(
                "generated.demo", "greet", String.class, String.class);

        assertEquals("generated.demo.GreetRequest", models.request().getName());
        assertEquals("generated.demo.GreetResponse", models.response().getName());
    }

    @Test
    void registersGeneratedModelsInTheCrossLanguageForyRuntime() {
        ForyRuntimeModelGenerator.GeneratedModels models = ForyRuntimeModelGenerator.unary(
                "generated.demo", "greet", String.class, String.class);
        ForyGeneratedRuntime runtime = new ForyGeneratedRuntime(models, 1000, 1001);
        Object request = construct(models.request(), "Ada");

        Object copy = runtime.deserialize(runtime.serialize(request), models.request());

        assertEquals("Ada", invokeValue(copy));
    }

    @Test
    void sharedRuntimeSupportsConcurrentCalls() throws Exception {
        ForyRuntimeModelGenerator.GeneratedModels models = ForyRuntimeModelGenerator.unary(
                "generated.demo", "greet", String.class, String.class);
        ForyGeneratedRuntime runtime = new ForyGeneratedRuntime(models, 1000, 1001);
        var pool = java.util.concurrent.Executors.newFixedThreadPool(4);
        try {
            var tasks = java.util.stream.IntStream.range(0, 32)
                    .mapToObj(i -> pool.submit(() -> {
                        Object request = construct(models.request(), "user-" + i);
                        return invokeValue(runtime.deserialize(runtime.serialize(request), models.request()));
                    })).toList();
            for (int i = 0; i < tasks.size(); i++) {
                assertEquals("user-" + i, tasks.get(i).get());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void registryResolvesDeployTimeBindingWithoutRebuildingMetadata() {
        ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of("demo.Greeter", Greeter.class);
        ForyRuntimeModelGenerator.GeneratedModels models = ForyRuntimeModelGenerator.unary(
                "generated.demo", "greet", String.class, String.class);
        ForyGeneratedServiceRegistry registry = new ForyGeneratedServiceRegistry();
        registry.register("/demo.Greeter/greet", "/demo.Greeter/greet", skeleton, models,
                new ForyGeneratedRuntime(models, 1000, 1001),
                ForyGrpcSchemaAdapter.IDENTITY);

        assertEquals("/demo.Greeter/greet", registry.lookup("/demo.Greeter/greet").skeletonPath());
        assertEquals(1, registry.size());
    }

    private static Object construct(Class<?> type, String value) {
        try {
            return type.getConstructor(String.class).newInstance(value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Object invokeValue(Object value) {
        try {
            return value.getClass().getMethod("value").invoke(value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
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
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains("message GreetRequest [id=1000]"));
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains("message GreetResponse [id=1001]"));
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains("string value = 1;"));
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains(
                "rpc Greet(GreetRequest) returns (GreetResponse);"));
    }
}

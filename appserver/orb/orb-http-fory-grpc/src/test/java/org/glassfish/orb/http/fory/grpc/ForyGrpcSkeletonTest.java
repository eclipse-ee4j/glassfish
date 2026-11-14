/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForyGrpcSkeletonTest {

    public interface Greeter {
        String greet(String name);
    }

    /** The shape of what the EJB container generates for a remote view. */
    public interface GeneratedGreeterRemote extends Remote {
        String greet(String name) throws RemoteException;
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
    void invokesATargetThatImplementsTheGeneratedRemoteInterface() throws Throwable {
        // What the container actually hands back for a remote view: a proxy
        // over the generated interface, which is not an instance of the
        // business interface the skeleton was built from.
        ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of("demo.Greeter", Greeter.class);
        Greeter bean = new GreeterBean();
        Object target = Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {GeneratedGreeterRemote.class},
                (proxy, method, args) -> bean.greet((String) args[0]));

        assertFalse(Greeter.class.isInstance(target), "the test target must not be a Greeter");
        assertEquals("Hello Ada", skeleton.invoke("/demo.Greeter/greet", target, "Ada"));
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
        // The ids come from the message names, not from the order the methods
        // happen to be discovered in; see ForyTypeIdsTest.
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains(
                "message GreetRequest [id=" + ForyTypeIds.of("demo.greeter", "GreetRequest") + "]"));
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains(
                "message GreetResponse [id=" + ForyTypeIds.of("demo.greeter", "GreetResponse") + "]"));
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains("string value = 1;"));
        org.junit.jupiter.api.Assertions.assertTrue(idl.contains(
                "rpc Greet(GreetRequest) returns (GreetResponse);"));
    }

    public interface Calc {
        int twice(int value);
    }

    public interface Admin {
        void ping();

        int answer();

        void accept(String value);

        String name();
    }

    public interface CaseClash {
        String foo(String value);

        String Foo(String value);
    }

    public interface WithAChar {
        char next(char value);
    }

    public interface WithAShort {
        short next(short value);
    }

    public interface WithAByte {
        byte next(byte value);
    }

    @Test
    void invokesAMethodWithAPrimitiveParameterAndResult() throws Throwable {
        // int.class.isInstance(Integer) is false, so a check against the
        // declared type refused every primitive parameter.
        ForyRuntimeModelGenerator.GeneratedModels models = ForyRuntimeModelGenerator.unary(
                "gen.calc", "twice", int.class, int.class);
        Object request = models.request().getConstructor(int.class).newInstance(21);

        Object response = ForyGrpcSkeleton.of("svc", Calc.class).invoke("/svc/twice", (Calc) v -> v * 2, request,
                ForyGeneratedSchemaAdapter.unary(models.request(), models.response()));

        assertEquals(42, invokeValue(response));
    }

    @Test
    void aPrimitiveParameterIsNeverNull() {
        ForyGrpcSchemaAdapter nothing = new ForyGrpcSchemaAdapter() {
            @Override
            public Object request(String path, Object request, Class<?> type) {
                return null;
            }

            @Override
            public Object response(String path, Object value, Class<?> type) {
                return value;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> ForyGrpcSkeleton.of("svc", Calc.class)
                .invoke("/svc/twice", (Calc) v -> v, new Object(), nothing));
    }

    @Test
    void aPrimitiveSurvivesTheWire() throws Throwable {
        ForyRuntimeModelGenerator.GeneratedModels models = ForyRuntimeModelGenerator.unary(
                "gen.calc3", "twice", int.class, int.class);
        ForyGeneratedRuntime runtime = new ForyGeneratedRuntime(models, 3000, 3001);
        Object request = runtime.deserialize(runtime.serialize(
                models.request().getConstructor(int.class).newInstance(21)), models.request());

        Object response = ForyGrpcSkeleton.of("svc", Calc.class).invoke("/svc/twice", (Calc) v -> v * 2, request,
                ForyGeneratedSchemaAdapter.unary(models.request(), models.response()));

        assertEquals(42, invokeValue(runtime.deserialize(runtime.serialize(response), models.response())));
    }

    @Test
    void invokesVoidAndNoArgumentMethods() throws Throwable {
        List<String> calls = new java.util.ArrayList<>();
        Admin bean = new Admin() {
            @Override
            public void ping() {
                calls.add("ping");
            }

            @Override
            public int answer() {
                return 42;
            }

            @Override
            public void accept(String value) {
                calls.add("accept:" + value);
            }

            @Override
            public String name() {
                return "admin";
            }
        };
        ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of("svc", Admin.class);

        ForyRuntimeModelGenerator.GeneratedModels ping = ForyRuntimeModelGenerator.unary(
                "gen.admin", "ping", void.class, void.class);
        ForyGeneratedRuntime pingRuntime = new ForyGeneratedRuntime(ping, 3100, 3101);
        Object pingRequest = ping.request().getConstructor().newInstance();
        Object pingResponse = skeleton.invoke("/svc/ping", bean, pingRequest,
                ForyGeneratedSchemaAdapter.unary(ping.request(), ping.response()));
        // Nothing to return is an empty message, and it survives the wire.
        assertNotNull(pingRuntime.deserialize(pingRuntime.serialize(pingResponse), ping.response()));
        assertNotNull(pingRuntime.deserialize(pingRuntime.serialize(pingRequest), ping.request()));

        ForyRuntimeModelGenerator.GeneratedModels answer = ForyRuntimeModelGenerator.unary(
                "gen.admin2", "answer", void.class, int.class);
        Object answerRequest = answer.request().getConstructor().newInstance();
        assertEquals(42, invokeValue(skeleton.invoke("/svc/answer", bean, answerRequest,
                ForyGeneratedSchemaAdapter.unary(answer.request(), answer.response()))));

        ForyRuntimeModelGenerator.GeneratedModels accept = ForyRuntimeModelGenerator.unary(
                "gen.admin3", "accept", String.class, void.class);
        Object acceptRequest = construct(accept.request(), "x");
        assertNotNull(skeleton.invoke("/svc/accept", bean, acceptRequest,
                ForyGeneratedSchemaAdapter.unary(accept.request(), accept.response())));
        assertEquals(List.of("ping", "accept:x"), calls);

        ForyRuntimeModelGenerator.GeneratedModels name = ForyRuntimeModelGenerator.unary(
                "gen.admin4", "name", void.class, String.class);
        Object nameRequest = name.request().getConstructor().newInstance();
        assertEquals("admin", invokeValue(skeleton.invoke("/svc/name", bean, nameRequest,
                ForyGeneratedSchemaAdapter.unary(name.request(), name.response()))));
    }

    @Test
    void theIdlOfAPrimitiveSignatureUsesTheWireTypes() {
        String idl = ForyIdlGenerator.generate("demo.calc", "Calc", Calc.class);

        assertTrue(idl.contains("int32 value = 1;"), idl);
        assertTrue(idl.contains("rpc Twice(TwiceRequest) returns (TwiceResponse);"), idl);
    }

    @Test
    void idlRejectsMethodsDifferingOnlyInCase() {
        // Both would be declared as FooRequest and rpc Foo.
        assertThrows(IllegalArgumentException.class,
                () -> ForyIdlGenerator.generate("p", "S", CaseClash.class));
    }

    @Test
    void idlRejectsNamesThatAreNotJavaNames() {
        assertThrows(IllegalArgumentException.class,
                () -> ForyIdlGenerator.generate("glassfish.2fa_app.module", "Greeter", Greeter.class));
        assertThrows(IllegalArgumentException.class,
                () -> ForyIdlGenerator.generate("glassfish.app.default", "Greeter", Greeter.class));
        assertThrows(IllegalArgumentException.class,
                () -> ForyIdlGenerator.generate("glassfish.app.module", "class", Greeter.class));
        assertThrows(IllegalArgumentException.class,
                () -> ForyIdlGenerator.generate("glassfish.app.module", "Not.Simple", Greeter.class));
    }

    @Test
    void idlRefusesATypeWithNoCounterpartOnTheWire() {
        assertThrows(IllegalArgumentException.class,
                () -> ForyIdlGenerator.generate("p", "S", WithAChar.class));
    }

    @Test
    void byteAndShortAreOneAndTwoBytesOnTheWire() throws Throwable {
        String idl = ForyIdlGenerator.generate("p", "S", WithAShort.class)
                + ForyIdlGenerator.generate("p", "S", WithAByte.class);
        assertTrue(idl.contains("int16 value = 1;"), idl);
        assertTrue(idl.contains("int8 value = 1;"), idl);

        ForyRuntimeModelGenerator.GeneratedModels models = ForyRuntimeModelGenerator.unary(
                "gen.small", "next", byte.class, byte.class);
        ForyGeneratedRuntime runtime = new ForyGeneratedRuntime(models, 3200, 3201);
        Object request = runtime.deserialize(runtime.serialize(
                models.request().getConstructor(byte.class).newInstance((byte) 41)), models.request());
        Object response = ForyGrpcSkeleton.of("svc", WithAByte.class).invoke("/svc/next",
                (WithAByte) v -> (byte) (v + 1), request,
                ForyGeneratedSchemaAdapter.unary(models.request(), models.response()));
        assertEquals((byte) 42, invokeValue(runtime.deserialize(runtime.serialize(response), models.response())));
    }

    public interface WithAHelper {
        String run(String value);

        static String help(String value) {
            return value;
        }
    }

    @Test
    void aStaticHelperOfTheViewIsNotAnRpc() {
        String idl = ForyIdlGenerator.generate("p", "S", WithAHelper.class);

        assertTrue(idl.contains("rpc Run("), idl);
        assertTrue(!idl.contains("Help"), idl);
        assertTrue(ForyGrpcSkeleton.of("svc", WithAHelper.class).contains("/svc/run"));
        assertTrue(!ForyGrpcSkeleton.of("svc", WithAHelper.class).contains("/svc/help"));
    }

    @Test
    void theIdlDeclaresNoMessageNobodyUses() {
        assertTrue(!ForyIdlGenerator.generate("p", "S", Greeter.class).contains("Empty"));
    }

    @Test
    void aServiceNamedLikeAMessageIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> ForyIdlGenerator.generate("p", "GreetRequest", Greeter.class));
        assertThrows(IllegalArgumentException.class,
                () -> ForyIdlGenerator.generate("p", "GreetResponse", Greeter.class));
    }

    @Test
    void theRuntimeDoesNotHoldTheContextLoaderOfTheThreadThatBuiltIt() {
        ClassLoader marker = new ClassLoader(getClass().getClassLoader()) { };
        ClassLoader before = Thread.currentThread().getContextClassLoader();
        ForyRuntimeModelGenerator.GeneratedModels models = ForyRuntimeModelGenerator.unary(
                "gen.loader", "greet", String.class, String.class);
        ForyGeneratedRuntime runtime;
        Thread.currentThread().setContextClassLoader(marker);
        try {
            runtime = new ForyGeneratedRuntime(models, 3300, 3301);
        } finally {
            Thread.currentThread().setContextClassLoader(before);
        }

        assertNotSame(marker, runtime.classLoader());
        assertSame(models.request().getClassLoader(), runtime.classLoader());
    }
}

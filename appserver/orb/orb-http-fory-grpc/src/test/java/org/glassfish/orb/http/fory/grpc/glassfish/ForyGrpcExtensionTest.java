/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */


package org.glassfish.orb.http.fory.grpc.glassfish;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.api.event.EventListener;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.orb.http.fory.grpc.ForyGeneratedServiceRegistry;
import org.glassfish.orb.http.fory.grpc.ForyGrpcCatalog;
import org.glassfish.orb.http.glassfish.GlassFishContainerBridge;
import org.glassfish.orb.http.glassfish.GlassFishSecurityBridge;
import org.glassfish.orb.http.protocol.EjbKey;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.server.ServerExchange;
import org.glassfish.orb.http.server.TransactionBridge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The extension as the endpoint drives it: which paths it claims, what it
 * does when an application is undeployed, and a call from request to answer.
 */
class ForyGrpcExtensionTest {

    private static final String ROOT = Protocol.CONTEXT_PATH;

    public interface Calc {
        int twice(int value);

        void ping();

        String hello(String name);
    }

    private final List<String> events = new ArrayList<>();

    @Test
    void claimsOnlyThePathsUnderItsOwnRoots() {
        ForyGrpcExtension extension = new ForyGrpcExtension();

        assertTrue(extension.serves(ROOT + "/fory/glassfish.app.m.B.V.V/Method"));
        assertTrue(extension.serves(ROOT + ForyGrpcCatalog.PREFIX));
        assertTrue(extension.serves(ROOT + ForyGrpcCatalog.PREFIX + "app/module/bean/view.fdl"));

        // The EJB endpoint's own routes, for an application, a module or a
        // bean that happens to be named like one of the segments.
        assertFalse(extension.serves(ROOT + "/ejb/v1/invoke/fory/module/bean/view/method"));
        assertFalse(extension.serves(ROOT + "/ejb/v1/invoke/app/fory/bean/view/method"));
        assertFalse(extension.serves(ROOT + "/ejb/v1/invoke/app/module/fory/view/method"));
        assertFalse(extension.serves(ROOT + "/ejb/v1/invoke/.well-known/ejb/module/bean/view/method"));
        assertFalse(extension.serves("/other/fory/x"));
        assertFalse(extension.serves(ROOT + "/naming/v1/lookup/fory/"));
    }

    @Test
    void anUndeployedApplicationIsForgottenAtOnceAndARedeployedOneIsFoundAgain() {
        List<ForyEjbIndex.DeployedView> deployed = new ArrayList<>();
        deployed.add(ForyEjbIndexTest.view("app", "module", "CalcBean", Calc.class));
        ForyEjbIndex index = new ForyEjbIndex(() -> List.copyOf(deployed), System::nanoTime);
        ForyGrpcExtension extension = new ForyGrpcExtension();
        extension.init(index, TransactionBridge.NONE);
        ForyGeneratedServiceRegistry registry = index.foryRegistry();
        assertEquals(3, registry.size());

        deployed.clear();
        extension.event(new EventListener.Event<>(Deployment.APPLICATION_UNLOADED, null));

        assertEquals(0, registry.size(), "what the undeployed application published is still held");
        assertEquals(0, index.catalog().size());

        deployed.add(ForyEjbIndexTest.view("app", "module", "CalcBean", Calc.class));
        extension.event(new EventListener.Event<>(Deployment.APPLICATION_STARTED, null));
        assertNotNull(registry.lookup(wire("Twice")), "the redeployed bean");
        assertEquals(3, registry.size());
    }

    @Test
    void undeployingOneApplicationLeavesTheOthersPublishedAndTheirBindingsUntouched() {
        List<ForyEjbIndex.DeployedView> deployed = new ArrayList<>();
        deployed.add(new ForyEjbIndex.DeployedView("a", "module", "CalcBean", Calc.class.getName(),
                ForyEjbIndexTest.view("a", "module", "CalcBean", Calc.class).loader(), false, "a"));
        deployed.add(new ForyEjbIndex.DeployedView("b", "module", "CalcBean", Calc.class.getName(),
                ForyEjbIndexTest.view("b", "module", "CalcBean", Calc.class).loader(), false, "b"));
        ForyEjbIndex index = new ForyEjbIndex(() -> List.copyOf(deployed), System::nanoTime);
        ForyGrpcExtension extension = new ForyGrpcExtension();
        extension.init(index, TransactionBridge.NONE);
        ForyGeneratedServiceRegistry registry = index.foryRegistry();
        String pathB = ROOT + "/fory/" + ForyEjbIndex.serviceName("b", "module", "CalcBean", Calc.class) + "/Twice";
        ForyGeneratedServiceRegistry.Binding bindingB = registry.lookup(pathB);
        assertEquals(6, registry.size());

        // The undeployment of a: the unload event from inside its unload, then
        // the success event, both before the registry lets go of it.
        extension.event(new EventListener.Event<>(Deployment.APPLICATION_UNLOADED,
                new ApplicationInfo(null, null, "a")));
        extension.event(new EventListener.Event<>(Deployment.UNDEPLOYMENT_SUCCESS, null));

        assertEquals(3, registry.size(), "the other application was unpublished along with a");
        deployed.remove(0);
        assertSame(bindingB, registry.lookup(pathB), "the other application's binding was built again");
        assertEquals(3, registry.size());
    }

    @Test
    void answersACallWithAPrimitiveParameterFromRequestToResponse() throws Exception {
        ForyGrpcExtension extension = extension(new Calc() {
            @Override
            public int twice(int value) {
                events.add("twice:" + value);
                return value * 2;
            }

            @Override
            public void ping() {
                events.add("ping");
            }

            @Override
            public String hello(String name) {
                return "Hello " + name;
            }
        });
        ForyGeneratedServiceRegistry.Binding binding = bindingOf(extension, "Twice");
        Exchange exchange = new Exchange(wire("Twice"), frame(binding, binding.models().request()
                .getConstructor(int.class).newInstance(21)));

        extension.dispatch(exchange.path, exchange);

        assertEquals(200, exchange.status);
        assertEquals("0", exchange.trailers.get("grpc-status"), exchange.trailers.toString());
        Object response = binding.runtime().deserialize(payload(exchange.body), binding.models().response());
        assertEquals(42, response.getClass().getMethod("value").invoke(response));
        assertEquals(List.of("establish:alice", "twice:21", "release", "clear"), events);
    }

    @Test
    void answersACallToAVoidMethodWithNoArguments() throws Exception {
        ForyGrpcExtension extension = extension(new Calc() {
            @Override
            public int twice(int value) {
                return value;
            }

            @Override
            public void ping() {
                events.add("ping");
            }

            @Override
            public String hello(String name) {
                return name;
            }
        });
        ForyGeneratedServiceRegistry.Binding binding = bindingOf(extension, "Ping");
        Exchange exchange = new Exchange(wire("Ping"), frame(binding, binding.models().request()
                .getConstructor().newInstance()));

        extension.dispatch(exchange.path, exchange);

        assertEquals("0", exchange.trailers.get("grpc-status"), exchange.trailers.toString());
        assertTrue(events.contains("ping"), events.toString());
    }

    @Test
    void listsTheDocumentsItPublishes() throws Exception {
        ForyGrpcExtension extension = extension(null);
        Exchange exchange = new Exchange(ROOT + ForyGrpcCatalog.PREFIX, new byte[0]);
        exchange.method = "GET";

        extension.dispatch(exchange.path, exchange);

        assertEquals(200, exchange.status);
        assertTrue(new String(exchange.body, StandardCharsets.UTF_8).contains("CalcBean"),
                new String(exchange.body, StandardCharsets.UTF_8));
    }

    private static String wire(String rpc) {
        return ROOT + "/fory/" + ForyEjbIndex.serviceName("app", "module", "CalcBean", Calc.class) + '/' + rpc;
    }

    private static ForyGeneratedServiceRegistry.Binding bindingOf(ForyGrpcExtension extension, String rpc)
            throws Exception {
        Field index = ForyGrpcExtension.class.getDeclaredField("index");
        index.setAccessible(true);
        return ((ForyEjbIndex) index.get(extension)).foryRegistry().lookup(wire(rpc));
    }

    private ForyGrpcExtension extension(Calc bean) throws Exception {
        ForyEjbIndex index = new ForyEjbIndex(
                () -> List.of(ForyEjbIndexTest.view("app", "module", "CalcBean", Calc.class)), System::nanoTime);
        ForyGrpcExtension extension = new ForyGrpcExtension();
        set(extension, "container", new GlassFishContainerBridge() {
            @Override
            public EjbKey resolve(String app, String module, String distinct, String beanName, byte[] session) {
                return new EjbKey(1L, new byte[] {0, 0, 0, 1});
            }

            @Override
            public Object getTargetObject(EjbKey key, String viewClassName) {
                return bean;
            }

            @Override
            public void releaseTargetObject(Object target) {
                events.add("release");
            }
        });
        set(extension, "security", new GlassFishSecurityBridge() {
            @Override
            public Object establish(String userName) {
                events.add("establish:" + userName);
                return "token";
            }

            @Override
            public void clear(Object token) {
                events.add("clear");
            }
        });
        extension.init(index, TransactionBridge.NONE);
        return extension;
    }

    private static byte[] frame(ForyGeneratedServiceRegistry.Binding binding, Object model) {
        byte[] payload = binding.runtime().serialize(model);
        byte[] frame = new byte[payload.length + 5];
        frame[1] = (byte) (payload.length >>> 24);
        frame[2] = (byte) (payload.length >>> 16);
        frame[3] = (byte) (payload.length >>> 8);
        frame[4] = (byte) payload.length;
        System.arraycopy(payload, 0, frame, 5, payload.length);
        return frame;
    }

    private static byte[] payload(byte[] frame) {
        byte[] payload = new byte[frame.length - 5];
        System.arraycopy(frame, 5, payload, 0, payload.length);
        return payload;
    }

    private static void set(Object owner, String name, Object value) throws Exception {
        Field field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(owner, value);
    }

    /** A gRPC request and what was answered. */
    private static final class Exchange implements ServerExchange {
        private final String path;
        private final byte[] request;
        private String method = "POST";
        private int status;
        private final Map<String, String> trailers = new LinkedHashMap<>();
        private byte[] body = new byte[0];

        Exchange(String path, byte[] request) {
            this.path = path;
            this.request = request;
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public byte[] pathBytes() {
            return path.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public int pathOffset() {
            return 0;
        }

        @Override
        public int pathLength() {
            return pathBytes().length;
        }

        @Override
        public String requestHeader(String name) {
            return "Content-Type".equalsIgnoreCase(name) ? "application/grpc+fory" : null;
        }

        @Override
        public String queryParameter(String name) {
            return null;
        }

        @Override
        public InputStream requestBody() {
            return new ByteArrayInputStream(request);
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public void setResponseHeader(String name, String value) {
        }

        @Override
        public boolean supportsResponseTrailers() {
            return true;
        }

        @Override
        public void setResponseTrailer(String name, String value) {
            trailers.put(name, value);
        }

        @Override
        public void writeBody(ByteBuffer[] buffers) throws IOException {
            int total = 0;
            for (ByteBuffer buffer : buffers) {
                total += buffer.remaining();
            }
            byte[] out = new byte[total];
            int at = 0;
            for (ByteBuffer buffer : buffers) {
                int length = buffer.remaining();
                buffer.get(out, at, length);
                at += length;
            }
            body = out;
        }

        @Override
        public String authenticatedUser() {
            return "alice";
        }
    }
}

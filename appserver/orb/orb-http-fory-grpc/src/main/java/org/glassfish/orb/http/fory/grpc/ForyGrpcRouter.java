/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.server.ServerExchange;

/** HTTP adapter for deploy-time Fory gRPC routes. */
public final class ForyGrpcRouter {

    public static final String PREFIX = "/fory/";
    private static final String CONTENT_TYPE = "application/grpc+fory";
    private final Map<String, Route> routes = new ConcurrentHashMap<>();

    public void register(String path, ForyGrpcDispatcher dispatcher,
                         TargetProvider targets) {
        if (path == null || !path.startsWith(PREFIX) || dispatcher == null || targets == null) {
            throw new IllegalArgumentException("invalid Fory gRPC route");
        }
        routes.put(path, new Route(dispatcher, targets));
    }

    public void dispatch(ServerExchange exchange) throws IOException {
        if (!"POST".equals(exchange.method())) {
            exchange.setStatus(405);
            return;
        }
        String path = new String(exchange.pathBytes(), exchange.pathOffset(),
                exchange.pathLength(), java.nio.charset.StandardCharsets.UTF_8);
        Route route = routes.get(path);
        if (route == null) {
            exchange.setStatus(Protocol.SC_NOT_FOUND);
            return;
        }
        try (Target target = route.targets.acquire(exchange)) {
            byte[] request = exchange.requestBody().readAllBytes();
            String skeletonPath = path.substring(PREFIX.length() - 1);
            byte[] response = route.dispatcher.dispatch(skeletonPath, target.value(), request,
                    target.loader(), target.filter());
            exchange.setStatus(Protocol.SC_OK);
            exchange.setResponseHeader("Content-Type", CONTENT_TYPE);
            exchange.setResponseHeader("Content-Length", Integer.toString(response.length));
            exchange.writeBody(new ByteBuffer[] {ByteBuffer.wrap(response)});
        } catch (ClassNotFoundException e) {
            throw new IOException("cannot decode Fory gRPC request", e);
        } catch (Throwable e) {
            throw new IOException("Fory gRPC invocation failed", e);
        }
    }

    public int size() {
        return routes.size();
    }

    @FunctionalInterface
    public interface TargetProvider {
        Target acquire(ServerExchange exchange) throws Exception;
    }

    public interface Target extends AutoCloseable {
        Object value();

        ClassLoader loader();

        ObjectInputFilter filter();

        @Override
        void close();
    }

    private record Route(ForyGrpcDispatcher dispatcher, TargetProvider targets) {
        private Route {
            Objects.requireNonNull(dispatcher, "dispatcher");
            Objects.requireNonNull(targets, "targets");
        }
    }
}

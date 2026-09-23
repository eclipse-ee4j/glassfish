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
    void rejectsUnknownMethodsWithoutReflectiveScanning() {
        ForyGrpcSkeleton skeleton = new ForyGrpcSkeleton(List.of());

        assertThrows(NoSuchMethodException.class,
                () -> skeleton.invoke("/demo.Greeter/missing", new GreeterBean(), null));
    }

    @Test
    void dispatcherUsesTheDeployTimeSkeletonForAFramedCall() throws Throwable {
        ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of("demo.Greeter", Greeter.class);
        ForyGrpcPayloadCodec codec = new ForyGrpcPayloadCodec();
        ForyGrpcDispatcher dispatcher = new ForyGrpcDispatcher(skeleton, codec, 1024 * 1024);

        byte[] response = dispatcher.dispatch("/demo.Greeter/greet", new GreeterBean(),
                codec.encodeFrame("Ada"), getClass().getClassLoader(),
                java.io.ObjectInputFilter.Config.createFilter("java.lang.String;!*"));

        assertEquals("Hello Ada", codec.decodeFrame(response, 1024 * 1024,
                getClass().getClassLoader(),
                java.io.ObjectInputFilter.Config.createFilter("java.lang.String;!*")));
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

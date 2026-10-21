/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The type id is part of the contract: a client generated from the published
 * IDL encodes it, so it has to survive the next deployment of the bean.
 */
class ForyTypeIdsTest {

    public interface Greeter {
        String greet(String name);

        String farewell(String name);
    }

    /** The same view after someone adds a method that sorts first. */
    public interface GreeterWithAnotherMethod {
        String greet(String name);

        String farewell(String name);

        String apologise(String name);
    }

    @Test
    void theSameNameAlwaysGetsTheSameId() {
        assertEquals(ForyTypeIds.of("demo.greeter", "GreetRequest"),
                ForyTypeIds.of("demo.greeter.GreetRequest"));
    }

    @Test
    void differentMessagesGetDifferentIds() {
        assertNotEquals(ForyTypeIds.of("demo.greeter", "GreetRequest"),
                ForyTypeIds.of("demo.greeter", "GreetResponse"));
    }

    @Test
    void idsStayAboveTheRangeForyKeepsForItself() {
        assertTrue(ForyTypeIds.of("demo.greeter", "GreetRequest") >= ForyTypeIds.FIRST_ID);
    }

    @Test
    void addingAMethodDoesNotMoveTheOtherMessages() {
        String before = ForyIdlGenerator.generate("demo.greeter", "Greeter", Greeter.class);
        String after = ForyIdlGenerator.generate("demo.greeter", "Greeter", GreeterWithAnotherMethod.class);

        // "apologise" sorts before both existing methods, so a counter would
        // have pushed every id along and broken every client already generated.
        String[] messages = {"GreetRequest", "GreetResponse", "FarewellRequest", "FarewellResponse"};
        for (String message : messages) {
            String declaration = "message " + message + " [id="
                    + ForyTypeIds.of("demo.greeter", message) + "]";
            assertTrue(before.contains(declaration), before);
            assertTrue(after.contains(declaration), after);
        }
    }

    @Test
    void theIdlPublishesTheIdsTheRuntimeRegisters() {
        String idl = ForyIdlGenerator.generate("demo.greeter", "Greeter", Greeter.class);

        // What EjbNameIndex passes to the runtime for the same view.
        int request = ForyTypeIds.of("demo.greeter", "GreetRequest");
        int response = ForyTypeIds.of("demo.greeter", "GreetResponse");

        assertTrue(idl.contains("message GreetRequest [id=" + request + "]"), idl);
        assertTrue(idl.contains("message GreetResponse [id=" + response + "]"), idl);
    }
}

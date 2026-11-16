/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

/**
 * The Fory type id of a generated message, derived from its name.
 *
 * <p>A client generated from the published IDL carries these ids in its own
 * generated code, so they are part of the contract: the id a client encodes
 * has to be the id the server registered, today and after the next deployment.
 * Numbering the messages in the order they are discovered does not give that -
 * adding a method to a remote interface shifts the id of every message that
 * sorts after it, and an existing client then puts the right bytes under the
 * wrong type. Deriving the id from the message's qualified name keeps every
 * other message where it was.
 *
 * <p>It also keeps the IDL and the runtime registration from drifting apart:
 * both ask this class rather than counting for themselves.
 *
 * <p>The hash is 32-bit FNV-1a, folded into a range that starts above the ids
 * Fory reserves for its built-in types and stays inside a signed int, which is
 * what both the Java registration and the IDL accept.
 */
public final class ForyTypeIds {

    /** Fory's own type ids are small; application ids start well clear of them. */
    static final int FIRST_ID = 1000;
    private static final int RANGE = 1_000_000;

    private static final int FNV_OFFSET = 0x811C9DC5;
    private static final int FNV_PRIME = 0x01000193;

    private ForyTypeIds() {
    }

    /**
     * The id for a message.
     *
     * @param packageName IDL package, for example {@code demo.greeter}
     * @param messageName message name, for example {@code GreetRequest}
     * @return a stable id in the application range
     */
    public static int of(String packageName, String messageName) {
        return of(packageName + '.' + messageName);
    }

    /**
     * The id for a message named in full.
     *
     * @param qualifiedName for example {@code demo.greeter.GreetRequest}
     * @return a stable id in the application range
     */
    public static int of(String qualifiedName) {
        int hash = FNV_OFFSET;
        for (int i = 0; i < qualifiedName.length(); i++) {
            hash ^= qualifiedName.charAt(i);
            hash *= FNV_PRIME;
        }
        return FIRST_ID + Math.floorMod(hash, RANGE);
    }
}

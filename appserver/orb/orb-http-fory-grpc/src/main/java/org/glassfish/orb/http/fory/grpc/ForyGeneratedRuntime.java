/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.util.Objects;

import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;

/** Fory cross-language runtime bound to one deploy-time model set. */
public final class ForyGeneratedRuntime {

    private final ThreadSafeFory fory;

    public ForyGeneratedRuntime(ForyRuntimeModelGenerator.GeneratedModels models,
                                int requestTypeId, int responseTypeId) {
        Objects.requireNonNull(models, "models");
        // These four settings are not a choice: they are what the Fory
        // compiler puts in every client it generates -
        //
        //   fory.New(fory.WithXlang(true), fory.WithRefTracking(true),
        //            fory.WithCompatible(true))
        //
        // - and both ends have to agree on them before a single byte means
        // anything. Compatible mode was the one that did not: the client sends
        // its field metadata with the data, the server read the same bytes as
        // though the schema were agreed in advance, and every field came out
        // null. A call still answered, with nulls in it, which is the worst
        // way for this to be wrong.
        fory = Fory.builder()
                .withLanguage(Language.XLANG)
                .withRefTracking(true)
                .withCompatible(true)
                .requireClassRegistration(true)
                .buildThreadSafeFory();
        fory.register(models.request(), requestTypeId);
        fory.register(models.response(), responseTypeId);
    }

    public byte[] serialize(Object value) {
        return fory.serialize(value);
    }

    public <T> T deserialize(byte[] payload, Class<T> type) {
        return fory.deserialize(payload, type);
    }
}

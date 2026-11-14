/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.annotation.ForyField;
import org.apache.fory.config.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The wire contract with a client generated from the published IDL.
 *
 * <p>The client itself is not here to be asked, so the shape the Fory compiler
 * generates stands in for it: every message field carries the number the IDL
 * gives it, as {@code fory:"id=1"}, and the runtime is built with xlang, ref
 * tracking and compatible mode. Both halves matter. A writer that declares a
 * field id puts the id on the wire where a writer that does not puts the
 * field's name, and a mismatch is not an error - the reader matches nothing
 * and hands back an object whose fields are all null. The call is answered,
 * empty, and nothing upstream can tell it went wrong, which is why this is
 * tested here rather than left to the live job to notice.
 */
class ForyWireContractTest {

    /** A message as the Fory compiler generates it for {@code string value = 1;}. */
    public static final class AsTheForyCompilerGeneratesIt {
        @ForyField(id = 1)
        private final String value;

        public AsTheForyCompilerGeneratesIt(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    private static final int REQUEST_ID = 268731;
    private static final int RESPONSE_ID = 26345;

    @Test
    void aClientsBytesArriveWithTheirFieldsIntact() throws Exception {
        ForyRuntimeModelGenerator.GeneratedModels models = models();
        ForyGeneratedRuntime server = new ForyGeneratedRuntime(models, REQUEST_ID, RESPONSE_ID);

        byte[] fromTheClient = client(REQUEST_ID).serialize(new AsTheForyCompilerGeneratesIt("Ada"));
        Object request = server.deserialize(fromTheClient, models.request());

        assertEquals("Ada", request.getClass().getMethod("value").invoke(request));
    }

    @Test
    void theServersBytesArriveAtAClientWithTheirFieldsIntact() throws Exception {
        ForyRuntimeModelGenerator.GeneratedModels models = models();
        ForyGeneratedRuntime server = new ForyGeneratedRuntime(models, REQUEST_ID, RESPONSE_ID);
        Object response = models.response().getConstructor(String.class).newInstance("Hello Ada");

        byte[] fromTheServer = server.serialize(response);

        AsTheForyCompilerGeneratesIt atTheClient = (AsTheForyCompilerGeneratesIt)
                client(RESPONSE_ID).deserialize(fromTheServer, AsTheForyCompilerGeneratesIt.class);
        assertEquals("Hello Ada", atTheClient.value());
    }

    private static ForyRuntimeModelGenerator.GeneratedModels models() {
        return ForyRuntimeModelGenerator.unary("generated.demo", "sayHello", String.class, String.class);
    }

    /**
     * A runtime built the way a generated client builds one:
     * {@code fory.New(fory.WithXlang(true), fory.WithRefTracking(true),
     * fory.WithCompatible(true))}.
     */
    private static ThreadSafeFory client(int typeId) {
        ThreadSafeFory fory = Fory.builder()
                .withLanguage(Language.XLANG)
                .withRefTracking(true)
                .withCompatible(true)
                .requireClassRegistration(true)
                .buildThreadSafeFory();
        fory.register(AsTheForyCompilerGeneratesIt.class, typeId);
        return fory;
    }
}

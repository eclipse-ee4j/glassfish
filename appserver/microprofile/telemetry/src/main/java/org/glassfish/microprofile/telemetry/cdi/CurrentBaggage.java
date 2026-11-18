/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.glassfish.microprofile.telemetry.cdi;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.BaggageEntry;
import io.opentelemetry.context.Context;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * The injectable {@link Baggage}: every call goes to the baggage current at the time of the
 * call, as required by the specification ({@code @Inject Baggage} behaves like
 * {@link Baggage#current()}).
 */
final class CurrentBaggage implements Baggage {

    static final CurrentBaggage INSTANCE = new CurrentBaggage();

    private CurrentBaggage() {
    }

    @Override
    public int size() {
        return Baggage.current().size();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super BaggageEntry> consumer) {
        Baggage.current().forEach(consumer);
    }

    @Override
    public Map<String, BaggageEntry> asMap() {
        return Baggage.current().asMap();
    }

    @Override
    public String getEntryValue(String entryKey) {
        return Baggage.current().getEntryValue(entryKey);
    }

    @Override
    public BaggageBuilder toBuilder() {
        return Baggage.current().toBuilder();
    }

    @Override
    public Context storeInContext(Context context) {
        return Baggage.current().storeInContext(context);
    }

    @Override
    public String toString() {
        return Baggage.current().toString();
    }
}

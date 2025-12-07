/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.microprofile.config.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for lazy evaluation
 *
 * @author Ondro Mihalyi
 */
public final class LazyProperty {

    private LazyProperty() {
    }

    /**
     * Returns a value lazily. Never returns {@code null}. If it would be {@code null}, it creates and sets the value
     * in a thread-safe way and returns the newly created value.
     * It's expected that the value set by the {@setter} will be returned by a subsequent call of the {@code getter}.
     * Therefore subsequent calls to this method would return the value created and set in a previous call.
     *
     * @param <OBJ> Type of the object that getter and setter get value from or set value to
     * @param <PROP> Type of the value to be returned by this method (type of the property to get or set)
     * @param object Object to get value from or set value to
     * @param getter Will be used to get the value from the {@code object} argument
     * @param propertyProducer Produces a new value if {@code getter} returns {@code null}.
     * @param setter Will be used to set the value produced by {@code propertyProducer} before the value is returned
     * @return Value returned by {@code getter} or by {@code propertyProducer} if {@code getter} returns {@code null}
     */
    public static <OBJ, PROP> PROP getOrCreateAndSet(OBJ object, Function<OBJ, PROP> getter, Supplier<PROP> propertyProducer, BiConsumer<OBJ, PROP> setter) {
        PROP result = getter.apply(object);
        if (result == null) {
            synchronized (object) {
                if (result == null) {
                    result = propertyProducer.get();
                    setter.accept(object, result);
                }
            }
        }
        return result;
    }

}
